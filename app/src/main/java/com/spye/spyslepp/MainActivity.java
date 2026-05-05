package com.spye.spyslepp;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etBotToken, etChatId;
    private SharedPreferences sharedPref;
    private static final String TAG = "SPY_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi View
        etBotToken = (EditText) findViewById(R.id.etBotToken);
        etChatId = (EditText) findViewById(R.id.etChatId);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        Button btnDelete = (Button) findViewById(R.id.btnDelete);

        // Load data yang tersimpan
        sharedPref = getSharedPreferences("SpyConfig", MODE_PRIVATE);
        etBotToken.setText(sharedPref.getString("bot_token", ""));
        etChatId.setText(sharedPref.getString("chat_id", ""));

        // Cek Izin Kamera saat app dibuka
        checkPermissions();

        // Log Debugging awal
        Log.d(TAG, "Aplikasi SPYSlepp Pro berhasil dibuka.");

        // Logika Tombol Simpan
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = etBotToken.getText().toString().trim();
                String cid = etChatId.getText().toString().trim();

                Log.d(TAG, "Mencoba menyimpan konfigurasi...");

                if (!token.isEmpty() && !cid.isEmpty()) {
                    sharedPref.edit()
                        .putString("bot_token", token)
                        .putString("chat_id", cid)
                        .apply();
                    
                    Log.d(TAG, "Data tersimpan: Token diawali " + (token.length() > 5 ? token.substring(0, 5) : "pendek"));
                    Toast.makeText(MainActivity.this, "Konfigurasi Disimpan!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Gagal simpan: Token atau ID kosong.");
                    Toast.makeText(MainActivity.this, "Token/ID tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Logika Tombol Hapus
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref.edit().clear().apply();
                etBotToken.setText("");
                etChatId.setText("");
                Log.d(TAG, "Konfigurasi telah dihapus.");
                Toast.makeText(MainActivity.this, "Data Dihapus!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Izin kamera belum diberikan. Meminta izin...");
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                Log.d(TAG, "Izin kamera sudah aktif.");
            }
        }
    }
}
