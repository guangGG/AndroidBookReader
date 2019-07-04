package gapp.season.reader.util;

import android.util.Log;

import gapp.season.reader.BookReader;

public class BrLog {
    public static void e(String tag, String msg) {
        if (isDev()) Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isDev()) Log.w(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (isDev()) Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDev()) Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDev()) Log.v(tag, msg);
    }

    private static boolean isDev() {
        return BookReader.isDev();
    }
}
