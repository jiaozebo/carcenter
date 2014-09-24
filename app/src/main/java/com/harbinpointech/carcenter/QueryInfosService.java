package com.harbinpointech.carcenter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.harbinpointech.carcenter.data.WebHelper;

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
                                Intent i = new Intent(ACTION_NOTIFICATIONS_RECEIVED);
                                i.putExtra(EXTRA_CHAT_ARRAY, params[0].toString());
                                LocalBroadcastManager.getInstance(QueryInfosService.this).sendBroadcast(i);
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
        if (t != null){
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
