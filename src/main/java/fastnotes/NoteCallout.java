package fastnotes;

/**
 * Represents an Obsidian Callout block (> [!TYPE] Title).
 */
public final class NoteCallout {
    private final String calloutType; // e.g. "NOTE", "WARNING", "TIP", "IMPORTANT", etc.
    private final String title;
    private final String content;
    private final char foldState;     // '+' for open, '-' for closed, ' ' for non-collapsible
    private final int startLine;
    private final int endLine;

    public NoteCallout(String calloutType, String title, String content, char foldState, int startLine, int endLine) {
        this.calloutType = calloutType != null ? calloutType.toUpperCase() : "NOTE";
        this.title = title != null ? title.trim() : "";
        this.content = content != null ? content : "";
        this.foldState = foldState;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public String getCalloutType() {
        return calloutType;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public char getFoldState() {
        return foldState;
    }

    public boolean isCollapsible() {
        return foldState == '+' || foldState == '-';
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    @Override
    public String toString() {
        return "> [!" + calloutType + "]" + (foldState != ' ' ? foldState : "") + " " + title + " (L:" + startLine + "-" + endLine + ")";
    }
}
