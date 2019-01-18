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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

public class CircularBufferData {
    private int maxSize;
    private int front = 0;
    private int rear = 0;
    private int bufLen = 0;
    private char[] buf;

    LinkedList<BufferDataItem> m_bufferDataQueue;



    Detectiondetails detection;
    //= new PriorityBlockingQueue<>();

    /**
     * constructor
     **/
    public CircularBufferData(int size) {
        maxSize = size;
        front = rear = 0;
        rear = 0;
        bufLen = 0;
        buf = new char[maxSize];
        m_bufferDataQueue = new LinkedList<>();
    }

    /**
     * function to get size of buffer
     **/
    public int getSize() {
        return bufLen;
    }


    /**
     * function to clear buffer
     **/
    public void clear() {
        front = rear = 0;
        rear = 0;
        bufLen = 0;
        buf = new char[maxSize];
        m_bufferDataQueue.clear();
    }


    /**
     * function to check if buffer is empty
     **/
    public boolean isEmpty() {
        return m_bufferDataQueue.size() == 0;

    }

    /**
     * function to check if buffer is full
     **/
    public boolean isFull() {
        return m_bufferDataQueue.size() == maxSize;
    }


    /**
     * insert an element
     **/
    public void insert(BufferDataItem c) {
        if (!isFull()) {
            bufLen++;
            rear = (rear + 1) % maxSize;
            m_bufferDataQueue.add(c);
            //buf[rear] = c;

        } else {
            System.out.println("Error : Underflow Exception 000");
            //m_bufferDataQueue.clear();
        }
    }

    /**
     * delete an element
     **/
    /*
    public BufferItem delete() {
        if (!isEmpty()) {
            bufLen--;
            front = (front + 1) % maxSize;
            return m_bufferDataQueue.poll();
        } else {
            System.out.println("Error : Underflow Exception 111");
            return null;
        }
    }
    */

    /**
     * function to print buffer
     **/
    public String display() {
        Log.d("TAG_buffer", "\nBuffer : ");
        //for (int i = 0; i < maxSize; i++)
        //Log.d("TAG_buffer_data", String.valueOf(m_bufferDataQueue.toString()));
        Log.d("TAG_buffer", String.valueOf(m_bufferDataQueue.element().rawAccZ));
        return String.valueOf(m_bufferDataQueue.element().rawAccZ);
    }

    ///// make a function which write all the buffer data to the file here



    public Detectiondetails getDetection() {
        return detection;
    }

    public void setDetection(Detectiondetails detection) {
        this.detection = detection;
    }

    void write_buffer_to_storage() {
        ////////////////////////////////////
        File root = android.os.Environment.getExternalStorageDirectory();
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM,dd,yyyy,hh");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");


        for (int i = 0; i < maxSize; i++) {
            if (!isEmpty()) {
                BufferDataItem getdata = m_bufferDataQueue.poll();
                BufferWTask bwtask = new BufferWTask((float)getdata.rawAccX, (float)getdata.rawAccY,(float)getdata.rawAccZ,(float)getdata.corrected_AccX,(float)getdata.corrected_AccY,(float)getdata.corrected_AccZ,(float)getdata.orientx,(float)getdata.orienty,(float)getdata.orientz,getdata.gpsLat, getdata.gpsLon,getdata.posErr,(float)getdata.speed,getdata.course,getdata.date_time_str);
                bwtask.execute();

            }
        }


    }


    class BufferWTask extends AsyncTask<Void, Void, String> /* Params, Progress, Result */ {

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

        BufferWTask(float id ,float id2,float id3,float id4 ,float id5,float id6,float id7 ,float id8,float id9, double lat, double longi ,double gps_accuracy,float speed_vehicle,double beari,String fname_date_string) {
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
            SimpleDateFormat fname_dat = new SimpleDateFormat("MM,dd,yyy,hh");
            String fname = fname_dat.format(System.currentTimeMillis());
            Detectiondetails dw = getDetection();
            File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_detections"  + "_" + fname);
            //File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_detections_rideId" + "_" + fname);
            fil.mkdirs();

            Log.d("tag_thread", "in background");


            ///////////

            //
           if(dw!=null) {
                File file = new File(fil, "det" + dw.det_lat + "_" + dw.det_long + "_" + dw.det_accuracy + "_" + fname + ".txt");
                //File file = new File(fil, "det" + "_" + fname + ".txt");
                try {
                    FileOutputStream f = new FileOutputStream(file, true);
                    PrintWriter pw = new PrintWriter(f);
                    String data_accel = id + "," + id2 + "," + id3 + "," + id4 + "," + id5 + "," + id6 + "," + id7 + "," + id8 + "," + id9 + "," + lat + "," + longi + "," + gps_accuracy + "," + speed_vehicle + "," + beari + "," + fname_date_string;
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
           }
            return String.valueOf("yy");
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("Completed", String.valueOf(id));
        }




    }


}

