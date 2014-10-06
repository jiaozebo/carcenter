package com.harbinpointech.carcenter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by John on 2014/9/30.
 */
public class Contacts implements BaseColumns {
    /**
     *  "__type": "ServiceMessage:#WcfService.Entity",
     "Message1": null,
     "SendID": null,
     "MessageGroupID": "2",
     "ReceiveID": null,
     "MessageGroup": "三个群",
     "MessageCount": null,
     "IsGroupMessage": null,
     "SendTime": null,
     "ID": null,
     "ReceiveTime": null
     *
     *
     */
    public static final String TABLE = "Contacts";
    public static final String ID = "ID";
    public static final String NAME = "Name";
    public static final String FATHER_NAME = "FatherName";
    public static final String PASSWORD = "Password";
    public static final String FRIEND = "FRIEND";

    public static void createTable(SQLiteDatabase db) {
        String sql = String
                .format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s tinyint(1) default 1, " +
                                "%s VARCHAR(10), " +
                                "%s VARCHAR(10), " +
                                "%s VARCHAR(10))", TABLE, BaseColumns._ID,
                        ID,
                        FRIEND,
                        NAME,
                        PASSWORD,
                        FATHER_NAME);
        db.execSQL(sql);
    }
}
