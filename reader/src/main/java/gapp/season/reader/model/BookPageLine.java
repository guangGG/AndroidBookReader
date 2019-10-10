package gapp.season.reader.model;

public class BookPageLine extends BookModel {
    private int mLineId; //在第n个BookLine中 [ParagraphId]
    private int mLineIndex; //在BookLine中的第n个展示行(0-...) [ParagraphLine]
    private long mWordIndex; //行首在Book中第n位(0-...)
    private int mWordLineIndex; //行首在BookLine中第n位(0-...)
    private int mWordLineLastIndex; //行末字符在BookLine中第n位(防止缩进对字符定位产生影响)
    private String mText; //行内文本内容

    public int getLineId() {
        return mLineId;
    }

    public void setLineId(int lineId) {
        mLineId = lineId;
    }

    public int getLineIndex() {
        return mLineIndex;
    }

    public void setLineIndex(int lineIndex) {
        mLineIndex = lineIndex;
    }

    public long getWordIndex() {
        return mWordIndex;
    }

    public void setWordIndex(long wordIndex) {
        mWordIndex = wordIndex;
    }

    public int getWordLineIndex() {
        return mWordLineIndex;
    }

    public void setWordLineIndex(int wordLineIndex) {
        mWordLineIndex = wordLineIndex;
    }

    public int getWordLineLastIndex() {
        return mWordLineLastIndex;
    }

    public void setWordLineLastIndex(int wordLineLastIndex) {
        mWordLineLastIndex = wordLineLastIndex;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}
