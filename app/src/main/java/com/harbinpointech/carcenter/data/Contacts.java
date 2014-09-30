package com.harbinpointech.carcenter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by John on 2014/9/30.
 */
public class Contacts implements BaseColumns {
    public static final String TABLE = "Contacts";
    public static final String ID = "ID";
    public static final String NAME = "Name";
    public static final String FATHER_NAME = "FatherName";
    public static final String PASSWORD = "Password";

    public static void createTable(SQLiteDatabase db) {
        String sql = String
                .format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s VARCHAR(10), " +
                                "%s VARCHAR(10), " +
                                "%s VARCHAR(10))", TABLE, BaseColumns._ID,
                        ID,
                        NAME,
                        PASSWORD,
                        FATHER_NAME);
        db.execSQL(sql);
    }
}
