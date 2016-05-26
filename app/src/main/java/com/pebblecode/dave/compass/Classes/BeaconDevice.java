package com.pebblecode.dave.compass.Classes;

/**
 * Created by admin on 26/05/16.
 */
public class BeaconDevice {

    private String deviceId;
    private int bearing;
    private  String name;

    public  BeaconDevice(String deviceId, String name, int bearing){
        this.deviceId = deviceId;
        this.bearing = bearing;
        this.name = name;
    }

    public  String getDeviceId(){
        return deviceId;
    }

    public  int getBearing(){
        return  bearing;
    }

    public  String getName(){
        return  name;
    }
}
