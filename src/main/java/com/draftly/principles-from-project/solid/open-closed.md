# SOLID: Open/Closed Principle

## Where this appears in Draftly
- `src/main/java/com/draftly/service/LatexFormatBuilder.java`
- `src/main/java/com/draftly/service/IeeeLatexFormatBuilder.java`
- `src/main/java/com/draftly/service/LncsLatexFormatBuilder.java`

The formatting system is open for extension through new implementations, but the existing interface does not need to change.

## Before
```java
public class LatexExporter {
    public String build(String format, List<PaperSection> sections) {
        if ("IEEE".equals(format)) {
            // build IEEE
        } else if ("LNCS".equals(format)) {
            // build LNCS
        } else {
            throw new IllegalArgumentException("Unsupported format");
        }
        return "";
    }
}
```

## After
```java
public interface LatexFormatBuilder {
    String getFormat();
    String buildAuthorsBlock();
    String buildSections(List<PaperSection> sections, Map<Integer, String> citationKeyMap);
    String buildKeywords(String keywords);
}
```

```java
@Component
public class IeeeLatexFormatBuilder implements LatexFormatBuilder {
    @Override
    public String getFormat() {
        return "IEEE";
    }
}
```

```java
@Component
public class LncsLatexFormatBuilder implements LatexFormatBuilder {
    @Override
    public String getFormat() {
        return "LNCS";
    }
}
```

## Why this is OCP
- New formats can be added by creating a new builder class.
- Existing builders and the interface can stay unchanged.
- The system grows by extension instead of repeated editing.