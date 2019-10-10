package gapp.season.reader.page;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import gapp.season.reader.R;
import gapp.season.reader.model.BookSearchItem;
import gapp.season.reader.util.BrUtil;

public class BookSearchAdapter extends BaseAdapter {
    private Context mContext;
    private DecimalFormat mPercentFormat;
    private List<BookSearchItem> mList;

    public BookSearchAdapter(Context context) {
        mContext = context;
    }

    public void setList(List<BookSearchItem> list) {
        mList = list;
        notifyDataSetChanged();
    }

    private DecimalFormat getPercentDf() {
        if (mPercentFormat == null)
            mPercentFormat = new DecimalFormat("0.##%");
        return mPercentFormat;
    }

    @Override
    public int getCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
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
        BookSearchItem item = mList.get(position);
        holder.text.setText(BrUtil.highLightKeyWord(item.getText(), item.getWord(), 0xffff3333));
        holder.progress.setText(getPercentDf().format(item.getProgress()));
        holder.time.setText(String.format(mContext.getString(R.string.br_chapter_num), (position + 1)));
        return convertView;
    }

    class ViewHolder {
        TextView text;
        TextView progress;
        TextView time;
    }
}
