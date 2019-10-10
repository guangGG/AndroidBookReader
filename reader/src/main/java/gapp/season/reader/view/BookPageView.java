package gapp.season.reader.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

import gapp.season.reader.loader.BookMarkMgr;
import gapp.season.reader.loader.LoaderConfig;
import gapp.season.reader.model.BookPageLine;
import gapp.season.reader.util.BrUtil;

public class BookPageView extends View {
    private GestureDetector mGestureDetector;
    private OnDetectorListener mDetectorListener;

    private List<BookPageLine> mPageLines;

    public BookPageView(Context context) {
        this(context, null);
    }

    public BookPageView(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private final int FLING_MIN_DISTANCE = 60; //px
            private final int FLING_MIN_VELOCITY = 90; // px/s

            @Override
            public boolean onDown(MotionEvent e) {
                return true; //返回true时才会回调onScroll/onFling事件
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mDetectorListener != null) {
                    mDetectorListener.onDetector(1, e);
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mDetectorListener != null) {
                    mDetectorListener.onDetector(2, e);
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // 触发条件 ：X轴的坐标位移大于m个像素，且X轴移动速度大于n个像素/秒，Y轴的坐标位移*1.5小于X轴的坐标位移
                if (mDetectorListener != null) {
                    if (Math.abs(e1.getY() - e2.getY()) * 1.5 < Math.abs(e1.getX() - e2.getX())) {
                        if (Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
                                // 向左侧滑动
                                mDetectorListener.onDetector(3, e2);
                                return true;
                            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE) {
                                // 向右侧滑动
                                mDetectorListener.onDetector(4, e2);
                                return true;
                            }
                        }
                    } else if (Math.abs(e1.getY() - e2.getY()) > Math.abs(e1.getX() - e2.getX()) * 1.5) {
                        if (Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                            if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE) {
                                // 向上侧滑动
                                mDetectorListener.onDetector(5, e2);
                                return true;
                            } else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE) {
                                // 向下侧滑动
                                mDetectorListener.onDetector(6, e2);
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector != null) {
                    return mGestureDetector.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    public void setDetectorListener(OnDetectorListener detectorListener) {
        mDetectorListener = detectorListener;
    }

    public void setPageLines(List<BookPageLine> pageLines) {
        mPageLines = pageLines;
        postInvalidate();
    }

    public List<BookPageLine> getPageLines() {
        return mPageLines;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        if (mPageLines != null) {
            int lineId = -1;
            for (BookPageLine bpl : mPageLines) {
                if (bpl != null && bpl.getText() != null) {
                    if (lineId >= 0 && bpl.getLineId() != lineId) {
                        sb.append("\n");
                    }
                    sb.append(bpl.getText());
                    lineId = bpl.getLineId();
                }
            }
        }
        return sb.toString();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = LoaderConfig.getInstanse().getPaint();
        int ts = BrUtil.dp2px(getContext(), LoaderConfig.getInstanse().getTextSize());
        float ls = ts * LoaderConfig.getInstanse().getLineSpace();
        if (mPageLines != null) {
            for (int i = 0; i < mPageLines.size(); i++) {
                BookPageLine line = mPageLines.get(i);
                if (line != null && line.getText() != null) {
                    boolean isMarkLine = BookMarkMgr.isMarkLine(line);
                    if (isMarkLine) { //标签行背景标记
                        paint.setColor(0x33dd4f42);
                        float top = i * (ts + ls);
                        canvas.drawRect(0, top, getWidth(), top + ts * 1.2f, paint);
                    }
                    paint.setColor(isMarkLine ? 0xff0088ff : 0xff333333);
                    canvas.drawText(line.getText(), 0, (i + 1) * (ts + ls) - ls, paint);
                }
            }
        }
    }

    public interface OnDetectorListener {
        /**
         * @param type 1点击，2长按，3向左滑，4向右滑，5向上滑，向下滑
         * @param e    操作的Event
         */
        void onDetector(int type, MotionEvent e);
    }
}
