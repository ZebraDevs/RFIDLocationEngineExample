package com.zebra.example;

import com.zebra.Location;
import com.zebra.LocationEvent;
import com.zebra.LocationEventEngine;
import com.zebra.LocationEventListener;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        LocationEventEngine eventEngine = new LocationEventEngine();

        // Import config from file or other appropriate source

        // Create locations
        Location l = new Location("Reader1",1, "Location 1");
        eventEngine.addLocation(l);
        l = new Location("Reader1",2, "Location 2");
        eventEngine.addLocation(l);
        l = new Location("Reader2",1, "Location 3");
        eventEngine.addLocation(l);
        l = new Location("Reader2",2, "Location 4");
        eventEngine.addLocation(l);
        l = new Location("Reader3",2, "Wristband Activation");
        l.setCloseRange(true);
        eventEngine.addLocation(l);

        // Set other config values
        eventEngine.setCloseRangeReadRSSI(-45);
        eventEngine.setWindowLength(10000);
        eventEngine.setMinimumWindowLength(1000);
        eventEngine.setLocationChangeResistance(67);
        eventEngine.setCloseRangeReadRSSI(10);

        // set up event listener
        eventEngine.registerLocationEventListener(new LocationEventListener() {
            @Override
            public void locationEvent(LocationEvent locationEvent) {
                log("Got location event: " + locationEvent.getTagId() + " is at " + locationEvent.getFriendlyName());
            }
        });

        // Generate some random data for testing
        String[] tagIDs = {"Tag1", "Tag2", "Tag3"};
        String[] readerNames = {"Reader1", "Reader2", "Reader3"};
        int[] antennaIDs = {1,2};
        Random random = new Random();

        // Inject random data
        for (int i = 0; i < 50; i++) {
            String tagID = tagIDs[random.nextInt(tagIDs.length)];
            long timestamp = System.currentTimeMillis();
            int rssi  = random.nextInt(50) - 99;
            int readCount  = random.nextInt(10) + 1;

            String readerName = readerNames[random.nextInt(readerNames.length)];
            int antennaID = antennaIDs[random.nextInt(antennaIDs.length)];

            // Logging
            System.out.println("\nNEW EVENT - " + tagID +
                    ", " + timestamp +
                    ", " + rssi +
                    ", " + readerName +
                    ", " + antennaID+"\n");
            eventEngine.tagEvent(tagID, timestamp, rssi, readerName, antennaID,readCount);

            try {
                Thread.sleep(100 + random.nextInt(400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }
}