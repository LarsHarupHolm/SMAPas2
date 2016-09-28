package com.smap16e.group02.weatheraarhus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.DecimalFormat;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smap16e.group02.weatheraarhus.db.DbHelper;
import com.smap16e.group02.weatheraarhus.db.WeatherHistory;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton floatingActionButton;
    private TextView descriptionTextView, tempTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBackgroundService();
        Toast.makeText(MainActivity.this, "Background service started", Toast.LENGTH_LONG).show();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something
            }
        });

        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        tempTextView = (TextView) findViewById(R.id.tempTextView);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BackgroundService.BROADCAST_NEW_WEATHER_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(onBackgroundServiceResult, filter);

        super.onResume();
    }

    private BroadcastReceiver onBackgroundServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(MainActivity.this, "Received new weather info from service", Toast.LENGTH_LONG).show();
            WeatherHistory currentWeather = DbHelper.readCurrentWeatherHistory(context);
            descriptionTextView.setText(currentWeather.getDescription());
            String tempFormatting = String.format(Locale.getDefault(), "%.2f", currentWeather.getTempMetric()) + " °C";
            tempTextView.setText(tempFormatting);
        }
    };

    public void refresh(View view) {

    }

    private void startBackgroundService(){
        Intent backgroundServiceIntent = new Intent(MainActivity.this, BackgroundService.class);
        startService(backgroundServiceIntent);
    }
}
