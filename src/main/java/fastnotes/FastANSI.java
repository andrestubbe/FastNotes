package fastnotes;

/**
 * FastANSI styling utility for 120-column terminal output and graph visualization.
 */
public final class FastANSI {
    private FastANSI() {}

    public static final String RESET             = "\033[0m";
    public static final String BOLD              = "\033[1m";
    public static final String DIM               = "\033[2m";
    public static final String ITALIC            = "\033[3m";
    public static final String UNDERLINE         = "\033[4m";

    // Standard 4-bit Foreground
    public static final String FG_BLACK          = "\033[30m";
    public static final String FG_RED            = "\033[31m";
    public static final String FG_GREEN          = "\033[32m";
    public static final String FG_YELLOW         = "\033[33m";
    public static final String FG_BLUE           = "\033[34m";
    public static final String FG_MAGENTA        = "\033[35m";
    public static final String FG_CYAN           = "\033[36m";
    public static final String FG_WHITE          = "\033[37m";

    // Bright 4-bit Foreground
    public static final String FG_BRIGHT_BLACK   = "\033[90m"; // Dark gray (branches)
    public static final String FG_BRIGHT_RED     = "\033[91m";
    public static final String FG_BRIGHT_GREEN   = "\033[92m";
    public static final String FG_BRIGHT_YELLOW  = "\033[93m";
    public static final String FG_BRIGHT_BLUE    = "\033[94m";
    public static final String FG_BRIGHT_MAGENTA = "\033[95m";
    public static final String FG_BRIGHT_CYAN    = "\033[96m";
    public static final String FG_BRIGHT_WHITE   = "\033[97m"; // Bold white values

    // 24-bit RGB
    public static String fg(int r, int g, int b) {
        return "\033[38;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String bg(int r, int g, int b) {
        return "\033[48;2;" + r + ";" + g + ";" + b + "m";
    }

    /**
     * Middle-path truncation utility for long file paths and node names in 120-column terminal view.
     */
    public static String truncateMiddle(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        if (maxLen <= 5) return text.substring(0, maxLen);
        int half = (maxLen - 3) / 2;
        return text.substring(0, half) + "..." + text.substring(text.length() - (maxLen - 3 - half));
    }

    /**
     * Pad or truncate a string to exact column width.
     */
    public static String padRight(String text, int width) {
        if (text == null) text = "";
        if (text.length() > width) return truncateMiddle(text, width);
        return text + " ".repeat(width - text.length());
    }
}
