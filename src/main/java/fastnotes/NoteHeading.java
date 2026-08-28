package fastnotes;

/**
 * Headings (H1 to H6) with anchor slug generation and hierarchy tracking.
 */
public final class NoteHeading {
    private final int level; // 1 to 6
    private final String text;
    private final String anchorId; // Normalized Obsidian anchor (e.g. "my-heading-title")
    private final int lineNumber;
    private final int charOffset;

    public NoteHeading(int level, String text, String anchorId, int lineNumber, int charOffset) {
        this.level = level;
        this.text = text != null ? text.trim() : "";
        this.anchorId = anchorId != null ? anchorId : slugify(this.text);
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    public static String slugify(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(text.length());
        boolean prevDash = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
                prevDash = false;
            } else if (c == ' ' || c == '-' || c == '_') {
                if (!prevDash && sb.length() > 0) {
                    sb.append('-');
                    prevDash = true;
                }
            }
        }
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '-') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public int getLevel() {
        return level;
    }

    public String getText() {
        return text;
    }

    public String getAnchorId() {
        return anchorId;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getCharOffset() {
        return charOffset;
    }

    @Override
    public String toString() {
        return "H" + level + " [" + text + " (#" + anchorId + ") L:" + lineNumber + "]";
    }
}
