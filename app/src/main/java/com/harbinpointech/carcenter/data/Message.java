package com.harbinpointech.carcenter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by John on 2014/9/23.
 */
public class Message implements BaseColumns {



    //    public static final String MSG_ADD_FRIEND = "[][][][]";
    /**
     * 表示请求加某人为好友
     */
    public static final String MSG_ADD_FRIEND = "MSG_ADD_FRIEND";
    /**
     * 表示接收某人的好友请求
     */
    public static final String MSG_ADD_FRIEND_ACCEPT = "MSG_ADD_FRIEND_ACCEPT";
    /**
     * 表示拒绝某人的好友请求
     */
    public static final String MSG_ADD_FRIEND_REJECT = "MSG_ADD_FRIEND_REJECT";
    
    /**
     * 表示收到了服务器推送的车辆故障信息
     */
    public static final String MSG_SERVER_PUSH_ERROR = "MSG_SERVER_PUSH_ERROR";

	/**
     * 表示收到了服务器推送的任务信息
     * 
     */
    public static final String MSG_SERVER_PUSH_TASK = "MSG_SERVER_PUSH_TASK";

    /*


    "__type": "ServiceMessage:#WcfService.Entity",
            "Message1": "就开导开导",
            "SendID": "1",
            "MessageGroupID": null,
            "ReceiveID": "75",
            "MessageGroup": null,
            "MessageCount": null,
            "IsGroupMessage": "N",
            "SendTime": "2014/10/5 23:43:56",
            "ID": "109",
            "ReceiveTime": null
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
    public static final String ISGROUP = "IsGroupMessage";
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
                        ISGROUP,
                        GROUP,
                        MESSAGE_COUNT,
                        CONTENT,
                        DATETIME,
                        RECEIVE_TIME,
                        STATE);
        db.execSQL(sql);
    }
}
