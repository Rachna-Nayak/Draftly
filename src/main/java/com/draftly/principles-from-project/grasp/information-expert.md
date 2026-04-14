# GRASP: Information Expert

## Where this appears in Draftly
- `src/main/java/com/draftly/service/ProjectService.java`
- `src/main/java/com/draftly/service/ReviewService.java`

The class with the needed data and responsibility performs the work.

## Before
```java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @PostMapping
    public ResearchProject createProject(@RequestBody Map<String, String> body) {
        List<String> suggestedKeywords = nlpService.extractKeywords(body.get("title") + " " + body.get("objectives"));
        ResearchProject project = new ResearchProject(
            body.get("title"),
            body.get("domain"),
            suggestedKeywords,
            body.get("objectives"),
            body.getOrDefault("ownerId", "default")
        );
        return projectRepository.save(project);
    }
}
```

## After
```java
@Service
public class ProjectService {

    public ResearchProject createProject(String title, String domain, String objectives, String ownerId) {
        List<String> suggestedKeywords = nlpService.extractKeywords(title + " " + objectives);

        ResearchProject project = new ResearchProject(title, domain, suggestedKeywords, objectives, ownerId);
        ResearchProject saved = projectRepository.save(project);

        Optional<User> userOpt = userRepository.findById(ownerId);
        userOpt.ifPresent(user -> {
            user.addProjectId(saved.getId());
            userRepository.save(user);
        });

        return saved;
    }
}
```

## Why this is GRASP Information Expert
- `ProjectService` knows the project data, persistence, and keyword generation flow.
- The logic is placed where the required information already exists.
- This keeps the controller thin and the business logic centralized.