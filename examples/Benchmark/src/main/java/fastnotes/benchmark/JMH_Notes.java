package fastnotes.benchmark;

import fastnotes.FastNotes;
import fastnotes.FastVault;
import fastnotes.NoteDocument;
import fastnotes.NoteParser;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class JMH_Notes {

    private String sampleMarkdown;
    private FastVault simulatedVault;

    @Setup
    public void setup() {
        sampleMarkdown = """
                ---
                title: JMH FastNotes Benchmark Note
                tags: [benchmark, core/performance, java]
                aliases: [JMHNote, PerfDoc]
                ---
                # Benchmark Title
                
                This note tests parser speed under heavy Markdown & Obsidian link workloads.
                
                ## Architecture & Vector Lanes
                Memory alignment is preserved. Linking to [[Engine#^avx512-spec]] and [[Graph-Store]].
                Also embed image ![[architecture.png]] and transclusion ![[Core#MemoryModel]].
                
                > [!NOTE] High Performance
                > Zero allocation on critical token boundaries.
                
                ### Task List
                - [x] SIMD token scanner #dev/simd
                - [ ] ARM64 neon optimization #dev/arm
                - [/] Graph query cache #dev/graph
                
                Anchor for testing block lookup. ^perf-block-anchor
                """;

        simulatedVault = FastVault.inMemory();
        for (int i = 0; i < 200; i++) {
            String noteName = "Note_" + i;
            String target1 = "Note_" + ((i + 1) % 200);
            String target2 = "Note_" + ((i + 50) % 200);
            String content = "---\ntitle: " + noteName + "\ntags: [benchmark, cluster_" + (i % 5) + "]\n---\n# " + noteName +
                    "\nReferences [[" + target1 + "]] and [[" + target2 + "#^block_" + target2 + "]].\n\n" +
                    "Block content for note " + i + ". ^block_" + noteName + "\n" +
                    "- [ ] Task item in " + noteName + "\n";
            simulatedVault.addDocument(noteName + ".md", content);
        }
    }

    @Benchmark
    public NoteDocument benchmarkFullNoteParse() {
        return NoteParser.parse("benchmark.md", sampleMarkdown);
    }

    @Benchmark
    public Object benchmarkVaultGraphRebuild() {
        simulatedVault.rebuildGraph();
        return simulatedVault.getMetrics();
    }

    @Benchmark
    public String benchmarkTransclusionResolution() {
        return simulatedVault.resolveTransclusion("Note_10#^block_Note_10");
    }

    @Benchmark
    public Object benchmarkTagHierarchyQuery() {
        return simulatedVault.findNotesByTag("benchmark");
    }
}
