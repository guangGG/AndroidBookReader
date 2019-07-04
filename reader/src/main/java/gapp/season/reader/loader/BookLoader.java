package gapp.season.reader.loader;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import gapp.season.reader.BookReader;
import gapp.season.reader.model.Book;
import gapp.season.reader.model.BookPageLine;
import gapp.season.reader.model.BookSchedule;
import gapp.season.reader.util.BrLog;
import gapp.season.reader.util.BrTaskListener;
import gapp.season.reader.util.BrUtil;

public class BookLoader {
    private static final int BOOK_VERSION = 1;
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; //可阅读的最大文件大小
    // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
    private static final String[] CHAPTER_PATTERNS = new String[]{
            "^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
            "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([:\uff1a\f\t])(.{0,30})$",
            "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
            "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$",
    };

    private static boolean isIllegal(char c) {
        return c < ' ' && c != '\n';
    }

    private static boolean isLineSeparator(char c) {
        return c == '\n';
    }

    public static String getCacheBookDir() {
        return BookReader.getBookDir() + "/books";
    }

    public static void loadBook(final String bookFilePath, final String charset, final BrTaskListener<Book> listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Book book = loadBook(bookFilePath, charset);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onTaskDone(book == null ? BrTaskListener.CODE_FAIL :
                                    BrTaskListener.CODE_SUCCESS, null, book);
                        }
                    }
                });
            }
        }).start();
    }

    //耗时操作，须在子线程执行
    private static Book loadBook(String bookFilePath, String charset) {
        if (!TextUtils.isEmpty(bookFilePath)) {
            File file = new File(bookFilePath);
            if (file.exists() && file.length() < MAX_FILE_SIZE) {
                // 从缓存中取
                String bid = BrUtil.getFileBookId(file);
                File fc = new File(getCacheBookDir(), bid);
                String cache = BrUtil.getFileContent(fc);
                if (cache != null) {
                    Gson gson = new Gson();
                    Book book = gson.fromJson(cache, Book.class);
                    if (book != null) {
                        boolean matchCharSet = (TextUtils.isEmpty(charset) || charset.equalsIgnoreCase(book.getCharSet()));
                        if (BOOK_VERSION == book.getVersion() && bid.equals(book.getBookId()) && matchCharSet) {
                            // 初始化书籍标签信息
                            BookMarkMgr.initBookMarks(bookFilePath);
                            return book;
                        }
                    }
                }

                InputStreamReader isr = null;
                try {
                    charset = TextUtils.isEmpty(charset) ? BrUtil.getFileCharset(file) : charset;
                    FileInputStream fis = new FileInputStream(file);
                    isr = new InputStreamReader(fis, charset);
                    Book bk = new Book();
                    bk.setVersion(BOOK_VERSION);
                    bk.setCharSet(charset);
                    bk.setBookId(bid);
                    long wordsIndex = 0;
                    int linesIndex = 0;
                    Book.BookLine bookLine = new Book.BookLine();
                    bk.addLine(bookLine);
                    char[] buffer = new char[1024];
                    int num;
                    while ((num = isr.read(buffer)) >= 0) {
                        for (int i = 0; i < num; i++) {
                            char c = buffer[i];
                            if (isLineSeparator(c)) {
                                wordsIndex++;
                                linesIndex++;
                                bookLine = new Book.BookLine();
                                bookLine.setWordIndex(wordsIndex);
                                bookLine.setIndex(linesIndex);
                                bk.addLine(bookLine);
                            } else {
                                if (!isIllegal(c)) { //无效字符则跳过
                                    bookLine.addWord(c);
                                    wordsIndex++;
                                }
                            }
                        }
                    }
                    bk.setWordNum(wordsIndex);
                    //匹配章节标题
                    for (Book.BookLine line : bk.getLines()) {
                        String lineText = line.getText();
                        int lineLength = lineText != null ? lineText.length() : 0;
                        if (lineLength > LoaderConfig.MAX_PARAG_SIZE) {
                            BrLog.w(BookReader.TAG, String.format("段落超长：第%s段，word：%s，length：%s",
                                    line.getIndex(), line.getWordIndex(), lineLength));
                        }
                        if (lineText != null && lineLength >= 3 && lineLength <= 300) {
                            lineText = lineLength > 30 ? lineText.substring(0, 30) : lineText;
                            for (String pattern : CHAPTER_PATTERNS) {
                                if (lineText.matches(pattern)) {
                                    bk.addChapter(line.getIndex());
                                    break;
                                }
                            }
                        }
                    }
                    // 保存起来
                    File f = new File(getCacheBookDir(), bid);
                    Gson gson = new Gson();
                    BrUtil.saveStringToFile(f, gson.toJson(bk));
                    // 初始化书籍标签信息
                    BookMarkMgr.initBookMarks(bookFilePath);
                    return bk;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (isr != null) {
                            isr.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static void loadPage(final Book book, final Map<Integer, List<BookPageLine>> lineMap, final int paragraph,
                                final int paragraphLine, final int direction, final BookLoadListener listener) {
        if (listener == null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (book != null && book.getLines() != null && book.getLines().size() > 0 && lineMap != null) {
                        int fParagraph = (book.getLines().size() > paragraph) ? paragraph : (book.getLines().size() - 1);
                        List<BookPageLine> bpls = getBookLineInfo(lineMap, book.getLines().get(fParagraph));
                        int fParagraphLine = (paragraphLine >= bpls.size()) ? bpls.size() - 1 : paragraphLine;
                        int bplIndex = fParagraphLine;
                        List<BookPageLine> list = new ArrayList<>();
                        int pageLines = LoaderConfig.getInstanse().getPageLines();
                        if (direction < 0) {
                            // 上一页
                            for (int i = fParagraph; i >= 0; i--) {
                                for (int j = bplIndex - 1; j >= 0; j--) {
                                    list.add(bpls.get(j));
                                    if (list.size() >= pageLines) {
                                        break;
                                    }
                                }
                                if (list.size() >= pageLines) {
                                    break;
                                }
                                if (i >= 1) {
                                    bpls = getBookLineInfo(lineMap, book.getLines().get(i - 1));
                                    bplIndex = bpls.size();
                                }
                            }
                            if (list.size() >= pageLines) {
                                List<BookPageLine> listPre = new ArrayList<>();
                                BookPageLine plp = list.get(pageLines - 1);
                                for (int i = pageLines - 1; i >= 0; i--) {
                                    listPre.add(list.get(i));
                                }
                                onLoadPage(plp.getLineId(), plp.getLineIndex(), listPre, listener, 0);
                                return;
                            } else {
                                loadPage(book, lineMap, 0, 0, 0, listener);
                                return;
                            }
                        } else if (direction > 0) {
                            //下一页
                            for (int i = fParagraph; i < book.getLines().size(); i++) {
                                List<BookPageLine> mbpls = getBookLineInfo(lineMap, book.getLines().get(i));
                                for (int j = bplIndex; j < mbpls.size(); j++) {
                                    list.add(mbpls.get(j));
                                    if (list.size() >= pageLines * 2) {
                                        break;
                                    }
                                }
                                bplIndex = 0;
                                if (list.size() >= pageLines * 2) {
                                    break;
                                }
                            }
                            if (list.size() > pageLines) {
                                List<BookPageLine> listAfter = new ArrayList<>();
                                BookPageLine pla = list.get(pageLines);
                                for (int i = pageLines; i < list.size(); i++) {
                                    listAfter.add(list.get(i));
                                }
                                onLoadPage(pla.getLineId(), pla.getLineIndex(), listAfter, listener, 0);
                                return;
                            }
                            onLoadPage(fParagraph, fParagraphLine, list, listener, 0);
                            return;
                        } else {
                            //当前页
                            for (int i = fParagraph; i < book.getLines().size(); i++) {
                                List<BookPageLine> mbpls = getBookLineInfo(lineMap, book.getLines().get(i));
                                for (int j = bplIndex; j < mbpls.size(); j++) {
                                    list.add(mbpls.get(j));
                                    if (list.size() >= pageLines) {
                                        onLoadPage(fParagraph, fParagraphLine, list, listener, 0);
                                        return;
                                    }
                                }
                                bplIndex = 0;
                            }
                            onLoadPage(fParagraph, fParagraphLine, list, listener, 0);
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                onLoadPage(paragraph, paragraphLine, null, listener, -1);
            }
        }).start();
    }

    private static void onLoadPage(final int paragraph, final int paragraphLine, final List<BookPageLine> list,
                                   final BookLoadListener listener, final int code) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onLoadPage(code, list, paragraph, paragraphLine);
            }
        });
    }

    public static BookSchedule toProgress(Book book, float progress, Map<Integer, List<BookPageLine>> lineMap) {
        long toword = (book == null) ? 0 : (long) (book.getWordNum() * progress);
        return toProgress(book, toword, lineMap);
    }

    private static BookSchedule toProgress(Book book, long toword, Map<Integer, List<BookPageLine>> lineMap) {
        BookSchedule schedule = new BookSchedule();
        if (book != null && book.getLines() != null && lineMap != null) {
            //计算第几段
            long aword = book.getWordNum();
            int pageLines = LoaderConfig.getInstanse().getPageLines();
            int minLineWords = LoaderConfig.getInstanse().getMinLineWords();
            if (aword - toword < pageLines * minLineWords) {
                //滑动末尾页的优化(使用估算值来提升性能)
                int ln = 0;
                for (int i = book.getLines().size() - 1; i >= 0; i--) {
                    Book.BookLine bl = book.getLines().get(i);
                    String text = bl.getText();
                    int lineNum = (text == null) ? 0 : (int) Math.ceil(1f * text.length() / minLineWords);
                    lineNum = (lineNum < 1) ? 1 : lineNum;
                    ln += lineNum;
                    if (ln >= pageLines) {
                        schedule.setParagraph(bl.getIndex());
                        schedule.setParagraphLine(ln - pageLines);
                        return schedule;
                    }
                }
                if (ln <= pageLines) {
                    return schedule;
                }
            }
            Book.BookLine bookLine = null;
            for (int i = 0; i < book.getLines().size(); i++) {
                Book.BookLine bl = book.getLines().get(i);
                if (bl.getWordIndex() < toword) {
                    bookLine = bl;
                } else {
                    break;
                }
            }
            if (bookLine != null) {
                schedule.setParagraph(bookLine.getIndex());
                //计算段落的第几行
                List<BookPageLine> list = getBookLineInfo(lineMap, bookLine);
                int paragraphLine = 0;
                for (int i = 0; i < list.size(); i++) {
                    BookPageLine bpl = list.get(i);
                    if (bpl.getWordIndex() < toword) {
                        paragraphLine = i;
                    } else {
                        break;
                    }
                }
                schedule.setParagraphLine(paragraphLine);
            }
        }
        return schedule;
    }

    @NonNull
    public static List<BookPageLine> getBookLineInfo(Map<Integer, List<BookPageLine>> lineMap, Book book, int paragraph) {
        Book.BookLine bookLine = null;
        if (paragraph >= 0 && book != null && book.getLines() != null && book.getLines().size() > paragraph) {
            bookLine = book.getLines().get(paragraph);
        }
        return getBookLineInfo(lineMap, bookLine);
    }

    @NonNull
    private static List<BookPageLine> getBookLineInfo(Map<Integer, List<BookPageLine>> lineMap, Book.BookLine bookLine) {
        if (lineMap == null || bookLine == null) {
            return new ArrayList<>();
        }
        List<BookPageLine> bpls = lineMap.get((bookLine.getIndex()));
        if (bpls != null && bpls.size() > 0) {
            return bpls;
        }
        bpls = new ArrayList<>();
        String orgText = bookLine.getText();
        String text = LoaderConfig.getInstanse().retract(orgText);
        int offset = text.length() - (TextUtils.isEmpty(orgText) ? 0 : orgText.length());
        if (text.length() > LoaderConfig.MAX_PARAG_SIZE) { //性能保护，对特别大的行省略处理
            BookPageLine line = new BookPageLine();
            line.setLineId(bookLine.getIndex());
            line.setLineIndex(0);
            line.setWordIndex(bookLine.getWordIndex());
            line.setWordLineIndex(0);
            line.setText(text.substring(0, 10) + "……");
            bpls.add(line);
            lineMap.put(bookLine.getIndex(), bpls);
        } else {
            int lineIndex = 0; //此行在段内为第几行
            int lineFirstIndex = 0; //行首字符的index
            BookPageLine line = new BookPageLine();
            line.setLineId(bookLine.getIndex());
            line.setLineIndex(0);
            line.setWordIndex(bookLine.getWordIndex());
            line.setWordLineIndex(0);
            line.setText("");
            bpls.add(line);
            for (int i = 0; i < text.length(); i++) {
                String lt = text.substring(lineFirstIndex, i + 1);
                if (LoaderConfig.getInstanse().isLineSpill(lt)) {
                    lineIndex++;
                    int realI = i - offset; //由于行首可能缩进，word序号需要做下修正
                    line = new BookPageLine();
                    line.setLineId(bookLine.getIndex());
                    line.setLineIndex(lineIndex);
                    line.setWordIndex(realI > 0 ? bookLine.getWordIndex() + realI : bookLine.getWordIndex());
                    line.setWordLineIndex(realI > 0 ? realI : 0); //使用修正值
                    line.setText(String.valueOf(text.charAt(i)));
                    bpls.add(line);
                    lineFirstIndex = i;
                } else {
                    line.setText(lt);
                }
            }
            lineMap.put(bookLine.getIndex(), bpls);
        }
        return bpls;
    }
}
