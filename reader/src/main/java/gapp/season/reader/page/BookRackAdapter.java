package gapp.season.reader.page;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gapp.season.reader.R;
import gapp.season.reader.loader.BookRackMgr;
import gapp.season.reader.util.BrUtil;

public class BookRackAdapter extends BaseAdapter {
    public static final int BOOK_TYPE_NORMAL = 0;
    public static final int BOOK_TYPE_CURRENT = 1;
    public static final int BOOK_TYPE_ADD = 2;

    protected Context mContext;
    private List<BookRackMgr.RackBook> mRackBooks;
    private String mCurrentBook;

    public BookRackAdapter(Context context) {
        mContext = context;
        setRackBooks(null);
    }

    public void setCurrentBook(String bookPath) {
        mCurrentBook = bookPath;
    }

    public void setRackBooks(List<BookRackMgr.RackBook> list) {
        List<BookRackMgr.RackBook> rackBooks = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (int i = list.size() - 1; i >= 0; i--) {
                rackBooks.add(list.get(i));
            }
        }
        rackBooks.add(new BookRackMgr.RackBook()); //末尾添加空book表示“添加书籍”按钮
        mRackBooks = rackBooks;
        notifyDataSetChanged();
    }

    // 0其它书籍，1当前书籍，2添加书籍
    public int getBookType(int position) {
        if (mRackBooks != null && mRackBooks.size() > position) {
            BookRackMgr.RackBook rb = mRackBooks.get(position);
            if (!TextUtils.isEmpty(mCurrentBook) && mCurrentBook.equals(rb.getBookPath())) {
                return BOOK_TYPE_CURRENT;
            }
            if (!TextUtils.isEmpty(rb.getBookPath())) {
                return BOOK_TYPE_NORMAL;
            } else {
                return BOOK_TYPE_ADD;
            }
        }
        return BOOK_TYPE_ADD;
    }

    public String getBookPath(int position) {
        if (mRackBooks != null && mRackBooks.size() > position) {
            BookRackMgr.RackBook rb = mRackBooks.get(position);
            return rb.getBookPath();
        }
        return "";
    }

    @Override
    public int getCount() {
        if (mRackBooks == null)
            return 0;
        return mRackBooks.size();
    }

    @Override
    public Object getItem(int position) {
        return mRackBooks.get(position);
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
            convertView = View.inflate(mContext, R.layout.br_item_bookrack, null);
            holder = new ViewHolder();
            holder.bookTips = convertView.findViewById(R.id.br_text_rack_tips);
            holder.bookCm = convertView.findViewById(R.id.br_text_rack_cm);
            holder.bookTitle = convertView.findViewById(R.id.br_text_rack);
            holder.removeBook = convertView.findViewById(R.id.br_text_rack_remove);
            convertView.setTag(holder);
        }
        final int type = getBookType(position);
        if (type == BOOK_TYPE_ADD) {
            holder.bookTips.setVisibility(View.INVISIBLE);
            holder.removeBook.setVisibility(View.INVISIBLE);
            holder.bookTitle.setText(R.string.br_add_book);
        } else {
            final String path = getBookPath(position);
            holder.bookTips.setVisibility(View.VISIBLE);
            holder.bookCm.setVisibility((type == BOOK_TYPE_CURRENT) ? View.VISIBLE : View.INVISIBLE);
            holder.removeBook.setVisibility(View.VISIBLE);
            holder.removeBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(mContext.getString(R.string.br_tips));
                    builder.setMessage(String.format(mContext.getString(R.string.br_remove_book_tips), BrUtil.getFileName(path)));
                    builder.setPositiveButton(mContext.getString(R.string.br_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BookRackMgr.removeBook(path);
                            if (type == BOOK_TYPE_CURRENT) {
                                if (mContext instanceof Activity) ((Activity) mContext).finish();
                            } else {
                                setRackBooks(BookRackMgr.getRackBooks());
                            }
                        }
                    });
                    builder.setNegativeButton(mContext.getString(R.string.br_cancel), null);
                    builder.show();
                }
            });
            holder.bookTitle.setText(BrUtil.getFileName(path));
        }
        return convertView;
    }

    class ViewHolder {
        View bookTips;
        View bookCm;
        TextView bookTitle;
        View removeBook;
    }
}
