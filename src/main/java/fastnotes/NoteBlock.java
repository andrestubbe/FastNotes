package fastnotes;

/**
 * Represents an indexed structural block (Paragraph, Code Fence, List, Callout, Table, Blockquote)
 * with optional Obsidian block identifier anchor (^blockId).
 */
public final class NoteBlock {

    public enum BlockType {
        PARAGRAPH,
        HEADING,
        CODE_BLOCK,
        CALLOUT,
        LIST_ITEM,
        BLOCKQUOTE,
        TABLE,
        THEMATIC_BREAK
    }

    private final BlockType type;
    private final String blockId; // e.g. "abc123" if ending with ^abc123, otherwise null
    private final String content;
    private final int startLine;
    private final int endLine;
    private final int startOffset;
    private final int endOffset;

    public NoteBlock(BlockType type, String blockId, String content, int startLine, int endLine, int startOffset, int endOffset) {
        this.type = type;
        this.blockId = blockId != null && !blockId.isEmpty() ? blockId : null;
        this.content = content != null ? content : "";
        this.startLine = startLine;
        this.endLine = endLine;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public BlockType getType() {
        return type;
    }

    public String getBlockId() {
        return blockId;
    }

    public boolean hasBlockId() {
        return blockId != null;
    }

    public String getContent() {
        return content;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public String toString() {
        return type + (blockId != null ? " [^" + blockId + "]" : "") + " (L:" + startLine + "-" + endLine + ")";
    }
}
