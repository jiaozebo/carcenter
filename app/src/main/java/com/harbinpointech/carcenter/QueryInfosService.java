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
import com.harbinpointech.carcenter.activity.MainActivity;
import com.harbinpointech.carcenter.data.Message;
import com.harbinpointech.carcenter.data.WebHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QueryInfosService extends Service {

    public static final String ACTION_NOTIFICATIONS_RECEIVED = "ACTION_NOTIFICATIONS_RECEIVED";
    /**
     * 表示接收到了添加好友响应
     */
    public static final String ACTION_REQUEST_FRIEND_ANSWERED = "ACTION_REQUEST_FRIEND_ANSWERED";
    public static final String EXTRA_CHAT_ARRAY = "EXTRA_CHAT_ARRAY";
    /**
     * 表示接收到了添加好友请求
     */
    public static final String ACTION_REQUEST_FRIEND = "ACTION_REQUEST_FRIEND";

    public static final String EXTRA_REQUEST_FRIEND_ANSWERED_USER_ID = "EXTRA_REQUEST_FRIEND_ANSWERED_USER_ID";
    public static final String EXTRA_REQUEST_FRIEND_ANSWERED_USER_NAME = "EXTRA_REQUEST_FRIEND_ANSWERED_USER_NAME";
    public static final String EXTRA_REQUEST_FRIEND_ANSWERED_ACCEPTED = "EXTRA_REQUEST_FRIEND_ANSWERED_ACCEPTED";
    public static final String EXTRA_REQUEST_FRIEND_USER_ID = "EXTRA_REQUEST_FRIEND_USER";
    public static final String EXTRA_REQUEST_FRIEND_USER_NAME = "EXTRA_REQUEST_FRIEND_USER_NAME";

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
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject message = array.getJSONObject(i);
                                    String content = message.getString(Message.CONTENT);
                                    String user = message.getString(Message.SENDER);
                                    if (content.indexOf(Message.MSG_ADD_FRIEND_ACCEPT) == 0 || content.indexOf(Message.MSG_ADD_FRIEND_REJECT) == 0) {
                                        boolean accept = content.indexOf(Message.MSG_ADD_FRIEND_ACCEPT) == 0;
                                        array.put(i, null);
                                        Intent intent = new Intent(ACTION_REQUEST_FRIEND_ANSWERED);
                                        intent.putExtra(EXTRA_REQUEST_FRIEND_ANSWERED_USER_ID, user);
                                        String name = content.substring(accept ? Message.MSG_ADD_FRIEND_ACCEPT.length() : Message.MSG_ADD_FRIEND_REJECT.length());
                                        intent.putExtra(EXTRA_REQUEST_FRIEND_ANSWERED_USER_NAME, name);
                                        intent.putExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_ACCEPTED, accept);
                                        boolean bResult = LocalBroadcastManager.getInstance(QueryInfosService.this).sendBroadcast(intent);
                                        if (!bResult) {
                                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                            Intent activityIntent = new Intent(QueryInfosService.this, MainActivity.class);

                                            activityIntent.putExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_USER_ID, user);
                                            activityIntent.putExtra(EXTRA_REQUEST_FRIEND_ANSWERED_USER_NAME, name);
                                            activityIntent.putExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_ACCEPTED, Message.MSG_ADD_FRIEND_ACCEPT.equals(content));
                                            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                            PendingIntent pi = PendingIntent.getActivity(QueryInfosService.this, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(QueryInfosService.this)
                                                    .setSmallIcon(R.drawable.icon_account)
                                                    .setContentTitle(getString(R.string.app_name))
                                                    .setContentText(String.format("%s %s了您的请求", user, accept ? "接受" : "拒绝"))
                                                    .setSound(alarmSound)
                                                    .setAutoCancel(true)
                                                    .setContentIntent(pi);
                                            nm.notify(0, builder.build());
                                        }
                                    } else if (content.indexOf(Message.MSG_ADD_FRIEND) == 0) {
                                        array.put(i, null);
                                        Intent intent = new Intent(ACTION_REQUEST_FRIEND);
                                        String name = content.substring(Message.MSG_ADD_FRIEND.length());
                                        intent.putExtra(EXTRA_REQUEST_FRIEND_USER_NAME, name);
                                        intent.putExtra(EXTRA_REQUEST_FRIEND_USER_ID, user);

                                        boolean bResult = LocalBroadcastManager.getInstance(QueryInfosService.this).sendBroadcast(intent);
                                        if (!bResult) {
                                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                            Intent activityIntent = new Intent(QueryInfosService.this, MainActivity.class);

                                            activityIntent.putExtra(QueryInfosService.EXTRA_REQUEST_FRIEND_USER_ID, user);
                                            activityIntent.putExtra(EXTRA_REQUEST_FRIEND_USER_NAME, name);
                                            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                            PendingIntent pi = PendingIntent.getActivity(QueryInfosService.this, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(QueryInfosService.this)
                                                    .setSmallIcon(R.drawable.icon_account)
                                                    .setContentTitle(getString(R.string.app_name))
                                                    .setContentText(String.format("%s请求添加您为好友", name))
                                                    .setSound(alarmSound)
                                                    .setAutoCancel(true)
                                                    .setContentIntent(pi);
                                            nm.notify(0, builder.build());
                                        }
                                    }
                                }
                                int size = CarSQLiteOpenHelper.insert(Message.TABLE, array);
                                if (size > 0) {
                                    Intent i = new Intent(ACTION_NOTIFICATIONS_RECEIVED);
                                    i.putExtra(EXTRA_CHAT_ARRAY, array.toString());
                                    boolean bResult = LocalBroadcastManager.getInstance(QueryInfosService.this).sendBroadcast(i);
                                    if (!bResult) {
                                        JSONObject msg = (JSONObject) array.get(0);
                                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        Intent activityIntent = new Intent(QueryInfosService.this, ChatActivity.class);

                                        activityIntent.putExtra(ChatActivity.SENDER_ID, msg.getString(Message.SENDER));
                                        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                        PendingIntent pi = PendingIntent.getActivity(QueryInfosService.this, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(QueryInfosService.this)
                                                .setSmallIcon(R.drawable.icon_account)
                                                .setContentTitle(getString(R.string.app_name))
                                                .setContentText("接收到了新消息")
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
