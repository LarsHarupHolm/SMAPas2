package com.smap16e.group02.weatheraarhus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smap16e.group02.weatheraarhus.db.DbHelper;
import com.smap16e.group02.weatheraarhus.db.WeatherHistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kåre on 22-09-2016.
 * References:
 *  https://developer.android.com/training/basics/network-ops/connecting.html
 *  https://developer.android.com/reference/android/app/Service.html
 *  http://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object
 *  http://stackoverflow.com/questions/13626126/calling-a-method-every-10-minutes-in-android
 *  http://www.survivingwithandroid.com/2013/05/build-weather-app-json-http-android.html
 *  Demo from Lecture 7: ServicesDemo 16-2
 */

public class BackgroundService extends Service {

    private static final String TAG = "BackgroundService";
    public static final String BROADCAST_NEW_WEATHER_RESULT = "new weather result";
    private static final int aarhusCityId = 2624652;
    private static String OPENWEATHER_API_KEY = "&APPID=a44e3f6accfae7c2afaaeb6d4f34bfaf";
    private static String OPENWEATHER_CURRENTWEATHER = "http://api.openweathermap.org/data/2.5/weather?id=";
    private boolean started = false;

    private final IBinder mBinder = new LocalBinder();

    public BackgroundService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        if(!started) {
            Timer timer = new Timer ();
            TimerTask recurringTask = new TimerTask () {
                @Override
                public void run () {
                    fetchWeatherInfo();
                }
            };

            timer.schedule (recurringTask, 0l, 1000*60*1);   // Run every 1 minutes
            started = true;
        }

        return START_STICKY;
    }

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");
        return mBinder;
    }

    public WeatherHistory getCurrentWeather(){
        Log.d(TAG, "getCurrentWeather called");
        Context context = getApplicationContext();
        return DbHelper.readCurrentWeatherHistory(context);
    }

    public List<WeatherHistory> getPastWeather(){
        Log.d(TAG, "getPastWeather called");
        Context context = getApplicationContext();
        return DbHelper.readHistoricWeatherHistory(context);
    }

    public void fetchWeatherInfo(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadCurrentWeatherData(OPENWEATHER_CURRENTWEATHER + aarhusCityId + OPENWEATHER_API_KEY);
        } else {
            Log.e(TAG, "Tried to get weather information. No connection");
        }
    }

    private void DownloadCurrentWeatherData(final String stringUrl) {

        final AsyncTask<Object, Object, String> task = new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object[] params) {
                String result = null;
                InputStream inputStream;
                int length = 500;

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
                    WeatherHistory history = buildWeatherHistory(result);

                    if(history != null){
                        Context context = getApplicationContext();
                        DbHelper.insertWeatherHistory(context, history);
                        broadCastNewInformation();
                    }
                }
            }
        };

        task.execute();
    }

    private WeatherHistory buildWeatherHistory(String jsonString) {
        WeatherHistory result = new WeatherHistory();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            result.setUnixTime(jsonObject.getLong("dt"));
            Log.e(TAG, "Got weather data with unix time: " + jsonObject.getLong("dt"));
            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            result.setDescription(weather.getString("main"));
            result.setIconCode(weather.getString("icon"));

            JSONObject main = jsonObject.getJSONObject("main");
            result.setMetricTempFromKelvin(main.getDouble("temp"));

            result.setCityId(aarhusCityId);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private void broadCastNewInformation(){
        Log.d(TAG, "broadCastNewInformation called");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BROADCAST_NEW_WEATHER_RESULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}