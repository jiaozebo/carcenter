package com.harbinpointech.carcenter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by John on 2014/9/23.
 */
public class Message implements BaseColumns {

    /*
    *
    * "__type": "ServiceMessage:#WcfService.Entity",
            "Message1": "??",
            "SendID": "75",
            "MessageGroupID": null,
            "ReceiveID": "1",
            "MessageGroup": null,
            "MessageCount": null,
            "SendTime": "2014/10/1 11:42:14",
            "ID": "57",
            "ReceiveTime": null
    *
    * */
    public static final String TABLE = "MSG";
    public static final String CONTENT = "Message1";
    public static final String SENDER = "SendID";
    public static final String GROUPID = "MessageGroupID";
    public static final String RECEIVER = "ReceiveID";
    public static final String GROUP = "MessageGroup";
    public static final String MESSAGE_COUNT = "MessageCount";
    public static final String DATETIME = "SendTime";
    public static final String ID = "ID";
    public static final String RECEIVE_TIME = "ReceiveTime";
    /**
     * -1表示正在发送
     */
    public static final String STATE = "STATE";


    public static void createTable(SQLiteDatabase db) {
        String sql = String
                .format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s VARCHAR(10), " +
                                "%s VARCHAR(50), " +
                                "%s VARCHAR(50), " +
                                "%s VARCHAR(1024), " +
                                "%s datetime not null," +
                                "%s datetime," +
                                "%s tinyint(1) default 0)", TABLE, BaseColumns._ID,
                        SENDER,
                        RECEIVER,
                        ID,
                        GROUPID,
                        GROUP,
                        MESSAGE_COUNT,
                        CONTENT,
                        DATETIME,
                        RECEIVE_TIME,
                        STATE);
        db.execSQL(sql);
    }
}
