package it.maior.docx;

public enum ParagraphStyle {

    TITLE ("Title"),
    SUBTITLE ("Subtitle"),
    HEADING1 ("Heading1"),
    HEADING2 ("Heading2"),
    HEADING3 ("Heading3"),
    HEADING4("Heading4"),
    HEADING6("Heading6"),
    NORMAL("Normal");



    private final String Style;

    private ParagraphStyle(final String style) {
        this.Style = style;
    }

    public String getStyle() { return Style; }

    public static String getHeadingPrefix(){
        return "Heading";
    }
}
