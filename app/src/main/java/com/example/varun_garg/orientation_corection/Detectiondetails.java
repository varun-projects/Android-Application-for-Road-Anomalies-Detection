package com.example.varun_garg.orientation_corection;

public class Detectiondetails
{
    double det_lat;

    public Detectiondetails(double det_lat, double det_long, double det_accuracy) {
        this.det_lat = det_lat;
        this.det_long = det_long;
        this.det_accuracy = det_accuracy;
    }

    int ride_id;

    public int getRide_id() {
        return ride_id;
    }

    public void setRide_id(int ride_id) {
        this.ride_id = ride_id;
    }

    public double getDet_lat() {
        return det_lat;
    }

    public void setDet_lat(double det_lat) {
        this.det_lat = det_lat;
    }

    public double getDet_long() {
        return det_long;
    }

    public void setDet_long(double det_long) {
        this.det_long = det_long;
    }

    public double getDet_accuracy() {
        return det_accuracy;
    }

    public void setDet_accuracy(double det_accuracy) {
        this.det_accuracy = det_accuracy;
    }

    double det_long;
    double det_accuracy;


}
