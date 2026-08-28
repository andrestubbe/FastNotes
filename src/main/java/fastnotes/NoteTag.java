package fastnotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a parsed tag in body text or frontmatter (e.g., #fastjava/indexing/speed).
 */
public final class NoteTag {
    private final String rawTag;         // e.g. "#fastjava/indexing/speed"
    private final String normalizedTag;  // e.g. "fastjava/indexing/speed"
    private final List<String> segments; // ["fastjava", "indexing", "speed"]
    private final int lineNumber;
    private final int charOffset;

    public NoteTag(String rawTag, int lineNumber, int charOffset) {
        this.rawTag = rawTag != null ? rawTag : "";
        String norm = this.rawTag.startsWith("#") ? this.rawTag.substring(1) : this.rawTag;
        this.normalizedTag = norm.toLowerCase();
        
        List<String> segList = new ArrayList<>();
        for (String s : norm.split("/")) {
            if (!s.isEmpty()) {
                segList.add(s.toLowerCase());
            }
        }
        this.segments = Collections.unmodifiableList(segList);
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    public String getRawTag() {
        return rawTag;
    }

    public String getNormalizedTag() {
        return normalizedTag;
    }

    public List<String> getSegments() {
        return segments;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getCharOffset() {
        return charOffset;
    }

    /**
     * Checks if this tag matches or is a descendant of the given parent tag query.
     * e.g. "#dev/ai" matches "#dev" or "dev".
     */
    public boolean matches(String tagQuery) {
        if (tagQuery == null || tagQuery.isEmpty()) return false;
        String q = tagQuery.startsWith("#") ? tagQuery.substring(1).toLowerCase() : tagQuery.toLowerCase();
        return normalizedTag.equals(q) || normalizedTag.startsWith(q + "/");
    }

    @Override
    public String toString() {
        return "#" + normalizedTag + " (L:" + lineNumber + ")";
    }
}
