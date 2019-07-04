package gapp.season.reader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import gapp.season.reader.page.BookReaderActivity;
import gapp.season.reader.util.BrUtil;

public class BookReader {
    public static final String TAG = "BookReader";
    private static final String BOOK_READER_DIR = "/bookreader";
    private static boolean sIsDev; //开发模式会打印一些日志
    private static int sPageTheme; //页面样式
    private static String sBookDir; //Book文件保存的文件夹位置

    public static void config(boolean isdev, int pageTheme, String bookDir) {
        sIsDev = isdev;
        sPageTheme = pageTheme;
        if (!TextUtils.isEmpty(bookDir)) {
            sBookDir = bookDir + BOOK_READER_DIR;
        }
    }

    public static boolean isDev() {
        return sIsDev;
    }

    public static int getPageTheme() {
        if (sPageTheme <= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sPageTheme = android.R.style.Theme_Material_Light_NoActionBar;
            } else {
                sPageTheme = android.R.style.Theme_Holo_Light_NoActionBar;
            }
        }
        return sPageTheme;
    }

    /**
     * 获取读书相关文件目录
     */
    public static String getBookDir() {
        return TextUtils.isEmpty(sBookDir) ? (Environment.getExternalStorageDirectory()
                .getAbsolutePath() + BOOK_READER_DIR) : sBookDir;
    }

    public static void readBook(Context context, String bookFilePath) {
        Intent intent = new Intent(context, BookReaderActivity.class);
        intent.putExtra(BrUtil.KEY_BOOK_PATH, bookFilePath);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
