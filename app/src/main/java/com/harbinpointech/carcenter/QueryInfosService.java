package com.harbinpointech.carcenter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

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


    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_CHECK_UPDATE = "com.harbinpointech.carcenter.ACTION_CHECK_UPDATE";
    public static final String ACTION_START_DOWNLOAD = "com.harbinpointech.carcenter.ACTION_START_DOWNLOAD";
    private static final String ACTION_CANCEL_NOTIFY = "com.harbinpointech.carcenter.ACTION_CANCEL_NOTIFY";
    public static final String EXTRA_NEWEST_VERSION = "com.harbinpointech.carcenter.extra.NEWEST_VERSION";
    public static final String EXTRA_DOWNLOAD_URL = "com.harbinpointech.carcenter.extra.DOWNLOAD_URL";
    public static final String EXTRA_UPDATE_LOG = "com.harbinpointech.carcenter.extra.UPDATE_LOG";


    public static final String EXTRA_SESSION = "EXTRA_SESSION";
    /**
     * 表示服务器下发任务
     */
    public static final String EXTRA_SERVER_PUSH_TASK = "EXTRA_SERVER_PUSH_TASK";
    public static final String EXTRA_SERVER_PUSH_ERROR = "EXTRA_SERVER_PUSH_ERROR";
    public static final String EXTRA_SERVER_PUSH_VERSION = "EXTRA_SERVER_PUSH_VERSION";
    private static final String TAG = "IM";
    public static final String EXTRA_NOTIFY_ID = "EXTRA_NOTIFY_ID";
    public static final String EXTRA_VEHICLE_ID = "extra_vehicle";
    public static final String EXTRA_VEHICLE_STATUS = "extra_vehicle_status";
    public static final String EXTRA_LNG = "extra_lng";
    public static final String EXTRA_LAT = "extra_lat";
    private Thread mQueryThread;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mQueryThread == null) {
            String session = intent.getStringExtra(EXTRA_SESSION);
            WebHelper.setSession(session);
            mQueryThread = new Thread("QUERY_LOOPER") {
                @Override
                public void run() {
                    JSONObject[] params = new JSONObject[1];
                    boolean test = true;
                    while (mQueryThread != null) {
                        try {
                            int result = WebHelper.recvMessage(params);
                            if (result == 0) {
                                JSONArray array = params[0].getJSONArray("d");
                                if (test) {
                                    test = false;
                                    String JSON = "{\n" +
                                            "            \"__type\": \"ServiceMessage:#WcfService.Entity\",\n" +
                                            "            \"Message1\": \"MSG_SERVER_PUSH_TASK {\\\"task\\\":\\\"这是一个任务\\\"}\",\n" +
                                            "            \"SendID\": \"1\",\n" +
                                            "            \"MessageGroupID\": null,\n" +
                                            "            \"ReceiveID\": \"1\",\n" +
                                            "            \"MessageGroup\": null,\n" +
                                            "            \"MessageCount\": null,\n" +
                                            "            \"IsGroupMessage\": \"N\",\n" +
                                            "            \"SendTime\": \"2014/11/27 23:55:15\",\n" +
                                            "            \"ID\": \"219\",\n" +
                                            "            \"ReceiveTime\": null\n" +
                                            "        }";
                                    array = new JSONArray(String.format("[%s]", JSON));
                                }
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
                                            activityIntent.putExtra(MainActivity.EXTRA_ACTION_FLAG, QueryInfosService.EXTRA_REQUEST_FRIEND_ANSWERED_USER_ID);
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

                                            activityIntent.putExtra(MainActivity.EXTRA_ACTION_FLAG, QueryInfosService.EXTRA_REQUEST_FRIEND_USER_ID);
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
                                    } else if (content.indexOf(Message.MSG_SERVER_PUSH_VERSION) == 0) {   // 服务器推送新版本通知
                                        array.put(i, null);
                                        String versionInfo = content.substring(Message.MSG_SERVER_PUSH_VERSION.length());
                                        versionInfo = versionInfo.trim();
//                                        {“version”:”2.0”,”log”:”修改了界面”,”download_url”:”http://www.xxxxxx.apk”}
                                        JSONObject updateJson = null;
                                        try {
                                            updateJson = new JSONObject(versionInfo);
                                            helpNotify(QueryInfosService.this, updateJson.getString("version"), updateJson.getString("log"), updateJson.getString("download_url"));
                                        } catch (JSONException e) {
                                            Log.e(TAG, String.format("received invalid updateinfo:%s", versionInfo));
                                            e.printStackTrace();
                                        }
                                    } else if (content.indexOf(Message.MSG_SERVER_PUSH_ERROR) == 0) {   // 服务器推送鼓掌
                                        array.put(i, null);
                                        String errorInfo = content.substring(Message.MSG_SERVER_PUSH_ERROR.length());
                                        errorInfo = errorInfo.trim();

                                        try {
//                                            \"vehicle\":\"黑A12345\"，\"status\":\"2\"}
                                            JSONObject errorJson = new JSONObject(errorInfo);
                                            double[] ll = new double[]{errorJson.getDouble("Lat"), errorJson.getDouble("Lng")};
                                            WebHelper.gps2lnglat(ll);
                                            helpNotifyError(QueryInfosService.this, errorJson.getString("vehicle"), errorJson.getInt("status"), ll[0], ll[1]);
                                        } catch (JSONException e) {
                                            Log.e(TAG, String.format("received invalid updateinfo:%s", errorInfo));
                                            e.printStackTrace();
                                        }
                                    } else if (content.indexOf(Message.MSG_SERVER_PUSH_TASK) == 0) {   // 服务器推送任务
                                        array.put(i, null);
                                        String task = content.substring(Message.MSG_SERVER_PUSH_TASK.length());
                                        task = task.trim();
                                        try {
//                                            \"vehicle\":\"黑A12345\"，\"status\":\"2\"}
                                            JSONObject taskJson = new JSONObject(task);
                                            helpNotifyTask(QueryInfosService.this, taskJson.getString("task"));
                                        } catch (JSONException e) {
                                            Log.e(TAG, String.format("received invalid updateinfo:%s", task));
                                            e.printStackTrace();
                                        }

                                    }
                                }
                                int size = CarSQLiteOpenHelper.insert(Message.TABLE, array);
                                if (size > 0) {
                                    Intent i = new Intent(ACTION_NOTIFICATIONS_RECEIVED);
                                    i.putExtra(EXTRA_CHAT_ARRAY, array.toString());
                                    boolean bResult = LocalBroadcastManager.getInstance(QueryInfosService.this).sendBroadcast(i);
                                    boolean notify = PreferenceManager.getDefaultSharedPreferences(QueryInfosService.this).getBoolean("notifications_new_message", true);
                                    if (!bResult && notify) {
                                        boolean sound = PreferenceManager.getDefaultSharedPreferences(QueryInfosService.this).getBoolean("notifications_new_message_sound", true);
                                        boolean vibrate = PreferenceManager.getDefaultSharedPreferences(QueryInfosService.this).getBoolean("notifications_new_message_vibrate", false);
                                        JSONObject msg = (JSONObject) array.get(0);
                                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        Intent activityIntent = new Intent(QueryInfosService.this, ChatActivity.class);

                                        activityIntent.putExtra(ChatActivity.SENDER_ID, msg.getString(Message.SENDER));
                                        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                        int defaults = 0;
                                        if (sound) defaults |= NotificationCompat.DEFAULT_SOUND;
                                        if (vibrate) defaults |= NotificationCompat.DEFAULT_VIBRATE;
                                        PendingIntent pi = PendingIntent.getActivity(QueryInfosService.this, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(QueryInfosService.this)
                                                .setSmallIcon(R.drawable.icon_account)
                                                .setContentTitle(getString(R.string.app_name))
                                                .setContentText("接收到了新消息")
                                                .setDefaults(defaults)
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
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            mQueryThread.start();
        }
        return START_REDELIVER_INTENT;
    }

    private static void helpNotifyTask(Context c, String task) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent activityIntent = new Intent(c, MainActivity.class);

        activityIntent.putExtra(MainActivity.EXTRA_ACTION_FLAG, EXTRA_SERVER_PUSH_TASK);
        activityIntent.putExtra(EXTRA_SERVER_PUSH_TASK, task);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(c, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.abc_ic_go)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText("服务器下发任务")
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setContentIntent(pi);
        nm.notify(0, builder.build());
    }

    private static void helpNotifyError(Context c, String vehicle, int status, double lat, double lng) {
        Intent intent;// notification...
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        intent = new Intent(c, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_ACTION_FLAG, EXTRA_SERVER_PUSH_ERROR);

        intent.putExtra(EXTRA_VEHICLE_ID, vehicle);
        intent.putExtra(EXTRA_VEHICLE_STATUS, status);
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_LNG, lng);

        PendingIntent pi = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText(String.format("车辆故障:%s", vehicle))
                .setSound(alarmSound)
                .setAutoCancel(false)
                .setContentIntent(pi);
        nm.notify(NOTIFY_ID, builder.build());
    }


    private static final int NOTIFY_ID = 0x1306;


    /**
     * 创建并发动一个通知，提示用户有版本可升级。
     *
     * @param c
     * @param newestVer
     * @param log
     * @param downloadUrl
     */
    public static void helpNotify(Context c, String newestVer, String log, String downloadUrl) {
        Intent intent;// notification...
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        intent = new Intent(c, DownloadService.class);

        intent.putExtra(EXTRA_NOTIFY_ID, NOTIFY_ID);
        intent.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl);
        intent.putExtra(EXTRA_NEWEST_VERSION, newestVer);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra(EXTRA_NEWEST_VERSION, newestVer);
        intent.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl);


        PendingIntent download_action = PendingIntent.getService(c, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.ic_launcher)
                .addAction(R.drawable.ic_stat_accept, "下载并更新", download_action)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText(String.format("检测到新版本:%s", newestVer))
                .setSound(alarmSound)
                .setAutoCancel(false)
                .setContentIntent(download_action);
        String[] logs = log.split("\n");
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(String.format("检测到新版本:%s", newestVer));
        boolean hasLine = false;
        for (int i = 0; i < logs.length; i++) {
            if (TextUtils.isEmpty(logs[i])) {
                continue;
            }
            hasLine = true;
            inboxStyle.addLine(logs[i]);
        }
        if (hasLine) {
            builder.setStyle(inboxStyle);
        }
        nm.notify(NOTIFY_ID, builder.build());
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
