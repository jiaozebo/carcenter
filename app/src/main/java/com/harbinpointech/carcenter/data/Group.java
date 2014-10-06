package com.harbinpointech.carcenter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by John on 2014/10/5.
 */
public class Group implements BaseColumns {

    /**
     * "__type": "ServiceMessage:#WcfService.Entity",
     * "Message1": null,
     * "SendID": null,
     * "MessageGroupID": "2",
     * "ReceiveID": null,
     * "MessageGroup": "三个群",
     * "MessageCount": null,
     * "IsGroupMessage": null,
     * "SendTime": null,
     * "ID": null,
     * "ReceiveTime": null
     */
    public static final String TABLE = "GroupT";
    public static final String ID = "MessageGroupID";
    public static final String NAME = "MessageGroup";

    public static void createTable(SQLiteDatabase db) {
        String sql = String
                .format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s VARCHAR(10))", TABLE, BaseColumns._ID,
                        ID,
                        NAME);
        db.execSQL(sql);
    }
}
