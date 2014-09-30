package com.harbinpointech.carcenter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.harbinpointech.carcenter.activity.ChatActivity;
import com.harbinpointech.carcenter.data.Message;
import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QueryInfosService extends Service {

    public static final String ACTION_NOTIFICATIONS_RECEIVED = "ACTION_NOTIFICATIONS_RECEIVED";
    public static final String EXTRA_CHAT_ARRAY = "EXTRA_CHAT_ARRAY";
    private Thread mQueryThread;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mQueryThread == null) {
            mQueryThread = new Thread("QUERY_LOOPER") {
                @Override
                public void run() {
                    JSONObject[] params = new JSONObject[1];
                    while (mQueryThread != null) {
                        try {
                            int result = WebHelper.recvMessage(params);
                            if (result == 0) {
                                JSONArray array = params[0].getJSONArray("d");
                                if (array.length() > 0) {
                                    CarSQLiteOpenHelper.insert(Message.TABLE, array);
                                    Intent i = new Intent(ACTION_NOTIFICATIONS_RECEIVED);
                                    i.putExtra(EXTRA_CHAT_ARRAY, array.toString());
                                    boolean bResult = LocalBroadcastManager.getInstance(QueryInfosService.this).sendBroadcast(i);
                                    if (!bResult) {
                                        JSONObject msg = (JSONObject) array.get(0);
                                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        Intent activityIntent = new Intent(QueryInfosService.this, ChatActivity.class);

                                        activityIntent.putExtra(ChatActivity.SENDER_ID, (int) msg.getInt(Message.SENDER));
                                        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                        PendingIntent pi = PendingIntent.getActivity(QueryInfosService.this, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(QueryInfosService.this)
                                                .setSmallIcon(R.drawable.icon_account)
                                                .setContentTitle(getString(R.string.app_name))
                                                .setContentText("接收到了新通知")
                                                .setSound(alarmSound)
                                                .setAutoCancel(true)
                                                .setContentIntent(pi);
                                        nm.notify(0, builder.build());
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            mQueryThread.start();
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Thread t = mQueryThread;
        if (t != null) {
            mQueryThread = null;
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
