package com.harbinpointech.carcenter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.harbinpointech.carcenter.data.Message;

/**
 * Created by John on 2014/9/23.
 */
public class CarSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "carcenter.db";
    private static final int VERSION = 1;

    public CarSQLiteOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Message.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
