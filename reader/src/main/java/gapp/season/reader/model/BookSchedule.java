package gapp.season.reader.model;

public class BookSchedule extends BookModel {
    private int mParagraph;
    private int mParagraphLine;

    public int getParagraph() {
        return mParagraph;
    }

    public void setParagraph(int paragraph) {
        mParagraph = paragraph;
    }

    public int getParagraphLine() {
        return mParagraphLine;
    }

    public void setParagraphLine(int paragraphLine) {
        mParagraphLine = paragraphLine;
    }
}
