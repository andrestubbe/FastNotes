package fastnotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a checklist or task item extracted from Markdown (e.g. - [x] or - [ ]).
 */
public final class NoteTask {
    private final char statusChar;
    private final boolean completed;
    private final String description;
    private final List<String> tags;
    private final int lineNumber;
    private final int charOffset;

    public NoteTask(char statusChar, String description, List<String> tags, int lineNumber, int charOffset) {
        this.statusChar = statusChar;
        this.completed = (statusChar == 'x' || statusChar == 'X');
        this.description = description != null ? description.trim() : "";
        this.tags = tags != null ? Collections.unmodifiableList(new ArrayList<>(tags)) : Collections.emptyList();
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    public char getStatusChar() {
        return statusChar;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isInProgress() {
        return statusChar == '/';
    }

    public boolean isCancelled() {
        return statusChar == '-';
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getCharOffset() {
        return charOffset;
    }

    @Override
    public String toString() {
        return "- [" + statusChar + "] " + description + " (L:" + lineNumber + ")";
    }
}
