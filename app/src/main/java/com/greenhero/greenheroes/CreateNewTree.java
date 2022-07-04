package com.greenhero.greenheroes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CreateNewTree {


    private String mDate;
    private String mTime;
    private double mLatitude;
    private double mLongitude;
    private String mTreeName;

    public CreateNewTree(){

    }
    public CreateNewTree(Double latitude, Double longitude, String TreeName )
    {
        mDate = currentDate();
        mTime = currentTime();
        mLatitude = latitude;
        mLongitude = longitude;
        mTreeName = TreeName;

    }


    public String getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }



    public String getTreeName() {
        return mTreeName;
    }

    public String currentDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        // get current date time with Date()
        Date date = new Date();
        return dateFormat.format(date);
    }

    public String currentTime() {
        String Time = java.text.DateFormat.getTimeInstance().format(new Date());
        return Time;
    }
}
