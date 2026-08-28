# FastNotes Changelog

## [0.1.0] - 2026-08-28

### Added
- **Initial Release of FastNotes** for the FastJava ecosystem.
- Single-pass procedural scanner for Markdown and Obsidian vault syntax.
- YAML frontmatter extraction (titles, tags, aliases, custom key-values).
- Heading hierarchy (H1-H6) with automated anchor slugification.
- Obsidian Wikilinks (`[[Note|Alias]]`, `[[Note#Heading]]`, `[[Note#^blockId]]`).
- Obsidian Transclusions / Embeds (`![[Note]]`, `![[Note#^blockId]]`, `![[image.png]]`).
- Block-level anchor indexing (`^blockId`) and sub-microsecond resolution.
- Hierarchical nested tags (`#engineering/ai/simd`) with wildcard sub-tree matching.
- Obsidian Callout / Admonition parser (`> [!NOTE]`, `> [!WARNING]+`, etc.).
- Task checklist aggregator (`- [ ]`, `- [x]`, `- [/]`, `- [-]`).
- Bidirectional knowledge graph engine (`FastVault`) with forward links and incoming backlinks.
- Topological graph analytics: PageRank power iteration, connected components, graph density, orphan note detection, ghost link tracking.
- 120-Column Terminal Hero Demo (`Demo.java`, `run-demo.bat`).
- Standardized OpenJDK JMH Benchmark suite in `examples/Benchmark` (`run-benchmark.bat`).
