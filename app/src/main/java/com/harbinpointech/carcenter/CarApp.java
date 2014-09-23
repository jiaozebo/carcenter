package com.harbinpointech.carcenter;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by John on 2014/9/23.
 */
public class CarApp extends Application {

    private static SQLiteDatabase sDB;

    @Override
    public void onCreate() {
        super.onCreate();
        CarSQLiteOpenHelper helper = new CarSQLiteOpenHelper(this);
        sDB = helper.getWritableDatabase();
    }

    public static SQLiteDatabase lockDataBase() {
        return sDB;
    }
}
