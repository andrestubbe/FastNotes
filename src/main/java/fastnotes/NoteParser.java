package fastnotes;

import java.util.*;

/**
 * Ultra-high-throughput procedural parser for Markdown and Obsidian Vault documents.
 * Extracts frontmatter, headings, wikilinks, embeds, block anchors, tags, callouts, and tasks
 * in a single scanning pass.
 */
public final class NoteParser {

    /**
     * Parses a Markdown/Obsidian document from a string.
     */
    public static NoteDocument parse(String path, String rawText) {
        if (rawText == null) rawText = "";
        String normalizedPath = path != null ? path.replace('\\', '/') : "";
        String fileName = extractFileName(normalizedPath);

        List<NoteHeading> headings = new ArrayList<>();
        List<NoteLink> links = new ArrayList<>();
        List<NoteBlock> blocks = new ArrayList<>();
        List<NoteTag> tags = new ArrayList<>();
        List<NoteTask> tasks = new ArrayList<>();
        List<NoteCallout> callouts = new ArrayList<>();
        Map<String, NoteBlock> blockIndex = new LinkedHashMap<>();
        Map<String, NoteHeading> headingIndex = new LinkedHashMap<>();

        // 1. Parse Frontmatter
        FrontmatterParseResult fmResult = parseFrontmatter(rawText);
        NoteFrontmatter frontmatter = fmResult.frontmatter;

        // 2. Scan Lines
        String[] lines = rawText.split("\r?\n", -1);
        int currentLineIdx = fmResult.nextScanLine; // 0-based
        int charOffset = fmResult.charOffsetAtScanLine;

        boolean inCodeFence = false;
        String codeFenceMarker = null;
        boolean inMathFence = false;

        // Callout accumulator
        boolean inCallout = false;
        String currentCalloutType = null;
        String currentCalloutTitle = null;
        char currentCalloutFold = ' ';
        StringBuilder calloutBody = new StringBuilder();
        int calloutStartLine = 0;

        // Paragraph / Block accumulator
        StringBuilder currentBlockContent = new StringBuilder();
        int currentBlockStartLine = -1;
        int currentBlockStartOffset = -1;
        NoteBlock.BlockType currentBlockType = NoteBlock.BlockType.PARAGRAPH;

        int totalWords = 0;

        for (int lineNum = currentLineIdx; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            int line1Based = lineNum + 1;
            String trimmed = line.trim();

            // Check Code Fences
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                String fence = trimmed.substring(0, 3);
                if (!inCodeFence) {
                    // Start of code block - flush pending paragraph block
                    flushBlock(blocks, blockIndex, currentBlockContent, currentBlockType, currentBlockStartLine, line1Based - 1, currentBlockStartOffset, charOffset);
                    inCodeFence = true;
                    codeFenceMarker = fence;
                    currentBlockStartLine = line1Based;
                    currentBlockStartOffset = charOffset;
                    currentBlockType = NoteBlock.BlockType.CODE_BLOCK;
                    currentBlockContent.setLength(0);
                    currentBlockContent.append(line).append('\n');
                } else if (trimmed.startsWith(codeFenceMarker)) {
                    // End of code block
                    inCodeFence = false;
                    codeFenceMarker = null;
                    currentBlockContent.append(line);
                    flushBlock(blocks, blockIndex, currentBlockContent, NoteBlock.BlockType.CODE_BLOCK, currentBlockStartLine, line1Based, currentBlockStartOffset, charOffset + line.length());
                } else {
                    currentBlockContent.append(line).append('\n');
                }
                charOffset += line.length() + 1;
                continue;
            }

            if (inCodeFence) {
                currentBlockContent.append(line).append('\n');
                charOffset += line.length() + 1;
                continue;
            }

            // Math fence check ($$)
            if (trimmed.equals("$$")) {
                inMathFence = !inMathFence;
                charOffset += line.length() + 1;
                continue;
            }
            if (inMathFence) {
                charOffset += line.length() + 1;
                continue;
            }

            // Callout and Blockquote handling
            if (trimmed.startsWith(">")) {
                String bqContent = trimmed.substring(1).trim();
                if (bqContent.startsWith("[!") && bqContent.contains("]")) {
                    // Flush prior callout if any
                    if (inCallout) {
                        callouts.add(new NoteCallout(currentCalloutType, currentCalloutTitle, calloutBody.toString().trim(), currentCalloutFold, calloutStartLine, line1Based - 1));
                        calloutBody.setLength(0);
                    }
                    inCallout = true;
                    calloutStartLine = line1Based;

                    int closeBracket = bqContent.indexOf(']');
                    String headerInside = bqContent.substring(2, closeBracket).trim();
                    char fold = ' ';
                    if (headerInside.endsWith("+") || headerInside.endsWith("-")) {
                        fold = headerInside.charAt(headerInside.length() - 1);
                        headerInside = headerInside.substring(0, headerInside.length() - 1).trim();
                    }
                    currentCalloutType = headerInside.toUpperCase();
                    currentCalloutFold = fold;
                    currentCalloutTitle = bqContent.substring(closeBracket + 1).trim();
                    if (currentCalloutTitle.isEmpty()) {
                        currentCalloutTitle = currentCalloutType;
                    }
                } else if (inCallout) {
                    calloutBody.append(bqContent).append('\n');
                }
            } else {
                if (inCallout) {
                    callouts.add(new NoteCallout(currentCalloutType, currentCalloutTitle, calloutBody.toString().trim(), currentCalloutFold, calloutStartLine, line1Based - 1));
                    inCallout = false;
                    calloutBody.setLength(0);
                }
            }

            // Empty line: boundary for blocks
            if (trimmed.isEmpty()) {
                flushBlock(blocks, blockIndex, currentBlockContent, currentBlockType, currentBlockStartLine, line1Based - 1, currentBlockStartOffset, charOffset);
                charOffset += line.length() + 1;
                continue;
            }

            // Heading detection (# ... ######)
            if (trimmed.startsWith("#")) {
                int hLevel = 0;
                while (hLevel < trimmed.length() && trimmed.charAt(hLevel) == '#') {
                    hLevel++;
                }
                if (hLevel >= 1 && hLevel <= 6 && hLevel < trimmed.length() && trimmed.charAt(hLevel) == ' ') {
                    flushBlock(blocks, blockIndex, currentBlockContent, currentBlockType, currentBlockStartLine, line1Based - 1, currentBlockStartOffset, charOffset);
                    String hText = trimmed.substring(hLevel + 1).trim();
                    String anchorId = NoteHeading.slugify(hText);
                    NoteHeading heading = new NoteHeading(hLevel, hText, anchorId, line1Based, charOffset);
                    headings.add(heading);
                    headingIndex.putIfAbsent(anchorId, heading);
                    blocks.add(new NoteBlock(NoteBlock.BlockType.HEADING, null, line, line1Based, line1Based, charOffset, charOffset + line.length()));
                    charOffset += line.length() + 1;
                    continue;
                }
            }

            // Task detection (- [ ] or - [x] or * [ ])
            if (isTaskLine(trimmed)) {
                char status = trimmed.charAt(trimmed.indexOf('[') + 1);
                int afterBracket = trimmed.indexOf(']') + 1;
                String taskDesc = trimmed.substring(afterBracket).trim();
                List<String> taskTags = extractTagsFromLine(taskDesc);
                tasks.add(new NoteTask(status, taskDesc, taskTags, line1Based, charOffset));
            }

            // Accumulate Block Content
            if (currentBlockContent.length() == 0) {
                currentBlockStartLine = line1Based;
                currentBlockStartOffset = charOffset;
                currentBlockType = trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.matches("^\\d+\\..*") 
                        ? NoteBlock.BlockType.LIST_ITEM 
                        : (trimmed.startsWith("|") ? NoteBlock.BlockType.TABLE : NoteBlock.BlockType.PARAGRAPH);
            }
            currentBlockContent.append(line).append('\n');

            // Parse Links and Embeds in line
            parseLinksFromLine(line, line1Based, charOffset, links);

            // Parse Tags in line
            parseTagsFromLine(line, line1Based, charOffset, tags);

            // Word counting
            totalWords += countWords(line);

            charOffset += line.length() + 1;
        }

        // Flush any trailing callout or block
        if (inCallout) {
            callouts.add(new NoteCallout(currentCalloutType, currentCalloutTitle, calloutBody.toString().trim(), currentCalloutFold, calloutStartLine, lines.length));
        }
        flushBlock(blocks, blockIndex, currentBlockContent, currentBlockType, currentBlockStartLine, lines.length, currentBlockStartOffset, charOffset);

        String title = frontmatter.getTitle();
        if (title.isEmpty() && !headings.isEmpty()) {
            title = headings.get(0).getText();
        }
        if (title.isEmpty()) {
            title = fileName.endsWith(".md") ? fileName.substring(0, fileName.length() - 3) : fileName;
        }

        return new NoteDocument(
                normalizedPath, fileName, title, rawText,
                frontmatter, headings, links, blocks,
                tags, tasks, callouts, blockIndex, headingIndex,
                totalWords, rawText.length()
        );
    }

    private static void flushBlock(List<NoteBlock> blocks, Map<String, NoteBlock> blockIndex,
                                   StringBuilder content, NoteBlock.BlockType type,
                                   int startLine, int endLine, int startOffset, int endOffset) {
        if (content.length() == 0 || startLine < 0) return;
        String text = content.toString().trim();
        if (!text.isEmpty()) {
            String blockId = extractBlockAnchor(text);
            NoteBlock block = new NoteBlock(type, blockId, text, startLine, endLine, startOffset, endOffset);
            blocks.add(block);
            if (blockId != null) {
                blockIndex.put(blockId, block);
            }
        }
        content.setLength(0);
    }

    /**
     * Extracts block anchor ^id from end of paragraph or line (e.g. "Some text ^my-block-id").
     */
    public static String extractBlockAnchor(String text) {
        if (text == null || text.length() < 3) return null;
        int caret = text.lastIndexOf('^');
        if (caret <= 0) return null;
        if (text.charAt(caret - 1) != ' ' && text.charAt(caret - 1) != '\t' && text.charAt(caret - 1) != '\n') {
            return null;
        }
        String candidate = text.substring(caret + 1).trim();
        if (candidate.isEmpty()) return null;
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '_') {
                return null;
            }
        }
        return candidate;
    }

    private static boolean isTaskLine(String trimmed) {
        if ((trimmed.startsWith("- [") || trimmed.startsWith("* [") || trimmed.startsWith("+ [")) && trimmed.length() >= 5) {
            return trimmed.charAt(4) == ']' || (trimmed.length() >= 6 && trimmed.charAt(4) == ' ' && trimmed.charAt(5) == ']');
        }
        return false;
    }

    /**
     * Parses Obsidian [[Wikilinks]], ![[Embeds]], and Markdown [links](target) from a line.
     */
    public static void parseLinksFromLine(String line, int lineNum, int lineStartOffset, List<NoteLink> links) {
        if (line == null || line.isEmpty()) return;

        int len = line.length();
        int i = 0;

        while (i < len) {
            // 1. Wikilinks or Embeds: [[Target]] or ![[Target]]
            boolean isEmbed = false;
            if (line.charAt(i) == '!' && i + 2 < len && line.charAt(i + 1) == '[' && line.charAt(i + 2) == '[') {
                isEmbed = true;
                i++; // advance past '!'
            }

            if (i + 1 < len && line.charAt(i) == '[' && line.charAt(i + 1) == '[') {
                int startPos = i;
                int closePos = line.indexOf("]]", i + 2);
                if (closePos != -1) {
                    String inside = line.substring(i + 2, closePos).trim();
                    String raw = (isEmbed ? "!" : "") + "[[" + inside + "]]";
                    NoteLink link = parseWikilinkContent(inside, raw, isEmbed, lineNum, lineStartOffset + (isEmbed ? startPos - 1 : startPos));
                    links.add(link);
                    i = closePos + 2;
                    continue;
                }
            }

            // 2. Standard Markdown Links or Images: [text](target) or ![alt](target)
            boolean isMdEmbed = false;
            if (line.charAt(i) == '!' && i + 1 < len && line.charAt(i + 1) == '[') {
                isMdEmbed = true;
                i++;
            }

            if (line.charAt(i) == '[') {
                int bracketEnd = line.indexOf(']', i + 1);
                if (bracketEnd != -1 && bracketEnd + 1 < len && line.charAt(bracketEnd + 1) == '(') {
                    int parenEnd = line.indexOf(')', bracketEnd + 2);
                    if (parenEnd != -1) {
                        String aliasText = line.substring(i + 1, bracketEnd).trim();
                        String target = line.substring(bracketEnd + 2, parenEnd).trim();
                        String raw = (isMdEmbed ? "!" : "") + "[" + aliasText + "](" + target + ")";
                        NoteLink.LinkType type = isMdEmbed ? NoteLink.LinkType.EMBED : NoteLink.LinkType.MARKDOWN;
                        links.add(new NoteLink(type, raw, target, null, aliasText, isMdEmbed, false, false, lineNum, lineStartOffset + (isMdEmbed ? i - 1 : i)));
                        i = parenEnd + 1;
                        continue;
                    }
                }
            }

            i++;
        }
    }

    private static NoteLink parseWikilinkContent(String inside, String raw, boolean isEmbed, int lineNum, int offset) {
        String targetPart = inside;
        String alias = null;

        int pipeIdx = inside.indexOf('|');
        if (pipeIdx != -1) {
            targetPart = inside.substring(0, pipeIdx).trim();
            alias = inside.substring(pipeIdx + 1).trim();
        }

        String targetDoc = targetPart;
        String subRef = null;
        boolean isBlockRef = false;
        boolean isHeadingRef = false;

        int hashIdx = targetPart.indexOf('#');
        if (hashIdx != -1) {
            targetDoc = targetPart.substring(0, hashIdx).trim();
            subRef = targetPart.substring(hashIdx + 1).trim();
            if (subRef.startsWith("^")) {
                isBlockRef = true;
                subRef = subRef.substring(1);
            } else {
                isHeadingRef = true;
            }
        }

        NoteLink.LinkType type;
        if (isEmbed) {
            type = NoteLink.LinkType.EMBED;
        } else if (isBlockRef) {
            type = NoteLink.LinkType.BLOCK_LINK;
        } else if (isHeadingRef) {
            type = NoteLink.LinkType.HEADING_LINK;
        } else {
            type = NoteLink.LinkType.WIKILINK;
        }

        return new NoteLink(type, raw, targetDoc, subRef, alias, isEmbed, isBlockRef, isHeadingRef, lineNum, offset);
    }

    /**
     * Parses Obsidian tags (#tag or #parent/child) from a text line.
     */
    public static void parseTagsFromLine(String line, int lineNum, int lineStartOffset, List<NoteTag> tags) {
        if (line == null || line.indexOf('#') == -1) return;

        int len = line.length();
        int i = 0;
        while (i < len) {
            char c = line.charAt(i);
            if (c == '#') {
                // Must be at start of line or preceded by whitespace/punctuation
                boolean validPrefix = (i == 0) || Character.isWhitespace(line.charAt(i - 1)) || "([{\"'".indexOf(line.charAt(i - 1)) != -1;
                if (validPrefix && i + 1 < len && isTagChar(line.charAt(i + 1), true)) {
                    int start = i;
                    i++;
                    while (i < len && isTagChar(line.charAt(i), false)) {
                        i++;
                    }
                    String rawTag = line.substring(start, i);
                    if (!rawTag.equals("#")) {
                        tags.add(new NoteTag(rawTag, lineNum, lineStartOffset + start));
                    }
                    continue;
                }
            }
            i++;
        }
    }

    private static List<String> extractTagsFromLine(String text) {
        List<NoteTag> tmp = new ArrayList<>();
        parseTagsFromLine(text, 1, 0, tmp);
        List<String> result = new ArrayList<>(tmp.size());
        for (NoteTag t : tmp) {
            result.add(t.getNormalizedTag());
        }
        return result;
    }

    private static boolean isTagChar(char c, boolean firstChar) {
        if (firstChar) {
            return Character.isLetter(c) || c == '_';
        }
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '/';
    }

    private static int countWords(String line) {
        if (line == null || line.isEmpty()) return 0;
        int words = 0;
        boolean inWord = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!Character.isWhitespace(c)) {
                if (!inWord) {
                    words++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }
        }
        return words;
    }

    private static String extractFileName(String path) {
        if (path == null || path.isEmpty()) return "untitled.md";
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
    }

    private static class FrontmatterParseResult {
        final NoteFrontmatter frontmatter;
        final int nextScanLine;
        final int charOffsetAtScanLine;

        FrontmatterParseResult(NoteFrontmatter frontmatter, int nextScanLine, int charOffsetAtScanLine) {
            this.frontmatter = frontmatter;
            this.nextScanLine = nextScanLine;
            this.charOffsetAtScanLine = charOffsetAtScanLine;
        }
    }

    private static FrontmatterParseResult parseFrontmatter(String rawText) {
        if (!rawText.startsWith("---")) {
            return new FrontmatterParseResult(NoteFrontmatter.empty(), 0, 0);
        }

        String[] lines = rawText.split("\r?\n", -1);
        if (lines.length < 2 || !lines[0].trim().equals("---")) {
            return new FrontmatterParseResult(NoteFrontmatter.empty(), 0, 0);
        }

        int closingLine = -1;
        int charOffset = lines[0].length() + 1;

        Map<String, Object> props = new LinkedHashMap<>();
        List<String> tags = new ArrayList<>();
        List<String> aliases = new ArrayList<>();
        String title = "";

        String currentListKey = null;
        List<String> currentList = null;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            charOffset += line.length() + 1;
            String trimmed = line.trim();

            if (trimmed.equals("---") || trimmed.equals("...")) {
                closingLine = i;
                break;
            }

            if (trimmed.startsWith("- ") && currentList != null) {
                currentList.add(trimmed.substring(2).trim().replace("\"", "").replace("'", ""));
                continue;
            }

            int colonIdx = line.indexOf(':');
            if (colonIdx != -1) {
                String key = line.substring(0, colonIdx).trim();
                String val = line.substring(colonIdx + 1).trim();

                if (val.isEmpty()) {
                    currentList = new ArrayList<>();
                    currentListKey = key.toLowerCase();
                    props.put(key, currentList);
                    if (currentListKey.equals("tags")) tags = currentList;
                    if (currentListKey.equals("aliases")) aliases = currentList;
                } else {
                    currentList = null;
                    currentListKey = null;

                    // Check for inline array: [a, b, c]
                    if (val.startsWith("[") && val.endsWith("]")) {
                        String inside = val.substring(1, val.length() - 1);
                        List<String> items = new ArrayList<>();
                        for (String part : inside.split(",")) {
                            String item = part.trim().replace("\"", "").replace("'", "");
                            if (!item.isEmpty()) items.add(item);
                        }
                        props.put(key, items);
                        if (key.equalsIgnoreCase("tags")) tags = items;
                        if (key.equalsIgnoreCase("aliases")) aliases = items;
                    } else {
                        String cleanVal = val.replace("\"", "").replace("'", "").trim();
                        props.put(key, cleanVal);
                        if (key.equalsIgnoreCase("title")) title = cleanVal;
                        if (key.equalsIgnoreCase("tags")) {
                            tags = Collections.singletonList(cleanVal);
                        }
                    }
                }
            }
        }

        if (closingLine != -1) {
            NoteFrontmatter fm = new NoteFrontmatter(props, tags, aliases, title, 1, closingLine + 1);
            return new FrontmatterParseResult(fm, closingLine + 1, charOffset);
        }

        return new FrontmatterParseResult(NoteFrontmatter.empty(), 0, 0);
    }
}
