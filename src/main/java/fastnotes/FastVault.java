package fastnotes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance Obsidian & Markdown Vault indexing engine with bidirectional graph linking,
 * global block registry, transclusion resolution, and topological analytics.
 */
public final class FastVault {
    private final Path vaultRoot;
    private final Map<String, NoteDocument> documentsByPath = new ConcurrentHashMap<>();
    private final Map<String, NoteDocument> documentsByName = new ConcurrentHashMap<>();
    private final Map<String, List<NoteDocument>> documentsByAlias = new ConcurrentHashMap<>();
    private final Map<String, List<Backlink>> backlinksByNote = new ConcurrentHashMap<>();
    private final Map<String, List<NoteDocument>> tagIndex = new ConcurrentHashMap<>();
    private final Map<String, NoteBlock> globalBlockIndex = new ConcurrentHashMap<>();
    private final Set<String> ghostNotes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile GraphMetrics cachedMetrics = null;

    private FastVault(Path vaultRoot) {
        this.vaultRoot = vaultRoot != null ? vaultRoot.toAbsolutePath().normalize() : null;
    }

    /**
     * Creates an empty in-memory vault.
     */
    public static FastVault inMemory() {
        return new FastVault(null);
    }

    /**
     * Scans and indexes an entire directory tree as an Obsidian vault.
     */
    public static FastVault scan(Path vaultDir) throws IOException {
        FastVault vault = new FastVault(vaultDir);
        vault.reload();
        return vault;
    }

    /**
     * Scans and indexes an entire directory tree as an Obsidian vault.
     */
    public static FastVault scan(String vaultDir) throws IOException {
        return scan(Path.of(vaultDir));
    }

    /**
     * Reloads and re-indexes all markdown files from the vault directory.
     */
    public synchronized void reload() throws IOException {
        clear();
        if (vaultRoot == null || !Files.exists(vaultRoot)) return;

        List<Path> mdFiles = new ArrayList<>();
        Files.walkFileTree(vaultRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String name = dir.getFileName() != null ? dir.getFileName().toString() : "";
                if (name.startsWith(".") || name.equalsIgnoreCase("target") || name.equalsIgnoreCase("node_modules") || name.equalsIgnoreCase("build")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().toLowerCase().endsWith(".md")) {
                    mdFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // Parallel parsing
        mdFiles.parallelStream().forEach(filePath -> {
            try {
                String relPath = vaultRoot.relativize(filePath).toString().replace('\\', '/');
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                NoteDocument doc = NoteParser.parse(relPath, content);
                indexDocument(doc, false);
            } catch (Exception ignored) {
            }
        });

        // Rebuild graph relationships and metrics
        rebuildGraph();
    }

    public synchronized void clear() {
        documentsByPath.clear();
        documentsByName.clear();
        documentsByAlias.clear();
        backlinksByNote.clear();
        tagIndex.clear();
        globalBlockIndex.clear();
        ghostNotes.clear();
        cachedMetrics = null;
    }

    /**
     * Adds or updates a document in the vault index and refreshes graph links.
     */
    public synchronized void addDocument(NoteDocument doc) {
        indexDocument(doc, true);
        rebuildGraph();
    }

    /**
     * Adds a note from path and raw content string.
     */
    public synchronized NoteDocument addDocument(String relativePath, String markdownText) {
        NoteDocument doc = NoteParser.parse(relativePath, markdownText);
        addDocument(doc);
        return doc;
    }

    private void indexDocument(NoteDocument doc, boolean syncBlockMap) {
        documentsByPath.put(doc.getPath(), doc);
        documentsByName.put(doc.getNoteName().toLowerCase(), doc);

        for (String alias : doc.getFrontmatter().getAliases()) {
            documentsByAlias.computeIfAbsent(alias.toLowerCase(), k -> new ArrayList<>()).add(doc);
        }

        // Tags
        Set<String> allTags = new HashSet<>();
        for (String t : doc.getFrontmatter().getTags()) {
            allTags.add(t.startsWith("#") ? t.substring(1).toLowerCase() : t.toLowerCase());
        }
        for (NoteTag t : doc.getTags()) {
            allTags.add(t.getNormalizedTag());
        }
        for (String t : allTags) {
            tagIndex.computeIfAbsent(t, k -> new ArrayList<>()).add(doc);
        }

        // Blocks
        for (NoteBlock block : doc.getBlocks()) {
            if (block.hasBlockId()) {
                globalBlockIndex.put(doc.getNoteName() + "#^" + block.getBlockId(), block);
                globalBlockIndex.putIfAbsent("^" + block.getBlockId(), block);
            }
        }
    }

    /**
     * Rebuilds bidirectional backlinks, ghost links, and PageRank metrics.
     */
    public synchronized void rebuildGraph() {
        backlinksByNote.clear();
        ghostNotes.clear();

        for (NoteDocument sourceDoc : documentsByPath.values()) {
            String[] lines = sourceDoc.getRawContent().split("\r?\n", -1);

            for (NoteLink link : sourceDoc.getLinks()) {
                if (link.isExternal()) continue;

                String targetNoteName = link.getTargetDoc();
                if (targetNoteName.isEmpty()) {
                    // Local anchor in same note
                    targetNoteName = sourceDoc.getNoteName();
                }

                NoteDocument targetDoc = findNote(targetNoteName);
                if (targetDoc != null) {
                    String snippet = (link.getLineNumber() > 0 && link.getLineNumber() <= lines.length)
                            ? lines[link.getLineNumber() - 1].trim()
                            : link.getRawText();

                    Backlink backlink = new Backlink(sourceDoc.getPath(), sourceDoc.getTitle(), link, snippet);
                    backlinksByNote.computeIfAbsent(targetDoc.getNoteName().toLowerCase(), k -> new ArrayList<>()).add(backlink);
                } else if (!targetNoteName.isEmpty() && !targetNoteName.contains(".")) {
                    // Ghost note
                    ghostNotes.add(targetNoteName);
                }
            }
        }

        cachedMetrics = computeMetricsInternal();
    }

    /**
     * Finds a note by name, path, or frontmatter alias.
     */
    public NoteDocument findNote(String query) {
        if (query == null || query.isEmpty()) return null;
        String q = query.replace('\\', '/');
        if (q.endsWith(".md")) {
            q = q.substring(0, q.length() - 3);
        }

        NoteDocument doc = documentsByName.get(q.toLowerCase());
        if (doc != null) return doc;

        doc = documentsByPath.get(query.replace('\\', '/'));
        if (doc != null) return doc;

        for (Map.Entry<String, NoteDocument> entry : documentsByPath.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(query) || entry.getKey().endsWith("/" + query) || entry.getKey().endsWith("/" + query + ".md")) {
                return entry.getValue();
            }
        }

        List<NoteDocument> aliases = documentsByAlias.get(q.toLowerCase());
        if (aliases != null && !aliases.isEmpty()) {
            return aliases.get(0);
        }

        return null;
    }

    /**
     * Returns all incoming backlinks to a note.
     */
    public List<Backlink> getBacklinks(String noteName) {
        if (noteName == null) return Collections.emptyList();
        String key = noteName.toLowerCase();
        if (key.endsWith(".md")) key = key.substring(0, key.length() - 3);
        List<Backlink> list = backlinksByNote.get(key);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    /**
     * Returns all notes matching a tag or its hierarchical sub-tags (e.g. #dev/java).
     */
    public List<NoteDocument> findNotesByTag(String tagQuery) {
        if (tagQuery == null || tagQuery.isEmpty()) return Collections.emptyList();
        String q = tagQuery.startsWith("#") ? tagQuery.substring(1).toLowerCase() : tagQuery.toLowerCase();
        Set<NoteDocument> matched = new LinkedHashSet<>();
        for (Map.Entry<String, List<NoteDocument>> entry : tagIndex.entrySet()) {
            String tag = entry.getKey();
            if (tag.equals(q) || tag.startsWith(q + "/")) {
                matched.addAll(entry.getValue());
            }
        }
        return new ArrayList<>(matched);
    }

    /**
     * Resolves an Obsidian transclusion / block embed into its text content.
     * Examples:
     * - "Architecture" -> full document content
     * - "Architecture#^fast-io-spec" -> paragraph ending with ^fast-io-spec
     * - "Architecture#Performance" -> heading section content
     */
    public String resolveTransclusion(String linkTarget) {
        if (linkTarget == null || linkTarget.isEmpty()) return "";
        String docName = linkTarget;
        String subRef = null;
        int hash = linkTarget.indexOf('#');
        if (hash != -1) {
            docName = linkTarget.substring(0, hash).trim();
            subRef = linkTarget.substring(hash + 1).trim();
        }

        NoteDocument doc = findNote(docName);
        if (doc == null) {
            return "[Unresolved Transclusion: " + linkTarget + "]";
        }

        if (subRef == null || subRef.isEmpty()) {
            return doc.getRawContent();
        }

        // Block reference (^id)
        if (subRef.startsWith("^")) {
            String blockId = subRef.substring(1);
            NoteBlock block = doc.getBlockById(blockId);
            if (block != null) {
                return block.getContent();
            }
            return "[Unresolved Block: " + linkTarget + "]";
        }

        // Heading section reference
        NoteHeading heading = doc.getHeadingByAnchor(subRef);
        if (heading != null) {
            return extractHeadingSection(doc, heading);
        }

        return "[Unresolved Section: " + linkTarget + "]";
    }

    private String extractHeadingSection(NoteDocument doc, NoteHeading heading) {
        String[] lines = doc.getRawContent().split("\r?\n", -1);
        StringBuilder sb = new StringBuilder();
        int startLine = heading.getLineNumber(); // 1-based
        int headingLevel = heading.getLevel();

        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                int level = 0;
                while (level < trimmed.length() && trimmed.charAt(level) == '#') level++;
                if (level >= 1 && level <= 6 && level <= headingLevel && trimmed.charAt(level) == ' ') {
                    break; // Next heading of equal or higher importance
                }
            }
            sb.append(line).append('\n');
        }
        return sb.toString().trim();
    }

    /**
     * Aggregates all tasks / checklist items across all notes in the vault.
     */
    public List<VaultTaskItem> getAllTasks(Boolean completedFilter) {
        List<VaultTaskItem> result = new ArrayList<>();
        for (NoteDocument doc : documentsByPath.values()) {
            for (NoteTask task : doc.getTasks()) {
                if (completedFilter == null || task.isCompleted() == completedFilter) {
                    result.add(new VaultTaskItem(doc.getPath(), doc.getTitle(), task));
                }
            }
        }
        return result;
    }

    public static class VaultTaskItem {
        private final String notePath;
        private final String noteTitle;
        private final NoteTask task;

        public VaultTaskItem(String notePath, String noteTitle, NoteTask task) {
            this.notePath = notePath;
            this.noteTitle = noteTitle;
            this.task = task;
        }

        public String getNotePath() {
            return notePath;
        }

        public String getNoteTitle() {
            return noteTitle;
        }

        public NoteTask getTask() {
            return task;
        }

        @Override
        public String toString() {
            return "[" + (task.isCompleted() ? "X" : " ") + "] " + task.getDescription() + " (" + noteTitle + ":" + task.getLineNumber() + ")";
        }
    }

    /**
     * Gets all indexed documents.
     */
    public Collection<NoteDocument> getDocuments() {
        return Collections.unmodifiableCollection(documentsByPath.values());
    }

    public int getDocumentCount() {
        return documentsByPath.size();
    }

    public Set<String> getGhostNotes() {
        return Collections.unmodifiableSet(ghostNotes);
    }

    public Set<String> getAllTags() {
        return Collections.unmodifiableSet(tagIndex.keySet());
    }

    public GraphMetrics getMetrics() {
        if (cachedMetrics == null) {
            cachedMetrics = computeMetricsInternal();
        }
        return cachedMetrics;
    }

    private GraphMetrics computeMetricsInternal() {
        int totalNodes = documentsByName.size();
        int totalEdges = 0;
        Map<String, Integer> inDeg = new LinkedHashMap<>();
        Map<String, Integer> outDeg = new LinkedHashMap<>();
        Map<String, List<String>> adjList = new HashMap<>();

        for (NoteDocument doc : documentsByName.values()) {
            String u = doc.getNoteName().toLowerCase();
            inDeg.put(u, 0);
            outDeg.put(u, 0);
            adjList.put(u, new ArrayList<>());
        }

        for (NoteDocument doc : documentsByName.values()) {
            String u = doc.getNoteName().toLowerCase();
            Set<String> targets = new HashSet<>();
            for (NoteLink link : doc.getLinks()) {
                if (link.isExternal()) continue;
                String targetName = link.getTargetDoc();
                if (targetName.isEmpty()) targetName = doc.getNoteName();
                NoteDocument targetDoc = findNote(targetName);
                if (targetDoc != null) {
                    targets.add(targetDoc.getNoteName().toLowerCase());
                }
            }

            outDeg.put(u, targets.size());
            totalEdges += targets.size();

            for (String v : targets) {
                inDeg.put(v, inDeg.getOrDefault(v, 0) + 1);
                adjList.get(u).add(v);
            }
        }

        // Orphans: in-degree == 0 && out-degree == 0
        List<String> orphans = new ArrayList<>();
        for (String node : documentsByName.keySet()) {
            if (inDeg.getOrDefault(node, 0) == 0 && outDeg.getOrDefault(node, 0) == 0) {
                orphans.add(node);
            }
        }

        // PageRank power iteration
        Map<String, Double> pageRanks = computePageRank(documentsByName.keySet(), adjList);

        // Connected components (undirected)
        int components = computeConnectedComponents(documentsByName.keySet(), adjList);

        double density = totalNodes > 1 ? (double) totalEdges / (totalNodes * (totalNodes - 1)) : 0.0;

        return new GraphMetrics(totalNodes, totalEdges, components, density, orphans, ghostNotes, inDeg, outDeg, pageRanks);
    }

    private Map<String, Double> computePageRank(Set<String> nodes, Map<String, List<String>> adjList) {
        Map<String, Double> pr = new HashMap<>();
        int n = nodes.size();
        if (n == 0) return pr;

        double initial = 1.0 / n;
        for (String node : nodes) {
            pr.put(node, initial);
        }

        double damping = 0.85;
        for (int iter = 0; iter < 20; iter++) {
            Map<String, Double> nextPr = new HashMap<>();
            double sinkContribution = 0.0;

            for (String node : nodes) {
                List<String> out = adjList.get(node);
                if (out == null || out.isEmpty()) {
                    sinkContribution += pr.get(node);
                }
            }

            for (String node : nodes) {
                double rankSum = 0.0;
                for (String src : nodes) {
                    List<String> out = adjList.get(src);
                    if (out != null && out.contains(node)) {
                        rankSum += pr.get(src) / out.size();
                    }
                }
                double newRank = (1.0 - damping) / n + damping * (rankSum + sinkContribution / n);
                nextPr.put(node, newRank);
            }
            pr = nextPr;
        }
        return pr;
    }

    private int computeConnectedComponents(Set<String> nodes, Map<String, List<String>> adjList) {
        Map<String, Set<String>> undirected = new HashMap<>();
        for (String node : nodes) {
            undirected.put(node, new HashSet<>());
        }
        for (Map.Entry<String, List<String>> e : adjList.entrySet()) {
            for (String neighbor : e.getValue()) {
                if (undirected.containsKey(neighbor)) {
                    undirected.get(e.getKey()).add(neighbor);
                    undirected.get(neighbor).add(e.getKey());
                }
            }
        }

        Set<String> visited = new HashSet<>();
        int count = 0;
        for (String node : nodes) {
            if (!visited.contains(node)) {
                count++;
                Queue<String> queue = new ArrayDeque<>();
                queue.add(node);
                visited.add(node);
                while (!queue.isEmpty()) {
                    String curr = queue.poll();
                    for (String adj : undirected.getOrDefault(curr, Collections.emptySet())) {
                        if (!visited.contains(adj)) {
                            visited.add(adj);
                            queue.add(adj);
                        }
                    }
                }
            }
        }
        return count;
    }
}
