package com.hi.appskin_v40.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.hi.appskin_v40.BuildConfig;
import com.hi.appskin_v40.model.Skin;

import java.io.File;

public class DownloadHelper {

    public enum DownloadingState {
        NotDownloaded,
        Downloaded,
        Downloading
    }

    public static String getThumbnailUrl(String name) {
        String correctName = name.replaceFirst("/", "%2F");
        String fbURL = BuildConfig.showedFirebaseURL;
        return "https://firebasestorage.googleapis.com/v0/b/"+ fbURL +"/o/" + correctName + "?alt=media";
    }

    public static String getScreenshotlUrl(String name) {
        String correctName = name.replaceFirst("/", "%2F");
        String fbURL = BuildConfig.showedFirebaseURL;
        return "https://firebasestorage.googleapis.com/v0/b/"+ fbURL +"/o/" + correctName + "?alt=media";
    }

    public static long downloadFile(Context context, String fileUrl) {
        Uri uri = Uri.parse(fileUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.getLastPathSegment());

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null)
            return 0;

        return downloadManager.enqueue(request);
    }

    public static DownloadingState getDownloadingStatus(Context context, Skin modInfo) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null)
            return DownloadingState.NotDownloaded;

        long downloadId = LocalStorage.getIdForModInfo(context, modInfo);
        if (downloadId == 0)
            return DownloadingState.NotDownloaded;

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        // Check if file is downloaded and still exists
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                String localFile = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                if (localFile != null) {
                    File file = new File(Uri.parse(localFile).getPath());
                    if (file.isFile()) {
                        cursor.close();
                        return DownloadingState.Downloaded;
                    }
                }
            } else if (status == DownloadManager.STATUS_RUNNING
                    || status == DownloadManager.STATUS_PAUSED
                    || status == DownloadManager.STATUS_PENDING) {
                cursor.close();
                return DownloadingState.Downloading;
            }
        }

        cursor.close();
        return DownloadingState.NotDownloaded;
    }
}
