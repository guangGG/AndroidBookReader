package gapp.season.reader.loader;

import android.content.Context;
import android.graphics.Paint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;

import gapp.season.reader.BookReader;
import gapp.season.reader.util.BrUtil;

public class LoaderConfig {
    static final int MAX_PARAG_SIZE = 20000; //（31行x25字x25页）约 20000
    private static final String BOOK_CONFIG_FILE = "reader-config.json";
    private static LoaderConfig sInstanse;
    private Context mContext;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    @Expose
    @SerializedName("retract")
    private int mRetract; //缩进类型：0行首缩进，1不缩进
    @Expose
    @SerializedName("bg")
    private int mBgStyle; //背景样式：0默认，1复古，2护眼
    @Expose
    @SerializedName("font")
    private int mTextSize = 16; //文字大小dp
    @Expose
    @SerializedName("space")
    private float mLineSpace = 0.25f; //行间距%

    public static LoaderConfig getInstanse() {
        if (sInstanse == null)
            sInstanse = new LoaderConfig();
        return sInstanse;
    }

    public Paint getPaint() {
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        mPaint.setTextSize(BrUtil.dp2px(mContext, mTextSize));
        return mPaint;
    }

    public void init(Context context) { //need ApplicationContext
        mContext = context;

        String json = BrUtil.getFileContent(new File(BookReader.getBookDir(), BOOK_CONFIG_FILE));
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation() //只序列化Expose注解的字段
                .create();
        LoaderConfig lastConfig = gson.fromJson(json, LoaderConfig.class);
        if (lastConfig != null) {
            mRetract = lastConfig.getRetract();
            mBgStyle = lastConfig.getBgStyle();
            mTextSize = lastConfig.getTextSize();
            mLineSpace = lastConfig.getLineSpace();
        }
    }

    private void saveConfig() {
        try {
            File file = new File(BookReader.getBookDir(), BOOK_CONFIG_FILE);
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation() //只序列化Expose注解的字段
                    .create();
            String json = gson.toJson(this);
            BrUtil.saveStringToFile(file, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void config(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getRetract() {
        return mRetract;
    }

    public void setRetract(int retract) {
        mRetract = retract;
        saveConfig();
    }

    public int getBgStyle() {
        return mBgStyle;
    }

    public void setBgStyle(int bgStyle) {
        mBgStyle = bgStyle;
        saveConfig();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        saveConfig();
    }

    public float getLineSpace() {
        return mLineSpace;
    }

    public void setLineSpace(float lineSpace) {
        mLineSpace = lineSpace;
        saveConfig();
    }

    public String retract(String text) {
        if (text != null && text.length() > 0) {
            if (mRetract == 1) {
                return text;
            } else {
                int start = 0;
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c <= ' ' || c == '　') {
                        start++;
                    } else {
                        break;
                    }
                }
                if (start < text.length()) {
                    return "　　" + text.substring(start);
                }
            }
        }
        return "";
    }

    public int getPageLines() {
        try {
            int ts = BrUtil.dp2px(mContext, mTextSize);
            return (int) ((mHeight + ts * mLineSpace) / (ts * (1 + mLineSpace)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 20;
    }

    public int getMinLineWords() {
        if (mWidth > 0 && mTextSize > 0)
            return mWidth / BrUtil.dp2px(mContext, mTextSize);
        return 20;
    }

    public boolean isLineSpill(String line) { //判断文字是否占满一行
        try {
            int minWords = getMinLineWords();
            if (line == null || line.length() < minWords)
                return false;
            return getPaint().measureText(line) > mWidth; //效率很低,耗时操作
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
