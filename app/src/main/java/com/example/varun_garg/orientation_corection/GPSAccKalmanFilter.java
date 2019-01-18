package com.example.varun_garg.orientation_corection;

import android.util.Log;

import  com.example.varun_garg.orientation_corection.Matrix;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;



public class GPSAccKalmanFilter {
    public static final String TAG = "GPSAccKalmanFilter";

    private double m_timeStampMsPredict;
    private double m_timeStampMsUpdate;
    private int m_predictCount;
    private KalmanFilter m_kf;
    private double m_accSigma;
    private boolean m_useGpsSpeed;
    private double mVelFactor = 1.0;
    private double mPosFactor = 1.0;
    double time_test;


    public GPSAccKalmanFilter(boolean useGpsSpeed,
                              double x, double y,
                              double xVel, double yVel,
                              double accDev, double posDev,
                              double timeStampMs,
                              double velFactor,
                              double posFactor,
                              double xvar,
                              double yvar,
                              double xvelvar,
                              double yvelvar

    ) {
        Log.d("tag_variance_lat_check",   String.valueOf(xvar));
        Log.d("tag_variance_long_check",  String.valueOf(yvar));



        int mesDim = useGpsSpeed ? 4 : 2;
        m_useGpsSpeed = useGpsSpeed;

        m_kf = new KalmanFilter(4, mesDim, 1);
        m_timeStampMsPredict = m_timeStampMsUpdate = timeStampMs;
        m_accSigma = accDev;
        m_predictCount = 0;
        m_kf.Xk_k.setData(x, y, xVel, yVel);
        m_kf.H.setIdentityDiag(); //state has 4d and measurement has 4d too. so here is identity



        ///////////// change it to
        m_kf.Pk_k.setIdentity();

        /// initalilazation of  the error covariance matrix

        double cm[] = {
                xvar, 0.0, 0.0, 0.0,
                0.0, yvar, 0.0, 0.0,
                0.0, 0.0, xvelvar, 0.0,
                0.0, 0.0, 0.0, yvelvar
        };


        double kk[] = {
                0.3, 0.0, 0.0, 0.0,
                0.0, 0.3, 0.0, 0.0,
                0.0, 0.0, 1, 0.0,
                0.0, 0.0, 0.0, 1
        };


       // m_kf.Pk_k.setDataErrorCovariance(kk);

        ///print  P0
        m_kf.Pk_k.printData();
        //Log.d("tag_P0_initial", String.valueOf(m_kf.Pk_k));
        //////////////////////////
        m_kf.Pk_k.scale(posDev);
        mVelFactor = velFactor;
        mPosFactor = posFactor;

        Log.d("tag", "reached_the constructor");
    }



    private void rebuildF(double dtPredict) {
        double f[] = {
                1.0, 0.0, dtPredict, 0.0,
                0.0, 1.0, 0.0, dtPredict,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
        };
        m_kf.F.setData(f);
    }

    private void rebuildU(double xAcc,
                          double yAcc) {
        m_kf.Uk.setData(xAcc, yAcc);
    }

    private void rebuildB(double dtPredict) {
        double dt2 = 0.5*dtPredict*dtPredict;
        double b[] = {
                dt2, 0.0,
                0.0, dt2,
                dtPredict, 0.0,
                0.0, dtPredict
        };
        m_kf.B.setData(b);
    }

    private void rebuildR(double posSigma, double velSigma) {

        posSigma *= mPosFactor;
        velSigma *= mVelFactor;

        Log.i(TAG, "rebuildR: { " +
                "velSigma : " + velSigma +
                ", posSigma : " + posSigma +
                ", velFactor : " + mVelFactor +
                ", posFactor :" + mPosFactor +
                "}");
        if (m_useGpsSpeed) {
            double R[] = {
                    posSigma, 0.0, 0.0, 0.0,
                    0.0, posSigma, 0.0, 0.0,
                    0.0, 0.0, velSigma, 0.0,
                    0.0, 0.0, 0.0, velSigma
            };
            m_kf.R.setData(R);
        } else {
            m_kf.R.setIdentity();
            m_kf.R.scale(posSigma);
        }
    }

    private void rebuildQ(double dtUpdate,
                          double accDev) {
//        now we use predictCount. but maybe there is way to use dtUpdate.
//        m_kf.Q.setIdentity();
//        m_kf.Q.scale(accSigma * dtUpdate);
        double velDev = accDev * m_predictCount;
        double posDev = velDev * m_predictCount / 2;
        double covDev = velDev * posDev;

        double posSig = posDev * posDev;
        double velSig = velDev * velDev;

        double Q[] = {
                posSig, 0.0, covDev, 0.0,
                0.0, posSig, 0.0, covDev,
                0.0, 0.0, velSig, 0.0,
                0.0, 0.0, 0.0, velSig
        };
        m_kf.Q.setData(Q);
    }

    public void predict(double timeNowMs,
                        double xAcc,
                        double yAcc) {
         double dtPredict = (timeNowMs - m_timeStampMsPredict) / 1000.0;
         if(dtPredict >= 0.01 ) dtPredict = 0.01;
        //double dtPredict = 0.009;
        Log.d("time_pridict" , String.valueOf(timeNowMs));
        Log.d("tag_predict_check",  String.valueOf(dtPredict));

        //double dtUpdate = (timeNowMs - m_timeStampMsUpdate) / 1000.0;
        double dtUpdate = (timeNowMs - m_timeStampMsUpdate) / 1000.0;

        Log.d("tag_update_check",  String.valueOf(dtUpdate));

        rebuildF(dtPredict);
        rebuildB(dtPredict);
        rebuildU(xAcc, yAcc);

        ++m_predictCount;
        rebuildQ(dtUpdate, m_accSigma);

        m_timeStampMsPredict = timeNowMs;
        m_kf.predict();
        Matrix.matrixCopy(m_kf.Xk_km1, m_kf.Xk_k);

    }

    public void update(double timeStamp,
                       double x,
                       double y,
                       double xVel,
                       double yVel,
                       double posDev,
                       double velErr) {

        Log.d("tag_queue_update" , "update function called");

        m_predictCount = 0;
        m_timeStampMsUpdate = timeStamp;
        Log.d("time_update" , String.valueOf(timeStamp));

        rebuildR(posDev, velErr);
        m_kf.Zk.setData(x, y, xVel, yVel);
        m_kf.update();
    }

    public double getCurrentX() {
        return m_kf.Xk_k.data[0][0];
    }

    public double getCurrentY() {
        return m_kf.Xk_k.data[1][0];
    }

    public double getCurrentXVel() {
        return m_kf.Xk_k.data[2][0];
    }

    public double getCurrentYVel() {
        return m_kf.Xk_k.data[3][0];
    }

    public double getCurrentXprior() {
        return m_kf.Xk_km1.data[0][0];
    }

    public double getCurrentYprior() {
        return m_kf.Xk_km1.data[1][0];
    }

    public double getCurrentKalmanGain0() {
        return m_kf.K.data[0][0];
    }

    public double getCurrentKalmanGain1() {
        return m_kf.K.data[1][0];
    }

    public double getCurrentKalmanGain2() {
        return m_kf.K.data[2][0];
    }


    public double getCurrentKalmanGain3() {
        return m_kf.K.data[3][0];
    }

    public double getbmatrix00() {
        return m_kf.B.data[0][0];
    }


    public double getbmatrix04() {
        return m_kf.B.data[1][1];
    }


    public double getbmatrix20() {
        return m_kf.B.data[2][0];
    }

    public double getfmatrix31() {
        return m_kf.F.data[3][1];
    }


    public double getauxbumatrix00() {
        return m_kf.auxBxU.data[0][0];
    }

    public double getauxbumatrix02() {
        return m_kf.auxBxU.data[2][0];
    }


}
