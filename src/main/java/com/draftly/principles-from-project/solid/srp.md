# SOLID: Single Responsibility Principle

## Where this appears in Draftly
- `src/main/java/com/draftly/service/IeeeLatexFormatBuilder.java`
- `src/main/java/com/draftly/service/LncsLatexFormatBuilder.java`

Each class focuses on one LaTeX format only.

## Before
```java
public class LatexExporter {
    public String buildLatex(String format, List<PaperSection> sections, Map<Integer, String> citationKeyMap) {
        if ("IEEE".equals(format)) {
            // IEEE-specific generation
        } else if ("LNCS".equals(format)) {
            // LNCS-specific generation
        }
        return "";
    }
}
```

## After
```java
@Component
public class IeeeLatexFormatBuilder implements LatexFormatBuilder {

    @Override
    public String getFormat() {
        return "IEEE";
    }

    @Override
    public String buildSections(List<PaperSection> sections, Map<Integer, String> citationKeyMap) {
        StringBuilder sectionsLatex = new StringBuilder();
        // IEEE-specific generation only
        return sectionsLatex.toString().trim();
    }
}
```

## Why this is SRP
- The IEEE builder handles only IEEE formatting rules.
- The LNCS builder handles only LNCS formatting rules.
- Each class has one reason to change: its own format rules.