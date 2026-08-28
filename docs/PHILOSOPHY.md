# FastNotes Design Philosophy

FastNotes is built on the core foundational tenets of the **FastJava** architecture:

## 1. Single-Pass Procedural Parsing Over Regex Juggernauts
Traditional Markdown and Markdown AST libraries (like CommonMark or Flexmark) construct deep recursive object trees and employ complex regular expression engines with backtracking risks. 
FastNotes replaces this with a deterministic, single-pass procedural scanning engine. It tokenizes headings, frontmatter, wikilinks, tags, embeds, callouts, and block anchors in a single linear pass $O(N)$ with minimal allocations and zero recursive stack frames.

## 2. Bidirectional Knowledge Graphs as First-Class Citizens
Personal knowledge management (PKM) and AI Retrieval-Augmented Generation (RAG) require hyper-connected graph traversal. FastNotes does not treat links as plain text strings—it builds inverted index adjacency matrices in memory, tracking forward links, incoming backlinks, ghost links, and topological PageRank centrality at microsecond speed.

## 3. Sub-Microsecond Block-Level Resolution
Transclusion in Obsidian allows notes to embed specific paragraphs (`![[Note#^block-id]]`) or sections (`![[Note#Heading]]`). FastNotes indexes blocks at parse time into a global registry, making transclusion resolution a direct hash lookup with zero runtime file re-parsing.

## 4. Native Mechanical Sympathy
- Zero GC overhead on hot paths.
- Linear cache locality.
- Predictable branch profiles for CPU instruction pipelining.
- Immediate 120-column terminal feedback for high-density systems debugging.
