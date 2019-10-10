package gapp.season.reader.model;

public class BookSchedule extends BookModel {
    private int mParagraph; //段落id
    private int mParagraphLine; //段落内的行id

    public BookSchedule() {
    }

    public BookSchedule(int paragraph, int paragraphLine) {
        mParagraph = paragraph;
        mParagraphLine = paragraphLine;
    }

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
