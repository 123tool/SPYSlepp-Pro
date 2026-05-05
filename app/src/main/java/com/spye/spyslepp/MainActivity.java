package com.spye.spyslepp;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
        
        // Mengubah warna Status Bar menjadi Kuning Saweria (#FFD400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#FFD400"));
            // Membuat ikon status bar tetap terlihat (gelap) karena background terang
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setContentView(R.layout.activity_main);

        // Inisialisasi View berdasarkan ID di XML Neo-Brutalism
        etBotToken = (EditText) findViewById(R.id.etBotToken);
        etChatId = (EditText) findViewById(R.id.etChatId);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        Button btnDelete = (Button) findViewById(R.id.btnDelete);

        // Load data dari penyimpanan lokal
        sharedPref = getSharedPreferences("SpyConfig", MODE_PRIVATE);
        etBotToken.setText(sharedPref.getString("bot_token", ""));
        etChatId.setText(sharedPref.getString("chat_id", ""));

        // Cek izin kamera saat aplikasi pertama kali dibuka
        checkPermissions();

        Log.d(TAG, "Aplikasi SPYSlepp Pro (Saweria Edition) Siap.");

        // Logika Tombol Simpan
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = etBotToken.getText().toString().trim();
                String cid = etChatId.getText().toString().trim();

                if (!token.isEmpty() && !cid.isEmpty()) {
                    sharedPref.edit()
                        .putString("bot_token", token)
                        .putString("chat_id", cid)
                        .apply();
                    
                    Log.d(TAG, "Konfigurasi tersimpan sukses.");
                    Toast.makeText(MainActivity.this, "Konfigurasi Disimpan!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Gagal simpan: Input kosong.");
                    Toast.makeText(MainActivity.this, "Isi Token & Chat ID dulu!", Toast.LENGTH_SHORT).show();
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
                Log.d(TAG, "Konfigurasi dihapus.");
                Toast.makeText(MainActivity.this, "Data Berhasil Dihapus!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        }
    }
}
