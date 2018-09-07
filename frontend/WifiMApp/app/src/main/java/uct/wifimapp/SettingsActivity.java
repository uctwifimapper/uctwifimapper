package uct.wifimapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MapActivity.class));
            }
        });


        Button saveButton = (Button) findViewById(R.id.btn_Save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    void saveSettings(){
        Switch today = (Switch) findViewById(R.id.sw_today);
        Switch wifisw = (Switch) findViewById(R.id.sw_WiFi_readings);

        boolean today_status = today.isChecked();
        boolean wifi_status = wifisw.isChecked();

        EditText v = findViewById(R.id.txf_Minutes);
        int min = (int) Integer.parseInt(v.getText().toString());

        SharedPreferences settings = getSharedPreferences("settings.txt", MODE_PRIVATE);
        settings.edit().putBoolean("today", today_status);
        settings.edit().putBoolean("upload", wifi_status);
        settings.edit().putInt("min", min);
        settings.edit().apply();
    }
}
