package com.harbinpointech.carcenter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by John on 2014/9/23.
 */
public class Message implements BaseColumns {
    public static final String TABLE = "MSG";
    public static final String CONTENT = "CONTENT";
    public static final String SENDER = "SENDER";
    public static final String RECEIVER = "RECEIVER";
    public static final String DATETIME = "DATETIME";
    public static final String STATE = "STATE";


    public static void createTable(SQLiteDatabase db) {
        String sql = String
                .format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s VARCHAR(1024), " +
                                "%s datetime not null," +
                                "%s INTEGER default 0)", TABLE, BaseColumns._ID,
                        SENDER,
                        RECEIVER,
                        CONTENT,
                        DATETIME,
                        STATE);
        db.execSQL(sql);
    }
}
