package com.smap16e.group02.weatheraarhus;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBackgroundService();
            }
        });
    }

    public void refresh(View view) {

    }

    private void startBackgroundService(){
        Intent backgroundServiceIntent = new Intent(MainActivity.this, BackgroundService.class);
        startService(backgroundServiceIntent);
    }
}
