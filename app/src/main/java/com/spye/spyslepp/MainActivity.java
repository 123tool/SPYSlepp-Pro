package com.spye.spyslepp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

        etBotToken = findViewById(R.id.etBotToken);
        etChatId = findViewById(R.id.etChatId);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        sharedPref = getSharedPreferences("SpyConfig", MODE_PRIVATE);

        // Load data lama jika ada
        etBotToken.setText(sharedPref.getString("bot_token", ""));
        etChatId.setText(sharedPref.getString("chat_id", ""));

        btnSave.setOnClickListener(v -> {
            String token = etBotToken.getText().toString();
            String cid = etChatId.getText().toString();

            if (!token.isEmpty() && !cid.isEmpty()) {
                sharedPref.edit().putString("bot_token", token).putString("chat_id", cid).apply();
                Toast.makeText(this, "Konfigurasi Disimpan!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            sharedPref.edit().clear().apply();
            etBotToken.setText("");
            etChatId.setText("");
            Toast.makeText(this, "Data dihapus!", Toast.LENGTH_SHORT).show();
        });
    }
}
