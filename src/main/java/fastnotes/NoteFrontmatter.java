package fastnotes;

import java.util.*;

/**
 * Structured YAML Frontmatter metadata extracted from note header fences (---).
 */
public final class NoteFrontmatter {
    private final Map<String, Object> properties;
    private final List<String> tags;
    private final List<String> aliases;
    private final String title;
    private final int rawStartLine;
    private final int rawEndLine;

    public NoteFrontmatter(Map<String, Object> properties, List<String> tags, List<String> aliases, String title, int rawStartLine, int rawEndLine) {
        this.properties = properties != null ? Collections.unmodifiableMap(new LinkedHashMap<>(properties)) : Collections.emptyMap();
        this.tags = tags != null ? Collections.unmodifiableList(new ArrayList<>(tags)) : Collections.emptyList();
        this.aliases = aliases != null ? Collections.unmodifiableList(new ArrayList<>(aliases)) : Collections.emptyList();
        this.title = title != null ? title : "";
        this.rawStartLine = rawStartLine;
        this.rawEndLine = rawEndLine;
    }

    public static NoteFrontmatter empty() {
        return new NoteFrontmatter(Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), "", 0, 0);
    }

    public boolean isEmpty() {
        return properties.isEmpty() && tags.isEmpty() && aliases.isEmpty() && title.isEmpty();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public String getString(String key) {
        Object val = properties.get(key);
        return val != null ? String.valueOf(val) : null;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getTitle() {
        return title;
    }

    public int getRawStartLine() {
        return rawStartLine;
    }

    public int getRawEndLine() {
        return rawEndLine;
    }

    @Override
    public String toString() {
        return "NoteFrontmatter{" +
                "title='" + title + '\'' +
                ", tags=" + tags +
                ", aliases=" + aliases +
                ", properties=" + properties.keySet() +
                '}';
    }
}
