package fastnotes;

import java.util.*;

/**
 * Rich AST representation of a parsed Markdown / Obsidian note.
 */
public final class NoteDocument {
    private final String path;
    private final String fileName;
    private final String title;
    private final String rawContent;
    private final NoteFrontmatter frontmatter;
    private final List<NoteHeading> headings;
    private final List<NoteLink> links;
    private final List<NoteBlock> blocks;
    private final List<NoteTag> tags;
    private final List<NoteTask> tasks;
    private final List<NoteCallout> callouts;
    private final Map<String, NoteBlock> blockIndex;
    private final Map<String, NoteHeading> headingIndex;
    private final int wordCount;
    private final int charCount;

    public NoteDocument(String path, String fileName, String title, String rawContent,
                        NoteFrontmatter frontmatter, List<NoteHeading> headings,
                        List<NoteLink> links, List<NoteBlock> blocks,
                        List<NoteTag> tags, List<NoteTask> tasks,
                        List<NoteCallout> callouts, Map<String, NoteBlock> blockIndex,
                        Map<String, NoteHeading> headingIndex, int wordCount, int charCount) {
        this.path = path != null ? path.replace('\\', '/') : "";
        this.fileName = fileName != null ? fileName : "";
        this.title = title != null ? title : "";
        this.rawContent = rawContent != null ? rawContent : "";
        this.frontmatter = frontmatter != null ? frontmatter : NoteFrontmatter.empty();
        this.headings = headings != null ? Collections.unmodifiableList(new ArrayList<>(headings)) : Collections.emptyList();
        this.links = links != null ? Collections.unmodifiableList(new ArrayList<>(links)) : Collections.emptyList();
        this.blocks = blocks != null ? Collections.unmodifiableList(new ArrayList<>(blocks)) : Collections.emptyList();
        this.tags = tags != null ? Collections.unmodifiableList(new ArrayList<>(tags)) : Collections.emptyList();
        this.tasks = tasks != null ? Collections.unmodifiableList(new ArrayList<>(tasks)) : Collections.emptyList();
        this.callouts = callouts != null ? Collections.unmodifiableList(new ArrayList<>(callouts)) : Collections.emptyList();
        this.blockIndex = blockIndex != null ? Collections.unmodifiableMap(new LinkedHashMap<>(blockIndex)) : Collections.emptyMap();
        this.headingIndex = headingIndex != null ? Collections.unmodifiableMap(new LinkedHashMap<>(headingIndex)) : Collections.emptyMap();
        this.wordCount = wordCount;
        this.charCount = charCount;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the canonical note name (filename without .md extension).
     */
    public String getNoteName() {
        if (fileName.toLowerCase().endsWith(".md")) {
            return fileName.substring(0, fileName.length() - 3);
        }
        return fileName;
    }

    public String getTitle() {
        if (title != null && !title.isEmpty()) return title;
        if (!frontmatter.getTitle().isEmpty()) return frontmatter.getTitle();
        if (!headings.isEmpty()) return headings.get(0).getText();
        return getNoteName();
    }

    public String getRawContent() {
        return rawContent;
    }

    public NoteFrontmatter getFrontmatter() {
        return frontmatter;
    }

    public List<NoteHeading> getHeadings() {
        return headings;
    }

    public List<NoteLink> getLinks() {
        return links;
    }

    public List<NoteBlock> getBlocks() {
        return blocks;
    }

    public List<NoteTag> getTags() {
        return tags;
    }

    public List<NoteTask> getTasks() {
        return tasks;
    }

    public List<NoteCallout> getCallouts() {
        return callouts;
    }

    public Map<String, NoteBlock> getBlockIndex() {
        return blockIndex;
    }

    public NoteBlock getBlockById(String blockId) {
        if (blockId == null) return null;
        String id = blockId.startsWith("^") ? blockId.substring(1) : blockId;
        return blockIndex.get(id);
    }

    public NoteHeading getHeadingByAnchor(String anchor) {
        if (anchor == null) return null;
        String a = anchor.startsWith("#") ? anchor.substring(1) : anchor;
        return headingIndex.get(NoteHeading.slugify(a));
    }

    public int getWordCount() {
        return wordCount;
    }

    public int getCharCount() {
        return charCount;
    }

    public double getReadingTimeMinutes() {
        return Math.max(1.0, wordCount / 200.0);
    }

    /**
     * Checks if note contains tag (either in frontmatter or in body text).
     */
    public boolean hasTag(String tagQuery) {
        for (String ft : frontmatter.getTags()) {
            String norm = ft.startsWith("#") ? ft.substring(1).toLowerCase() : ft.toLowerCase();
            String q = tagQuery.startsWith("#") ? tagQuery.substring(1).toLowerCase() : tagQuery.toLowerCase();
            if (norm.equals(q) || norm.startsWith(q + "/")) return true;
        }
        for (NoteTag tag : tags) {
            if (tag.matches(tagQuery)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "NoteDocument{" +
                "path='" + path + '\'' +
                ", title='" + getTitle() + '\'' +
                ", headings=" + headings.size() +
                ", links=" + links.size() +
                ", blocks=" + blocks.size() +
                ", tags=" + tags.size() +
                ", tasks=" + tasks.size() +
                '}';
    }
}
