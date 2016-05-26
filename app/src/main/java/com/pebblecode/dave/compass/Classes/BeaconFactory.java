package com.pebblecode.dave.compass.Classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 26/05/16.
 */
public class BeaconFactory {

    public static List<BeaconDevice> BuildBeaconMap(){
        ArrayList<BeaconDevice> bdevices = new ArrayList<BeaconDevice>();
        bdevices.add(new BeaconDevice("5A:68:F1:AB:15:55", "Yellow", 180));
        bdevices.add(new BeaconDevice("1C:1A:C0:60:71:15", "", 90));
        bdevices.add(new BeaconDevice("48:49:23:34:EC:46", "Yellow", 270));
        return bdevices;
    }
}
