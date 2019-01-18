package com.example.varun_garg.orientation_corection;

import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class orientation_correction_asynctask extends AsyncTask {


    float[] orig_accel_data  = new float[3];
    float[] grav_accel_data  = new float[3];
    float[] magField_data    = new float[3];
    float[] earthAcc         = new float[16];



    public orientation_correction_asynctask(float[] orig_accel_data, float[] grav_accel_data , float[] magField_data) {
        this.grav_accel_data=grav_accel_data;
        this.orig_accel_data=orig_accel_data;
        this.magField_data=magField_data;

        Log.d("tag_thread", "in class");


    }

    @Override
    protected Object doInBackground(Object[] objects) {

            //Initialization of rotational matrix returned by function getRotationMatrix
            float[] R = new float[16];
            //Get rotation matrix, R, by inputting gravity and magnetic field values, returns boolean success or fail state
            if(SensorManager.getRotationMatrix(R, null, grav_accel_data, magField_data)) {

                //Must add 0 to acceleration vector as MultiplyMV function requires vector of length 4
                float[] orig_accel_data1 = new float[] {orig_accel_data[0], orig_accel_data[1], orig_accel_data[2], 0};

                //Initialize inverse matrix array and earth acceleration matrix array
                float[] inverse = new float[16];
                //earthAcc = new float[16];

                //Find inverse of rotation matrix and preform multiplication
                android.opengl.Matrix.invertM(inverse, 0, R, 0);
                android.opengl.Matrix.multiplyMV(earthAcc, 0, inverse, 0, orig_accel_data1, 0);

                //if(getLatitute()!= 0 && orig_accel_data[0] != 0) write_regular_acceleration_ToSDFile();

                Log.d("tag_thread", "in do it in background");


                    /*
                     Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                        loc.getAccuracy(),
                        Utils.nano2milli(loc.getElapsedRealtimeNanos()),
                        Utils.DEFAULT_VEL_FACTOR,
                        Utils.DEFAULT_POS_FACTOR,
                     */
                //write_to_firebase();

                /*
                for (int pp=0;pp<=1000000;pp++){
                    Log.d("tag_delay","delay");
                }
                */



            }

            return earthAcc;
        }





    }




