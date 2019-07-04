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
import gapp.season.reader.model.Book;
import gapp.season.reader.util.BrUtil;

//书架：在读书籍的历史记录
public class BookRackMgr {
    private static final String BOOK_RACK_FILE = "bookrack.json";
    private static List<RackBook> mRackBooks;

    private static List<RackBook> initRackBooks() {
        try {
            File file = new File(BookReader.getBookDir(), BOOK_RACK_FILE);
            String json = BrUtil.getFileContent(file);
            Gson gson = new Gson();
            Type type = new TypeToken<List<RackBook>>() {
            }.getType();
            List<RackBook> saveList = gson.fromJson(json, type);
            if (saveList != null) {
                return saveList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static void saveRackBooks() {
        if (mRackBooks != null) {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(mRackBooks);
                File file = new File(BookReader.getBookDir(), BOOK_RACK_FILE);
                BrUtil.saveStringToFile(file, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTopBook() {
        if (mRackBooks == null) {
            mRackBooks = initRackBooks();
        }
        if (mRackBooks.size() > 0) {
            RackBook rackBook = mRackBooks.get(mRackBooks.size() - 1);
            if (rackBook != null) {
                return rackBook.getBookPath();
            }
        }
        return null;
    }

    public static void updateBookPage(String bookPath, Book book, long paragraph, long paragraphLine) {
        if (TextUtils.isEmpty(bookPath) || book == null) {
            return;
        }
        if (mRackBooks == null) {
            mRackBooks = initRackBooks();
        }
        RackBook rackBook = new RackBook();
        rackBook.setBookId(book.getBookId());
        rackBook.setBookPath(bookPath);
        rackBook.setParagraph((int) paragraph);
        rackBook.setParagraphLine((int) paragraphLine);
        mRackBooks.remove(rackBook);
        mRackBooks.add(rackBook);
        saveRackBooks();
    }

    public static void removeBook(String bookPath) {
        if (TextUtils.isEmpty(bookPath)) {
            return;
        }
        if (mRackBooks == null) {
            mRackBooks = initRackBooks();
        }
        RackBook rackBook = new RackBook();
        rackBook.setBookPath(bookPath);
        mRackBooks.remove(rackBook);
        saveRackBooks();
    }

    public static void clearBook() {
        mRackBooks = new ArrayList<>();
        saveRackBooks();
    }

    public static RackBook getNonNullRackBook(String bookPath, Book book) {
        if (TextUtils.isEmpty(bookPath) || book == null) {
            return new RackBook();
        }
        if (mRackBooks == null) {
            mRackBooks = initRackBooks();
        }
        for (RackBook rb : mRackBooks) {
            if (TextUtils.equals(bookPath, rb.getBookPath())
                    && TextUtils.equals(book.getBookId(), rb.getBookId())) {
                return rb;
            }
        }
        return new RackBook();
    }

    public static List<RackBook> getRackBooks() {
        return mRackBooks;
    }

    public static class RackBook {
        @Expose
        @SerializedName("bid")
        private String mBookId; //用于校验book是否被修改，文件改动后从0开始阅读
        @Expose
        @SerializedName("path")
        private String mBookPath; //键值
        @Expose
        @SerializedName("para")
        private int mParagraph;
        @Expose
        @SerializedName("pl")
        private int mParagraphLine;

        public String getBookId() {
            return mBookId;
        }

        public void setBookId(String bookId) {
            mBookId = bookId;
        }

        public String getBookPath() {
            return mBookPath;
        }

        public void setBookPath(String bookPath) {
            mBookPath = bookPath;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RackBook rackBook = (RackBook) o;
            return BrUtil.equals(mBookPath, rackBook.mBookPath);
        }
    }
}
