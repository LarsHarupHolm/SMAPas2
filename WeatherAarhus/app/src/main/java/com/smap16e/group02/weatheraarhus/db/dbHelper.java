package com.smap16e.group02.weatheraarhus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;

/**
 * Created by Lars on 22-09-2016.
 * References:
 * CRUD: https://developer.android.com/training/basics/data-storage/databases.html
 * Singleton: http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
 * onUpgrade: https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
 */

public class DbHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "Group02";
    private static final int DATABASE_VERSION = 1;

    //region WeatherHistory Table constants

    private static class WeatherEntry implements BaseColumns{
        private static final String TABLE_WEATHERHISTORY = "WeatherHistory";
        private static final String FOREIGN_COLUMN_CITY = "cityId";
        private static final String COLUMN_DESCRIPTION = "description";
        private static final String COLUMN_TEMP_METRIC = "temperatureMetric";
        private static final String COLUMN_DATE = "date";
        private static final String COLUMN_ICONCODE = "icon";
    }

    private static final String CREATE_TABLE_WEATHERHISTORY = "CREATE TABLE "
            + WeatherEntry.TABLE_WEATHERHISTORY + " ("
            + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + WeatherEntry.FOREIGN_COLUMN_CITY + " INTEGER,"
            + WeatherEntry.COLUMN_DESCRIPTION + " TEXT,"
            + WeatherEntry.COLUMN_TEMP_METRIC + " REAL,"
            + WeatherEntry.COLUMN_DATE + " INTEGER,"
            + WeatherEntry.COLUMN_ICONCODE + " TEXT" + ")";

    //endregion

    private DbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //region Overrides
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WEATHERHISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
    //endregion

    //region CRUD for WeatherHistory

    public static long insertWeatherHistory(Context context, WeatherHistory weatherHistory)
    {
        DbHelper helper = new DbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WeatherEntry.FOREIGN_COLUMN_CITY, weatherHistory.getCityId());
        values.put(WeatherEntry.COLUMN_DESCRIPTION, weatherHistory.getDescription());
        values.put(WeatherEntry.COLUMN_TEMP_METRIC, weatherHistory.getTempMetric());
        values.put(WeatherEntry.COLUMN_DATE, weatherHistory.getUnixTime());
        values.put(WeatherEntry.COLUMN_ICONCODE, weatherHistory.getIconCode());

        return db.insert(WeatherEntry.TABLE_WEATHERHISTORY, null, values);
    }

    private static Cursor readWeatherHistory(Context context){
        DbHelper helper = new DbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] projection = {
                WeatherEntry.COLUMN_DATE,
                WeatherEntry.COLUMN_DESCRIPTION,
                WeatherEntry.COLUMN_TEMP_METRIC,
                WeatherEntry.COLUMN_ICONCODE,
        };

        String sortOrder =
                WeatherEntry._ID + " DESC";

        Cursor c = db.query(
                WeatherEntry.TABLE_WEATHERHISTORY,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return c;
    }

    public static WeatherHistory readCurrentWeatherHistory(Context context){
        return parseToWeatherHistory(readWeatherHistory(context)).get(0);
    }

    public static List<WeatherHistory> readHistoricWeatherHistory(Context context){
        return parseToWeatherHistory(readWeatherHistory(context)).subList(1,49);
    }

    //Delete weatherHistory
    //Returns the number of rows deleted (SHOULD BE 0 OR 1)
    public int deleteWeatherHistory(Context context, WeatherHistory weatherHistory) {
        DbHelper helper = new DbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        String whereClause = WeatherEntry._ID + " = ?";
        String[] whereArgs = {String.valueOf(weatherHistory.getId())};

        return db.delete(WeatherEntry.TABLE_WEATHERHISTORY, whereClause, whereArgs);
    }

    private static List<WeatherHistory> parseToWeatherHistory(Cursor c){
        List<WeatherHistory> weatherHistoryList = new ArrayList<>();
        c.moveToFirst();

        boolean endOfList = false;
        while(!endOfList){
            WeatherHistory result = new WeatherHistory();
            result.setUnixTime(c.getLong(c.getColumnIndexOrThrow(WeatherEntry.COLUMN_DATE)));
            result.setDescription(c.getString(c.getColumnIndexOrThrow(WeatherEntry.COLUMN_DESCRIPTION)));
            result.setTempFromMetric(c.getDouble(c.getColumnIndexOrThrow(WeatherEntry.COLUMN_TEMP_METRIC)));
            result.setIconCode(c.getString(c.getColumnIndexOrThrow(WeatherEntry.COLUMN_ICONCODE)));

            weatherHistoryList.add(result);

            if(c.isLast())
                endOfList = true;
            else
                c.moveToNext();
        }

        return weatherHistoryList;
    }
}
