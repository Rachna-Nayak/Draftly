package com.draftly.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.draftly.model.Feedback;
import com.draftly.model.ResearchPaper;
import com.draftly.repository.ResearchPaperRepository;

/*
 Feedback Service - Handles faculty review and feedback submission. (UC6)
 */

@Service
public class FeedbackService {

    private final ResearchPaperRepository researchPaperRepository;
    private final NLPService nlpService;

    public FeedbackService(ResearchPaperRepository researchPaperRepository, NLPService nlpService) {
        this.researchPaperRepository = researchPaperRepository;
        this.nlpService = nlpService;
    }

    /*
     UC6: Submit feedback on a specific section of a paper.
    */

    public ResearchPaper submitFeedback(String paperId, String facultyId, String facultyName,
                                         String sectionName, String comment, String status) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(paperId);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("Research paper not found: " + paperId);
        }

        ResearchPaper paper = paperOpt.get();
        Feedback feedback = new Feedback(facultyId, facultyName, sectionName, comment, status);
        paper.addFeedback(feedback);

        // Update paper status based on feedback
        if ("REVISION_NEEDED".equals(status)) {
            paper.setStatus("IN_REVIEW");
        }

        return researchPaperRepository.save(paper);
    }

    /*
     UC6: Get all feedback for a research paper.
    */

    public ResearchPaper getPaperWithFeedback(String paperId) {
        return researchPaperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + paperId));
    }

    /*
    UC6: Approve a paper (all sections pass review).
    */
   
    public ResearchPaper approvePaper(String paperId) {
        ResearchPaper paper = getPaperWithFeedback(paperId);
        paper.setStatus("APPROVED");
        return researchPaperRepository.save(paper);
    }
}
