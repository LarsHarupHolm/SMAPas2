package com.smap16e.group02.weatheraarhus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smap16e.group02.weatheraarhus.db.DbHelper;
import com.smap16e.group02.weatheraarhus.db.WeatherHistory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton floatingActionButton;
    private TextView descriptionTextView, tempTextView;
    private ImageView iconImageView;
    private ListView listView;

    private ArrayList<WeatherHistory> weatherRecords;
    private WeatherHistory currentWeather;

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
                refresh();
            }
        });

        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        tempTextView = (TextView) findViewById(R.id.tempTextView);
        iconImageView = (ImageView) findViewById(R.id.imageView);
        listView = (ListView) findViewById(R.id.main_lst_weatherhistory);

        weatherRecords = new ArrayList<>();
//        WeatherHistory w1 = new WeatherHistory();
//        w1.setIconCode("10n"); w1.setDescription("SUPER NICE WEATHER"); w1.setUnixTime(1475094981); w1.setTempFromMetric(23);
//        WeatherHistory w2 = new WeatherHistory();
//        w2.setIconCode("02d"); w2.setDescription("KINDA OK WEATHER"); w2.setUnixTime(1475091981); w2.setTempFromMetric(18);
//        weatherRecords.add(w1);
//        weatherRecords.add(w2);
        WeatherHistory[] weatherRecordsArray = new WeatherHistory[weatherRecords.size()];
        listView.setAdapter(new HistoryArrayAdapter(this, weatherRecords.toArray(weatherRecordsArray)));
        
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
            WeatherHistory curWeather = DbHelper.readCurrentWeatherHistory(context);
            setCurrentWeather(curWeather);
        }
    };

    private void setCurrentWeather(WeatherHistory weather) {
        descriptionTextView.setText(weather.getDescription());
        tempTextView.setText(weather.getTempString());

        if (currentWeather != null) {
            //Remove too old historyRecords
            Iterator<WeatherHistory> iterator = weatherRecords.iterator();
            while (iterator.hasNext()) {
                WeatherHistory cur = iterator.next();
                if (cur.getDate().before(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24)))) { //if the date is before 24 hours ago, remove it
                    iterator.remove();
                }
            }
            weatherRecords.add(currentWeather);
        }
        currentWeather = weather;
    }

    public void refresh() {
        //Get the service to fetch weather from api and update the currentWeather
    }

    private void startBackgroundService(){
        Intent backgroundServiceIntent = new Intent(MainActivity.this, BackgroundService.class);
        startService(backgroundServiceIntent);
    }

    private class HistoryArrayAdapter extends ArrayAdapter<WeatherHistory> {

        ImageView image;

        public HistoryArrayAdapter(Context context, WeatherHistory[] records) {
            super(context, R.layout.weather_listview, records);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.weather_listview, null);
            }

            WeatherHistory w = getItem(position);

            if (w != null) {
                image = (ImageView) v.findViewById(R.id.hist_imageView);
                TextView description = (TextView) v.findViewById(R.id.hist_descriptionTextView);
                TextView date = (TextView) v.findViewById(R.id.dateTextView);
                TextView temp = (TextView) v.findViewById(R.id.hist_tempTextView);
                TextView time = (TextView) v.findViewById(R.id.timeTextView);

                if (image != null) {
                    new LoadImage().execute("http://openweathermap.org/img/w/"+w.getIconCode()+".png");
                }

                if (description != null) {
                    description.setText(w.getDescription());
                }

                if (date != null) {
                    date.setText(w.getDate().getDay() + "-" + w.getDate().getMonth() + "-" + w.getDate().getYear()); //Refactor this deprecated shit
                }

                if (temp != null) {
                    temp.setText(String.valueOf(w.getTempString()));
                }

                if (time != null) {
                    time.setText(w.getDate().getHours()+":"+w.getDate().getMinutes());
                }
            }

            return v;
        }
        private class LoadImage extends AsyncTask<String, String, Bitmap> {

            protected Bitmap doInBackground(String... args) {
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            protected void onPostExecute(Bitmap bitmap) {
                if(bitmap != null && image != null){
                    image.setImageBitmap(bitmap);

                }else{
                    Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
