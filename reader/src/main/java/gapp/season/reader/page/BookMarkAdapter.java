package gapp.season.reader.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import gapp.season.reader.R;
import gapp.season.reader.loader.BookMarkMgr;

public class BookMarkAdapter extends BaseAdapter {
    private Context mContext;
    private DecimalFormat mPercentFormat;
    private SimpleDateFormat mDateFormat;
    private List<BookMarkMgr.BookMark> mBookMarks;

    public BookMarkAdapter(Context context) {
        mContext = context;
    }

    public void setBookMarks(List<BookMarkMgr.BookMark> bookMarks) {
        mBookMarks = bookMarks;
        notifyDataSetChanged();
    }

    private DecimalFormat getPercentDf() {
        if (mPercentFormat == null)
            mPercentFormat = new DecimalFormat("0.##%");
        return mPercentFormat;
    }

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat getDateFormat() {
        if (mDateFormat == null)
            mDateFormat = new SimpleDateFormat("yyyy-M-d H:mm:ss");
        return mDateFormat;
    }

    @Override
    public int getCount() {
        if (mBookMarks == null)
            return 0;
        return mBookMarks.size();
    }

    @Override
    public Object getItem(int position) {
        return mBookMarks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(mContext, R.layout.br_item_bookmark, null);
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.br_text_mark);
            holder.progress = convertView.findViewById(R.id.br_mark_progress);
            holder.time = convertView.findViewById(R.id.br_mark_time);
            convertView.setTag(holder);
        }
        BookMarkMgr.BookMark bookMark = mBookMarks.get(position);
        holder.text.setText(bookMark.getText());
        holder.progress.setText(getPercentDf().format(bookMark.getProgress()));
        if (bookMark.isChapter()) {
            holder.time.setText(String.format(mContext.getString(R.string.br_chapter_num), (position + 1)));
        } else {
            holder.time.setText(getDateFormat().format(bookMark.getTime()));
        }
        return convertView;
    }

    class ViewHolder {
        TextView text;
        TextView progress;
        TextView time;
    }
}
