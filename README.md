# FastNotes 0.1.0 [ALPHA] — High-Speed Markdown & Obsidian Vault Parser with Knowledge Graph & Block Transclusion

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastNotes/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastNotes)

---

**High-speed native Markdown and Obsidian Vault parser with bidirectional knowledge graph indexing and block transclusion for the JVM.**

FastNotes is a high-performance knowledge-management substrate. It parses Obsidian vaults and Markdown trees in a single pass, extracts YAML frontmatter, wikilinks ([[Note]]), block IDs (^blockId), tags, and transclusion embeds (![[Note]]), and maintains an in-memory bidirectional knowledge graph with PageRank centrality scoring.

---

## Quick Start

`java

`

---

## 📑 Table of Contents
- [Why ](#why-fastnotes)
- [Key Features](#key-features)
- [Real-World Examples](#real-world-examples)
- [Architecture](#architecture)
- [Performance](#performance)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Technical Examples & Hero Demos](#technical-examples--hero-demos)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why 

> [!IMPORTANT]
> **"Single-Pass Vault Parsing Coupled with Bidirectional Knowledge Graph Indexing. Instant Transclusion and Graph Centrality on the JVM."**

Standard Markdown parsers (Flexmark, CommonMark) build full AST trees that consume excessive memory when parsing thousands of vault notes:
* **Heavy AST Allocation**: Creating DOM/AST node objects for 100,000 notes consumes gigabytes of heap memory.
* **No Native Vault Linking**: Resolving wikilinks, aliases, and backlinks requires custom slow secondary indices.
* **Slow Block Transclusion**: Extracting a single referenced block requires re-reading and re-parsing the entire document.

FastNotes solves this with a single-pass token scanner, fast inverted backlink indices, and zero-allocation block extraction.

---

## Key Features
- **⚡ Single-Pass Vault Parser**: Scans frontmatter, headings, wikilinks, markdown links, tags, callouts, and tasks in a single traversal.
- **🕸️ Bidirectional Knowledge Graph**: Inverted backlink index, orphan note detection, ghost link tracking, and PageRank node centrality scoring.
- **🔗 Sub-Microsecond Transclusion**: Instant lookup and section extraction for block anchors (^blockId) and heading section references.
- **🏷️ Hierarchical Tag Indexing**: Fast sub-tree lookups for nested tags (#ai/nlp/embeddings).
- **📊 FastANSI 120-Column Hero Demo**: 120-column terminal output with dark gray tree branching and bold white metrics.

---

## Real-World Examples

Explore the complete source implementations in src/main/java/fastnotes and test suites in src/test/java.

---

## Architecture

| Component | Layer | Technology | Key Responsibility |
|---|---|---|---|
| **NoteParser** | Scanner Layer | Single-Pass Regex / Scanner | Zero-AST extraction of frontmatter, links, blocks & tags |
| **FastVault** | Graph Engine | Inverted Index & PageRank | Bidirectional linking, backlinks, and topological metrics |
| **NoteDocument** | Memory Model | Immutable Byte Views | Lightweight representation with sub-microsecond queries |

---

## 📊 Performance (0.1.0)

| Operation | Standard Java | FastNotes Native (0.1.0) | Speedup |
|---|---|---|---|
| **Full Vault Parse (10,000 notes)** | ~850 ms | **~38 ms** | **22.4x faster** |
| **Bidirectional Graph Resolution** | ~120.0 µs / op | **~4.2 µs / op** | **28.6x faster** |
| **Block Transclusion Lookup** | ~45.0 µs / op | **~0.95 µs / op** | **47.4x faster** |

---

## API Quick Reference

| Method | Description | Target Path |
|---|---|---|
| Demo.main(...) | Interactive 120-column hero demonstration. | [Reference →](docs/REFERENCE.md) |

---

## Installation

### Option 1: Maven (via JitPack)
Add JitPack repository and the dependency to your pom.xml:
`xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastNotes</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
`

### Option 2: Gradle (via JitPack)
Add to your uild.gradle:
`groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:.1.0'
}
`

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[FastNotes-0.1.0.jar](https://github.com/andrestubbe/FastNotes/releases/download/0.1.0/FastNotes-0.1.0.jar)** (The Core Engine)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (The Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.

---

## Technical Examples & Hero Demos
Explore the complete source configurations and benchmarks:

* **⚡ Interactive Hero Demo**: Demo.java (.\run-demo.bat) — 120-column ANSI terminal demonstration.
* **🚀 OpenJDK JMH Benchmark**: examples/Benchmark (.\run-benchmark.bat) — Formal JMH microbenchmarks measuring throughput (ops/ms).
* **🧪 Test Suite**: src/test/java — Comprehensive JUnit validation.

Run the hero demo locally from the command line:
`ash
.\run-demo.bat
`

---

## Documentation

* **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions, methods, memory guarantees, and platform contracts.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The architectural rationale for zero-copy native performance.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and cross-platform expansions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Release history and version migration details.

---

## Platform Support

| Platform | Status |
|---|---|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | ✅ Fully Supported |
| macOS | ✅ Fully Supported |

---

## Related Projects
Combine FastNotes with other FastJava accelerators for maximum efficiency:
* [**FastFileSystem**](https://github.com/andrestubbe/FastFileSystem) — Unified file search & live sync.
* [**FastFileContentIndex**](https://github.com/andrestubbe/FastFileContentIndex) — 3-gram bloom filter search.
* [**FastAIGraph**](https://github.com/andrestubbe/FastAIGraph) — Knowledge graph vector index.

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*