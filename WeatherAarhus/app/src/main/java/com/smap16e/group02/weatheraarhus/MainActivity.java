package com.smap16e.group02.weatheraarhus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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

import com.smap16e.group02.weatheraarhus.db.WeatherHistory;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton floatingActionButton;
    private TextView descriptionTextView, tempTextView;
    private ImageView iconImageView;
    private ListView listView;

    private ArrayList<WeatherHistory> weatherRecords;
    private BackgroundService mBoundService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((BackgroundService.LocalBinder) service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(MainActivity.this,
                BackgroundService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBackgroundService();
        doBindService();

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

        updateWeatherRecordsList();

        if(mBoundService != null)
            setCurrentWeather(mBoundService.getCurrentWeather());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
            WeatherHistory curWeather = mBoundService.getCurrentWeather();
            setCurrentWeather(curWeather);
            updateWeatherRecordsList();
        }
    };

    private void setCurrentWeather(WeatherHistory weather) {
        descriptionTextView.setText(weather.getDescription());
        tempTextView.setText(weather.getTempString());
        new LoadImage(iconImageView).execute(weather.getIconCode());
    }

    private void updateWeatherRecordsList()
    {
        //Remove too old historyRecords
        if(mBoundService == null)
            return;

        List<WeatherHistory> weatherHistoryList = mBoundService.getPastWeather();
        weatherRecords.clear();
        weatherRecords.addAll(weatherHistoryList);
        Iterator<WeatherHistory> iterator = weatherRecords.iterator();
        while (iterator.hasNext()) {
            WeatherHistory cur = iterator.next();
            if (cur.getCalendar().before(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24)))) { //if the date is before 24 hours ago, remove it
                iterator.remove();
            }
        }
        if (listView != null) {
            WeatherHistory[] weatherRecordsArray = new WeatherHistory[weatherRecords.size()];
            listView.setAdapter(new HistoryArrayAdapter(this, weatherRecords.toArray(weatherRecordsArray)));
        }
    }

    public void refresh() {
        mBoundService.fetchWeatherInfo();
        Toast.makeText(MainActivity.this, "Checking for new weather info...", Toast.LENGTH_SHORT).show();
    }

    private void startBackgroundService(){
        Intent backgroundServiceIntent = new Intent(MainActivity.this, BackgroundService.class);
        startService(backgroundServiceIntent);
    }

    private class HistoryArrayAdapter extends ArrayAdapter<WeatherHistory> {
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
                ImageView image = (ImageView) v.findViewById(R.id.hist_imageView);
                TextView description = (TextView) v.findViewById(R.id.hist_descriptionTextView);
                TextView date = (TextView) v.findViewById(R.id.dateTextView);
                TextView temp = (TextView) v.findViewById(R.id.hist_tempTextView);
                TextView time = (TextView) v.findViewById(R.id.timeTextView);

                if (image != null) {
                    new LoadImage(image).execute(w.getIconCode());
                }

                if (description != null) {
                    description.setText(w.getDescription());
                }

                if (date != null) {
                    date.setText(new SimpleDateFormat("dd/MM/yyyy").format(w.getCalendar().getTime()));
                }

                if (temp != null) {
                    temp.setText(String.valueOf(w.getTempString()));
                }

                if (time != null) {
                    time.setText(new SimpleDateFormat("HH:mm:ss").format(w.getCalendar().getTime()));
                }
            }

            return v;
        }

    }
    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        private ImageView imageView;
        public LoadImage(ImageView imageView)
        {
            this.imageView = imageView;
        }
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL("http://openweathermap.org/img/w/"+args[0]+".png").getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null && imageView != null){
                imageView.setImageBitmap(bitmap);

            }else{
                Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
