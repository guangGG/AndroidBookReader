package gapp.season.reader.model;

import com.google.gson.Gson;

import androidx.annotation.NonNull;

public class BookModel {
    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
