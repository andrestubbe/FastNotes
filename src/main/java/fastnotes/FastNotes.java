package fastnotes;

import java.io.IOException;
import java.nio.file.Path;

/**
 * FastNotes: Ultra-high-throughput Markdown & Obsidian Vault parser and graph indexing engine.
 */
public final class FastNotes {
    private FastNotes() {}

    /**
     * Parses a single markdown string into an AST NoteDocument.
     */
    public static NoteDocument parse(String markdown) {
        return NoteParser.parse("note.md", markdown);
    }

    /**
     * Parses a single markdown document with an explicit file path.
     */
    public static NoteDocument parse(String path, String markdown) {
        return NoteParser.parse(path, markdown);
    }

    /**
     * Opens and scans an Obsidian vault from a directory path.
     */
    public static FastVault openVault(Path directory) throws IOException {
        return FastVault.scan(directory);
    }

    /**
     * Opens and scans an Obsidian vault from a directory string.
     */
    public static FastVault openVault(String directory) throws IOException {
        return FastVault.scan(directory);
    }

    /**
     * Creates an in-memory vault for dynamic note graph assembly.
     */
    public static FastVault createMemoryVault() {
        return FastVault.inMemory();
    }
}
