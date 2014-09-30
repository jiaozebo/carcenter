package com.harbinpointech.carcenter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.harbinpointech.carcenter.data.Contacts;
import com.harbinpointech.carcenter.data.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by John on 2014/9/23.
 */
public class CarSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "carcenter.db";
    private static final int VERSION = 3;

    public CarSQLiteOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Message.createTable(db);
        Contacts.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("drop table if exists '%s'", Contacts.TABLE));
        db.execSQL(String.format("drop table if exists '%s'", Message.TABLE));
        onCreate(db);
    }

    public static void insert(String table, JSONArray array) throws JSONException {
        SQLiteDatabase db = CarApp.lockDataBase();
        Cursor c = null;
        ContentValues cv = new ContentValues();
        try {
            db.beginTransaction();
            for (int i = 0; i < array.length(); i++) {
                JSONObject chat = array.getJSONObject(i);
                cv.clear();
                chat.remove("__type");

                json2contentValue(chat, cv);
                Integer index = cv.getAsInteger(Contacts.ID);
                if (index != null) {
                    c = db.query(table, new String[]{BaseColumns._ID}, Contacts.ID + " = ?", new String[]{String.valueOf(index)}, null, null, null);
                    if (c.moveToFirst()) {
                        db.update(table, cv, BaseColumns._ID + "=?", new String[]{c.getString(0)});
                        continue;
                    }
                }
                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
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
