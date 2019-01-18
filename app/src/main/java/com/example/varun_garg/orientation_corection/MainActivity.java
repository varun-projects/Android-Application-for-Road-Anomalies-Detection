package com.example.varun_garg.orientation_corection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {

    private String provider;
    private GPSAccKalmanFilter m_kalmanFilter;
    SensorManager senSensorManager;
    Sensor senAccelerometer;
    Sensor senLinAccelerometer;
    Sensor senGrav;
    Sensor senGyroscope;
    Sensor geo_magnetic_sensor;
    Sensor senGame;
    Sensor senMag;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    LocationManager locationManager;
    private long lastUpdate_gyro = 0;
    private float last_a, last_b, last_c;

    //Initialize globally to hold data throughout sensor iterations
    float[] orig_accel_data  = new float[3];
    float   accel_data_prev  =0;
    float[] grav_accel_data  = new float[3];
    float[] magField_data    = new float[3];
    float[] earthAcc         = new float[16];
    float[] orientation_data = new float[3];
    public double latitute;
    public double longitude ;
    public double altitude;
    public double elapsedRealtimeNanos;
    public double bearing;
    public double bearing_accuracy;
    public Boolean hasbear;
    public float heading;

    public double accuracy;
    public float speed_vehicle;

    String model_number;
    ///// initialization code
    public double sum_lat;
    public double sum_long;
    public double sum_xvel =0;
    public double sum_yvel =0;

    public double avg_lat;
    public double avg_long;
    public double avg_xvel;
    public double avg_yvel;
    ArrayList <Double> latitude_samples  = new ArrayList();
    ArrayList <Double> longitude_samples = new ArrayList();
    ArrayList <Double> xvel_samples      = new ArrayList();
    ArrayList <Double> yvel_samples      = new ArrayList();

    /// Avoid the threshold
    private static final double SHAKE_THRESHOLD = 800;

    public TextView acceleration_view;
    public TextView orient;
    public TextView corrected_acceleration;
    Button file_button;
    FirebaseDatabase db    ;
    DatabaseReference myRef;
    StorageReference  mStorageRef;
    SensorCalibrator m_sensorCalibrator;

    public boolean flag =FALSE;
    public int counter=0;
    public int kcounter=0;

    ////// circular buffer variables
    CircularBufferData cirbuff;
    boolean cb_insert_flag =TRUE;

    BufferItem bi_global;

    public int kalman_control =0;

    GeomagneticField geoField;

    int loc_c =0;
    Location temp = new Location(LocationManager.GPS_PROVIDER);


    private Queue<SensorGpsDataItem> m_sensorDataQueue = new PriorityBlockingQueue<>();

     //Queue<BufferItem> file_sensorDataQueue = new PriorityBlockingQueue<BufferItem>();

     //LinkedList<BufferDataItem> file_DataQueue = new LinkedList<BufferDataItem>();
     LinkedList<BufferDataItem> file_sensorDataQueue = new LinkedList<BufferDataItem>();



     //initalization of variables

     String  vehicle_t     = "typ1";
     String  plocation_t   = "typ1";
     String  road_hazard_t = "typ1";
     String  encounter_t   = "typ1";
     String   trail_speed_t= "typ1";
     int      rideId=1;

    public String getRoad_hazard_t() {
        return road_hazard_t;
    }

    public void setRoad_hazard_t(String road_hazard_t) {
        this.road_hazard_t = road_hazard_t;
    }

    public String getTrail_speed_t() {
        return trail_speed_t;
    }

    public void setTrail_speed_t(String trail_speed_t) {
        this.trail_speed_t = trail_speed_t;
    }


    public String getVehicle_t() {
        return vehicle_t;
    }

    public void setVehicle_t(String vehicle_t) {
        this.vehicle_t = vehicle_t;
    }

    public String getPlocation_t() {
        return plocation_t;
    }

    public void setPlocation_t(String plocation_t) {
        this.plocation_t = plocation_t;
    }

    public String getEncounter_t() {
        return encounter_t;
    }

    public void setEncounter_t(String encounter_t) {
        this.encounter_t = encounter_t;
    }




    filelogger_raw_gps raw_gps_log;
    int predict_counter=0;
    int buffer_len =1200;
    //// spinner definations
    Spinner spinner_vehicle_type;
    Spinner cellphone_location;
    Spinner road_hazard;
    Spinner encounter;
    Spinner speed;




    //@SuppressLint({"MissingPermission", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acceleration_view = findViewById(R.id.acccel);
        orient = findViewById(R.id.orientation);
        corrected_acceleration = findViewById(R.id.corrected_accel);
        file_button =findViewById(R.id.file_up);

        ///////////////////////////////////////////////////////////////////////////////////Sensor code
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ///////////////////////////////////////////////////////////////// GPS code///////////////////////////////////////////////////////////
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Initialize the location fields
        // default
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setBearingRequired(true);
        provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location   location = locationManager.getLastKnownLocation(provider);

        LocationListener mlocListener = new MyLocationListener();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mlocListener);

        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            mlocListener.onLocationChanged(location);


        } else {
            Log.d("TAGE_error", "Location not av");
            //longitudeField.setText("Location not available");
        }
////////////////////////////////////////////////////////////////////////////
        // calling the sensors needed
        //senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senLinAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //senLinAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGame = senSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        senMag = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senGrav = senSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        // defining the sampling period of sensors
        //senSensorManager.registerListener(this, senGyroscope ,10000);
        senSensorManager.registerListener(this, senMag ,Utils.hertz2periodUs(100));
        senSensorManager.registerListener(this, senLinAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        senSensorManager.registerListener(this, senGrav ,Utils.hertz2periodUs(100));
        //senSensorManager.registerListener(this, senAccelerometer, 10000);
        senSensorManager.registerListener(this, senGame, 20000);



        ////////////////// firebase config ////

        db      = FirebaseDatabase.getInstance();
        myRef  = db.getReference();

        mStorageRef = FirebaseStorage.getInstance().getReference();

       //model_number = Build.MODEL + Build.getSerial() +Build.BRAND +Build. ;

       //Log.d("model" ,model_number);
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Log.d("device id",telephonyManager.getDeviceId());

        //Log.d("tag_caliberation", m_sensorCalibrator.getCalibrationStatus());

        cirbuff = new CircularBufferData(buffer_len);

        setRideId(1);

   /*
        Spinner spinner_vehicle_type;
        Spinner cellphone_location;
        Spinner road_hazard;
        Spinner encounter;
        Spinner speed;

        <string-array name="phoneloc_array">
        <string-array name="encounter_array">
        <string-array name="road_condition_array">
        <string-array name="speed_array">



    android:id="@+id/vehicle_spinner"

    android:id="@+id/celllocation_spinner"

    android:id="@+id/roadhazad_spinner"

    android:id="@+id/encounter_spinner"

    android:id="@+id/speed_spinner"


    */
        spinner_vehicle_type = (Spinner) findViewById(R.id.vehicle_spinner);
        ArrayAdapter<CharSequence> veh_ad =ArrayAdapter.createFromResource(this, R.array.vehicle_array ,android.R.layout.simple_spinner_dropdown_item);
        veh_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_vehicle_type.setAdapter(veh_ad);

        spinner_vehicle_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sel_veh = adapterView.getItemAtPosition(i).toString();
                orient.setText("rideID is  "+rideId + " spinner_value1" + sel_veh );
                Log.d("spinner_val",sel_veh);
                setVehicle_t(sel_veh);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        cellphone_location = (Spinner) findViewById(R.id.celllocation_spinner);
        ArrayAdapter<CharSequence> cell_loc_ad =ArrayAdapter.createFromResource(this, R.array.phoneloc_array ,android.R.layout.simple_spinner_dropdown_item);
        cell_loc_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cellphone_location.setAdapter(cell_loc_ad);

        cellphone_location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sel_cel_loc = adapterView.getItemAtPosition(i).toString();
                orient.setText("rideID is  "+rideId + " spinner_value1" + sel_cel_loc );
                Log.d("spinner_val",sel_cel_loc);
                setPlocation_t(sel_cel_loc);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        encounter = (Spinner) findViewById(R.id.encounter_spinner);
        ArrayAdapter<CharSequence> encounter_ad =ArrayAdapter.createFromResource(this, R.array.encounter_array ,android.R.layout.simple_spinner_dropdown_item);
        encounter_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encounter.setAdapter(encounter_ad);

        encounter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sel_enc = adapterView.getItemAtPosition(i).toString();
                orient.setText("rideID is  "+rideId + " spinner_value1" + sel_enc);
                Log.d("spinner_val",sel_enc);
                setEncounter_t(sel_enc);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        road_hazard = (Spinner) findViewById(R.id.roadhazad_spinner);
        ArrayAdapter<CharSequence> hazard_ad =ArrayAdapter.createFromResource(this, R.array.road_condition_array ,android.R.layout.simple_spinner_dropdown_item);
        hazard_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        road_hazard.setAdapter(hazard_ad);

        road_hazard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sel_road = adapterView.getItemAtPosition(i).toString();
                orient.setText("rideID is  "+rideId + " spinner_value1" + sel_road);
                Log.d("spinner_val",sel_road);
                setRoad_hazard_t(sel_road);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });







        speed = (Spinner) findViewById(R.id.speed_spinner);
        ArrayAdapter<CharSequence> speed_ad =ArrayAdapter.createFromResource(this, R.array.speed_array ,android.R.layout.simple_spinner_dropdown_item);
        speed_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speed.setAdapter(speed_ad);

        speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sel_speed = adapterView.getItemAtPosition(i).toString();
                orient.setText("rideID is  "+rideId + " spinner_value1" + sel_speed);
                Log.d("spinner_val",sel_speed);
                setTrail_speed_t(sel_speed);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        send_to_io_buffer_to_storage_low_memory();
        /// add code to restart the app and clear memory
        // do it later

    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Log.d("tag_caliberation", m_sensorCalibrator.getCalibrationStatus());
        //Log.d("tag", "hello");
        long now = android.os.SystemClock.elapsedRealtime();
        long nowMs = now ;
        Log.d("timestamp_check" , String.valueOf(nowMs));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            long now_check = SystemClock.elapsedRealtimeNanos();
             long now_check_milli =Utils.nano2milli(now_check);
            Log.d("timestamp_check_nano" , String.valueOf(now_check_milli));
        }

        Sensor mySensor = sensorEvent.sensor;
            //Get Gravity data
        if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            for(int i=0; i<3; i++){
                grav_accel_data[i] = sensorEvent.values[i];
            }
        }
            //Get Linear Acceleration data
        else if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            for(int i=0; i<3; i++){
                orig_accel_data[i] = sensorEvent.values[i];
            }



            SimpleDateFormat date_time = new SimpleDateFormat("hh,mm,ss,SS");
            String Date_time_stringss = date_time.format(System.currentTimeMillis());
            //TestTask task = new TestTask(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2],earthAcc[0],earthAcc[1],earthAcc[2],orientation_data[0],orientation_data[1],orientation_data[2],getLatitute(),getLongitude(),getAccuracy(),getSpeed_vehicle(),getBearing(),Date_time_stringss);
            //task.execute();


            //write_regular_acceleration_ToSDFile();
            buffer_entry();


        }
            //Get Magnetic Field Data
        else if (mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            for(int i=0; i<3; i++){
                magField_data[i] = sensorEvent.values[i];
            }
        }
        else if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {
            for(int i=0; i<3; i++){
                orientation_data[i] = sensorEvent.values[i];
            }
            //corrected_acceleration.setText("Azimuth, Pitch ,Roll "+ orientation_data[0] + " , " + orientation_data[1] + " , " + orientation_data[2] );
        }

        //orientation_correction_asynctask orient_async =new orientation_correction_asynctask(orig_accel_data,grav_accel_data,magField_data);
        //orient_async.execute(); ///need to get the data back to log into the file
        orientation_asynctask or_async =new orientation_asynctask();
        or_async.execute();



        if(orig_accel_data[2] > 3 ){
            if(getLongitude()!=0){
            cb_insert_flag = TRUE;
            Log.d("tag_buff_get_size" , String.valueOf(cirbuff.getSize()));

            //if(getRideId() == Integer.parseInt(null))setRideId(1);
            Detectiondetails dd =new Detectiondetails(getLatitute(),getLongitude(),getAccuracy());
            cirbuff.setDetection(dd);

            }

        }

        Log.d("TAG_buff_hell", "working");
        Log.d("TAG_buff_z", String.valueOf(orig_accel_data[2]));
        //buffer_entry(now);

        // send_to_firebase_storage();

        //////// code to perform the kalman algorithm
        /*
        SensorGpsDataItem sdi;
        double lastTimeStamp =0.0;
        if(m_kalmanFilter != null) {

            kcounter++;

            while ((sdi = m_sensorDataQueue.poll()) != null) {


                //Log.d("tag_t_sensor_gps_d" , String.valueOf(sdi.compareTo(sdi)));
                Log.d("tag_queue" , String.valueOf(sdi.gpsLat));
                if(sdi.gpsLat !=361.0 ){
                    Log.d("tag_queue_update" , "no 361");
                    handleUpdate(sdi);
                }


                if (sdi.getTimestamp() < lastTimeStamp) {
                   // Log.d("tag_time_stamp")
                    continue;
                }
                lastTimeStamp = sdi.getTimestamp();

                //warning!!!
                if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED) {

                    handlePredict(sdi);
                    Log.d("kf_func", "predict called");

                    predict_counter++;
                    //GeoPoint kp=locationAfterPredictStep(sdi);
                    //if(predict_counter %4==0)
                        locationAfterPredictStep(sdi);
                    //if(predict_counter %10==0)
                    write_prior_estimates_ToSDFile();
                    //bi_global = new BufferItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], orientation_data[0],orientation_data[1],orientation_data[2],getSpeed_vehicle(), getAccuracy(), now ,getLatitute(),getLongitude());
                    SimpleDateFormat fname_date_time = new SimpleDateFormat("HH,mm,ss,SS");
                    String fname_date_string = fname_date_time.format(System.currentTimeMillis());
                    //bi_global = new BufferItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], earthAcc[0],earthAcc[1],earthAcc[2], orientation_data[0], orientation_data[1], orientation_data[2],getSpeed_vehicle(), getAccuracy(), fname_date_string ,kp.Latitude, kp.Longitude ,getLatitute() ,getLongitude(),now);
                    //write_estimates_10hz_ToSDFile(kcounter,sdi,bi_global);
                    //buffer_entry(now);

                    if(predict_counter ==102){

                        SensorGpsDataItem sdi2 = new SensorGpsDataItem(
                                Utils.nano2milli((long) getElapsedRealtimeNanos()), getLatitute(), getLongitude(), getAltitude(),
                                SensorGpsDataItem.NOT_INITIALIZED,
                                SensorGpsDataItem.NOT_INITIALIZED,
                                SensorGpsDataItem.NOT_INITIALIZED,
                                getSpeed_vehicle(),
                                getBearing(),
                                getAccuracy(),
                                getAccuracy()*0.1,
                                0.0);
                        m_sensorDataQueue.add(sdi2);
                        //handleUpdate(sdi2);

                        predict_counter =1;

                    }

                } else {

                    Log.d("kf_func_up", "update called");

                    handleUpdate(sdi);
                    GeoPoint ku= locationAfterUpdateStep(sdi);
                    write_posterier_estimates_ToSDFile();

                    SimpleDateFormat fname_date_time = new SimpleDateFormat("HH,mm,ss");
                    String fname_date_string = fname_date_time.format(System.currentTimeMillis());
                    /// /  if(getLatitute()!=null && orig_accel_data[0] != 0) write_regular_acceleration_ToSDFile();
                   // bi_global = new BufferItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], earthAcc[0],earthAcc[1],earthAcc[2],getSpeed_vehicle(), getAccuracy(), now ,getLatitute(),getLongitude());
                    //bi_global = new BufferItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], earthAcc[0],earthAcc[1],earthAcc[2], orientation_data[0], orientation_data[1], orientation_data[2],getSpeed_vehicle(), getAccuracy(), fname_date_string ,ku.Latitude, ku.Longitude ,getLatitute() ,getLongitude(),now);

                    //write_estimates_10hz_ToSDFile(kcounter,sdi,bi_global);
                    // publishProgress(loc);
                }
            }

        }

        */


    }


    public void orientation_correction_module(float[] orig_accel_data, float[] grav_accel_data , float[] magField_data){
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

            Log.d("TAG_module_function_run","running");
            //if(getLatitute()!= 0 && orig_accel_data[0] != 0) write_regular_acceleration_ToSDFile();


                    /*
                     Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                        loc.getAccuracy(),
                        Utils.nano2milli(loc.getElapsedRealtimeNanos()),
                        Utils.DEFAULT_VEL_FACTOR,
                        Utils.DEFAULT_POS_FACTOR,
                     */
            //write_to_firebase();


        }

    }


    public class orientation_asynctask extends AsyncTask {


        /*
        float[] orig_accel_data  = new float[3];
        float[] grav_accel_data  = new float[3];
        float[] magField_data    = new float[3];
        float[] earthAcc         = new float[16];



        public orientation_asynctask(float[] orig_accel_data, float[] grav_accel_data , float[] magField_data) {
            this.grav_accel_data=grav_accel_data;
            this.orig_accel_data=orig_accel_data;
            this.magField_data=magField_data;

            Log.d("tag_thread", "in class");


        }
        */

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

            return null;
        }





    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


    public  void  buffer_entry(){
        //long now = android.os.SystemClock.elapsedRealtime();
        SimpleDateFormat fname_date_time = new SimpleDateFormat("hh,mm,ss,SS");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        Log.d("time_stamp", String.valueOf(System.currentTimeMillis()));
        //BufferItem bi_pridict  = new BufferItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], earthAcc[0],earthAcc[1],earthAcc[2], orientation_data[0], orientation_data[1], orientation_data[2],getLatitute(), getLongitude(), getAccuracy() ,getSpeed_vehicle(), getBearing() ,getElapsedRealtimeNanos() ,fname_date_string);
        BufferDataItem bi_pridict  = new BufferDataItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], earthAcc[0],earthAcc[1],earthAcc[2], orientation_data[0], orientation_data[1], orientation_data[2],getLatitute(), getLongitude(), getAccuracy() ,getSpeed_vehicle(), getBearing() ,System.currentTimeMillis() ,fname_date_string);
        cirbuff.insert(bi_pridict);
        file_sensorDataQueue.add(bi_pridict);

        Log.d("TAG_buffer_data_size", String.valueOf(cirbuff.getSize()));

        if(cb_insert_flag ==TRUE && cirbuff.getSize() ==buffer_len -10){

            cirbuff.write_buffer_to_storage();
            Log.d("TAG_buffer" ,"can write the buffer data to the sd card");
            cb_insert_flag =FALSE;

        }

        if(cirbuff.getSize() >=  buffer_len) cirbuff.clear();



    }

    private void write_regular_acceleration_ToSDFile() {


        ////////////////////////////////////
        File root = android.os.Environment.getExternalStorageDirectory();
        //tv.append("\nExternal file system root: "+root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        /////////////////
        //File dir = new File (root.getAbsolutePath() + "/Road_monitor1");
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM-dd-yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");

        Log.d("tag_files", String.valueOf(fil.listFiles()));

        fil.mkdirs();
        fil2.mkdirs();

        ///////////
        //File file = new File(fil, "acceleration_reguler.txt");
        File file = new File(fil, "acceleration_reguler" + fname_date_string + ".txt");


        try {
            FileOutputStream f = new FileOutputStream(file, true);
            // FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
            PrintWriter pw = new PrintWriter(f);
            SimpleDateFormat date_time = new SimpleDateFormat("hh,mm,ss,SS");
            String Date_time_string = date_time.format(System.currentTimeMillis());
           // GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(), m_kalmanFilter.getCurrentY());
            String data_accel =   orig_accel_data[0]+","+orig_accel_data[1]+","+orig_accel_data[2]+"," + earthAcc[0] + ","+earthAcc[1]+ "," + earthAcc[2]+ ","+orientation_data[0] +","+ orientation_data[1]+ "," + orientation_data[2] + "," + getLatitute() + ","+ getLongitude()+ ","+ getAccuracy()+","+ getSpeed_vehicle() +"," +  getBearing()+ ","+ Date_time_string;
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
        //tv.append("\n\nFile written to "+file);
    }


    private void write_prior_estimates_ToSDFile() {

        ////////////////////////////////////
        File root = android.os.Environment.getExternalStorageDirectory();
        //tv.append("\nExternal file system root: "+root);
        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        /////////////////
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM-dd-yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");
        fil.mkdirs();
        fil2.mkdirs();
        ///////////
        File file = new File(fil, "prior_estimates" + fname_date_string + ".txt");
        try {
            FileOutputStream f = new FileOutputStream(file, true);
            // FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
            PrintWriter pw = new PrintWriter(f);
            SimpleDateFormat date_time = new SimpleDateFormat("ss SS");
            String Date_time_string = date_time.format(System.currentTimeMillis());
            GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentXprior(), m_kalmanFilter.getCurrentYprior());
            String data_accel = pp.Latitude + ","+ pp.Longitude + "," + Date_time_string;
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
        //tv.append("\n\nFile written to "+file);
    }


    private void write_raw_gps_ToSDFile() {

        ////////////////////////////////////
        File root = android.os.Environment.getExternalStorageDirectory();
        //tv.append("\nExternal file system root: "+root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        /////////////////
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM-dd-yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");
        fil.mkdirs();
        fil2.mkdirs();
        ///////////
        File file = new File(fil, "raw_gps" + fname_date_string + ".txt");

        try {
            FileOutputStream f = new FileOutputStream(file, true);
            // FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
            PrintWriter pw = new PrintWriter(f);
            SimpleDateFormat date_time = new SimpleDateFormat("yyyy  MM  dd  HH  mm  ss");
            String Date_time_string = date_time.format(System.currentTimeMillis());
            //GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(), m_kalmanFilter.getCurrentY());
            String data_accel = getLatitute() + ","+ getLongitude() ;
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
        //tv.append("\n\nFile written to "+file);
    }

    private void write_posterier_estimates_ToSDFile() {

        ////////////////////////////////////
        File root = android.os.Environment.getExternalStorageDirectory();
        //tv.append("\nExternal file system root: "+root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        /////////////////
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM-dd-yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");
        fil.mkdirs();
        fil2.mkdirs();
        ///////////
        File file = new File(fil, "posterier_estimates" + fname_date_string + ".txt");

        try {
            FileOutputStream f = new FileOutputStream(file, true);
            // FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
            PrintWriter pw = new PrintWriter(f);
            SimpleDateFormat date_time = new SimpleDateFormat("yyyy  MM  dd  HH  mm  ss");
            String Date_time_string = date_time.format(System.currentTimeMillis());
            GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(), m_kalmanFilter.getCurrentY());
            String data_accel = pp.Latitude + ","+ pp.Longitude ;
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
        //tv.append("\n\nFile written to "+file);
    }


    private void write_estimates_10hz_ToSDFile(int counter ,SensorGpsDataItem sd ,BufferItem bufferI) {

        ////////////////////////////////////
        File root = android.os.Environment.getExternalStorageDirectory();
        //tv.append("\nExternal file system root: "+root);
        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        /////////////////
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM-dd-yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");
        fil.mkdirs();
        fil2.mkdirs();
        ///////////
        File file = new File(fil, "estimates_10hz" + fname_date_string + ".txt");

        try {
            FileOutputStream f = new FileOutputStream(file, true);
            // FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
            PrintWriter pw = new PrintWriter(f);
            //SimpleDateFormat date_time = new SimpleDateFormat("yyyy  MM  dd  HH  mm  ss");
            SimpleDateFormat date_time = new SimpleDateFormat("ss");
            String Date_time_string = date_time.format(System.currentTimeMillis());

            GeoPoint pp =null;
            String data_accel = " ";


                if (counter % 10 == 0) {
                    pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentXprior(), m_kalmanFilter.getCurrentYprior());
                } else if (sd.getGpsLat() != SensorGpsDataItem.NOT_INITIALIZED) {

                    pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(), m_kalmanFilter.getCurrentY());
                }

                if (pp != null) {
                    //data_accel = pp.Latitude + "," + pp.Longitude + "," + Date_time_string;
                    data_accel =  bufferI.rawAccX +  bufferI.rawAccY +bufferI.rawAccZ + bufferI.corrected_AccX +bufferI.corrected_AccY +bufferI.corrected_AccZ + pp.Latitude + "," + pp.Longitude + "," + Date_time_string;
                    pw.println(data_accel);
                }


            //+ "," + m_kalmanFilter.getCurrentKalmanGain0()+ "," + m_kalmanFilter.getCurrentKalmanGain2();


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
        //tv.append("\n\nFile written to "+file);
    }


    void write_to_firebase(){

        SimpleDateFormat date_time =  new SimpleDateFormat("HH mm ss");
        String Date_time_string = date_time.format(System.currentTimeMillis());
        //myRef.push().child("Professor Wick's Car").child(Date_time_string).child("Sensor_data").setValue(getLatitute() + "  " + getLongitude());
        myRef.push().child("Sensor_data").setValue(getLatitute() + "  " + getLongitude());

    }

    public void send_to_firebase_storage(View view){

        SimpleDateFormat date_time =  new SimpleDateFormat("MM-dd-yyyy");
        String Date_time_string = date_time.format(System.currentTimeMillis());

        //String path =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/acc.txt";

        //String path   =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +"/sensor_data"+ "/acceleration_reguler" + Date_time_string + ".txt" ;
        //String path   =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +"/sensor_data"+ "/prior_estimates" + Date_time_string + ".txt" ;
        String path   =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +"/sensor_data"+ "/buffer_data" + Date_time_string + ".txt" ;

        //
        Log.d("file sending path" , path);
        //Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        Uri file = Uri.fromFile(new File(path));
        String fname = "sensor_data" + Date_time_string;
        StorageReference riversRef = mStorageRef.child("Professor Wick").child(fname);
        //StorageReference riversRef = mStorageRef.child("Test").child(fname);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("file sending path" , "Successfull");

                        orient.setText("upload Successful");
                        Toast.makeText(getApplicationContext(), "Sent successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("file sending path" , "error");
                        Toast.makeText(getApplicationContext(), "Sending Error Please resend",
                                Toast.LENGTH_SHORT).show();

                    }
                });
    }


    public void send_prior_to_firebase_storage(View view){

        SimpleDateFormat date_time =  new SimpleDateFormat("MM-dd-yyyy");
        String Date_time_string = date_time.format(System.currentTimeMillis());

        //String path =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/acc.txt";

        //String path   =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +"/sensor_data"+ "/acceleration_reguler" + Date_time_string + ".txt" ;
        String path   =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +"/sensor_data"+ "/prior_estimates" + Date_time_string + ".txt" ;
        //String path   =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +"/sensor_data"+ "/buffer_data" + Date_time_string + ".txt" ;

        //
        Log.d("file sending path" , path);
        //Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        Uri file = Uri.fromFile(new File(path));
        String fname = "sensor_data" + Date_time_string;
        StorageReference riversRef = mStorageRef.child("Professor Wick").child(fname);
        //StorageReference riversRef = mStorageRef.child("Test").child(fname);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("file sending path" , "Successfull");

                        orient.setText("upload Successful");
                        Toast.makeText(getApplicationContext(), "Sent successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("file sending path" , "error");
                        Toast.makeText(getApplicationContext(), "Sending Error Please resend",
                                Toast.LENGTH_SHORT).show();

                    }
                });
    }


    public void send_to_io_buffer_to_storage(View view){

        File root = android.os.Environment.getExternalStorageDirectory();
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM,dd,yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getVehicle_t() +"/"+getPlocation_t()+"/"+getRoad_hazard_t()+"/"+getEncounter_t()+"/"+getTrail_speed_t()+"/"+getRideId() );
        fil.mkdirs();
        fil2.mkdirs();
        //File file = new File(fil, "queue_data_ride" +rideId+ fname_date_string + ".txt");
        //File file = new File(fil, "data_ride" +getVehicle_t() +"phone_location_"+getPlocation_t()+ "road_hazard_"+getRoad_hazard_t()+"_encounter_"+getEncounter_t()+"_speed_"+getTrail_speed_t()+".txt");

        File file = new File(fil, "data_ride" +getVehicle_t() +"phone_location_"+getPlocation_t()+"road_hazard_"+getRoad_hazard_t()+"_encounter_"+getEncounter_t()+"_speed_"+getTrail_speed_t()+"_trail_"+getRideId() +rideId+ fname_date_string + ".txt");

        FileOutputStream f = null;
        try {
            f = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(f);
        SimpleDateFormat date_time = new SimpleDateFormat("HH  mm  ss SS");

        if (file_sensorDataQueue.size() != 0) {
        for (int i = 0; i < file_sensorDataQueue.size(); i++) {

                BufferDataItem getdata = file_sensorDataQueue.poll();
                //String data_accel = m_bufferDataQueue.poll().rawAccX + "," + m_bufferDataQueue.poll().rawAccY + "," + m_bufferDataQueue.poll().rawAccZ + "," + m_bufferDataQueue.poll().corrected_AccX + "," + m_bufferDataQueue.poll().corrected_AccY + "," + m_bufferDataQueue.poll().corrected_AccZ + "," + m_bufferDataQueue.poll().azimuth + "," + m_bufferDataQueue.poll().pitch + "," + m_bufferDataQueue.poll().roll + "," + m_bufferDataQueue.poll().gpsLon + "," + m_bufferDataQueue.poll().gpsLon + "," + m_bufferDataQueue.poll().posErr + "," + m_bufferDataQueue.poll().speed + "," + m_bufferDataQueue.poll().timestamp;
            String data_accel = getdata.rawAccX + "," + getdata.rawAccY + "," + getdata.rawAccZ + "," + getdata.orientx + "," + getdata.orienty + "," + getdata.orientz + "," + getdata.gpsLat + "," + getdata.gpsLon + "," + getdata.posErr + "," + getdata.speed + "," + getdata.course  + "," + getdata.date_time_str  + "," + getdata.timestamp +   "," + getdata.corrected_AccX  + "," + getdata.corrected_AccY + "," + getdata.corrected_AccZ;

            //String data_accel = getdata.rawAccX + "," + getdata.rawAccY + "," + getdata.rawAccZ + "," + getdata.corrected_AccX + "," + getdata.corrected_AccY + "," + getdata.corrected_AccZ + "," + getdata.orientx + "," + getdata.orienty + "," + getdata.orientz + "," + getdata.gpsLat + "," + getdata.gpsLon + "," + getdata.posErr + "," + 1.029999971 + "," + 62.40000153 + "," + getdata.date_time_str;
            //1.029999971	62.40000153

            pw.println(data_accel);
                Log.d("tag_queue_pop_size", String.valueOf(file_sensorDataQueue.size()));
            }
        }

         pw.flush();
          pw.close();
        try {
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        orient.setText("Done saving data to csv");
    }


    public void send_to_io_buffer_to_storage_low_memory(){

        File root = android.os.Environment.getExternalStorageDirectory();
        SimpleDateFormat fname_date_time = new SimpleDateFormat("MM,dd,yyyy");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        File fil2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data1");
        File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data_shuttle_bus_deploy");
        fil.mkdirs();
        fil2.mkdirs();
        File file = new File(fil, "queue_data_ride" +rideId+ fname_date_string + ".txt");

        FileOutputStream f = null;
        try {
            f = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream f_append_pothole = openFileOutput("readme1231.txt",  MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(f);
        SimpleDateFormat date_time = new SimpleDateFormat("HH  mm  ss SS");

        if (file_sensorDataQueue.size() != 0) {
            for (int i = 0; i < file_sensorDataQueue.size(); i++) {

                BufferDataItem getdata = file_sensorDataQueue.poll();
                //String data_accel = m_bufferDataQueue.poll().rawAccX + "," + m_bufferDataQueue.poll().rawAccY + "," + m_bufferDataQueue.poll().rawAccZ + "," + m_bufferDataQueue.poll().corrected_AccX + "," + m_bufferDataQueue.poll().corrected_AccY + "," + m_bufferDataQueue.poll().corrected_AccZ + "," + m_bufferDataQueue.poll().azimuth + "," + m_bufferDataQueue.poll().pitch + "," + m_bufferDataQueue.poll().roll + "," + m_bufferDataQueue.poll().gpsLon + "," + m_bufferDataQueue.poll().gpsLon + "," + m_bufferDataQueue.poll().posErr + "," + m_bufferDataQueue.poll().speed + "," + m_bufferDataQueue.poll().timestamp;
                String data_accel = getdata.rawAccX + "," + getdata.rawAccY + "," + getdata.rawAccZ + "," + getdata.orientx + "," + getdata.orienty + "," + getdata.orientz + "," + getdata.gpsLat + "," + getdata.gpsLon + "," + getdata.posErr + "," + getdata.speed + "," + getdata.course  + "," + getdata.date_time_str  + "," + getdata.timestamp +   "," + getdata.corrected_AccX  + "," + getdata.corrected_AccY + "," + getdata.corrected_AccZ;

                //String data_accel = getdata.rawAccX + "," + getdata.rawAccY + "," + getdata.rawAccZ + "," + getdata.corrected_AccX + "," + getdata.corrected_AccY + "," + getdata.corrected_AccZ + "," + getdata.orientx + "," + getdata.orienty + "," + getdata.orientz + "," + getdata.gpsLat + "," + getdata.gpsLon + "," + getdata.posErr + "," + 1.029999971 + "," + 62.40000153 + "," + getdata.date_time_str;
                //1.029999971	62.40000153

                pw.println(data_accel);
                Log.d("tag_queue_pop_size", String.valueOf(file_sensorDataQueue.size()));
            }
        }

        pw.flush();
        pw.close();
        try {
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        orient.setText("Done saving data to csv");
    }


    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public void rideId_increment(View view){


         rideId =getRideId() +1;
         setRideId(rideId);
         //orient.setText("rideID is  "+rideId );
        orient.setText("rideID is  "+rideId );
    }



        public void download_file_firebase_storage(View view) throws IOException {


        File localFile = File.createTempFile("acc", "txt");
        StorageReference riversRef = mStorageRef.child("files");

        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("file sending path" , "Successfully downloaded data to local file");
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                Log.d("file sending path" , "error downloading");
                // ...
            }
        });
    }


    double getVariance( ArrayList <Double> data, double mean) {
        int size = data.size();
        Log.d("tag_population", String.valueOf(data.size()));
        //double mean = getMean();
        double temp = 0;
        //for(double a :data)
        for(int i=0; i<size ; i++) temp += (data.get(i)-mean)*(data.get(i)-mean);
        return temp/(size);
    }


     void handlePredict(SensorGpsDataItem sdi) {
         Log.d("kf_predict", "handle predict called");
        m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
       // Log.d("tag_predict", String.valueOf(Utils.LogMessageType.KALMAN_PREDICT.ordinal()));
    }

     void handleUpdate(SensorGpsDataItem sdi) {
         Log.d("kf_update", "handle update called");
         Log.d("kf_update_value_lat", String.valueOf(sdi.getGpsLat()));

         double xVel = sdi.getSpeed()*Math.cos(Math.toRadians(sdi.getCourse()));
         double yVel = sdi.getSpeed()*Math.sin(Math.toRadians(sdi.getCourse()));

         m_kalmanFilter.update(
                sdi.getTimestamp(),
                Coordinates.longitudeToMeters(sdi.getGpsLon()),
                Coordinates.latitudeToMeters(sdi.getGpsLat()),
                xVel,
                yVel,
                sdi.getPosErr(),
                sdi.getVelErr()
        );


    }




    GeoPoint  locationAfterPredictStep() {

        GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentXprior(), m_kalmanFilter.getCurrentYprior());
        Log.d("tag_state_raw",String.valueOf(getLatitute()) + "  ,  "+String.valueOf(getLongitude()));
        Log.d("tag_state_x_prior", String.valueOf(pp.Latitude));
        Log.d("tag_state_y_prior", String.valueOf(pp.Longitude));
        SimpleDateFormat fname_date_time = new SimpleDateFormat("HH,mm,ss,SS");
        String fname_date_string = fname_date_time.format(System.currentTimeMillis());
        Log.d("tag_state_time", String.valueOf(fname_date_string));
        long now = android.os.SystemClock.elapsedRealtime();
        //BufferItem bi_pridict  = new BufferItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], earthAcc[0],earthAcc[1],earthAcc[2], orientation_data[0], orientation_data[1], orientation_data[2],getSpeed_vehicle(), getAccuracy(), fname_date_string ,pp.Latitude, pp.Longitude ,getLatitute() ,getLongitude(),now);
       // cirbuff.insert(bi_pridict);
        //write_estimates_10hz_ToSDFile(kcounter,sdi,bi_pridict);
        return pp;

    }


    //private Location locationAfterUpdateStep(SensorGpsDataItem sdi) {
     GeoPoint  locationAfterUpdateStep(SensorGpsDataItem sdi) {
        double xVel, yVel;
        // loc = new Location(TAG);
        GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(),
                m_kalmanFilter.getCurrentY());

        //setLatitute(String.valueOf(pp.Latitude));
         Log.d("tag_state_raw_acc",String.valueOf(orig_accel_data[0]) + "  ,  "+String.valueOf(orig_accel_data[1])+"  ,  "+String.valueOf(orig_accel_data[1]) );
         Log.d("tag_state_raw",String.valueOf(getLatitute()) + "  ,  "+String.valueOf(getLongitude()));
         Log.d("tag_state_x_update", String.valueOf(pp.Latitude));
         Log.d("tag_state_y_update", String.valueOf(pp.Longitude));
         Log.d("tag_state_x_kalman_gain", String.valueOf(m_kalmanFilter.getCurrentKalmanGain0()));
         Log.d("tag_state_y_kalman_gain", String.valueOf(m_kalmanFilter.getCurrentKalmanGain1()));

         Log.d("tag_state_auxb1", String.valueOf(m_kalmanFilter.getauxbumatrix00()));
         Log.d("tag_state_auxb1", String.valueOf(m_kalmanFilter.getauxbumatrix02()));


         return  pp;
    }





    class kalman_asynctask extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {

            if(m_kalmanFilter != null) {
                SensorGpsDataItem sdi;
                double lastTimeStamp =0.0;

                kcounter++;

                while ((sdi = m_sensorDataQueue.poll()) != null) {


                    //Log.d("tag_t_sensor_gps_d" , String.valueOf(sdi.compareTo(sdi)));
                    Log.d("tag_queue" , String.valueOf(sdi.gpsLat));
                    if(sdi.gpsLat !=361.0 ){
                        Log.d("tag_queue_update" , "no 361");
                        handleUpdate(sdi);
                    }


                    if (sdi.getTimestamp() < lastTimeStamp) {
                        // Log.d("tag_time_stamp")
                        continue;
                    }
                    lastTimeStamp = sdi.getTimestamp();

                    //warning!!!
                    if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED) {

                        handlePredict(sdi);
                        Log.d("kf_func", "predict called");


                        locationAfterPredictStep();
                        //if(predict_counter %10==0)
                        //write_prior_estimates_ToSDFile();
                        //bi_global = new BufferItem(orig_accel_data[0],orig_accel_data[1],orig_accel_data[2], orientation_data[0],orientation_data[1],orientation_data[2],getSpeed_vehicle(), getAccuracy(), now ,getLatitute(),getLongitude());

                        SimpleDateFormat fname_date_time = new SimpleDateFormat("HH,mm,ss,SS");
                        String fname_date_string = fname_date_time.format(System.currentTimeMillis());




                    } else {

                        Log.d("kf_func_up", "update called");

                        handleUpdate(sdi);
                        GeoPoint ku= locationAfterUpdateStep(sdi);
                        //write_posterier_estimates_ToSDFile();

                        SimpleDateFormat fname_date_time = new SimpleDateFormat("HH,mm,ss");
                        String fname_date_string = fname_date_time.format(System.currentTimeMillis());

                    }
                }

            }

            return null;
        }
    }


    class TestTask extends AsyncTask<Void, Void, String> /* Params, Progress, Result */ {

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
        //final float[] orig_accel ;
        //float[] earthAccl  ;
        //float[] orientation_d;
        double lat;
        double longi;
        double beari;
        double gps_accuracy;
        float speed_vehicle;
        String date_time;


        TestTask(float id ,float id2,float id3,float id4 ,float id5,float id6,float id7 ,float id8,float id9, double lat, double longi ,double gps_accuracy,float speed_vehicle,double beari ,String fname_date_string) {
            this.id  = id;
            this.id2 =id2;
            this.id3 =id3;
            this.id4 =id4;
            this.id5 =id5;
            this.id6 =id6;

            this.id7 =id7;
            this.id8 =id8;
            this.id9 =id9;


            //this.orig_accel=orig_accel;
            //this.earthAccl    =earthAccl ;
            //this.orientation_d=orientation_d;
            this.lat=lat;
            this.longi=longi;
            this.gps_accuracy=gps_accuracy;
            this.speed_vehicle=speed_vehicle;
            this.beari=beari;
            this.fname_date_string = fname_date_string;
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d("multthread","doInBackground: entered");
            Log.d("multithread","doInBackground: is about to finish, taskExecutionNumber = ");

            //write_to_file();
            SimpleDateFormat fname_date_time = new SimpleDateFormat("MM-dd-yyy");
            String fname_date_str = fname_date_time.format(System.currentTimeMillis());
            File fil = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "sensor_data");
            fil.mkdirs();

            Log.d("tag_thread", "in background");

            ///////////
            File file = new File(fil, "buffer_data_rideid_" + getRideId()+ "_" + fname_date_str + ".txt");
            try {
                FileOutputStream f = new FileOutputStream(file, true);
                PrintWriter pw = new PrintWriter(f);
                //SimpleDateFormat date_time = new SimpleDateFormat("hh,mm,ss,SS");
                //String Date_time_stringss = date_time.format(System.currentTimeMillis());
                //String data_accel =   orig_accel_data[0]+","+orig_accel_data[1]+","+orig_accel_data[2]+"," + earthAcc[0] + ","+earthAcc[1]+ "," + earthAcc[2]+ ","+orientation_data[0] +","+ orientation_data[1]+ "," + orientation_data[2] + "," + getLatitute() + ","+ getLongitude()+ ","+ getAccuracy()+","+ getSpeed_vehicle() +"," +  getBearing()+ ","+ Date_time_stringss;
                //String data_accel   =  id + ","+ id2  + "," + id3 +","+id4 + ","+ id5  + "," + id6 +"," + id7 +"," +id8 +"," +id9+ "," + lat +"," + longi +"," + gps_accuracy +"," + speed_vehicle +"," + beari +"," +Date_time_stringss;
                //String data_accel   =  id + ","+ id2  + "," + id3  +"," + id7 +"," +id8 +"," +id9+ "," + lat +"," + longi +"," + gps_accuracy +"," + speed_vehicle +"," + beari +"," +fname_date_string;
                String data_accel   =  id + ","+ id2  + "," + id3  +"," + id7 +"," +id8 +"," +id9+ "," + lat +"," + longi +"," + gps_accuracy +"," + speed_vehicle +"," + beari +"," +fname_date_string;
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

            Log.d("Completed" , String.valueOf(id));
        }

        private void log(String msg) {
            Log.d("TestTask #" + id, msg);
        }


        void write_to_file(){
        }


    }


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public float getSpeed_vehicle() {
        return speed_vehicle;
    }

    public void setSpeed_vehicle(float speed_vehicle) {
        this.speed_vehicle = speed_vehicle;
    }




    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }


    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }


    public double getElapsedRealtimeNanos() {
        return elapsedRealtimeNanos;
    }



    public void setElapsedRealtimeNanos(double elapsedRealtimeNanos) {
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public double getBearing_accuracy() {
        return bearing_accuracy;
    }

    public void setBearing_accuracy(double bearing_accuracy) {
        this.bearing_accuracy = bearing_accuracy;
    }

    public Boolean getHasbear() {
        return hasbear;
    }

    public void setHasbear(Boolean hasbear) {
        this.hasbear = hasbear;
    }



    public class MyLocationListener implements LocationListener {
        // ArrayList var =new ArrayList();
        //int count=0;
        @SuppressLint("NewApi")
        @Override
        public void onLocationChanged(Location loc) {

            //raw_gps_log =new filelogger_raw_gps(loc.getLatitude(),loc.
            // getLongitude());

            double bearingTo = temp.bearingTo(loc);

            temp.setLatitude(loc.getLatitude());
            temp.setLongitude(loc.getLongitude());


            // var.add(String.valueOf(loc.getLatitude()));
            //lati.setText((CharSequence) var);
            setLatitute(loc.getLatitude());
            setLongitude(loc.getLongitude());
            setAccuracy(loc.getAccuracy());
            setSpeed_vehicle(loc.getSpeed());
            setBearing(loc.getBearing());
            setHasbear(loc.hasBearing());
            loc.getLatitude();
            loc.getLongitude();
            loc.getSpeed();
            loc.hasBearing();


            geoField = new GeomagneticField(
                    Double.valueOf(loc.getLatitude()).floatValue(),
                    Double.valueOf(loc.getLongitude()).floatValue(),
                    Double.valueOf(loc.getAltitude()).floatValue(),
                    System.currentTimeMillis()
            );

            String.valueOf(geoField.getDeclination());
             heading =  geoField.getDeclination();




            Log.d("tag_loc1_time", String.valueOf(loc.getElapsedRealtimeNanos()));
            Log.d("tag_loc1_system_namo", String.valueOf(System.nanoTime()));
            Log.d("tag_loc1_diff_sys_namo", String.valueOf(System.nanoTime() - loc.getElapsedRealtimeNanos()));


            //loc.setElapsedRealtimeNanos(System.nanoTime());
            Log.d("tag_location_update", String.valueOf(loc.getLatitude()));
            acceleration_view.setText("Latitude" + getLatitute()+ "longitude"+ getLongitude()  + "accuracy" + loc.getAccuracy());

            if (m_kalmanFilter != null){
                //GeoPoint pcp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(), m_kalmanFilter.getCurrentY());
                //acceleration_view.setText(String.valueOf(pcp.Latitude));
              }

            ///////////////////////if flag =flase do this

            if (flag==FALSE) {

                /// counter
                counter =counter +1;
                //calculate the sum of latitude
                sum_lat  = sum_lat +loc.getLatitude();
                //calculate the sum of longitude
                sum_long = sum_long+loc.getLongitude();

                sum_xvel = sum_xvel +  loc.getSpeed() * Math.cos(loc.getBearing());
                sum_yvel = sum_yvel +  loc.getSpeed() * Math.sin(loc.getBearing()) ;

                //array list
                longitude_samples.add(Coordinates.longitudeToMeters(Double.valueOf(getLongitude())));
                latitude_samples.add(Coordinates.longitudeToMeters(Double.valueOf(getLatitute())));
                xvel_samples.add(loc.getSpeed() * Math.cos(loc.getBearing()));
                yvel_samples.add(loc.getSpeed() * Math.sin(loc.getBearing()));


            }
            int samples=10;
            if(counter == samples ){
                ///perform average first 100 gps samples
                /// calculate  average of latitude
                avg_lat  = sum_lat/samples  ;
                avg_long = sum_long/samples ;
                avg_xvel = sum_xvel/samples ;
                avg_yvel = sum_yvel/samples ;
                avg_long = Coordinates.longitudeToMeters(avg_long);
                avg_lat  = Coordinates.longitudeToMeters(avg_lat);
                //double diff = Coordinates.latitudeToMeters(Double.parseDouble(getLatitute())) - avg_lat ;

                /// calculate of longitude
                // perform the average calculation here
                Log.d("tag_data_lat",      String.valueOf(latitude_samples));
                Log.d("tag_data_lat_avg",  String.valueOf(avg_lat));
                Log.d("tag_data_xvel",     String.valueOf(xvel_samples));
                Log.d("tag_variance_lat",  String.valueOf(getVariance(longitude_samples,avg_lat)));
                Log.d("tag_variance_long", String.valueOf(getVariance(latitude_samples,avg_long)));
                Log.d("tag_variance_xvel", String.valueOf(getVariance(xvel_samples,avg_xvel)));
                Log.d("tag_variance_yvel", String.valueOf(getVariance(yvel_samples,avg_yvel)));
                Log.d("tag_diffence",      String.valueOf(Coordinates.latitudeToMeters(getLatitute()) - avg_lat )) ;
                flag=TRUE;
            }


            /////////////////////////////////
            // calculate the variance

            /////////////////////////////

            //set a flag to start the kalman filter
            if (m_kalmanFilter == null) {
            //if (m_kalmanFilter == null && flag ==TRUE) {
                // initalise the kalman filter with avg value
                //&& (flag=true) {
                // log2File("%d%d KalmanAlloc : lon=%f, lat=%f, speed=%f, course=%f, m_accDev=%f, posDev=%f",
                //        Utils.LogMessageType.KALMAN_ALLOC.ordinal(),
                //        timeStamp, x, y, speed, course, m_settings.accelerationDeviation, posDev);

                m_kalmanFilter = new GPSAccKalmanFilter(
                        false, //todo move to settings
                        Coordinates.longitudeToMeters(loc.getLongitude()),
                        Coordinates.latitudeToMeters(loc.getLatitude()),
                        loc.getSpeed() * Math.cos(Math.toRadians(loc.getBearing())),
                        loc.getSpeed() * Math.sin(Math.toRadians(loc.getBearing())),
                        Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                        loc.getAccuracy(),
                        Utils.nano2milli(loc.getElapsedRealtimeNanos()),
                        Utils.DEFAULT_VEL_FACTOR,
                        Utils.DEFAULT_POS_FACTOR,1.0,1.0,1.0,1.0);



            }
            else {

                //write_raw_gps_ToSDFile();

                //Log.d("tag_time_loc", String.valueOf(Utils.nano2milli(loc.getElapsedRealtimeNanos())));

                //acceleration_view.setText("Latitude" + getLatitute() + "Longitude" + getLatitute() + "Bearing" + getBearing() + "Speed" + getSpeed_vehicle());
                SensorGpsDataItem sdi = new SensorGpsDataItem(
                        Utils.nano2milli(loc.getElapsedRealtimeNanos()), loc.getLatitude(), loc.getLongitude(), loc.getAltitude(),
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        loc.getSpeed(),
                        loc.getBearing(),
                        loc.getAccuracy(),
                        loc.getAccuracy()*0.1,
                        0.0);
                m_sensorDataQueue.add(sdi);
                //handleUpdate(sdi);

            }

            ///m_kalmanFilter.
            /////////////////////////////
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }


    }
}
