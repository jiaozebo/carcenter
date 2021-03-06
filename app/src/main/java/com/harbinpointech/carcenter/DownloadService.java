package com.harbinpointech.carcenter;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startDownload(Context context, int notifyId, String url, String version) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(QueryInfosService.ACTION_START_DOWNLOAD);
        intent.putExtra(QueryInfosService.EXTRA_NOTIFY_ID, notifyId);
        intent.putExtra(QueryInfosService.EXTRA_DOWNLOAD_URL, url);
        intent.putExtra(QueryInfosService.EXTRA_NEWEST_VERSION, version);
        context.startService(intent);
    }

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (QueryInfosService.ACTION_START_DOWNLOAD.equals(action)) {
                final int notify_id = intent.getIntExtra(QueryInfosService.EXTRA_NOTIFY_ID, 0);
                String url = intent.getStringExtra(QueryInfosService.EXTRA_DOWNLOAD_URL);
                String lastVersion = intent.getStringExtra(QueryInfosService.EXTRA_NEWEST_VERSION);
                handleActionDownload(notify_id, url, lastVersion);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(int notify_id, String url, String lastVersion) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(notify_id);
        Uri resource = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(
                resource);
        // 设置文件类型
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap
                .getMimeTypeFromExtension(MimeTypeMap
                        .getFileExtensionFromUrl(url));
        request.setMimeType("application/vnd.android.package-archive");
        // 在通知栏中显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        } else {
            request.setShowRunningNotification(true);
        }
        request.setVisibleInDownloadsUi(true);
        request.setTitle(getResources().getString(R.string.app_name));
        // sdcard的目录下的download文件夹
        String name = String.format("%s_%s.apk", getString(R.string.app_name), lastVersion);
        try {
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, name);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            long id = dm.enqueue(request);
            if (id == -1) {
                Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
