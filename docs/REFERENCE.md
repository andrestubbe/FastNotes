# FastNotes API Reference

## Primary Classes & Interfaces

### `FastNotes`
Top-level static convenience facade.
- `static NoteDocument parse(String markdown)`: Parses a markdown string.
- `static NoteDocument parse(String path, String markdown)`: Parses a markdown string with a designated file path.
- `static FastVault openVault(Path directory)`: Scans and indexes an on-disk Obsidian vault directory.
- `static FastVault createMemoryVault()`: Initializes an in-memory vault.

### `FastVault`
In-memory knowledge graph and vault indexer.
- `void addDocument(NoteDocument doc)`: Adds or updates an indexed document.
- `void reload()`: Scans the directory root and re-indexes all `.md` files in parallel.
- `NoteDocument findNote(String query)`: Looks up a note by exact path, filename, or alias.
- `List<Backlink> getBacklinks(String noteName)`: Returns all incoming backlinks to the specified note.
- `String resolveTransclusion(String linkTarget)`: Resolves an embed target (`Note#^blockId` or `Note#Heading`) into its text content.
- `List<NoteDocument> findNotesByTag(String tagQuery)`: Returns notes matching hierarchical tags (e.g. `core` matches `#core/performance`).
- `List<VaultTaskItem> getAllTasks(Boolean completedFilter)`: Aggregates checklist tasks across all notes.
- `GraphMetrics getMetrics()`: Returns topological metrics, PageRank scores, orphan notes, and ghost links.

### `NoteDocument`
Parsed immutable AST container for a single note.
- `String getPath()`, `String getFileName()`, `String getNoteName()`, `String getTitle()`
- `NoteFrontmatter getFrontmatter()`: YAML metadata.
- `List<NoteHeading> getHeadings()`: List of headings with anchor slug IDs.
- `List<NoteLink> getLinks()`: Outgoing wikilinks, embeds, and markdown links.
- `List<NoteBlock> getBlocks()`: Structural blocks and paragraphs.
- `NoteBlock getBlockById(String blockId)`: Fast `^blockId` lookup.
- `NoteHeading getHeadingByAnchor(String anchor)`: Fast heading section lookup.
- `List<NoteTag> getTags()`, `List<NoteTask> getTasks()`, `List<NoteCallout> getCallouts()`.

### `GraphMetrics`
Topological knowledge network metrics.
- `int getTotalNodes()`, `int getTotalEdges()`, `int getConnectedComponents()`, `double getGraphDensity()`
- `List<String> getOrphanNotes()`: Notes with 0 in-degree and 0 out-degree.
- `Set<String> getGhostNotes()`: Linked notes that do not exist in the vault.
- `int getInDegree(String note)`, `int getOutDegree(String note)`
- `double getPageRank(String note)`: Centrality score computed via power iteration.
