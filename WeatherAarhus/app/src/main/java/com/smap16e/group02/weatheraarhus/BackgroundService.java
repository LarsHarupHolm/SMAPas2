package com.smap16e.group02.weatheraarhus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.smap16e.group02.weatheraarhus.db.DbHelper;
import com.smap16e.group02.weatheraarhus.db.WeatherHistory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//Kilder:
//https://developer.android.com/training/basics/network-ops/connecting.html
//http://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object
//http://stackoverflow.com/questions/13626126/calling-a-method-every-10-minutes-in-android

public class BackgroundService extends Service {

    private static final String TAG = "BackgroundService";
    public static final String BROADCAST_NEW_WEATHER_RESULT = "new weather result";
    private static String OPENWEATHER_API_KEY = "&APPID={a44e3f6accfae7c2afaaeb6d4f34bfaf}";
    private static String OPENWEATHER_CURRENTWEATHER = "api.openweathermap.org/data/2.5/weather?id=";
    private Gson gson = new Gson();
    private int aarhusCityId = 2624652;

    public BackgroundService() {
        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                fetchWeatherInfo(aarhusCityId);
            }
        };

        timer.schedule (hourlyTask, 0l, 1000*60*30);   // Run every 30 minutes
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void fetchWeatherInfo(int cityId){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadWeatherData(OPENWEATHER_CURRENTWEATHER + cityId + OPENWEATHER_API_KEY);
        } else {
            Log.e(TAG, "Tried to get weather information. No connection");
        }
        return;
    }

    public List<WeatherHistory> getPastWeather(int cityId){

        // Need a get method in dbHelper

        return null;
    }

    private void DownloadWeatherData(final String stringUrl) {

        final AsyncTask<Object, Object, String> task = new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object[] params) {
                String result = null;
                InputStream inputStream;
                int length = 1000;

                try {
                    URL url = new URL(stringUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    int status = urlConnection.getResponseCode();

                    switch (status) {
                        case 200:
                            inputStream = urlConnection.getInputStream();
                            Reader reader = new InputStreamReader(inputStream, "UTF-8");
                            char[] buffer = new char[length];
                            reader.read(buffer);
                            result = new String(buffer);
                        default:
                            Log.e(TAG, "Got wrong status code: " + status);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if(result != null){
                    WeatherHistory history = gson.fromJson(result, WeatherHistory.class);
                    DbHelper.insertWeatherHistory(history);

                    broadCastNewInformation();
                }
            }
        };

        task.execute();
    }

    private void broadCastNewInformation(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BROADCAST_NEW_WEATHER_RESULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}


