package com.at.agromap;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button openOnlineMap = (Button) findViewById(R.id.online_map_btn);
        Button openOfflineMap = (Button) findViewById(R.id.offline_map_btn);
        TextView logo = (TextView) findViewById(R.id.agro_logo);

        final Intent intentOnlineMap = new Intent(MainActivity.this, OnlineMapActivity.class);
//        final Intent intentOfflineMap = new Intent(MainActivity.this, TailedMapActivity.class);
        final Intent intentOfflineMap = new Intent(MainActivity.this, MapDemo.class);

        // Проверка версии сборки для использования fromHtml()
        // в версии N и выше.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            logo.setText(Html.fromHtml(getString(R.string.logo_text), Html.FROM_HTML_MODE_LEGACY));
        } else {
            logo.setText(Html.fromHtml(getString(R.string.logo_text)));
        }

        openOnlineMap.setOnClickListener(new View.OnClickListener() {
            public  void onClick(View v) {
                MainActivity.this.startActivity(intentOnlineMap);
            }
        });

        openOfflineMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.startActivity(intentOfflineMap);
            }
        });
    }
}
