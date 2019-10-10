package gapp.season.reader.page;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gapp.season.reader.BookReader;
import gapp.season.reader.R;
import gapp.season.reader.loader.BookLoadListener;
import gapp.season.reader.loader.BookLoader;
import gapp.season.reader.loader.BookMarkMgr;
import gapp.season.reader.loader.BookRackMgr;
import gapp.season.reader.loader.LoaderConfig;
import gapp.season.reader.model.Book;
import gapp.season.reader.model.BookPageLine;
import gapp.season.reader.model.BookSchedule;
import gapp.season.reader.model.BookSearchItem;
import gapp.season.reader.util.BrClipboardUtil;
import gapp.season.reader.util.BrFileUtil;
import gapp.season.reader.util.BrLog;
import gapp.season.reader.util.BrTaskListener;
import gapp.season.reader.util.BrUtil;
import gapp.season.reader.view.BookPageView;

public class BookReaderActivity extends Activity {
    private static final int REQUEST_CODE_IMPORT_BOOK = 1001;
    private boolean mHasLoaderInit;
    private boolean mIsSafe;

    private View mPage;
    private TextView mTitleView;
    private TextView mPageShow;
    private BookPageView mPageView;
    private View mRackLayout;
    private View mProgressLayout;
    private View mSetLayout;
    private View mMarkLayout;
    private View mSearchLayout;
    private SeekBar mSeekProgress;
    private RadioButton mCharsetU8;
    private RadioButton mCharsetGbk;
    private RadioButton mRetractY;
    private RadioButton mRetractN;
    private RadioButton mBgDef;
    private RadioButton mBgOld;
    private RadioButton mBgEye;
    private RadioButton mTs12;
    private RadioButton mTs14;
    private RadioButton mTs16;
    private RadioButton mTs18;
    private RadioButton mTs20;
    private RadioButton mLs05;
    private RadioButton mLs15;
    private RadioButton mLs25;
    private RadioButton mLs35;
    private RadioButton mLs45;

    private BookRackAdapter mRackAdapter;
    private ListView mMarkListView;
    private ListView mChapterListView;
    private BookMarkAdapter mMarkAdapter;
    private BookMarkAdapter mChapterAdapter;
    private boolean mInBookMark;
    private BookSearchAdapter mSearchAdapter;
    private EditText mSearchWordView;

    private String mBookPath;
    private Book mBook;
    private int mParagraph;
    private int mParagraphLine;
    private float mProgress;

    private Map<Integer, List<BookPageLine>> mLineMap; //缓存段落的行数据
    private DecimalFormat mPercentFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoaderConfig.getInstanse().init(getApplicationContext());
        setTheme(BookReader.getPageTheme());
        setContentView(R.layout.br_activity_book_reader);
        mPage = findViewById(R.id.br_all_page);
        mTitleView = findViewById(R.id.br_title_show);
        mPageShow = findViewById(R.id.br_page_show);
        mPageView = findViewById(R.id.br_book_page);
        mRackLayout = findViewById(R.id.br_rack_layout);
        mProgressLayout = findViewById(R.id.br_progress_layout);
        mSetLayout = findViewById(R.id.br_set_layout);
        mMarkLayout = findViewById(R.id.br_mark_layout);
        mSearchLayout = findViewById(R.id.br_search_layout);
        mSeekProgress = findViewById(R.id.br_seek_progress);
        mCharsetU8 = findViewById(R.id.br_rb_cs_u8);
        mCharsetGbk = findViewById(R.id.br_rb_cs_gbk);
        mRetractY = findViewById(R.id.br_rb_rt_y);
        mRetractN = findViewById(R.id.br_rb_rt_n);
        mBgDef = findViewById(R.id.br_rb_bg_def);
        mBgOld = findViewById(R.id.br_rb_bg_old);
        mBgEye = findViewById(R.id.br_rb_bg_eye);
        mTs12 = findViewById(R.id.br_rb_ts_12);
        mTs14 = findViewById(R.id.br_rb_ts_14);
        mTs16 = findViewById(R.id.br_rb_ts_16);
        mTs18 = findViewById(R.id.br_rb_ts_18);
        mTs20 = findViewById(R.id.br_rb_ts_20);
        mLs05 = findViewById(R.id.br_rb_ls_05);
        mLs15 = findViewById(R.id.br_rb_ls_15);
        mLs25 = findViewById(R.id.br_rb_ls_25);
        mLs35 = findViewById(R.id.br_rb_ls_35);
        mLs45 = findViewById(R.id.br_rb_ls_45);


        mPageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mHasLoaderInit) {
                    LoaderConfig.getInstanse().config(mPageView.getWidth(), mPageView.getHeight());
                    BrLog.d(BookReader.TAG, String.format("View:[%s,%s]-Text:[%s,%s]", mPageView.getWidth(),
                            mPageView.getHeight(), LoaderConfig.getInstanse().getTextSize(),
                            LoaderConfig.getInstanse().getLineSpace()));
                    mHasLoaderInit = true;
                }
            }
        });
        mPageView.setDetectorListener(new BookPageView.OnDetectorListener() {
            @Override
            public void onDetector(int type, MotionEvent e) {
                switch (type) {
                    case 1:
                        float x = e.getX();
                        int width = mPageView.getWidth();
                        float pos = (width > 0) ? (x / width) : 0;
                        if (pos < 0.3) {
                            toPrePage();
                        } else if (pos > 0.7) {
                            toNextPage();
                        } else {
                            openBookProgress(null);
                        }
                        break;
                    case 2:
                        openBookMark(null);
                        break;
                    case 3:
                        toNextPage();
                        break;
                    case 4:
                        if (mParagraph == 0 && mParagraphLine == 0) {
                            openBookMark(null);
                        } else {
                            toPrePage();
                        }
                        break;
                    case 5:
                        openBookProgress(null);
                        break;
                    case 6:
                        openBookRack(null);
                        break;
                }
            }
        });
        updatePageBg();
        updateBmLabel();
        mRackAdapter = new BookRackAdapter(this);
        GridView rackView = findViewById(R.id.br_my_bookrack);
        rackView.setAdapter(mRackAdapter);
        rackView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int type = mRackAdapter.getBookType(position);
                switch (type) {
                    case BookRackAdapter.BOOK_TYPE_NORMAL:
                        exitOptMode(null);
                        initData(mRackAdapter.getBookPath(position));
                        break;
                    case BookRackAdapter.BOOK_TYPE_CURRENT:
                        exitOptMode(null);
                        break;
                    case BookRackAdapter.BOOK_TYPE_ADD:
                        exitOptMode(null);
                        importBook(); //导入book
                        break;
                }
            }
        });
        mSeekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    BookSchedule schedule = BookLoader.toProgress(mBook, 1f * progress / 10000, getLineMap());
                    mParagraph = schedule.getParagraph();
                    mParagraphLine = schedule.getParagraphLine();
                    updatePage(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mMarkAdapter = new BookMarkAdapter(this);
        mMarkListView = findViewById(R.id.br_lv_bookmark);
        mMarkListView.setAdapter(mMarkAdapter);
        mMarkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookMarkMgr.BookMark bm = (BookMarkMgr.BookMark) mMarkAdapter.getItem(position);
                mParagraph = bm.getParagraphId();
                mParagraphLine = BookMarkMgr.getParagraphLine(bm, BookLoader.getBookLineInfo(getLineMap(), mBook, mParagraph));
                updatePage(0);
                exitOptMode(null);
            }
        });
        mMarkListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(BookReaderActivity.this)
                        .setMessage(R.string.br_remove_bookmark_tips)
                        .setPositiveButton(R.string.br_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BookMarkMgr.BookMark bm = (BookMarkMgr.BookMark) mMarkAdapter.getItem(position);
                                BookMarkMgr.removeBookMark(mBookPath, bm);
                                updatePage(0);
                                updateBookMarkList();
                            }
                        }).show();
                return true;
            }
        });
        findViewById(R.id.br_menu_bm).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(BookReaderActivity.this)
                        .setMessage(R.string.br_clear_bookmark_tips)
                        .setPositiveButton(R.string.br_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BookMarkMgr.clearBookMark(mBookPath);
                                updatePage(0);
                                updateBookMarkList();
                            }
                        }).show();
                return true;
            }
        });
        mChapterAdapter = new BookMarkAdapter(this);
        mChapterListView = findViewById(R.id.br_lv_chapters);
        mChapterListView.setAdapter(mChapterAdapter);
        mChapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookMarkMgr.BookMark bm = (BookMarkMgr.BookMark) mChapterAdapter.getItem(position);
                mParagraph = bm.getParagraphId();
                mParagraphLine = 0;
                updatePage(0);
                exitOptMode(null);
            }
        });
        mSearchAdapter = new BookSearchAdapter(this);
        ListView searchView = findViewById(R.id.br_search_list);
        searchView.setAdapter(mSearchAdapter);
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookSearchItem item = (BookSearchItem) mSearchAdapter.getItem(position);
                if (item.getSchedule() != null) {
                    mParagraph = item.getSchedule().getParagraph();
                    mParagraphLine = item.getSchedule().getParagraphLine();
                }
                updatePage(0);
                exitOptMode(null);
            }
        });
        mSearchWordView = findViewById(R.id.br_search_text);
        mSearchWordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(null);
                    return true;
                }
                return false;
            }
        });

        String path = null;
        if (getIntent() != null) {
            if (getIntent().getData() != null && Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                path = getIntent().getData().getPath();
            } else {
                path = getIntent().getStringExtra(BrUtil.KEY_BOOK_PATH);
            }
        }
        final String bookFilePath = TextUtils.isEmpty(path) ? BookRackMgr.getTopBook() : path;
        if (TextUtils.isEmpty(bookFilePath)) {
            openBookRack(null);
        } else {
            mPageView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initData(bookFilePath);
                }
            }, 50); //延迟是因为需要等PageView加载完毕，LoaderConfig初始化完成
        }
        mIsSafe = true;
    }

    @Override
    protected void onDestroy() {
        mIsSafe = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mRackLayout.getVisibility() == View.VISIBLE
                || mProgressLayout.getVisibility() == View.VISIBLE
                || mSetLayout.getVisibility() == View.VISIBLE
                || mMarkLayout.getVisibility() == View.VISIBLE
                || mSearchLayout.getVisibility() == View.VISIBLE) {
            exitOptMode(null);
            return;
        }
        super.onBackPressed();
    }

    private void initData(String bookFilePath) {
        initData(bookFilePath, null);
    }

    private void initData(final String bookFilePath, String charset) {
        BrLog.i(BookReader.TAG, "loadBook start:" + bookFilePath);
        mTitleView.setText(R.string.br_loading); //loading态
        BookLoader.loadBook(bookFilePath, charset, new BrTaskListener<Book>() {
            @Override
            public void onTaskDone(int code, String msg, Book data) {
                BrLog.i(BookReader.TAG, String.format("loadBook end:%s, code:%s, paragraphNum:%s, chapterNum:%s", bookFilePath, code,
                        ((data == null || data.getLines() == null) ? -1 : data.getLines().size()),
                        ((data == null || data.getChapters() == null) ? -1 : data.getChapters().size())));
                if (mIsSafe) {
                    mTitleView.setText(R.string.br_app_name);
                    if (data != null && data.getLines() != null && data.getLines().size() > 0) {
                        mBookPath = bookFilePath;
                        mBook = data;
                        getLineMap().clear();
                        // 从书架获取进度
                        BookRackMgr.RackBook rackBook = BookRackMgr.getNonNullRackBook(mBookPath, mBook);
                        mParagraph = rackBook.getParagraph();
                        mParagraphLine = rackBook.getParagraphLine();
                        updatePage(0);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.br_load_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void toPrePage() {
        updatePage(-1);
    }

    private void toNextPage() {
        updatePage(1);
    }

    private void updatePage(int direction) { //-1前一页，0当前页，1后一页
        BrLog.d(BookReader.TAG, String.format("updatePage: direction:%s, paragraph:%s.%s", direction, mParagraph, mParagraphLine));
        BookLoader.loadPage(mBook, getLineMap(), mParagraph, mParagraphLine, direction, new BookLoadListener() {
            @Override
            public void onLoadPage(int code, List<BookPageLine> list, int paragraph, int paragraphLine) {
                if (mIsSafe) {
                    mParagraph = paragraph;
                    mParagraphLine = paragraphLine;
                    //更新内容
                    mPageView.setPageLines(list);
                    //更新标题
                    String bookName = BrUtil.getFileName(mBookPath);
                    if (TextUtils.isEmpty(bookName)) {
                        mTitleView.setText(R.string.br_app_name);
                    } else {
                        mTitleView.setText(bookName);
                    }
                    //更新百分比
                    long cword = (list == null || list.size() < 1) ? 0 : list.get(0).getWordIndex();
                    long aword = mBook != null ? mBook.getWordNum() : 0;
                    float progress = 0;
                    if (aword > 0) {
                        progress = 1f * cword / aword;
                        progress = (progress > 1) ? 1 : progress;
                    }
                    mProgress = progress;
                    mPageShow.setText(String.format("%s", getPercentDf().format(progress)));
                    // 更新进度到书架
                    BookRackMgr.updateBookPage(mBookPath, mBook, mParagraph, mParagraphLine);
                    mRackAdapter.setCurrentBook(mBookPath);
                    //日志
                    BrLog.d(BookReader.TAG, String.format("onLoadPage: code:%s, paragraph:%s.%s/%s, progress:%s/%s, lines:%s", code,
                            paragraph, paragraphLine, ((mBook == null || mBook.getLines() == null) ? -1 : mBook.getLines().size()),
                            cword, aword, list != null ? list.size() : -1));
                }
            }
        });
    }

    private DecimalFormat getPercentDf() {
        if (mPercentFormat == null)
            mPercentFormat = new DecimalFormat("0.##%");
        return mPercentFormat;
    }

    @SuppressLint("UseSparseArrays")
    private Map<Integer, List<BookPageLine>> getLineMap() {
        if (mLineMap == null)
            mLineMap = new HashMap<>();
        return mLineMap;
    }

    private void updatePageBg() {
        switch (LoaderConfig.getInstanse().getBgStyle()) {
            case 1:
                mPage.setBackgroundColor(0xfffff2e2);
                break;
            case 2:
                mPage.setBackgroundColor(0xffcce8cf);
                break;
            default:
                mPage.setBackgroundColor(Color.WHITE);
                break;
        }
    }

    private void updateBmLabel() {
        TextView bmView = findViewById(R.id.br_menu_bm);
        TextView ctView = findViewById(R.id.br_menu_ct);
        if (mInBookMark) {
            bmView.setTextColor(getResources().getColor(R.color.br_white));
            ctView.setTextColor(getResources().getColor(R.color.br_color_999));
        } else {
            bmView.setTextColor(getResources().getColor(R.color.br_color_999));
            ctView.setTextColor(getResources().getColor(R.color.br_white));
        }
    }

    public void importBook() {
        try {
            if (BookReader.getBrListener() != null) {
                BookReader.getBrListener().importBook(this, REQUEST_CODE_IMPORT_BOOK);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_IMPORT_BOOK);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.br_no_import_app, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_BOOK) {
            if (resultCode == RESULT_OK && data != null) {
                if (BookReader.getBrListener() != null) {
                    String path = BookReader.getBrListener().onImportBook(this, data);
                    BrLog.i(BookReader.TAG, "onImportBook path = " + path);
                    if (path != null && path.startsWith("/")) {
                        initData(path);
                    }
                } else {
                    if (data.getData() != null) {
                        String path = BrFileUtil.getPath(this, data.getData()); //data.getData().getPath() maybe /external_files/a.txt
                        BrLog.i(BookReader.TAG, "import book path = " + path);
                        if (path != null && path.startsWith("/")) {
                            initData(path);
                        }
                    }
                }
            }
        }
    }

    public void openBookRack(View view) {
        exitOptMode(null);
        mRackLayout.setVisibility(View.VISIBLE);
        mRackAdapter.setRackBooks(BookRackMgr.getRackBooks());
    }

    public void openBookProgress(View view) {
        exitOptMode(null);
        mProgressLayout.setVisibility(View.VISIBLE);
        mSeekProgress.setProgress(Math.round(mProgress * 10000));
    }

    public void openBookSetting(View view) {
        exitOptMode(null);
        mSetLayout.setVisibility(View.VISIBLE);
        if (mBook != null) {
            if (TextUtils.equals(mBook.getCharSet(), BrUtil.GBK)) {
                mCharsetGbk.setChecked(true);
            } else {
                mCharsetU8.setChecked(true);
            }
        }
        if (LoaderConfig.getInstanse().getRetract() == 1) {
            mRetractN.setChecked(true);
        } else {
            mRetractY.setChecked(true);
        }
        switch (LoaderConfig.getInstanse().getBgStyle()) {
            case 1:
                mBgOld.setChecked(true);
                break;
            case 2:
                mBgEye.setChecked(true);
                break;
            default:
                mBgDef.setChecked(true);
                break;
        }
        switch (LoaderConfig.getInstanse().getTextSize()) {
            case 12:
                mTs12.setChecked(true);
                break;
            case 14:
                mTs14.setChecked(true);
                break;
            case 18:
                mTs18.setChecked(true);
                break;
            case 20:
                mTs20.setChecked(true);
                break;
            case 16:
            default:
                mTs16.setChecked(true);
                break;
        }
        float ls = LoaderConfig.getInstanse().getLineSpace();
        if (ls <= 0.1) {
            mLs05.setChecked(true);
        } else if (ls < 0.2) {
            mLs15.setChecked(true);
        } else if (ls < 0.3) {
            mLs25.setChecked(true);
        } else if (ls < 0.4) {
            mLs35.setChecked(true);
        } else {
            mLs45.setChecked(true);
        }
    }

    public void openBookMark(View view) {
        exitOptMode(null);
        mMarkLayout.setVisibility(View.VISIBLE);
        //更新章节列表
        updateBookChapterList();
        //更新书签列表
        updateBookMarkList();
    }

    private void updateBookChapterList() {
        List<BookMarkMgr.BookMark> chapterMarks = new ArrayList<>();
        if (mBook != null && mBook.getLines() != null && mBook.getChapters() != null) {
            for (int cpid : mBook.getChapters()) {
                Book.BookLine line = mBook.getLines().get(cpid);
                BookMarkMgr.BookMark bm = new BookMarkMgr.BookMark();
                bm.setChapter(true);
                bm.setProgress(1f * line.getWordIndex() / mBook.getWordNum());
                bm.setText(line.getText());
                bm.setParagraphId(line.getIndex());
                bm.setWordLineIndex(0);
                bm.setTime(0);
                chapterMarks.add(bm);
            }
        }
        mChapterAdapter.setBookMarks(chapterMarks);
    }

    private void updateBookMarkList() {
        List<BookMarkMgr.BookMark> bookMarks = new ArrayList<>();
        List<BookMarkMgr.BookMark> bms = BookMarkMgr.getBookMarks(mBookPath);
        if (bms != null) {
            bookMarks.addAll(bms);
        }
        mMarkAdapter.setBookMarks(bookMarks);
    }

    public void closePage(View view) {
        finish();
    }

    public void doNothing(View view) {
        //不处理逻辑，防止点击事件透传到底层view
    }

    public void exitOptMode(View view) {
        mRackLayout.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.GONE);
        mSetLayout.setVisibility(View.GONE);
        mMarkLayout.setVisibility(View.GONE);
        mSearchLayout.setVisibility(View.GONE);
        hideSoftInput();
    }

    public void clearBookRack(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.br_tips));
        builder.setMessage(getString(R.string.br_clear_bookrack_tips));
        builder.setPositiveButton(getString(R.string.br_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BookRackMgr.clearBook();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.br_cancel), null);
        builder.show();
    }

    public void markBook(View view) {
        exitOptMode(null);
        int markResult = BookMarkMgr.markBook(mBookPath, mProgress, mPageView.getPageLines());
        int resId = R.string.br_bookmark_f;
        if (markResult == 1) {
            resId = R.string.br_bookmark_s;
        } else if (markResult == 2) {
            resId = R.string.br_bookmark_remove_s;
        }
        mPageView.postInvalidate();
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    public void copyPage(View view) {
        exitOptMode(null);
        BrClipboardUtil.putText(this, mPageView.getText());
        Toast.makeText(this, R.string.br_copy_s, Toast.LENGTH_SHORT).show();
    }

    public void searchBook(View view) {
        exitOptMode(null);
        mSearchLayout.setVisibility(View.VISIBLE);
    }

    public void search(View view) {
        hideSoftInput();
        String word = mSearchWordView.getText().toString();
        if (!TextUtils.isEmpty(word)) {
            findViewById(R.id.br_search_loading).setVisibility(View.VISIBLE);
            BookLoader.searchWord(mBook, word, getLineMap(), new BrTaskListener<List<BookSearchItem>>() {
                @Override
                public void onTaskDone(int code, String msg, List<BookSearchItem> data) {
                    findViewById(R.id.br_search_loading).setVisibility(View.GONE);
                    if (mIsSafe) {
                        Toast.makeText(getApplicationContext(), String.format(getString(R.string.br_search_result_tips),
                                (data == null ? 0 : data.size())), Toast.LENGTH_LONG).show();
                        mSearchAdapter.setList(data);
                        ((ListView) findViewById(R.id.br_search_list)).setSelection(0);
                    }
                }
            });
        }
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchWordView.getWindowToken(), 0);
    }

    public void clearSearchWord(View view) {
        mSearchWordView.setText(null);
    }

    public void setCsU8(View view) {
        initData(mBookPath, BrUtil.UTF_8);
    }

    public void setCsGbk(View view) {
        initData(mBookPath, BrUtil.GBK);
    }

    public void setRtY(View view) {
        LoaderConfig.getInstanse().setRetract(0);
        getLineMap().clear();
        updatePage(0);
    }

    public void setRtN(View view) {
        LoaderConfig.getInstanse().setRetract(1);
        getLineMap().clear();
        updatePage(0);
    }

    public void setBgDef(View view) {
        LoaderConfig.getInstanse().setBgStyle(0);
        updatePageBg();
    }

    public void setBgOld(View view) {
        LoaderConfig.getInstanse().setBgStyle(1);
        updatePageBg();
    }

    public void setBgEye(View view) {
        LoaderConfig.getInstanse().setBgStyle(2);
        updatePageBg();
    }

    public void setTs12(View view) {
        LoaderConfig.getInstanse().setTextSize(12);
        getLineMap().clear();
        updatePage(0);
    }

    public void setTs14(View view) {
        LoaderConfig.getInstanse().setTextSize(14);
        getLineMap().clear();
        updatePage(0);
    }

    public void setTs16(View view) {
        LoaderConfig.getInstanse().setTextSize(16);
        getLineMap().clear();
        updatePage(0);
    }

    public void setTs18(View view) {
        LoaderConfig.getInstanse().setTextSize(18);
        getLineMap().clear();
        updatePage(0);
    }

    public void setTs20(View view) {
        LoaderConfig.getInstanse().setTextSize(20);
        getLineMap().clear();
        updatePage(0);
    }

    public void setLs05(View view) {
        LoaderConfig.getInstanse().setLineSpace(0.05f);
        updatePage(0);
    }

    public void setLs15(View view) {
        LoaderConfig.getInstanse().setLineSpace(0.15f);
        updatePage(0);
    }

    public void setLs25(View view) {
        LoaderConfig.getInstanse().setLineSpace(0.25f);
        updatePage(0);
    }

    public void setLs35(View view) {
        LoaderConfig.getInstanse().setLineSpace(0.35f);
        updatePage(0);
    }

    public void setLs45(View view) {
        LoaderConfig.getInstanse().setLineSpace(0.45f);
        updatePage(0);
    }

    public void showBookMarks(View view) {
        mInBookMark = true;
        mChapterListView.setVisibility(View.GONE);
        mMarkListView.setVisibility(View.VISIBLE);
        updateBmLabel();
    }

    public void showChapters(View view) {
        mInBookMark = false;
        mChapterListView.setVisibility(View.VISIBLE);
        mMarkListView.setVisibility(View.GONE);
        updateBmLabel();
    }
}
