package gapp.season.reader.demo;

import android.app.Application;

import java.io.File;

import gapp.season.reader.BookReader;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        File externalFilesDir = getExternalFilesDir(null);
        BookReader.config(BuildConfig.DEBUG, 0, (externalFilesDir == null) ?
                null : externalFilesDir.getAbsolutePath());
    }
}
