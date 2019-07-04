package gapp.season.reader.loader;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import gapp.season.reader.BookReader;
import gapp.season.reader.model.BookPageLine;
import gapp.season.reader.util.BrUtil;

//书签管理 问题:字体大小和行首缩进的改变会影响标签定位的准确性
public class BookMarkMgr {
    private static String mPath; //书籍文件路径
    private static List<BookMark> mBookMarks;

    public static String getBookMarkDir() {
        return BookReader.getBookDir() + "/marks";
    }

    public static void initBookMarks(String path) {
        if (!TextUtils.isEmpty(path)) {
            String pathId = BrUtil.getMd5Hash(path);
            if (!TextUtils.isEmpty(pathId)) {
                File file = new File(getBookMarkDir(), pathId);
                if (file.exists()) {
                    String json = BrUtil.getFileContent(file);
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<BookMark>>() {
                    }.getType();
                    List<BookMark> saveList = gson.fromJson(json, type);
                    if (saveList != null) {
                        mPath = path;
                        mBookMarks = saveList;
                        return;
                    }
                }
            }
        }
        mPath = path;
        mBookMarks = null;
    }

    public static List<BookMark> getBookMarks(String path) {
        if (!TextUtils.equals(mPath, path)) {
            initBookMarks(path);
        }
        return mBookMarks;
    }

    public static int markBook(String bookPath, float progress, List<BookPageLine> pageLines) {
        if (removeBookMarks(bookPath, pageLines)) {
            return 2;
        }
        if (pageLines != null && pageLines.size() > 0) {
            BookPageLine bpl = pageLines.get(0);
            if (markBook(bookPath, progress, bpl.getText(), bpl.getLineId(), bpl.getWordLineIndex())) {
                return 1;
            }
        }
        return -1;
    }

    private static boolean removeBookMarks(String bookPath, List<BookPageLine> pageLines) {
        boolean b = false;
        if (TextUtils.equals(mPath, bookPath)) {
            if (mBookMarks != null && pageLines != null) {
                for (int i = mBookMarks.size() - 1; i >= 0; i--) {
                    for (BookPageLine bpl : pageLines) {
                        if (isMarkInLine(bpl, mBookMarks.get(i))) {
                            mBookMarks.remove(i);
                            b = true;
                            break;
                        }
                    }
                }
            }
        }
        return b && saveBookMarks();
    }

    private static boolean markBook(String bookPath, float progress, String text, int paragraph, int wordLineIndex) {
        try {
            if (!TextUtils.isEmpty(bookPath)) {
                if (!TextUtils.equals(mPath, bookPath)) {
                    initBookMarks(bookPath);
                }
                if (mBookMarks == null) {
                    mBookMarks = new ArrayList<>();
                }
                BookMark bookMark = new BookMark();
                bookMark.setChapter(false);
                bookMark.setProgress(progress);
                bookMark.setText(text);
                bookMark.setParagraphId(paragraph);
                bookMark.setWordLineIndex(wordLineIndex);
                bookMark.setTime(System.currentTimeMillis());
                mBookMarks.add(0, bookMark);
                //缓存到文件
                return saveBookMarks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean saveBookMarks() {
        try {
            String pathId = BrUtil.getMd5Hash(mPath);
            if (!TextUtils.isEmpty(pathId)) {
                File file = new File(getBookMarkDir(), pathId);
                Gson gson = new Gson();
                BrUtil.saveStringToFile(file, gson.toJson(mBookMarks));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isMarkLine(BookPageLine line) {
        if (mBookMarks != null) {
            for (BookMark bookMark : mBookMarks) {
                if (isMarkInLine(line, bookMark)) return true;
            }
        }
        return false;
    }

    public static int getParagraphLine(BookMark bm, List<BookPageLine> paragraphLines) {
        if (bm != null && paragraphLines != null) {
            for (BookPageLine bpl : paragraphLines) {
                if (isMarkInLine(bpl, bm)) return bpl.getLineIndex();
            }
        }
        return 0;
    }

    private static boolean isMarkInLine(BookPageLine line, BookMark bookMark) {
        if (bookMark != null && line != null && line.getLineId() == bookMark.getParagraphId()) {
            int start = line.getWordLineIndex();
            int end = start + (line.getText() == null ? 0 : line.getText().length());
            return bookMark.getWordLineIndex() >= start && bookMark.getWordLineIndex() < end;
        }
        return false;
    }

    public static class BookMark {
        @Expose
        @SerializedName("chapter")
        private boolean mChapter;
        @Expose
        @SerializedName("progress")
        private float mProgress;
        @Expose
        @SerializedName("text")
        private String mText; //标签展示文本内容
        @Expose
        @SerializedName("para")
        private int mParagraphId; //段落id
        @Expose
        @SerializedName("wli")
        private int mWordLineIndex; //书签在段落中的index
        @Expose
        @SerializedName("time")
        private long mTime;

        public boolean isChapter() {
            return mChapter;
        }

        public void setChapter(boolean chapter) {
            mChapter = chapter;
        }

        public float getProgress() {
            return mProgress;
        }

        public void setProgress(float progress) {
            mProgress = progress;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            mText = text;
        }

        public int getParagraphId() {
            return mParagraphId;
        }

        public void setParagraphId(int paragraphId) {
            mParagraphId = paragraphId;
        }

        public int getWordLineIndex() {
            return mWordLineIndex;
        }

        public void setWordLineIndex(int wordLineIndex) {
            mWordLineIndex = wordLineIndex;
        }

        public long getTime() {
            return mTime;
        }

        public void setTime(long time) {
            mTime = time;
        }
    }
}
