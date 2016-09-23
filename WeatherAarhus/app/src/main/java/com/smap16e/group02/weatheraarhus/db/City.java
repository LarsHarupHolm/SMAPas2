package com.smap16e.group02.weatheraarhus.db;

/**
 * Created by Lars on 23-09-2016.
 */

public class City {
    private int id;
    private String name;
    private String country;

    public City() {}

    public City(int id, String name, String country)
    {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public void setId(int id) {this.id = id;}
    public int getId(){return this.id;}

    public void setName(String name){if(this.name != name){this.name = name;}}
    public String getName(){return this.name;}

    public void setCountry(String country){if(this.country != country){this.country = country;}}
    public String getCountry(){return this.country;}
}
