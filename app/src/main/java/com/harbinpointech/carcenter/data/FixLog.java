package com.harbinpointech.carcenter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by John on 2014/9/30.
 */
public class FixLog implements BaseColumns {

    public static final String TABLE = "FixLog";
    public static final String ID = "Id";
    public static final String Faultreason = "Faultreason";
    public static final String Time = "Time";
    public static final String Method = "Method";
    public static final String Repairemployee = "Repairemployee";
    public static final String Remark = "Remark";
    public static final String CarName = "CarName";
    public static final String Summary = "Summary";


    public static void createTable(SQLiteDatabase db) {
        String sql = String
                .format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s VARCHAR(50), " +
                                "%s VARCHAR(100), " +
                                "%s VARCHAR(100), " +
                                "%s VARCHAR(100), " +
                                "%s VARCHAR(10), " +
                                "%s VARCHAR(100), " +
                                "%s datetime)", TABLE, BaseColumns._ID,
                        ID,
                        Repairemployee,
                        Faultreason,
                        Method,
                        Remark,
                        CarName,
                        Summary,
                        Time);
        db.execSQL(sql);
    }
}
