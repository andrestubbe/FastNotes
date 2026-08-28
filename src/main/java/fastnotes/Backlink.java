package fastnotes;

/**
 * Represents a resolved incoming link from a source document pointing to a target note.
 */
public final class Backlink {
    private final String sourceNotePath;
    private final String sourceNoteTitle;
    private final NoteLink link;
    private final String contextExcerpt; // Surrounding snippet line(s)

    public Backlink(String sourceNotePath, String sourceNoteTitle, NoteLink link, String contextExcerpt) {
        this.sourceNotePath = sourceNotePath != null ? sourceNotePath : "";
        this.sourceNoteTitle = sourceNoteTitle != null ? sourceNoteTitle : "";
        this.link = link;
        this.contextExcerpt = contextExcerpt != null ? contextExcerpt.trim() : "";
    }

    public String getSourceNotePath() {
        return sourceNotePath;
    }

    public String getSourceNoteTitle() {
        return sourceNoteTitle;
    }

    public NoteLink getLink() {
        return link;
    }

    public String getContextExcerpt() {
        return contextExcerpt;
    }

    public NoteLink.LinkType getLinkType() {
        return link.getType();
    }

    public int getLineNumber() {
        return link.getLineNumber();
    }

    @Override
    public String toString() {
        return sourceNoteTitle + " -> " + link.getTargetDoc() + " [L:" + link.getLineNumber() + "] \"" + contextExcerpt + "\"";
    }
}
