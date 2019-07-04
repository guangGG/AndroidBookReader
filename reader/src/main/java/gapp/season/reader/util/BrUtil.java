package gapp.season.reader.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

public class BrUtil {
    public static final String KEY_BOOK_PATH = "key_bookfile_path";
    public static final String UTF_8 = "UTF-8";
    public static final String GBK = "GBK";
    private static final int BUFFER_SIZE = 10240;

    /**
     * 判断文件的编码"UTF-8"还是"GBK"（原理是判断文件中汉字是否会乱码）
     *
     * @return 文件不存在返回null，无法判断则默认返回"UTF-8"
     */
    public static String getFileCharset(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        // 常见编码{"UTF-8", "GBK", "UNICODE", "BIG5","ISO-8859-1", "ASCII" }
		/* unicode编码范围：
			汉字：[0x4e00,0x9fa5](或十进制[19968,40869])
			数字：[0x30,0x39](或十进制[48, 57])
			小写字母：[0x61,0x7a](或十进制[97, 122])
			大写字母：[0x41,0x5a](或十进制[65, 90])*/
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[2048];
            // 通过检查前2k个字节判断编码
            int length = fis.read(buffer);
            fis.close();
            if (length == -1) {
                // 文件内容为空，返回默认编码
                return UTF_8;
            }
            // 判断是否UTF-8编码(非UTF-8编码的汉字用UTF-8读取会出现未知字符unicode码:65533)
            String text = new String(buffer, 0, length, UTF_8);
            boolean isU8 = true;
            int chineseWord = 0, unUTF8Word = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (65533 == c) {
                    unUTF8Word++;
                    if (unUTF8Word >= 5) {
                        // UTF8不能识别的字符不少于5个，则断定文件不是UTF8编码
                        isU8 = false;
                        break;
                    }
                } else if (c > 19967 && c < 40870) {
                    chineseWord++;
                }
            }
            if (isU8 && unUTF8Word > 0) {
                // UTF8不能识别的字符占可识别汉字字符的20%以上,则断定文件不是UTF8编码
                if (chineseWord == 0) {
                    isU8 = false;
                } else {
                    float uPer = ((float) unUTF8Word) / chineseWord;
                    if (uPer >= 0.2) {
                        isU8 = false;
                    }
                }
            }
            if (isU8) {
                return UTF_8;
            } else {
                return GBK;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return UTF_8;
    }

    public static String getFileBookId(File file) {
        if (file != null)
            return getFileFid(file) + "-" + file.length();
        return null;
    }

    /**
     * 计算文件的FID校验码(前、中、后各取一段进行sha1校验)
     */
    public static String getFileFid(File file) {
        if (file == null)
            return null;

        try {
            if (file.length() <= BUFFER_SIZE * 3) {
                return sha1Encode(new FileInputStream(file));
            } else {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

                // 每次读取一段
                byte[] buffer = new byte[BUFFER_SIZE];
                //前段
                long position = 0;
                updateDigest(messageDigest, randomAccessFile, buffer, position);
                //中间段
                position = file.length() / 2 - BUFFER_SIZE / 2;
                updateDigest(messageDigest, randomAccessFile, buffer, position);
                //后段
                position = file.length() - BUFFER_SIZE;
                updateDigest(messageDigest, randomAccessFile, buffer, position);

                randomAccessFile.close();
                byte[] digest = messageDigest.digest();
                return bytesToHexStr(digest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String sha1Encode(InputStream is) {
        try {
            // "SHA1"、"SHA-1"、"SHA"都可以
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            // 每次读取8k字节
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, length);
            }
            is.close();
            byte[] digest = messageDigest.digest();
            return bytesToHexStr(digest);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateDigest(MessageDigest messageDigest, RandomAccessFile randomAccessFile, byte[] buffer, long position) throws IOException {
        randomAccessFile.seek(position);
        int length = randomAccessFile.read(buffer);
        messageDigest.update(buffer, 0, length);
    }

    public static String bytesToHexStr(byte[] bytes) {
        String temp;
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int n = 0; n < bytes.length; n++) {
            // 字节做"与"运算，去除高位置字节 11111111
            temp = Integer.toHexString(bytes[n] & 0xFF);
            if (temp.length() == 1) {
                // 如果是0至F的单位字符串，则添加0
                sb.append("0").append(temp);
            } else {
                sb.append(temp);
            }
        }
        return sb.toString();
    }

    public static String getFileContent(File file) {
        if (!file.exists()) {
            return null;
        }
        InputStreamReader isr = null;
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, UTF_8);
            char[] buffer = new char[1024];
            int length;
            while ((length = isr.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
            isr.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveStringToFile(File file, String content) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), UTF_8);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(content);
        bw.close();
    }

    public static float getDensityScale(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int dp2px(Context context, float dpVal) {
        float pxVal = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
        return (int) (pxVal + 0.5f);
    }

    public static float px2dp(Context context, float pxVal) {
        return (pxVal / getDensityScale(context));
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static String getFileName(String bookFilePath) {
        if (bookFilePath != null && bookFilePath.length() > 0) {
            int index = bookFilePath.lastIndexOf("/");
            if (index + 1 < bookFilePath.length())
                return bookFilePath.substring(index + 1);
        }
        return "";
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static String getContentFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        InputStream reader = null;
        try {
            try {
                reader = context.getContentResolver().openInputStream(uri);
            } catch (Exception e) {
                e.printStackTrace();
                // fix: opening provider android.support.v4.content.FileProvider from ProcessRecord
                String path = uri.getPath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        && path != null && path.startsWith("/external")) {
                    reader = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + path.replace("/external", ""));
                }
            }
            if (reader == null) {
                return null;
            }
            byte[] buffer = new byte[reader.available()];
            while ((reader.read(buffer)) != -1) {
                builder.append(new String(buffer));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public static String getMd5Hash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return bytesToHexStr(md.digest());
        } catch (Exception e) {
            return null;
        }
    }
}
