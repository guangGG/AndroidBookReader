package gapp.season.reader.util;

import android.content.Context;
import android.text.ClipboardManager;

/**
 * 系统剪贴板工具类
 */
@SuppressWarnings("deprecation")
public class BrClipboardUtil {
    /**
     * 往Clip中放入数据
     */
    public static void putText(Context context, CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setText(text);
        }
    }

    /**
     * 从Clip中取数据
     */
    public static CharSequence getText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return null;
        } else {
            return clipboard.getText();
        }
    }
}
