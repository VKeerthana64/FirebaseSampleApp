package app.test.firebasesampleapp;

public class User {

    private String name;
    private String lat;
    private String desc;
    private double lon;


    public User(){}
    public User(String n, String c, double w,String d){
        this.name=n;
        this.lat=c;
        this.lon=w;
        this.desc=d;

    }
    public String getName() {
        return name;
    }

    public String getlat() {
        return lat;
    }

    public double getlon() {
        return lon;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return name +" "+ lat +" " +lon + " " + desc;
    }
}