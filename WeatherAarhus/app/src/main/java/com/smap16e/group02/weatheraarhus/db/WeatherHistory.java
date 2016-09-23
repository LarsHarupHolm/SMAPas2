package com.smap16e.group02.weatheraarhus.db;

import java.util.Date;

/**
 * Created by Lars on 23-09-2016.
 */

public class WeatherHistory {
    private int id;
    private int cityId;
    private String description;
    private double tempMetric;
    private long unixTime;

    public WeatherHistory() {}

    public WeatherHistory(int id, int cityId, String description, double tempMetric, long unixTime, boolean inSeconds)
    {
        this.id = id;
        this.cityId = cityId;
        this.description = description;
        this.tempMetric = tempMetric;
        if(inSeconds) {
        this.unixTime = unixTime*1000;
        } else {
            this.unixTime = unixTime;
        }
    }

    public WeatherHistory(int id, int cityId, String description, double tempMetric, Date dateUTC)
    {
        this.id = id;
        this.cityId = cityId;
        this.description = description;
        this.tempMetric = tempMetric;
        this.unixTime = dateUTC.getTime();
    }

    public void setId(int id) {this.id = id;}
    public int getId(){return this.id;}

    public void setCityId(int cityId){ if(this.cityId != cityId) {this.cityId = cityId;}}
    public int getCityId() {return this.cityId;}

    public void setDescription(String description){ if(this.description != description) {this.description = description;}}
    public String getDescription(){return this.description;}

    public void setTempMetric(double tempMetric){ if(this.tempMetric != tempMetric) {this.tempMetric = tempMetric;}}
    public double getTempMetric() {return this.tempMetric;}

    public void setUnixTime(Date dateUTC){
        long inputTimeUnix = dateUTC.getTime();
        if(this.unixTime != inputTimeUnix) {
            this.unixTime = inputTimeUnix;
        }
    }
    public void setUnixTime(long unixTime, boolean inSeconds){
        if(inSeconds) {
            if (this.unixTime != (unixTime*1000)) {
                this.unixTime = unixTime*1000;
            }
        } else {
            if(this.unixTime != unixTime) {
                this.unixTime = unixTime;
            }
        }

    }
    public long getUnixTime(){return this.unixTime;}

}
