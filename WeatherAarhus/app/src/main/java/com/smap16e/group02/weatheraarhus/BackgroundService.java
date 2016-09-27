package com.smap16e.group02.weatheraarhus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.smap16e.group02.weatheraarhus.db.WeatherHistory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

//Kilder:
//https://developer.android.com/training/basics/network-ops/connecting.html
//http://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object

public class BackgroundService extends Service {

    private static String OPENWEATHER_API_KEY = "&APPID={a44e3f6accfae7c2afaaeb6d4f34bfaf}";
    private static String OPENWEATHER_CURRENTWEATHER = "api.openweathermap.org/data/2.5/weather?id=";
    private Gson gson = new Gson();


    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public WeatherHistory getCurrentWeather(int cityId){

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWeatherData().execute(OPENWEATHER_CURRENTWEATHER + cityId + OPENWEATHER_API_KEY);
        } else {
            // display error
        }


        return new WeatherHistory();
    }

    public List<WeatherHistory> getPastWeather(int cityId){

        return null;
    }

    private class DownloadWeatherData extends AsyncTask<String, Void, String> {
        private InputStream inputStream = null;
        private int length = 1000;
        String result = null;

        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL(params[0]);
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
                        break;
                    default:
                        // Error
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Intent broadcastIntent = new Intent();
            //broadcastIntent.setAction(BROADCAST_BACKGROUND_SERVICE_RESULT);
            //broadcastIntent.putExtra(EXTRA_TASK_RESULT, result);
            //Gem i databasen
            //LocalBroadcastManager.getInstance(this).sendBroadcast()
        }
    }
}


