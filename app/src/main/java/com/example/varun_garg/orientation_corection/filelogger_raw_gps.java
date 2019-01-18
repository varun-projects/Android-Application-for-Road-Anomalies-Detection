package com.example.varun_garg.orientation_corection;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class filelogger_raw_gps extends AsyncTask {


    double latitude;
    double longitude;
    public filelogger_raw_gps(double lat, double lon) {
        this.latitude=lat;
        this.longitude=lon;

        Log.d("tag_thread", "in class");


    }

    @Override
    protected Object doInBackground(Object[] objects) {

        //tv.append("\nExternal file system root: "+root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        /////////////////
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM-dd-yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");
        fil.mkdirs();
        fil2.mkdirs();
        Log.d("tag_thread", "in background");

        ///////////
        File file = new File(fil, "raw_gps_different_thread" + fname_date_string + ".txt");

        try {
            FileOutputStream f = new FileOutputStream(file, true);
            // FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
            PrintWriter pw = new PrintWriter(f);
            SimpleDateFormat date_time = new SimpleDateFormat("yyyy  MM  dd  HH  mm  ss");
            String Date_time_string = date_time.format(System.currentTimeMillis());
            //GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(), m_kalmanFilter.getCurrentY());
            String data_accel = latitude + ","+ longitude ;
            //+ "," + m_kalmanFilter.getCurrentKalmanGain0()+ "," + m_kalmanFilter.getCurrentKalmanGain2();
            pw.println(data_accel);

            pw.flush();
            pw.close();
            f.close();
            //  appendFileInternalStorage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("TAG_file", "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }





        return null;
    }



}
