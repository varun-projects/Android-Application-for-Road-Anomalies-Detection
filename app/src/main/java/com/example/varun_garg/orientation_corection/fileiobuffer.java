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

class send_to_io_bufferWTask extends AsyncTask<Void, Void, String> /* Params, Progress, Result */ {

    final float id;
    final float id2;
    final float id3;
    final float id4;
    final float id5;
    final float id6;
    final float id7;
    final float id8;
    final float id9;

    String fname_date_string;
    double lat;
    double longi;
    double beari;
    double gps_accuracy;
    float speed_vehicle;

    Ride_number rideId;

    public Ride_number getRideId() {
        return rideId;
    }

    public void setRideId(Ride_number rideId) {
        this.rideId = rideId;
    }




    send_to_io_bufferWTask(float id ,float id2,float id3,float id4 ,float id5,float id6,float id7 ,float id8,float id9, double lat, double longi ,double gps_accuracy,float speed_vehicle,double beari) {
        this.id  = id;
        this.id2 =id2;
        this.id3 =id3;
        this.id4 =id4;
        this.id5 =id5;
        this.id6 =id6;

        this.id7 =id7;
        this.id8 =id8;
        this.id9 =id9;


        this.lat=lat;
        this.longi=longi;
        this.gps_accuracy=gps_accuracy;
        this.speed_vehicle=speed_vehicle;
        this.beari=beari;
        this.fname_date_string = fname_date_string;
    }

    @Override
    protected String doInBackground(Void... params) {
        Log.d("multthread", "doInBackground: entered");
        Log.d("multithread", "doInBackground: is about to finish, taskExecutionNumber = ");


        //write_to_file();
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM,dd,yyy,hh");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_detections");
        fil.mkdirs();

        Log.d("tag_thread", "in background");

        ///////////
        //File file = new File(fil, "det"+lat+"_"+longi +"_"+ gps_accuracy +fname_date_string + ".txt");
        Ride_number rr = getRideId();
        File file = new File(fil, "queue_data_ride" +rideId+ fname_date_string + ".txt");

        try {
            FileOutputStream f = new FileOutputStream(file, true);
            PrintWriter pw = new PrintWriter(f);
            SimpleDateFormat date_time = new SimpleDateFormat("hh,mm,ss,SS");
            String Date_time_stringss = date_time.format(System.currentTimeMillis());
            //String data_accel =   orig_accel_data[0]+","+orig_accel_data[1]+","+orig_accel_data[2]+"," + earthAcc[0] + ","+earthAcc[1]+ "," + earthAcc[2]+ ","+orientation_data[0] +","+ orientation_data[1]+ "," + orientation_data[2] + "," + getLatitute() + ","+ getLongitude()+ ","+ getAccuracy()+","+ getSpeed_vehicle() +"," +  getBearing()+ ","+ Date_time_stringss;
            //String data_accel = id + "," + id2 + "," + id3 + "," + id4 + "," + id5 + "," + id6 + "," + id7 + "," + id8 + "," + id9 + "," +  Date_time_stringss;
            String data_accel   =  id + ","+ id2  + "," + id3 +","+id4 + ","+ id5  + "," + id6 +"," + id7 +"," +id8 +"," +id9+ "," + lat +"," + longi +"," + gps_accuracy +"," + speed_vehicle +"," + beari +"," +Date_time_stringss;
            pw.println(data_accel);
            pw.flush();
            pw.close();
            f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("TAG_file", "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ///
        return String.valueOf(id);
    }

    @Override
    protected void onPostExecute(String result) {

        Log.d("Completed", String.valueOf(id));
    }

    private void log(String msg) {
        Log.d("TestTask #" + id, msg);
    }


    void write_to_file() {
    }


}