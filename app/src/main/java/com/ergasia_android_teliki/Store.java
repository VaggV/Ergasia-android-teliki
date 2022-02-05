package com.ergasia_android_teliki;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class Store {
    private String address;
    private String id;
    private GeoPoint geoPoint;
    private List<Timestamp> dates;
    private String orderId;

    public Store(String address, String id, GeoPoint geoPoint, List<Timestamp> dates) {
        this.address = address;
        this.id = id;
        this.geoPoint = geoPoint;
        this.dates = dates;
    }

    public Store(String address, String id, GeoPoint geoPoint, List<Timestamp> dates, String orderId) {
        this.address = address;
        this.id = id;
        this.geoPoint = geoPoint;
        this.dates = dates;
        this.orderId = orderId;
    }

    @NonNull
    @Override
    public String toString() {
        return address;
    }

    public String getId() {
        return id;
    }

    public Location getLocation(){
        Location loc = new Location("");
        loc.setLatitude(geoPoint.getLatitude());
        loc.setLongitude(geoPoint.getLongitude());
        return loc;
    }

    public List<Timestamp> getDates() {
        return dates;
    }

    public String getOrderId() {
        return orderId;
    }
}
