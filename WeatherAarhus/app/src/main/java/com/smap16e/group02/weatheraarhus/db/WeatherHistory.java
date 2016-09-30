package com.smap16e.group02.weatheraarhus.db;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Lars on 23-09-2016.
 */

public class WeatherHistory {
    private int id;
    private int cityId;
    private String description;
    private String iconCode;
    private double tempMetric;
    private long unixTime;

    public WeatherHistory() {}

    public void setId(int id) {this.id = id;}
    public int getId(){return this.id;}

    public void setCityId(int cityId){ if(this.cityId != cityId) {this.cityId = cityId;}}
    public int getCityId() {return this.cityId;}

    public void setDescription(String description){ if(!Objects.equals(this.description, description)) {this.description = description;}}
    public String getDescription(){return this.description;}

    public void setMetricTempFromKelvin(double tempKelvin){
        this.tempMetric = tempKelvin-273.15;
    }
    public void setTempFromMetric(double tempMetric){
        this.tempMetric = tempMetric;
    }

    public double getTempMetric() {return this.tempMetric;}

    public void setUnixTime(long unixTime) {this.unixTime = unixTime;}
    public long getUnixTime(){return this.unixTime;}

    public Calendar getCalendar(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.unixTime*1000L);
        return cal;
    }

    public String getTempString() { return String.format(Locale.getDefault(), "%.0f", getTempMetric()) + " °C"; }

    public void setIconCode(String iconCode){ if(!Objects.equals(this.iconCode, iconCode)) {this.iconCode = iconCode;}}
    public String getIconCode(){return this.iconCode;}

}
