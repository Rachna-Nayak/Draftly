# GRASP: Controller Pattern

## Where this appears in Draftly
- `src/main/java/com/draftly/controller/ProjectController.java`
- `src/main/java/com/draftly/controller/ReviewController.java`

The controllers receive HTTP requests, extract route/body data, and delegate the actual work to service classes.

## Before
```java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @GetMapping
    public List<ResearchProject> listProjects(@RequestParam(required = false) String ownerId,
                                              HttpServletRequest request) {
        UserRole currentRole = (UserRole) request.getAttribute("currentUserRole");
        String currentUserId = (String) request.getAttribute("currentUserId");

        if (currentRole != null && currentRole.matches(UserRole.ADMIN) && (ownerId == null || ownerId.isBlank())) {
            return projectService.getAllProjects();
        }

        String effectiveOwnerId = (ownerId == null || ownerId.isBlank()) ? currentUserId : ownerId;
        if (effectiveOwnerId == null || effectiveOwnerId.isBlank()) {
            effectiveOwnerId = "default";
        }
        return projectService.getProjectsByOwner(effectiveOwnerId);
    }
}
```

## After
```java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @GetMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<ResearchProject> listProjects(@RequestParam(required = false) String ownerId,
                                              HttpServletRequest request) {
        UserRole currentRole = (UserRole) request.getAttribute("currentUserRole");
        String currentUserId = (String) request.getAttribute("currentUserId");

        if (currentRole != null && currentRole.matches(UserRole.ADMIN) && (ownerId == null || ownerId.isBlank())) {
            return projectService.getAllProjects();
        }

        String effectiveOwnerId = (ownerId == null || ownerId.isBlank()) ? currentUserId : ownerId;
        if (effectiveOwnerId == null || effectiveOwnerId.isBlank()) {
            effectiveOwnerId = "default";
        }
        return projectService.getProjectsByOwner(effectiveOwnerId);
    }
}
```

## Why this is GRASP Controller
- The controller handles the request lifecycle.
- The service layer owns business operations.
- The controller stays focused on coordination instead of business logic.