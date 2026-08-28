package fastnotes;

/**
 * Represents an outgoing link: Obsidian Wikilink, Embed transclusion, or standard Markdown link.
 */
public final class NoteLink {

    public enum LinkType {
        WIKILINK,       // [[Note]] or [[Note|Alias]]
        EMBED,          // ![[Note]] or ![[image.png]] or ![[Note#^block]]
        MARKDOWN,       // [text](target.md) or [text](https://...)
        HEADING_LINK,   // [[Note#Heading]]
        BLOCK_LINK      // [[Note#^blockId]]
    }

    private final LinkType type;
    private final String rawText;
    private final String targetDoc;
    private final String subReference;    // Heading anchor or ^blockId
    private final String alias;           // Display text or override alias
    private final boolean isEmbed;
    private final boolean isBlockRef;
    private final boolean isHeadingRef;
    private final int lineNumber;
    private final int charOffset;

    public NoteLink(LinkType type, String rawText, String targetDoc, String subReference, String alias,
                    boolean isEmbed, boolean isBlockRef, boolean isHeadingRef, int lineNumber, int charOffset) {
        this.type = type;
        this.rawText = rawText != null ? rawText : "";
        this.targetDoc = targetDoc != null ? targetDoc.trim() : "";
        this.subReference = subReference != null ? subReference.trim() : null;
        this.alias = alias != null ? alias.trim() : null;
        this.isEmbed = isEmbed;
        this.isBlockRef = isBlockRef;
        this.isHeadingRef = isHeadingRef;
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    public LinkType getType() {
        return type;
    }

    public String getRawText() {
        return rawText;
    }

    public String getTargetDoc() {
        return targetDoc;
    }

    public String getSubReference() {
        return subReference;
    }

    public String getAlias() {
        return alias;
    }

    public String getDisplayText() {
        if (alias != null && !alias.isEmpty()) return alias;
        if (subReference != null && !subReference.isEmpty()) {
            return targetDoc.isEmpty() ? "#" + subReference : targetDoc + " > " + subReference;
        }
        return targetDoc;
    }

    public boolean isEmbed() {
        return isEmbed;
    }

    public boolean isBlockReference() {
        return isBlockRef;
    }

    public boolean isHeadingReference() {
        return isHeadingRef;
    }

    public boolean isExternal() {
        return targetDoc.startsWith("http://") || targetDoc.startsWith("https://") || targetDoc.startsWith("mailto:");
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getCharOffset() {
        return charOffset;
    }

    @Override
    public String toString() {
        return (isEmbed ? "!" : "") + "[[" + targetDoc +
                (subReference != null ? "#" + subReference : "") +
                (alias != null ? "|" + alias : "") + "]] (L:" + lineNumber + ")";
    }
}
