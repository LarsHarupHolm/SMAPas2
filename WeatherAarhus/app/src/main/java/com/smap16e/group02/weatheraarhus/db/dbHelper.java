package com.smap16e.group02.weatheraarhus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lars on 22-09-2016.
 * References:
 * CRUD: https://developer.android.com/training/basics/data-storage/databases.html
 * Singleton: http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
 * onUpgrade: https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
 */

class DbHelper extends SQLiteOpenHelper{

    private static DbHelper sInstance;

    private static final String DATABASE_NAME = "Group02";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "id";

    //region WeatherHistory Table constants
    private static final String TABLE_WEATHERHISTORY = "WeatherHistory";
    private static final String FOREIGN_COLUMN_CITY = "cityId";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_TEMP_METRIC = "temperatureMetric";
    private static final String COLUMN_DATE = "date";

    private static final String CREATE_TABLE_WEATHERHISTORY = "CREATE TABLE "
            + TABLE_WEATHERHISTORY + " (" + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + FOREIGN_COLUMN_CITY + " INTEGER,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_TEMP_METRIC + " REAL,"
            + COLUMN_DATE + " INTEGER" + ")";

    private static final String DROP_TABLE_WEATHERHISTORY = "DROP TABLE IF EXISTS " + TABLE_WEATHERHISTORY;
    //endregion

    //region City Table constants
    private static final String TABLE_CITY = "City";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_COUNTRY = "country";

    private static final String CREATE_TABLE_CITY = "CREATE TABLE "
            + TABLE_CITY + " (" + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_NAME + "TEXT, "
            + COLUMN_COUNTRY + "TEXT" + ")";

    private static final String DROP_TABLE_CITY = "DROP TABLE IF EXISTS " + TABLE_CITY;
    //endregion

    //region Singleton
    public static synchronized DbHelper getInstance(Context context) {
        if(sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext());
        }
        return sInstance;
    }
    private DbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //endregion

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WEATHERHISTORY);
        db.execSQL(CREATE_TABLE_CITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    //region CRUD for WeatherHistory
    public long insertWeatherHistory(WeatherHistory weatherHistory)
    {
        SQLiteDatabase db  = sInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, weatherHistory.getId());
        values.put(FOREIGN_COLUMN_CITY, weatherHistory.getCityId());
        values.put(COLUMN_DESCRIPTION, weatherHistory.getDescription());
        values.put(COLUMN_TEMP_METRIC, weatherHistory.getTempMetric());
        values.put(COLUMN_DATE, weatherHistory.getUnixTime());

        return db.insert(TABLE_WEATHERHISTORY, null, values);
    }
    //endregion

    //region CRUD for City
    public long insertCity(City city)
    {
        SQLiteDatabase db  = sInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, city.getId());
        values.put(COLUMN_NAME, city.getName());
        values.put(COLUMN_COUNTRY, city.getCountry());

        return db.insert(TABLE_CITY, null, values);
    }
    //endregion

}
