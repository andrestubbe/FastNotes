package fastnotes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FastNotesTest {

    private FastVault vault;

    @BeforeEach
    public void setUp() {
        vault = FastVault.inMemory();
    }

    @Test
    public void testFrontmatterParsing() {
        String md = """
                ---
                title: FastJava Architecture
                tags: [performance, java, low-level]
                aliases: [FastArch, Core]
                version: 0.1.0
                ---
                # FastJava Architecture
                Main body text.
                """;

        NoteDocument doc = NoteParser.parse("Architecture.md", md);
        NoteFrontmatter fm = doc.getFrontmatter();

        assertFalse(fm.isEmpty());
        assertEquals("FastJava Architecture", fm.getTitle());
        assertEquals("FastJava Architecture", doc.getTitle());
        assertEquals(3, fm.getTags().size());
        assertTrue(fm.getTags().contains("performance"));
        assertTrue(fm.getTags().contains("java"));
        assertEquals(2, fm.getAliases().size());
        assertTrue(fm.getAliases().contains("FastArch"));
        assertEquals("0.1.0", fm.getString("version"));
    }

    @Test
    public void testHeadingsAndAnchors() {
        String md = """
                # Top Level Heading
                Some text.
                ## Sub Section & Special Chars!
                More text.
                ### Deep Subheading
                Content.
                """;

        NoteDocument doc = NoteParser.parse("guide.md", md);
        List<NoteHeading> headings = doc.getHeadings();

        assertEquals(3, headings.size());
        assertEquals(1, headings.get(0).getLevel());
        assertEquals("Top Level Heading", headings.get(0).getText());
        assertEquals("top-level-heading", headings.get(0).getAnchorId());

        assertEquals(2, headings.get(1).getLevel());
        assertEquals("Sub Section & Special Chars!", headings.get(1).getText());
        assertEquals("sub-section-special-chars", headings.get(1).getAnchorId());

        assertEquals(3, headings.get(2).getLevel());
        assertEquals("deep-subheading", headings.get(2).getAnchorId());

        assertNotNull(doc.getHeadingByAnchor("top-level-heading"));
        assertNotNull(doc.getHeadingByAnchor("#sub-section-special-chars"));
    }

    @Test
    public void testWikilinksAndEmbeds() {
        String md = """
                Linking to [[NoteA]] and [[NoteB|Alias B]].
                Heading ref: [[NoteC#My Heading]].
                Block ref: [[NoteD#^block123]].
                Embed image: ![[screenshot.png]].
                Embed block: ![[NoteE#^perf-block]].
                Markdown link: [External Site](https://fastjava.org).
                """;

        NoteDocument doc = NoteParser.parse("source.md", md);
        List<NoteLink> links = doc.getLinks();

        assertEquals(7, links.size());

        NoteLink l1 = links.get(0);
        assertEquals(NoteLink.LinkType.WIKILINK, l1.getType());
        assertEquals("NoteA", l1.getTargetDoc());
        assertNull(l1.getAlias());

        NoteLink l2 = links.get(1);
        assertEquals("NoteB", l2.getTargetDoc());
        assertEquals("Alias B", l2.getAlias());

        NoteLink l3 = links.get(2);
        assertEquals(NoteLink.LinkType.HEADING_LINK, l3.getType());
        assertEquals("NoteC", l3.getTargetDoc());
        assertEquals("My Heading", l3.getSubReference());

        NoteLink l4 = links.get(3);
        assertEquals(NoteLink.LinkType.BLOCK_LINK, l4.getType());
        assertEquals("NoteD", l4.getTargetDoc());
        assertEquals("block123", l4.getSubReference());
        assertTrue(l4.isBlockReference());

        NoteLink l5 = links.get(4);
        assertEquals(NoteLink.LinkType.EMBED, l5.getType());
        assertTrue(l5.isEmbed());
        assertEquals("screenshot.png", l5.getTargetDoc());

        NoteLink l6 = links.get(5);
        assertEquals(NoteLink.LinkType.EMBED, l6.getType());
        assertTrue(l6.isEmbed());
        assertEquals("NoteE", l6.getTargetDoc());
        assertEquals("perf-block", l6.getSubReference());

        NoteLink l7 = links.get(6);
        assertEquals(NoteLink.LinkType.MARKDOWN, l7.getType());
        assertEquals("https://fastjava.org", l7.getTargetDoc());
        assertTrue(l7.isExternal());
    }

    @Test
    public void testBlockAnchorIndexing() {
        String md = """
                First paragraph without anchor.
                
                Critical performance parameter: 100M ops/sec. ^perf-metric
                
                ```java
                // Code block should not register ^fake-anchor
                int x = 42;
                ```
                
                Another block here. ^second-anchor
                """;

        NoteDocument doc = NoteParser.parse("test.md", md);
        Map<String, NoteBlock> blockIndex = doc.getBlockIndex();

        assertTrue(blockIndex.containsKey("perf-metric"));
        assertTrue(blockIndex.containsKey("second-anchor"));
        assertFalse(blockIndex.containsKey("fake-anchor"));

        NoteBlock block = doc.getBlockById("perf-metric");
        assertNotNull(block);
        assertTrue(block.getContent().contains("Critical performance parameter"));
    }

    @Test
    public void testHierarchicalTags() {
        String md = """
                ---
                tags: [engineering/backend, fastjava]
                ---
                # Dev Notes
                Working on #ai/vector/simd and #ai/reasoner components.
                Also tagged #engineering/backend/optimization in text.
                """;

        NoteDocument doc = NoteParser.parse("dev.md", md);

        assertTrue(doc.hasTag("engineering"));
        assertTrue(doc.hasTag("engineering/backend"));
        assertTrue(doc.hasTag("engineering/backend/optimization"));
        assertTrue(doc.hasTag("ai"));
        assertTrue(doc.hasTag("ai/vector"));
        assertTrue(doc.hasTag("ai/vector/simd"));
        assertTrue(doc.hasTag("#fastjava"));
        assertFalse(doc.hasTag("frontend"));
    }

    @Test
    public void testTasksAndCallouts() {
        String md = """
                # Tasks & Callouts
                
                > [!WARNING]+ Dangerous Operation
                > Make sure to allocate direct memory.
                
                Checklist:
                - [x] Step 1: Initialize buffer #setup
                - [ ] Step 2: Run benchmark
                - [/] Step 3: Profile cache misses
                - [-] Step 4: Deprecated approach
                """;

        NoteDocument doc = NoteParser.parse("tasks.md", md);

        List<NoteCallout> callouts = doc.getCallouts();
        assertEquals(1, callouts.size());
        NoteCallout c = callouts.get(0);
        assertEquals("WARNING", c.getCalloutType());
        assertEquals("Dangerous Operation", c.getTitle());
        assertEquals('+', c.getFoldState());
        assertTrue(c.getContent().contains("Make sure to allocate direct memory."));

        List<NoteTask> tasks = doc.getTasks();
        assertEquals(4, tasks.size());
        assertTrue(tasks.get(0).isCompleted());
        assertEquals('x', tasks.get(0).getStatusChar());
        assertFalse(tasks.get(1).isCompleted());
        assertTrue(tasks.get(2).isInProgress());
        assertTrue(tasks.get(3).isCancelled());
    }

    @Test
    public void testBidirectionalGraphAndBacklinks() {
        vault.addDocument("Alpha.md", """
                # Alpha Node
                Links to [[Beta]] and [[Gamma]].
                """);

        vault.addDocument("Beta.md", """
                # Beta Node
                Links to [[Alpha]] and [[Gamma]].
                """);

        vault.addDocument("Gamma.md", """
                # Gamma Node
                Links to [[Alpha]] and non-existent [[GhostNode]].
                """);

        vault.addDocument("Orphan.md", """
                # Orphan Node
                Isolated without connections.
                """);

        // Backlinks to Alpha
        List<Backlink> alphaBacklinks = vault.getBacklinks("Alpha");
        assertEquals(2, alphaBacklinks.size()); // From Beta and Gamma

        // Backlinks to Gamma
        List<Backlink> gammaBacklinks = vault.getBacklinks("Gamma");
        assertEquals(2, gammaBacklinks.size()); // From Alpha and Beta

        // Graph Metrics
        GraphMetrics metrics = vault.getMetrics();
        assertEquals(4, metrics.getTotalNodes());
        assertEquals(5, metrics.getTotalEdges()); // Alpha->Beta, Alpha->Gamma, Beta->Alpha, Beta->Gamma, Gamma->Alpha

        // Ghost notes
        Set<String> ghosts = metrics.getGhostNotes();
        assertTrue(ghosts.contains("GhostNode"));

        // Orphan notes
        List<String> orphans = metrics.getOrphanNotes();
        assertEquals(1, orphans.size());
        assertEquals("orphan", orphans.get(0));

        // Degrees
        assertEquals(2, metrics.getInDegree("alpha"));
        assertEquals(2, metrics.getOutDegree("alpha"));
        assertEquals(2, metrics.getInDegree("gamma"));
        assertEquals(1, metrics.getOutDegree("gamma"));
        assertEquals(0, metrics.getInDegree("orphan"));
        assertEquals(0, metrics.getOutDegree("orphan"));

        // PageRank: Alpha and Gamma should have highest ranks
        Map<String, Double> ranks = metrics.getPageRanks();
        assertTrue(ranks.get("alpha") > ranks.get("orphan"));
        assertTrue(ranks.get("gamma") > ranks.get("orphan"));
    }

    @Test
    public void testTransclusionResolution() {
        vault.addDocument("Spec.md", """
                # Specifications
                
                ## Performance Requirements
                System must sustain 5,000,000 requests/sec with p99 < 100 microseconds.
                
                ## Hardware Specs
                Target CPU: AMD Zen 5 / Intel Arrow Lake with AVX-512. ^cpu-spec
                """);

        // 1. Block transclusion
        String blockText = vault.resolveTransclusion("Spec#^cpu-spec");
        assertTrue(blockText.contains("Target CPU: AMD Zen 5 / Intel Arrow Lake with AVX-512. ^cpu-spec"));

        // 2. Heading transclusion
        String headingText = vault.resolveTransclusion("Spec#Performance Requirements");
        assertTrue(headingText.contains("System must sustain 5,000,000 requests/sec"));

        // 3. Full doc transclusion
        String fullDoc = vault.resolveTransclusion("Spec");
        assertTrue(fullDoc.contains("# Specifications"));
    }

    @Test
    public void testVaultTagSearchAndTaskAggregation() {
        vault.addDocument("Dev1.md", """
                ---
                tags: [engineering/ai]
                ---
                # AI Subsystem
                - [x] Train quantized model #ai
                - [ ] Deploy to runtime
                """);

        vault.addDocument("Dev2.md", """
                # Performance Subsystem
                Tagged with #engineering/performance.
                - [x] AVX-512 optimization
                """);

        List<NoteDocument> engNotes = vault.findNotesByTag("engineering");
        assertEquals(2, engNotes.size());

        List<NoteDocument> aiNotes = vault.findNotesByTag("engineering/ai");
        assertEquals(1, aiNotes.size());

        List<FastVault.VaultTaskItem> allTasks = vault.getAllTasks(null);
        assertEquals(3, allTasks.size());

        List<FastVault.VaultTaskItem> completedTasks = vault.getAllTasks(true);
        assertEquals(2, completedTasks.size());

        List<FastVault.VaultTaskItem> pendingTasks = vault.getAllTasks(false);
        assertEquals(1, pendingTasks.size());
    }
}
