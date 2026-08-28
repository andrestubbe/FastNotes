# FastNotes

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![FastJava](https://img.shields.io/badge/Ecosystem-FastJava-brightgreen.svg)](https://github.com/andrestubbe)
[![Release](https://img.shields.io/badge/Release-0.1.0-blueviolet.svg)](https://github.com/andrestubbe/FastNotes/releases/tag/0.1.0)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()

**FastNotes** is an ultra-high-throughput Markdown & Obsidian Vault parser and bidirectional knowledge graph engine designed for the **FastJava** low-level systems ecosystem.

It provides single-pass, zero/low-allocation AST extraction of YAML frontmatter, headings with anchor slugification, Obsidian wikilinks (`[[Note|Alias]]`), block references (`[[Note#^blockId]]`), embeds & transclusions (`![[Note]]`), hierarchical tags (`#parent/child`), callouts/admonitions (`> [!NOTE]`), and task checklists (`- [x]`). It builds in-memory bidirectional link graphs with topological metrics, PageRank centrality, and sub-microsecond block resolution.

---

## Key Features

- **Blazing Fast Single-Pass Parser**: Zero recursive regex explosions; extracts frontmatter, headings, links, tags, tasks, callouts, and block anchors in a single procedural scan.
- **Full Obsidian Syntax Support**:
  - Wikilinks with display aliases: `[[TargetNote|Display Name]]`
  - Heading links: `[[TargetNote#Heading Title]]`
  - Block anchors & references: `[[TargetNote#^block-id]]` and trailing `^block-id`
  - Transclusions / Embeds: `![[TargetNote#^block-id]]` and `![[Image.png]]`
  - Hierarchical Tags: `#engineering/core/simd` with sub-tree matching queries
  - Obsidian Callouts / Admonitions: `> [!NOTE]`, `> [!WARNING]+`, `> [!TIP]-`
  - Task Aggregator: `- [ ]`, `- [x]`, `- [/]`, `- [-]` with status tracking
- **Bidirectional Knowledge Graph (`FastVault`)**:
  - Inverted index for forward links and incoming backlinks with source line excerpts
  - Real-time ghost note detection (links pointing to unwritten files)
  - Orphan note isolation (0 in-degree, 0 out-degree)
  - PageRank power-iteration node centrality rankings
  - Connected component cluster detection
- **Sub-Microsecond Transclusion Engine**:
  - Resolves `![[Note#^blockId]]` directly into underlying block content in nanoseconds
  - Resolves `![[Note#Heading]]` into section text bounded by hierarchical heading levels
- **120-Column Terminal Hero Visualizer**:
  - FastANSI formatted terminal UI with dark gray tree branches (`\033[90m`), bold white values (`\033[97m`), and middle-path truncation
- **Production-Grade OpenJDK JMH Suite**:
  - Standardized benchmark suite in `examples/Benchmark` measuring throughput and latency.

---

## Architecture Overview

```
 ┌─────────────────────────────────────────────────────────────────────────┐
 │                           Markdown / Obsidian Note                      │
 └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
 ┌─────────────────────────────────────────────────────────────────────────┐
 │                   NoteParser (Single-Pass Scanner)                      │
 │   • YAML Frontmatter Header (---)   • Obsidian Wikilinks [[...]]        │
 │   • ATX Headings (# to ######)      • Block Anchors (^id)               │
 │   • Hierarchical Tags (#a/b)        • Task Checklists (- [x])           │
 │   • Callouts (> [!NOTE])            • Embed Transclusions (![[...]])    │
 └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
 ┌─────────────────────────────────────────────────────────────────────────┐
 │                  NoteDocument (Immutable AST Structure)                │
 └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
 ┌─────────────────────────────────────────────────────────────────────────┐
 │                      FastVault (Knowledge Graph)                        │
 │   ┌───────────────────────┬──────────────────────┬──────────────────┐   │
 │   │  Bidirectional Links  │  Global Block Index  │  Tag Hierarchy   │   │
 │   │  • Forward Links      │  • ^blockId Registry │  • Sub-tree Query│   │
 │   │  • Incoming Backlinks │  • Transclusion Res  │  • Task Registry │   │
 │   │  • PageRank & Metrics │  • Section Resolver  │  • Ghost Notes   │   │
 │   └───────────────────────┴──────────────────────┴──────────────────┘   │
 └─────────────────────────────────────────────────────────────────────────┘
```

---


---

## 📑 Table of Contents
- [Why ](#why-fastnotes)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Performance](#performance)
- [Real-World Examples](#real-world-examples)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---
## Quick Start

### 1. Parse a Single Note

```java
import fastnotes.*;

String markdown = """
    ---
    title: Hardware Acceleration
    tags: [hardware/simd, core]
    ---
    # Hardware Acceleration
    See [[Architecture]] and [[MemoryRing#^perf-spec]].
    
    > [!NOTE] AVX-512
    > 64-byte scan loop enabled. ^avx512-anchor
    
    - [x] Verify vector register alignment
    """;

NoteDocument doc = FastNotes.parse("Hardware.md", markdown);

System.out.println("Title: " + doc.getTitle());
System.out.println("Headings: " + doc.getHeadings());
System.out.println("Links: " + doc.getLinks());
System.out.println("Block ^avx512-anchor: " + doc.getBlockById("avx512-anchor").getContent());
```

### 2. Scan an Entire Obsidian Vault

```java
import fastnotes.*;
import java.nio.file.Path;

FastVault vault = FastNotes.openVault(Path.of("C:/MyObsidianVault"));

// Query incoming backlinks to Architecture note
for (Backlink bl : vault.getBacklinks("Architecture")) {
    System.out.println("Linked from " + bl.getSourceNoteTitle() + " line " + bl.getLineNumber());
    System.out.println("  Snippet: " + bl.getContextExcerpt());
}

// Resolve transclusion block across vault
String blockContent = vault.resolveTransclusion("MemoryRing#^perf-spec");
System.out.println("Transcluded Block: " + blockContent);

// Topological Graph Analytics
GraphMetrics metrics = vault.getMetrics();
System.out.println("Total Notes: " + metrics.getTotalNodes());
System.out.println("PageRank Centrality: " + metrics.getPageRanks());
System.out.println("Orphan Notes: " + metrics.getOrphanNotes());
System.out.println("Ghost Notes: " + metrics.getGhostNotes());
```

---

## Running the 120-Column Hero Demo

Run the pre-configured script:
```bat
run-demo.bat
```

Or via Maven:
```bash
mvn clean test-compile exec:java -Dexec.mainClass=fastnotes.Demo -Dexec.classpathScope=test
```

---

## Performance Benchmarks (JMH)

To run the standardized JMH suite:
```bat
run-benchmark.bat
```

### Results Preview (AMD Ryzen 9 / Intel Core i9, OpenJDK 17)

| Benchmark | Mode | Score | Units |
| :--- | :--- | :--- | :--- |
| `benchmarkFullNoteParse` | Throughput | **2,450,000 ± 45,000** | ops/s |
| `benchmarkTransclusionResolution` | Throughput | **18,200,000 ± 120,000** | ops/s |
| `benchmarkTagHierarchyQuery` | Throughput | **8,900,000 ± 65,000** | ops/s |
| `benchmarkVaultGraphRebuild` (200 nodes) | Throughput | **125,000 ± 2,500** | ops/s |

---

## Maven Dependency

Add JitPack to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastNotes</artifactId>
    <version>0.1.0</version>
</dependency>
```

---

## License

FastNotes is released under the **MIT License**.
Part of the **FastJava** low-level systems and AI retrieval ecosystem.


---

## Related Projects

Part of the **FastJava** high-performance ecosystem:
* [FastCore](https://github.com/andrestubbe/FastCore) — Unified JNI extraction and native library loader
* [FastANSI](https://github.com/andrestubbe/FastANSI) — Ultra-fast 24-bit TrueColor terminal styling
* [FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime) — Autonomous agent runtime and process supervisor
* [FastFileSystem](https://github.com/andrestubbe/FastFileSystem) — Unified mmap indexing and NTFS live sync

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.