package fastnotes;

import java.util.*;

/**
 * FastNotes 120-Column Hero Architecture Demo showcasing Obsidian Vault Parsing,
 * Bidirectional Link Graph Traversal, Block Anchor Indexing, Transclusion Resolution,
 * and Graph Centrality Analytics.
 */
public class Demo {

    private static final int TERM_WIDTH = 120;

    public static void main(String[] args) {
        printHeader();

        // 1. Construct Simulated Obsidian Knowledge Vault
        FastVault vault = FastVault.inMemory();
        populateSampleVault(vault);

        // 2. Display Vault Overview & Node Hierarchy
        printVaultTree(vault);

        // 3. Bidirectional Link Graph & Backlinks Inspection
        printBidirectionalGraph(vault);

        // 4. Block-Level Indexing & Transclusion Resolution
        printTransclusionShowcase(vault);

        // 5. Hierarchical Tags & Task Aggregator
        printTagsAndTasks(vault);

        // 6. Graph Centrality & Topological Analytics
        printGraphAnalytics(vault);

        // 7. Micro-Benchmark Performance Throughput
        runThroughputBenchmark(vault);

        printFooter();
    }

    private static void printHeader() {
        System.out.println(FastANSI.FG_BRIGHT_CYAN + "=".repeat(TERM_WIDTH) + FastANSI.RESET);
        String title = "FASTNOTES // HIGH-PERFORMANCE OBSIDIAN VAULT PARSER & BIDIRECTIONAL GRAPH ENGINE";
        String subtitle = "Zero-Allocation AST Extraction | Block-Level Indexing | PageRank Knowledge Topology | FastJava 0.1.0";
        System.out.println(center(FastANSI.BOLD + FastANSI.FG_BRIGHT_WHITE + title + FastANSI.RESET, TERM_WIDTH));
        System.out.println(center(FastANSI.FG_BRIGHT_BLACK + subtitle + FastANSI.RESET, TERM_WIDTH));
        System.out.println(FastANSI.FG_BRIGHT_CYAN + "=".repeat(TERM_WIDTH) + FastANSI.RESET);
        System.out.println();
    }

    private static void populateSampleVault(FastVault vault) {
        // Document 1: Architecture
        vault.addDocument("Architecture.md", """
                ---
                title: System Architecture & Zero-Copy Engine
                tags: [architecture, core/performance, spec]
                aliases: [CoreArchitecture, SystemArch]
                ---
                # System Architecture & Zero-Copy Engine
                
                The FastJava ecosystem implements high-throughput procedural parsers with zero GC overhead.
                
                ## Memory Model
                Memory layout utilizes contiguous byte structures and SIMD vector lanes.
                See [[SIMD-Engine]] for hardware vectorization primitives.
                
                > [!NOTE] Design Principle
                > All parsers must operate in single-pass linear time without recursive stack explosions.
                
                Key component integration:
                - Forward links to [[Graph-Store#^query-perf]]
                - Hardware integration via [[SIMD-Engine#^avx512-spec]]
                - Roadmap planning in [[Roadmap-2026]]
                
                Critical performance constraint:
                Throughput must exceed 2,000,000 notes/sec per core. ^arch-constraint
                """);

        // Document 2: SIMD-Engine
        vault.addDocument("SIMD-Engine.md", """
                ---
                title: SIMD Hardware Vectorization
                tags: [hardware/simd, core/vector, avx]
                aliases: [VectorEngine]
                ---
                # SIMD Hardware Vectorization
                
                Hardware vectorization module leveraging 256-bit AVX2 and 512-bit AVX-512 registers.
                
                ## AVX-512 Kernel
                Direct memory vector registers scan 64 bytes per cycle for delimiter matching.
                AVX-512 register bandwidth reaches 42 GB/s on modern server CPUs. ^avx512-spec
                
                Referenced by [[Architecture]] and [[Memory-Buffer]].
                
                ### Tasks
                - [x] Implement 64-byte delimiter scan kernel #dev/simd
                - [ ] Add ARM Neon 128-bit fallback path #dev/arm
                - [/] Benchmark Zen 5 AVX-512 throughput #benchmark
                """);

        // Document 3: Graph-Store
        vault.addDocument("Graph-Store.md", """
                ---
                title: Bidirectional Graph & Backlink Registry
                tags: [graph, core/topology, indexing]
                ---
                # Bidirectional Graph & Backlink Registry
                
                Maintains inverted index of incoming and outgoing hyperlinks across the vault.
                
                ## Query Engine
                Sub-microsecond backlink resolution enables real-time 2-hop graph queries.
                Query response latency is guaranteed under 45 nanoseconds. ^query-perf
                
                Incoming links from [[Architecture]], transclusions in [[Daily-Log]], and tasks in [[Roadmap-2026]].
                Also references unwritten [[Neural-Search]] and [[GPU-Accelerator]].
                
                > [!TIP]+ Optimization Note
                > Pre-allocating node arrays eliminates hash collisions in dense subgraphs.
                """);

        // Document 4: Memory-Buffer
        vault.addDocument("Memory-Buffer.md", """
                ---
                title: Off-Heap Native Memory Ring
                tags: [memory, core/offheap]
                ---
                # Off-Heap Native Memory Ring
                
                Contiguous off-heap buffer ring for zero-copy streaming.
                
                Related to [[Architecture]] and [[SIMD-Engine]].
                - [x] Zero-copy DMA transfers verified
                - [ ] Memory pool compaction routine
                """);

        // Document 5: Roadmap-2026
        vault.addDocument("Roadmap-2026.md", """
                ---
                title: FastJava 2026 Strategic Roadmap
                tags: [planning, roadmap/2026]
                ---
                # Strategic Roadmap 2026
                
                Targeting enterprise knowledge graph orchestration and microsecond AI retrieval.
                
                ## Deliverables
                - Release [[Architecture]] v0.1.0 specifications
                - Optimize [[Graph-Store]] PageRank power iteration
                - Integrate with [[Neural-Search]]
                
                - [x] Complete Markdown and Obsidian parser
                - [ ] Publish open-source benchmarks to GitHub
                - [ ] Full JNI SIMD acceleration for macOS ARM64
                """);

        // Document 6: Daily-Log
        vault.addDocument("Daily-Log.md", """
                ---
                title: Engineering Daily Log
                tags: [daily, log/dev]
                ---
                # Engineering Daily Log
                
                Today's focus was verifying transclusion embeds and block anchors.
                
                Transclusion 1 (SIMD spec):
                ![[SIMD-Engine#^avx512-spec]]
                
                Transclusion 2 (Graph query performance):
                ![[Graph-Store#^query-perf]]
                
                Backlink check: [[Architecture]] is the central hub.
                """);

        // Document 7: Standalone-Notes (Orphan)
        vault.addDocument("Standalone-Notes.md", """
                ---
                title: Isolated Scratchpad
                tags: [scratchpad, draft]
                ---
                # Isolated Scratchpad
                
                This note is an isolated node with no incoming or outgoing links.
                """);
    }

    private static void printVaultTree(FastVault vault) {
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "▶ SECTION 1: VAULT STRUCTURE & AST DOCUMENT REGISTRY" + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_BLACK + "─".repeat(TERM_WIDTH) + FastANSI.RESET);

        String colFormat = "%-32s %-30s %-8s %-8s %-8s %-8s %-12s";
        System.out.println(FastANSI.FG_BRIGHT_BLACK + String.format(colFormat, "NOTE PATH / NAME", "DOCUMENT TITLE", "HEADINGS", "LINKS", "BLOCKS", "TAGS", "WORD COUNT") + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_BLACK + "─".repeat(TERM_WIDTH) + FastANSI.RESET);

        for (NoteDocument doc : vault.getDocuments()) {
            String path = FastANSI.truncateMiddle(doc.getPath(), 30);
            String title = FastANSI.truncateMiddle(doc.getTitle(), 28);
            System.out.printf(colFormat + "%n",
                    FastANSI.FG_BRIGHT_WHITE + path + FastANSI.RESET,
                    FastANSI.FG_CYAN + title + FastANSI.RESET,
                    FastANSI.BOLD + doc.getHeadings().size() + FastANSI.RESET,
                    FastANSI.BOLD + doc.getLinks().size() + FastANSI.RESET,
                    FastANSI.BOLD + doc.getBlocks().size() + FastANSI.RESET,
                    FastANSI.BOLD + doc.getTags().size() + FastANSI.RESET,
                    FastANSI.FG_BRIGHT_YELLOW + doc.getWordCount() + " w" + FastANSI.RESET
            );
        }
        System.out.println();
    }

    private static void printBidirectionalGraph(FastVault vault) {
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "▶ SECTION 2: BIDIRECTIONAL KNOWLEDGE GRAPH & BACKLINK RESOLUTION" + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_BLACK + "─".repeat(TERM_WIDTH) + FastANSI.RESET);

        for (NoteDocument doc : vault.getDocuments()) {
            List<Backlink> backlinks = vault.getBacklinks(doc.getNoteName());
            List<NoteLink> outLinks = doc.getLinks();

            System.out.println(FastANSI.FG_BRIGHT_WHITE + FastANSI.BOLD + "● [NODE] " + doc.getNoteName() + FastANSI.RESET +
                    FastANSI.FG_BRIGHT_BLACK + " (" + doc.getPath() + ") " +
                    "| In-Degree: " + FastANSI.FG_GREEN + backlinks.size() + FastANSI.FG_BRIGHT_BLACK +
                    " | Out-Degree: " + FastANSI.FG_CYAN + outLinks.size() + FastANSI.RESET);

            // Outgoing Links
            if (!outLinks.isEmpty()) {
                System.out.println(FastANSI.FG_BRIGHT_BLACK + "  ├── Outgoing Hyperlinks (" + outLinks.size() + "):" + FastANSI.RESET);
                for (int i = 0; i < outLinks.size(); i++) {
                    NoteLink link = outLinks.get(i);
                    boolean last = (i == outLinks.size() - 1);
                    String branch = last ? "  │   └── " : "  │   ├── ";
                    String typeTag = link.isEmbed() ? "[EMBED]" : (link.isBlockReference() ? "[BLOCK]" : "[WIKILINK]");
                    System.out.println(FastANSI.FG_BRIGHT_BLACK + branch + FastANSI.FG_CYAN + typeTag + " " +
                            FastANSI.FG_BRIGHT_WHITE + link.getTargetDoc() +
                            (link.getSubReference() != null ? FastANSI.FG_YELLOW + " (#" + link.getSubReference() + ")" : "") +
                            FastANSI.FG_BRIGHT_BLACK + " [Line " + link.getLineNumber() + "]" + FastANSI.RESET);
                }
            }

            // Incoming Backlinks
            if (!backlinks.isEmpty()) {
                System.out.println(FastANSI.FG_BRIGHT_BLACK + "  └── Incoming Backlinks (" + backlinks.size() + "):" + FastANSI.RESET);
                for (int i = 0; i < backlinks.size(); i++) {
                    Backlink bl = backlinks.get(i);
                    boolean last = (i == backlinks.size() - 1);
                    String branch = last ? "      └── " : "      ├── ";
                    System.out.println(FastANSI.FG_BRIGHT_BLACK + branch + "From: " + FastANSI.FG_BRIGHT_WHITE + bl.getSourceNoteTitle() +
                            FastANSI.FG_BRIGHT_BLACK + " [L:" + bl.getLineNumber() + "] -> \"" +
                            FastANSI.FG_WHITE + FastANSI.truncateMiddle(bl.getContextExcerpt(), 48) + FastANSI.FG_BRIGHT_BLACK + "\"" + FastANSI.RESET);
                }
            } else {
                System.out.println(FastANSI.FG_BRIGHT_BLACK + "  └── Incoming Backlinks: (none)" + FastANSI.RESET);
            }
            System.out.println();
        }
    }

    private static void printTransclusionShowcase(FastVault vault) {
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "▶ SECTION 3: BLOCK INDEXING & TRANSCLUSION RESOLUTION" + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_BLACK + "─".repeat(TERM_WIDTH) + FastANSI.RESET);

        String[] sampleTargets = {
                "SIMD-Engine#^avx512-spec",
                "Graph-Store#^query-perf",
                "Architecture#^arch-constraint",
                "Architecture#Memory Model"
        };

        for (String target : sampleTargets) {
            String resolved = vault.resolveTransclusion(target);
            System.out.println(FastANSI.FG_BRIGHT_BLACK + "Query Transclusion: " + FastANSI.FG_BRIGHT_CYAN + "![[" + target + "]]" + FastANSI.RESET);
            System.out.println(FastANSI.FG_BRIGHT_BLACK + "  └── Resolved AST Block Content:" + FastANSI.RESET);
            for (String line : resolved.split("\n")) {
                System.out.println(FastANSI.FG_BRIGHT_BLACK + "      │ " + FastANSI.FG_BRIGHT_WHITE + line + FastANSI.RESET);
            }
            System.out.println();
        }
    }

    private static void printTagsAndTasks(FastVault vault) {
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "▶ SECTION 4: HIERARCHICAL TAG INDEX & VAULT TASK AGGREGATOR" + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_BLACK + "─".repeat(TERM_WIDTH) + FastANSI.RESET);

        // Tags hierarchy
        System.out.println(FastANSI.FG_BRIGHT_WHITE + "Hierarchical Tag Hierarchy Search (#core):" + FastANSI.RESET);
        List<NoteDocument> coreNotes = vault.findNotesByTag("core");
        for (NoteDocument doc : coreNotes) {
            System.out.println(FastANSI.FG_BRIGHT_BLACK + "  ├── Matched Note: " + FastANSI.FG_BRIGHT_WHITE + doc.getTitle() +
                    FastANSI.FG_BRIGHT_BLACK + " (" + doc.getPath() + ")" + FastANSI.RESET);
        }
        System.out.println();

        // Tasks
        System.out.println(FastANSI.FG_BRIGHT_WHITE + "Aggregated Tasks Across Vault:" + FastANSI.RESET);
        List<FastVault.VaultTaskItem> tasks = vault.getAllTasks(null);
        for (FastVault.VaultTaskItem item : tasks) {
            NoteTask t = item.getTask();
            String icon = t.isCompleted() ? FastANSI.FG_GREEN + "[✔] COMPLETE" :
                    (t.isInProgress() ? FastANSI.FG_YELLOW + "[⏳] IN PROGRESS" : FastANSI.FG_RED + "[✖] PENDING ");
            System.out.printf("  %s %s%-45s%s %s(Origin: %s L:%d)%s%n",
                    icon,
                    FastANSI.FG_BRIGHT_WHITE, FastANSI.truncateMiddle(t.getDescription(), 45), FastANSI.RESET,
                    FastANSI.FG_BRIGHT_BLACK, item.getNoteTitle(), t.getLineNumber(), FastANSI.RESET);
        }
        System.out.println();
    }

    private static void printGraphAnalytics(FastVault vault) {
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "▶ SECTION 5: TOPOLOGICAL GRAPH ANALYTICS & PAGERANK CENTRALITY" + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_BLACK + "─".repeat(TERM_WIDTH) + FastANSI.RESET);

        GraphMetrics metrics = vault.getMetrics();
        System.out.printf(FastANSI.FG_BRIGHT_BLACK + "Total Nodes: " + FastANSI.FG_BRIGHT_WHITE + "%d" +
                        FastANSI.FG_BRIGHT_BLACK + " | Total Edges: " + FastANSI.FG_BRIGHT_WHITE + "%d" +
                        FastANSI.FG_BRIGHT_BLACK + " | Connected Components: " + FastANSI.FG_BRIGHT_WHITE + "%d" +
                        FastANSI.FG_BRIGHT_BLACK + " | Density: " + FastANSI.FG_BRIGHT_WHITE + "%.4f" +
                        FastANSI.FG_BRIGHT_BLACK + " | Ghost Links: " + FastANSI.FG_RED + "%s" + FastANSI.RESET + "%n%n",
                metrics.getTotalNodes(), metrics.getTotalEdges(), metrics.getConnectedComponents(),
                metrics.getGraphDensity(), metrics.getGhostNotes());

        System.out.println(FastANSI.FG_BRIGHT_WHITE + "PageRank Node Centrality Ranking:" + FastANSI.RESET);
        List<Map.Entry<String, Double>> sortedRanks = new ArrayList<>(metrics.getPageRanks().entrySet());
        sortedRanks.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (int i = 0; i < sortedRanks.size(); i++) {
            Map.Entry<String, Double> entry = sortedRanks.get(i);
            String node = entry.getKey();
            double pr = entry.getValue();
            int in = metrics.getInDegree(node);
            int out = metrics.getOutDegree(node);

            int barWidth = (int) (pr * 180);
            String bar = "█".repeat(Math.max(1, barWidth));

            System.out.printf("  #%d %-22s | In:%2d Out:%2d | PR: %s%6.4f%s | %s%s%s%n",
                    i + 1,
                    FastANSI.FG_BRIGHT_WHITE + node + FastANSI.RESET,
                    in, out,
                    FastANSI.FG_BRIGHT_YELLOW, pr, FastANSI.RESET,
                    FastANSI.FG_CYAN, bar, FastANSI.RESET
            );
        }

        if (!metrics.getOrphanNotes().isEmpty()) {
            System.out.println();
            System.out.println(FastANSI.FG_BRIGHT_BLACK + "Orphan Nodes (0 In-Degree, 0 Out-Degree): " +
                    FastANSI.FG_YELLOW + metrics.getOrphanNotes() + FastANSI.RESET);
        }
        System.out.println();
    }

    private static void runThroughputBenchmark(FastVault vault) {
        System.out.println(FastANSI.FG_BRIGHT_YELLOW + "▶ SECTION 6: IN-MEMORY PARSER THROUGHPUT BENCHMARK" + FastANSI.RESET);
        System.out.println(FastANSI.FG_BRIGHT_BLACK + "─".repeat(TERM_WIDTH) + FastANSI.RESET);

        String sampleNote = """
                ---
                title: Benchmarking High-Speed Parser
                tags: [benchmark, throughput, fastjava]
                aliases: [BenchNote]
                ---
                # Benchmarking High-Speed Parser
                
                This note tests parser scanning performance across multiple headings, links, and block anchors.
                
                ## Subheading Alpha
                Referencing [[Architecture]] and transclusion ![[SIMD-Engine#^avx512-spec]].
                
                > [!NOTE] Benchmark Block
                > Testing callout parser efficiency.
                
                - [x] Completed task item #benchmark
                - [ ] Open task item
                
                End of paragraph with anchor. ^bench-anchor
                """;

        // Warmup
        for (int i = 0; i < 10_000; i++) {
            NoteParser.parse("warmup.md", sampleNote);
        }

        // Timed measurement
        int iterations = 100_000;
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            NoteParser.parse("bench.md", sampleNote);
        }
        long durationNs = System.nanoTime() - start;

        double durationSec = durationNs / 1_000_000_000.0;
        double opsPerSec = iterations / durationSec;
        double latencyNs = (double) durationNs / iterations;

        System.out.printf(FastANSI.FG_BRIGHT_WHITE + "Processed " + FastANSI.FG_BRIGHT_GREEN + "%,d" +
                        FastANSI.FG_BRIGHT_WHITE + " notes in " + FastANSI.FG_BRIGHT_CYAN + "%.3f ms" +
                        FastANSI.FG_BRIGHT_WHITE + " (" + FastANSI.BOLD + FastANSI.FG_BRIGHT_YELLOW + "%,.0f notes/sec" +
                        FastANSI.RESET + FastANSI.FG_BRIGHT_WHITE + " | " + FastANSI.FG_CYAN + "%.1f ns/note" +
                        FastANSI.FG_BRIGHT_WHITE + ")" + FastANSI.RESET + "%n%n",
                iterations, durationSec * 1000.0, opsPerSec, latencyNs);
    }

    private static void printFooter() {
        System.out.println(FastANSI.FG_BRIGHT_CYAN + "=".repeat(TERM_WIDTH) + FastANSI.RESET);
        System.out.println(center(FastANSI.BOLD + FastANSI.FG_BRIGHT_GREEN + "✔ FastNotes Verification & Demonstration Complete" + FastANSI.RESET, TERM_WIDTH));
        System.out.println(FastANSI.FG_BRIGHT_CYAN + "=".repeat(TERM_WIDTH) + FastANSI.RESET);
    }

    private static String center(String text, int width) {
        String stripped = text.replaceAll("\033\\[[0-9;]*m", "");
        int pad = (width - stripped.length()) / 2;
        if (pad <= 0) return text;
        return " ".repeat(pad) + text;
    }
}
