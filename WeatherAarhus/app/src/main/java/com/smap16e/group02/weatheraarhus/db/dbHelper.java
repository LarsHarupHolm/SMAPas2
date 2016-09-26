package com.smap16e.group02.weatheraarhus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Lars on 22-09-2016.
 * References:
 * CRUD: https://developer.android.com/training/basics/data-storage/databases.html
 * Singleton: http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
 * onUpgrade: https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
 */

public class DbHelper extends SQLiteOpenHelper{

    private static DbHelper sInstance;

    private static final String DATABASE_NAME = "Group02";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "id";

    //region WeatherHistory Table constants
    private static final String TABLE_WEATHERHISTORY = "WeatherHistory";
    private static final String FOREIGN_COLUMN_CITY = "cityId";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_ICONCODE = "iconCode";
    private static final String COLUMN_TEMP_METRIC = "temperatureMetric";
    private static final String COLUMN_DATE = "date";

    private static final String CREATE_TABLE_WEATHERHISTORY = "CREATE TABLE "
            + TABLE_WEATHERHISTORY + " (" + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + FOREIGN_COLUMN_CITY + " INTEGER,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_ICONCODE + " TEXT,"
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

    //Returns the row-id of inserted row (-1 if error)!
    //May not be equivalent to the actual ID of the inserted entry.
    public long insertWeatherHistory(WeatherHistory weatherHistory)
    {
        SQLiteDatabase db  = sInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, weatherHistory.getId());
        values.put(FOREIGN_COLUMN_CITY, weatherHistory.getCityId());
        values.put(COLUMN_DESCRIPTION, weatherHistory.getDescription());
        values.put(COLUMN_ICONCODE, weatherHistory.getIconCode());
        values.put(COLUMN_TEMP_METRIC, weatherHistory.getTempMetric());
        values.put(COLUMN_DATE, weatherHistory.getUnixTime());

        return db.insert(TABLE_WEATHERHISTORY, null, values);
    }

    //Delete weatherHistory
    //Returns the number of rows deleted (SHOULD BE 0 OR 1)
    public int deleteWeatherHistory(WeatherHistory weatherHistory) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(weatherHistory.getId())};

        return db.delete(TABLE_WEATHERHISTORY, whereClause, whereArgs);
    }

    //Used to flush history for a city.
    //Returns the number of rows deleted.
    public int deleteWeatherHistoriesForCity(int cityId)
    {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String whereClause = FOREIGN_COLUMN_CITY + " = ?";
        String[] whereArgs = {String.valueOf(cityId)};

        return db.delete(TABLE_WEATHERHISTORY, whereClause, whereArgs);
    }

    //Updates WeatherHistory
    //Returns the number of rows updated.
    public int updateWeatherHistory(WeatherHistory weatherHistory){
        SQLiteDatabase db = sInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FOREIGN_COLUMN_CITY, weatherHistory.getCityId());
        values.put(COLUMN_DESCRIPTION, weatherHistory.getDescription());
        values.put(COLUMN_ICONCODE, weatherHistory.getIconCode());
        //Todo: Should be able to update the date and value of measurement?
        //values.put(COLUMN_TEMP_METRIC, weatherHistory.getTempMetric());
        //values.put(COLUMN_DATE, weatherHistory.getUnixTime());

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(weatherHistory.getId())};

        return db.update(TABLE_WEATHERHISTORY, values, whereClause, whereArgs);
    }

    //gets and returns the weatherHistory with id.
    //returns null if not found.
    public WeatherHistory getWeatherHistory(int id) {
        SQLiteDatabase db = sInstance.getReadableDatabase();

        String[] projection = {
                COLUMN_ID,
                FOREIGN_COLUMN_CITY,
                COLUMN_DESCRIPTION,
                COLUMN_ICONCODE,
                COLUMN_TEMP_METRIC,
                COLUMN_DATE
        };

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        Cursor cursor = db.query(TABLE_WEATHERHISTORY,
                projection,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        if(cursor == null)
        {
            return null;
        }
        cursor.moveToFirst();
        WeatherHistory weatherHistory = new WeatherHistory(
                Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)),
                cursor.getString(2),
                cursor.getString(3),
                Double.parseDouble(cursor.getString(4)),
                Long.parseLong(cursor.getString(5)),
                false
        );

        return weatherHistory;
    }

    public ArrayList<WeatherHistory> getWeatherHistoriesByCity(int cityId)
    {
        ArrayList<WeatherHistory> weatherHistories = new ArrayList<WeatherHistory>();
        SQLiteDatabase db = sInstance.getReadableDatabase();

        String[] projection = {
                COLUMN_ID,
                FOREIGN_COLUMN_CITY,
                COLUMN_DESCRIPTION,
                COLUMN_ICONCODE,
                COLUMN_TEMP_METRIC,
                COLUMN_DATE
        };

        String whereClause = FOREIGN_COLUMN_CITY + " = ?";
        String[] whereArgs = {String.valueOf(cityId)};

        String orderBy = COLUMN_ID + " DESC";

        Cursor cursor = db.query(TABLE_WEATHERHISTORY,
                projection,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        if(cursor == null)
        {
            return null;
        }
        //// TODO: 26-09-2016 : Finish method implementation.
        return weatherHistories;
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
