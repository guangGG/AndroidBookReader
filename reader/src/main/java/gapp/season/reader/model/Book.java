package gapp.season.reader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Book extends BookModel {
    @Expose
    @SerializedName("ver")
    private int mVersion;
    @Expose
    @SerializedName("cs")
    private String mCharSet;
    @Expose
    @SerializedName("bid")
    private String mBookId; //fid+"-"+length
    @Expose
    @SerializedName("wnum")
    private long mWordNum; //总字数
    @Expose
    @SerializedName("lines")
    private List<BookLine> mLines; //所有文本行(段落)列表
    @Expose
    @SerializedName("chapters")
    private List<Integer> mChapters; //章节标题行id列表

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        mVersion = version;
    }

    public String getCharSet() {
        return mCharSet;
    }

    public void setCharSet(String charSet) {
        mCharSet = charSet;
    }

    public String getBookId() {
        return mBookId;
    }

    public void setBookId(String bookId) {
        mBookId = bookId;
    }

    public long getWordNum() {
        return mWordNum;
    }

    public void setWordNum(long wordNum) {
        mWordNum = wordNum;
    }

    public List<BookLine> getLines() {
        return mLines;
    }

    public void setLines(List<BookLine> lines) {
        mLines = lines;
    }

    public void addLine(BookLine bookLine) {
        if (mLines == null)
            mLines = new ArrayList<>();
        mLines.add(bookLine);
    }

    public List<Integer> getChapters() {
        return mChapters;
    }

    public void setChapters(List<Integer> chapters) {
        mChapters = chapters;
    }

    public void addChapter(int lineIndex) {
        if (mChapters == null)
            mChapters = new ArrayList<>();
        mChapters.add(lineIndex);
    }

    public static class BookLine extends BookModel {
        @Expose
        @SerializedName("lid")
        private int mIndex; //第n行(0-<LineNum)
        @Expose
        @SerializedName("wid")
        private long mWordIndex; //行首在总字数第n位(0-<WordNum)
        @Expose
        @SerializedName("text")
        private String mText; //行内文本内容

        public int getIndex() {
            return mIndex;
        }

        public void setIndex(int index) {
            mIndex = index;
        }

        public long getWordIndex() {
            return mWordIndex;
        }

        public void setWordIndex(long wordIndex) {
            mWordIndex = wordIndex;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            mText = text;
        }

        public void addWord(char c) {
            if (mText == null) {
                mText = String.valueOf(c);
            } else {
                mText = mText + c;
            }
        }
    }
}
