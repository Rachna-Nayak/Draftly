package com.draftly.service;

import com.draftly.model.Paper;
import com.draftly.repository.PaperRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Search Service - Discovers and ranks relevant academic literature. (UC2)
 */
@Service
public class SearchService {

    private static final String CROSSREF_URL = "https://api.crossref.org/works";
    private static final String OPENALEX_URL = "https://api.openalex.org/works";
    private static final int EXTERNAL_ROWS = 20;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PaperRepository paperRepository;
    private final HttpClient httpClient;

    public SearchService(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public List<Paper> rankByRelevance(List<String> keywords) {
        List<Paper> allPapers = paperRepository.findAll();
        return allPapers.stream()
                .sorted((p1, p2) -> Double.compare(computeRelevanceScore(keywords, p2), computeRelevanceScore(keywords, p1)))
                .collect(Collectors.toList());
    }

    /**
     * UC2: Search with filters (year range, domain, minimum citations).
     * FR3: Uses both local DB and external scholarly APIs (CrossRef + OpenAlex).
     */
    public List<Paper> searchWithFilters(List<String> keywords, String domain,
                                         Integer startYear, Integer endYear,
                                         Integer minCitations) {
        List<String> normalizedKeywords = normalizeKeywords(keywords);
        List<Paper> localPapers = paperRepository.findAll();
        List<Paper> externalPapers = fetchExternalPapers(normalizedKeywords, domain);

        Map<String, Paper> deduped = new LinkedHashMap<>();
        for (Paper paper : localPapers) {
            deduped.put(buildDedupKey(paper), paper);
        }
        for (Paper paper : externalPapers) {
            deduped.putIfAbsent(buildDedupKey(paper), paper);
        }

        return deduped.values().stream()
            .filter(p -> domain == null || domain.isBlank() || (p.getDomain() != null && p.getDomain().equalsIgnoreCase(domain)))
                .filter(p -> startYear == null || p.getPublicationYear() >= startYear)
                .filter(p -> endYear == null || p.getPublicationYear() <= endYear)
                .filter(p -> minCitations == null || p.getCitationCount() >= minCitations)
                .sorted((p1, p2) -> Double.compare(computeRelevanceScore(normalizedKeywords, p2), computeRelevanceScore(normalizedKeywords, p1)))
                .collect(Collectors.toList());
    }

    private List<String> normalizeKeywords(List<String> keywords) {
        if (keywords == null) {
            return List.of();
        }
        return keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .toList();
    }

    private double computeRelevanceScore(List<String> queryKeywords, Paper paper) {
        if (paper == null) {
            return 0.0;
        }

        Set<String> paperKeywords = new HashSet<>();
        if (paper.getKeywords() != null) {
            paper.getKeywords().forEach(k -> {
                if (k != null) {
                    paperKeywords.add(k.toLowerCase());
                }
            });
        }

        if (paper.getTitle() != null) {
            Arrays.stream(paper.getTitle().toLowerCase().split("\\W+"))
                    .forEach(paperKeywords::add);
        }

        if (paper.getAbstractText() != null) {
            Arrays.stream(paper.getAbstractText().toLowerCase().split("\\W+"))
                    .forEach(paperKeywords::add);
        }

        long keywordHits = queryKeywords.stream()
                .filter(paperKeywords::contains)
                .count();

        double citationBoost = Math.log10(Math.max(1, paper.getCitationCount()) + 1);
        return keywordHits * 2.0 + citationBoost;
    }

    private List<Paper> fetchExternalPapers(List<String> keywords, String domain) {
        if (keywords.isEmpty()) {
            return List.of();
        }

        String query = String.join(" ", keywords);
        List<Paper> combined = new ArrayList<>();
        combined.addAll(fetchFromCrossRef(query, domain));
        combined.addAll(fetchFromOpenAlex(query, domain));
        return combined;
    }

    private List<Paper> fetchFromCrossRef(String query, String domain) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String endpoint = CROSSREF_URL + "?query=" + encodedQuery + "&rows=" + EXTERNAL_ROWS;

        try {
            JsonNode root = getJson(endpoint);
            JsonNode items = root.path("message").path("items");
            if (!items.isArray()) {
                return List.of();
            }

            List<Paper> papers = new ArrayList<>();
            for (JsonNode item : items) {
                String title = firstText(item.path("title"));
                if (title.isBlank()) {
                    continue;
                }

                Paper paper = new Paper();
                paper.setTitle(title);
                paper.setAuthors(parseCrossRefAuthors(item.path("author")));
                paper.setAbstractText(cleanAbstract(item.path("abstract").asText("")));
                paper.setJournal(firstText(item.path("container-title")));
                paper.setPublicationYear(parseCrossRefYear(item));
                paper.setCitationCount(item.path("is-referenced-by-count").asInt(0));
                paper.setDomain(resolveDomain(domain, item));
                paper.setKeywords(parseCrossRefKeywords(item));
                paper.setDoi(normalizeDoi(item.path("DOI").asText("")));

                papers.add(saveOrReusePaper(paper));
            }
            return papers;
        } catch (Exception exception) {
            return List.of();
        }
    }

    private List<Paper> fetchFromOpenAlex(String query, String domain) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String endpoint = OPENALEX_URL + "?search=" + encodedQuery + "&per-page=" + EXTERNAL_ROWS;

        try {
            JsonNode root = getJson(endpoint);
            JsonNode results = root.path("results");
            if (!results.isArray()) {
                return List.of();
            }

            List<Paper> papers = new ArrayList<>();
            for (JsonNode item : results) {
                String title = item.path("display_name").asText("");
                if (title.isBlank()) {
                    continue;
                }

                Paper paper = new Paper();
                paper.setTitle(title);
                paper.setAuthors(parseOpenAlexAuthors(item.path("authorships")));
                paper.setAbstractText("");
                paper.setJournal(item.path("primary_location").path("source").path("display_name").asText(""));
                paper.setPublicationYear(item.path("publication_year").asInt(0));
                paper.setCitationCount(item.path("cited_by_count").asInt(0));
                paper.setDomain(resolveOpenAlexDomain(item, domain));
                paper.setKeywords(parseOpenAlexKeywords(item));
                paper.setDoi(normalizeDoi(item.path("doi").asText("")));

                papers.add(saveOrReusePaper(paper));
            }

            return papers;
        } catch (Exception exception) {
            return List.of();
        }
    }

    private JsonNode getJson(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Accept", "application/json")
                .header("User-Agent", "Draftly/1.0 (academic-platform)")
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("External API call failed with status " + response.statusCode());
        }
        return OBJECT_MAPPER.readTree(response.body());
    }

    private String firstText(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray() || arrayNode.isEmpty()) {
            return "";
        }
        return arrayNode.get(0).asText("").trim();
    }

    private int parseCrossRefYear(JsonNode item) {
        JsonNode dateParts = item.path("issued").path("date-parts");
        if (dateParts.isArray() && !dateParts.isEmpty() && dateParts.get(0).isArray() && !dateParts.get(0).isEmpty()) {
            return dateParts.get(0).get(0).asInt(0);
        }

        dateParts = item.path("published-print").path("date-parts");
        if (dateParts.isArray() && !dateParts.isEmpty() && dateParts.get(0).isArray() && !dateParts.get(0).isEmpty()) {
            return dateParts.get(0).get(0).asInt(0);
        }

        dateParts = item.path("published-online").path("date-parts");
        if (dateParts.isArray() && !dateParts.isEmpty() && dateParts.get(0).isArray() && !dateParts.get(0).isEmpty()) {
            return dateParts.get(0).get(0).asInt(0);
        }

        return 0;
    }

    private List<String> parseCrossRefAuthors(JsonNode authorsNode) {
        if (!authorsNode.isArray()) {
            return List.of();
        }

        List<String> authors = new ArrayList<>();
        for (JsonNode author : authorsNode) {
            String given = author.path("given").asText("").trim();
            String family = author.path("family").asText("").trim();
            String fullName = (given + " " + family).trim();
            if (!fullName.isBlank()) {
                authors.add(fullName);
            }
        }
        return authors;
    }

    private List<String> parseOpenAlexAuthors(JsonNode authorshipsNode) {
        if (!authorshipsNode.isArray()) {
            return List.of();
        }

        List<String> authors = new ArrayList<>();
        for (JsonNode authorship : authorshipsNode) {
            String name = authorship.path("author").path("display_name").asText("").trim();
            if (!name.isBlank()) {
                authors.add(name);
            }
        }
        return authors;
    }

    private List<String> parseCrossRefKeywords(JsonNode item) {
        JsonNode subjects = item.path("subject");
        if (!subjects.isArray()) {
            return List.of();
        }
        List<String> keywords = new ArrayList<>();
        for (JsonNode subject : subjects) {
            String value = subject.asText("").trim();
            if (!value.isBlank()) {
                keywords.add(value);
            }
        }
        return keywords;
    }

    private List<String> parseOpenAlexKeywords(JsonNode item) {
        JsonNode concepts = item.path("concepts");
        if (!concepts.isArray()) {
            return List.of();
        }
        List<String> keywords = new ArrayList<>();
        for (JsonNode concept : concepts) {
            String value = concept.path("display_name").asText("").trim();
            if (!value.isBlank()) {
                keywords.add(value);
            }
        }
        return keywords;
    }

    private String resolveDomain(String requestedDomain, JsonNode item) {
        if (requestedDomain != null && !requestedDomain.isBlank()) {
            return requestedDomain;
        }

        JsonNode subjects = item.path("subject");
        if (subjects.isArray() && !subjects.isEmpty()) {
            return subjects.get(0).asText("General");
        }
        return "General";
    }

    private String resolveOpenAlexDomain(JsonNode item, String requestedDomain) {
        if (requestedDomain != null && !requestedDomain.isBlank()) {
            return requestedDomain;
        }
        String domain = item.path("primary_topic").path("domain").path("display_name").asText("").trim();
        return domain.isBlank() ? "General" : domain;
    }

    private String cleanAbstract(String rawAbstract) {
        if (rawAbstract == null || rawAbstract.isBlank()) {
            return "";
        }
        return rawAbstract.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String normalizeDoi(String doi) {
        if (doi == null || doi.isBlank()) {
            return "";
        }
        String normalized = doi.trim();
        if (normalized.startsWith("https://doi.org/")) {
            normalized = normalized.substring("https://doi.org/".length());
        }
        return normalized;
    }

    private Paper saveOrReusePaper(Paper candidate) {
        String doi = candidate.getDoi();
        if (doi != null && !doi.isBlank()) {
            Optional<Paper> existing = paperRepository.findByDoi(doi);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return paperRepository.save(candidate);
    }

    private String buildDedupKey(Paper paper) {
        if (paper.getDoi() != null && !paper.getDoi().isBlank()) {
            return "doi:" + paper.getDoi().toLowerCase(Locale.ROOT);
        }
        String title = paper.getTitle() == null ? "" : paper.getTitle().toLowerCase(Locale.ROOT).trim();
        return "title:" + title + "|year:" + paper.getPublicationYear();
    }

    /**
     * Get all papers in the database.
     */
    public List<Paper> getAllPapers() {
        return paperRepository.findAll();
    }

    /**
     * Get a paper by ID.
     */
    public Optional<Paper> getPaperById(String paperId) {
        return paperRepository.findById(paperId);
    }

    /**
     * Add a paper to the database.
     */
    public Paper addPaper(Paper paper) {
        return paperRepository.save(paper);
    }
}
