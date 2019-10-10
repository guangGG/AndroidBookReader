package gapp.season.reader.model;

public class BookSearchItem extends BookModel {
    private String mWord; //关键词
    private String mText; //搜索结果展示文案
    private BookSchedule mSchedule; //搜索结果的定位位置(段落及行位置)
    private float mProgress; //搜索结果的展示进度

    public String getWord() {
        return mWord;
    }

    public void setWord(String word) {
        mWord = word;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public BookSchedule getSchedule() {
        return mSchedule;
    }

    public void setSchedule(BookSchedule schedule) {
        mSchedule = schedule;
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        mProgress = progress;
    }
}
