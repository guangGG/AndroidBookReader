package gapp.season.reader.loader;

import java.util.List;

import gapp.season.reader.model.BookPageLine;

public interface BookLoadListener {
    void onLoadPage(int code, List<BookPageLine> list, int paragraph, int paragraphLine);
}
