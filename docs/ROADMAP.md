# FastNotes Strategic Roadmap

## Milestone 0.2.0 - Native Vector SIMD Delimiter Scanning
- [ ] JNI integration with FastSIMD / AVX-512 vector scan kernel for raw byte streams.
- [ ] Direct off-heap zero-copy memory-mapped file parsing (`FastMemory`).

## Milestone 0.3.0 - Obsidian Canvas & Visual Graph
- [ ] Obsidian Canvas (`.canvas`) JSON parser and visual node-edge extraction.
- [ ] Export bidirectional graph to standard GraphViz DOT, Cytoscape JSON, and GEXF formats.

## Milestone 0.4.0 - Live Vault File Watcher & Incremental Sync
- [ ] Native Windows `ReadDirectoryChangesW` integration (`FastFileWatch`) for microsecond incremental AST diffing.
- [ ] Real-time local HTTP / WebSocket knowledge graph streaming server.
