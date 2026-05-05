package com.spye.spyslepp;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etBotToken, etChatId;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Minta Izin Kamera saat aplikasi dibuka
        checkPermissions();

        etBotToken = findViewById(R.id.etBotToken);
        etChatId = findViewById(R.id.etChatId);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        sharedPref = getSharedPreferences("SpyConfig", MODE_PRIVATE);

        etBotToken.setText(sharedPref.getString("bot_token", ""));
        etChatId.setText(sharedPref.getString("chat_id", ""));

        btnSave.setOnClickListener(v -> {
            String token = etBotToken.getText().toString().trim();
            String cid = etChatId.getText().toString().trim();

            if (!token.isEmpty() && !cid.isEmpty()) {
                sharedPref.edit().putString("bot_token", token).putString("chat_id", cid).apply();
                Toast.makeText(this, "Konfigurasi SPYSlepp Pro Disimpan!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Wajib isi Token & ID!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            sharedPref.edit().clear().apply();
            etBotToken.setText("");
            etChatId.setText("");
            Toast.makeText(this, "Konfigurasi Dihapus!", Toast.LENGTH_SHORT).show();
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
