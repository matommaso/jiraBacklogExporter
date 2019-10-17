package it.maior.docx;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.*;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.math.BigInteger;
import java.util.List;

public class DocxFileCreator {

    private WordprocessingMLPackage wordPackage;
    private MainDocumentPart mainDocumentPart;

    public DocxFileCreator() {
        init();
    }

    private void init() {
        try {
            this.wordPackage = WordprocessingMLPackage.createPackage();
            this.mainDocumentPart = this.wordPackage.getMainDocumentPart();

            addNumberingDefinitions();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNumberingDefinitions() throws InvalidFormatException, JAXBException {
        final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
        ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(initialNumbering));

        this.wordPackage.getMainDocumentPart().addTargetPart(ndp);
    }

    public P createStyledParagraphOfText(String styleId, String text) {
        return mainDocumentPart.createStyledParagraphOfText(styleId, text);
    }

    public P createParagraphOfText(String text) {
        return mainDocumentPart.createParagraphOfText(text);
    }

    public void addParagraph(P paragraph) {
        addToMainDocumentPart(paragraph);
    }

    /**
     * Create a paragraph with a hard coded configuration: object r and rpr.
     *
     * @param s the paragraph text.
     */
    public void addParagraphWithConfiguration(String s) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        R r = factory.createR();
        Text t = factory.createText();
        t.setValue(s);
        r.getContent().add(t);
        p.getContent().add(r);
        RPr rpr = factory.createRPr();
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        rpr.setB(b);
        rpr.setI(b);
        rpr.setCaps(b);
        Color red = factory.createColor();
        red.setVal("green");
        rpr.setColor(red);
        r.setRPr(rpr);
        addToMainDocumentPart(p);
    }

    private boolean addToMainDocumentPart(Object element) {
        return mainDocumentPart.getContent().add(element);
    }

    /**
     * Create a table with the styled paragraphs.
     *
     * @param paragraphs the styled paragraphs as an array of [rows][cells in rows].
     */
    public void addTableWith(List<List<List<P>>> paragraphs) {
        final int writableWidthTwips = wordPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();

        addToMainDocumentPart(createTableFromStyledParagraphs(paragraphs, writableWidthTwips));
    }

    private Tbl createTableFromStyledParagraphs(List<List<List<P>>> paragraphs, int tableWidthTwips) {
        final int rowNumber = (int) paragraphs.stream().filter((List<List<P>> l) -> l.size() > 0 ).count();
        final int columnNumber = paragraphs.get(0).size();

        final Tbl tbl = TblFactory.createTable(rowNumber, columnNumber, calculateCellWidthTwips(tableWidthTwips, columnNumber));

        final java.util.List<Object> rows = getRows(tbl);

        for (int i = 0; i < rowNumber; i++) {
            final java.util.List<Object> cells = getCellsInRow(rows, i);
            for (int j = 0; j < columnNumber; j++) {
                final Tc cell = (Tc) cells.get(j);
                final java.util.List<Object> paragraphsInCell = getCleanCellContent(cell);
                paragraphs.get(i).get(j).forEach(p -> paragraphsInCell.add(p));
            }
        }
        return tbl;
    }

    private java.util.List<Object> getCleanCellContent(Tc cell) {
        final java.util.List<Object> paragraphsInCell = cell.getContent();
        if(paragraphsInCell.size() > 0) {
            paragraphsInCell.remove(0);
        }
        return paragraphsInCell;
    }

    private int calculateCellWidthTwips(int tableWidthTwips, int columnNumber) {
        return tableWidthTwips / columnNumber;
    }

    private java.util.List<Object> getRows(Tbl tbl) {
        return tbl.getContent();
    }

    private java.util.List<Object> getCellsInRow(java.util.List<Object> rows, int i) {
        return ((Tr) rows.get(i)).getContent();
    }

    public void addEmptyLine() {
        addToMainDocumentPart(createParagraphOfText(""));
    }

    public P createImage(byte[] fileContent, String filenameHint) throws Exception {
        try {
            final BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage, fileContent);
            final Inline inline = imagePart.createImageInline(filenameHint, "Alt Text", 1, 2, false);
            return createParagraphImage(inline);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public void writeTitle(String title) {
        addParagraph(createStyledParagraphOfText(ParagraphStyle.HEADING2.getStyle(), title));
    }

    private P createParagraphImage(Inline inline) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        R r = factory.createR();
        p.getContent().add(r);
        Drawing drawing = factory.createDrawing();
        r.getContent().add(drawing);
        drawing.getAnchorOrInline().add(inline);
        return p;
    }


    public void saveFileDocx(String outputPath) {
        try {
            File exportFile = new File(outputPath);
            wordPackage.save(exportFile);
        } catch (Docx4JException e) {
            e.printStackTrace();
        }
    }


    public P createBulletParagraphOfText(long numId, long ilvl, String paragraphText) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();

        Text t = factory.createText();
        t.setValue(paragraphText);

        R run = factory.createR();
        run.getContent().add(t);

        p.getContent().add(run);

        org.docx4j.wml.PPr ppr = factory.createPPr();
        p.setPPr(ppr);

        // Create and add <w:numPr>
        PPrBase.NumPr numPr = factory.createPPrBaseNumPr();
        ppr.setNumPr(numPr);

        // The <w:ilvl> element
        Ilvl ilvlElement = factory.createPPrBaseNumPrIlvl();
        numPr.setIlvl(ilvlElement);
        ilvlElement.setVal(BigInteger.valueOf(ilvl));

        // The <w:numId> element
        NumId numIdElement = factory.createPPrBaseNumPrNumId();
        numPr.setNumId(numIdElement);
        numIdElement.setVal(BigInteger.valueOf(numId));

        return p;
    }

    static final String initialNumbering = "<w:numbering xmlns:ve=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\">"
            + "<w:abstractNum w:abstractNumId=\"0\">"
            + "<w:nsid w:val=\"2DD860C0\"/>"
            + "<w:multiLevelType w:val=\"multilevel\"/>"
            + "<w:tmpl w:val=\"0409001D\"/>"
            + "<w:lvl w:ilvl=\"0\">"
            + "<w:start w:val=\"1\"/>"
            + "<w:numFmt w:val=\"bullet\"/>"
            + "<w:lvlText w:val=\"•\"/>"
            + "<w:lvlJc w:val=\"left\"/>"
            + "<w:pPr>"
            + "<w:ind w:left=\"360\" w:hanging=\"360\"/>"
            + "</w:pPr>"
            + "</w:lvl>"
            + "<w:lvl w:ilvl=\"1\">"
            + "<w:start w:val=\"1\"/>"
            + "<w:numFmt w:val=\"bullet\"/>"
            + "<w:lvlText w:val=\"○\"/>"
            + "<w:lvlJc w:val=\"left\"/>"
            + "<w:pPr>"
            + "<w:ind w:left=\"720\" w:hanging=\"360\"/>"
            + "</w:pPr>"
            + "</w:lvl>"
            + "<w:lvl w:ilvl=\"2\">"
            + "<w:start w:val=\"1\"/>"
            + "<w:numFmt w:val=\"bullet\"/>"
            + "<w:lvlText w:val=\"◘\"/>"
            + "<w:lvlJc w:val=\"left\"/>"
            + "<w:pPr>"
            + "<w:ind w:left=\"1080\" w:hanging=\"360\"/>"
            + "</w:pPr>"
            + "</w:lvl>"
            + "</w:abstractNum>"
            + "<w:num w:numId=\"1\">"
            + "<w:abstractNumId w:val=\"0\"/>"
            + "</w:num>"
            + "</w:numbering>";

}