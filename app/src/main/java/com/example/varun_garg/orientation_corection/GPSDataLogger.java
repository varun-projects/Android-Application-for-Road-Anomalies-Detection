package com.example.varun_garg.orientation_corection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

//import com.elvishew.xlog.XLog;
import com.example.varun_garg.orientation_corection.Utils;



public class GPSDataLogger implements LocationListener {

    private LocationManager m_locationManager = null;
    private Context m_context = null;
    private String m_lastLoggedGPSMessage;

    public String getLastLoggedGPSMessage() {
        return m_lastLoggedGPSMessage;
    }

    public GPSDataLogger(LocationManager locationManager,
                         Context context) {
        m_locationManager = locationManager;
        m_context = context;
    }

    public boolean start() {
        if (m_locationManager == null)
            return false;
        if (ActivityCompat.checkSelfPermission(m_context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_locationManager.removeUpdates(this);

            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Utils.GPS_MIN_TIME, Utils.GPS_MIN_DISTANCE, this);
            return true;
        }
        return false;
    }

    public void stop() {
        if (m_locationManager == null)
            return;
        m_locationManager.removeUpdates(this);
    }


    @Override
    public void onLocationChanged(Location loc) {
        double speedAccuracyMpS = 0.1 * loc.getAccuracy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && loc.hasAccuracy()) {
            speedAccuracyMpS = loc.getSpeedAccuracyMetersPerSecond();
        }

//        long now = System.currentTimeMillis();
        long now = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            now = loc.getElapsedRealtimeNanos() / 1000000;
        }
        m_lastLoggedGPSMessage = String.format("%d%d GPS : pos lat=%f, lon=%f, alt=%f, hdop=%f, speed=%f, bearing=%f, sa=%f",
                Utils.LogMessageType.GPS_DATA.ordinal(),
                now, loc.getLatitude(),
                loc.getLongitude(), loc.getAltitude(), loc.getAccuracy(),
                loc.getSpeed(), loc.getBearing(), speedAccuracyMpS);

      //Log to file
      //  XLog.i(m_lastLoggedGPSMessage);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
