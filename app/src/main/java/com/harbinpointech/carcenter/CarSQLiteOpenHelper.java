package com.harbinpointech.carcenter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.harbinpointech.carcenter.data.Contacts;
import com.harbinpointech.carcenter.data.FixLog;
import com.harbinpointech.carcenter.data.Group;
import com.harbinpointech.carcenter.data.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by John on 2014/9/23.
 */
public class CarSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "carcenter.db";
    private static final int VERSION = 12;

    public CarSQLiteOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Message.createTable(db);
        Contacts.createTable(db);
        Group.createTable(db);
        FixLog.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("drop table if exists '%s'", Contacts.TABLE));
        db.execSQL(String.format("drop table if exists '%s'", Message.TABLE));
        db.execSQL(String.format("drop table if exists '%s'", Group.TABLE));
        db.execSQL(String.format("drop table if exists '%s'", FixLog.TABLE));
        onCreate(db);
    }

    public static int insert(String table, JSONArray array) throws JSONException {
        SQLiteDatabase db = CarApp.lockDataBase();
        Cursor c = null;
        ContentValues cv = new ContentValues();
        try {
            db.beginTransaction();
            int count = 0;
            A:
            for (int i = 0; i < array.length(); i++) {
                JSONObject chat = array.getJSONObject(i);
                if (chat == null) {
                    continue;
                }
                cv.clear();
                chat.remove("__type");

                json2contentValue(chat, cv);
                if (table.equals(Contacts.TABLE) || table.equals(Message.TABLE)) {
                    if (cv.containsKey(Contacts.ID)) {
                        Integer index = cv.getAsInteger(Contacts.ID);
                        if (index != null) {
                            c = db.query(table, new String[]{BaseColumns._ID}, Contacts.ID + " = ?", new String[]{String.valueOf(index)}, null, null, null);
                            if (c.moveToFirst()) {
                                db.update(table, cv, BaseColumns._ID + "=?", new String[]{c.getString(0)});
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                } else if (table.equals(Group.TABLE)) {
                    Iterator<String> it = cv.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        if (key.equals(Group.ID)) {
                            Integer index = cv.getAsInteger(key);
                            if (index != null) {
                                // id 合法的才留着
                                continue;
                            } else {
                                continue A;
                            }
                        } else if (key.equals(Group.NAME)) {
                            continue;
                        }
                        it.remove();
                    }
                    c = db.query(table, new String[]{BaseColumns._ID}, Group.ID + " = ?", new String[]{String.valueOf(cv.getAsInteger(Group.ID))}, null, null, null);
                    if (c.moveToFirst()) {
                        db.update(table, cv, BaseColumns._ID + "=?", new String[]{c.getString(0)});
                        continue;
                    }
                }
                db.insertOrThrow(table, null, cv);
                ++count;
            }
            db.setTransactionSuccessful();
            return count;
        } finally {
            db.endTransaction();
        }

    }

    public static final void json2contentValue(JSONObject json, ContentValues values) throws JSONException {
        Iterator<String> it = json.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object obj = json.get(key);
            if (obj instanceof String) {
                if (Pattern.matches("\\d+/\\d+/\\d+ \\d+:\\d+:\\d+", (CharSequence) obj)) {
//                    Pattern p = Pattern.compile("\\d+");
//                    Matcher m = p.matcher((CharSequence) obj);
//                    obj = String.format("%04d-%02d-%02d %02d:%02d:%02d",m.group(0), m.group(1),m.group(2),m.group(3),m.group(4),m.group(5));

                    Pattern p = Pattern.compile("\\d+");
                    Matcher m = p.matcher((CharSequence) obj);
                    int[] iargs = new int[6];
                    int i = 0;
                    while (m.find()) {
                        iargs[i++] = Integer.parseInt(m.group());
                    }
                    obj = String.format("%04d-%02d-%02d %02d:%02d:%02d", iargs[0], iargs[1], iargs[2], iargs[3], iargs[4], iargs[5]);
                }
                values.put(key, (String) obj);
            } else if (obj instanceof Integer) {
                values.put(key, (Integer) obj);
            } else if (obj instanceof Float) {
                values.put(key, (Float) obj);
            } else {
                values.put(key, obj.toString());
            }
        }
    }
}
