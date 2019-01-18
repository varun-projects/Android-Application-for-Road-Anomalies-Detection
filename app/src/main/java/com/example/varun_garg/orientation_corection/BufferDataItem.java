package com.example.varun_garg.orientation_corection;

import android.support.annotation.NonNull;

public class BufferDataItem{


        double timestamp;
        String date_time_str;
        double gpsLat;
        double gpsLon;
        double gpsAlt;
        double kal_gpsLat;
        double kal_gpsLon;

        double rawAccX;
        double rawAccY;
        double rawAccZ;
        double speed;
        double course;
        double posErr;
        double velErr;
        double azimuth;
        double pitch;
        double roll;
        double corrected_AccX;
        double corrected_AccY;
        double corrected_AccZ;

        double orientx;
        double orienty;
        double orientz;






    public BufferDataItem(
                      double rawAccX, double rawAccY, double rawAccZ,
                      double corrected_AccX, double corrected_AccY, double corrected_AccZ,double orientx ,double orienty, double orientz,
                      double gpsLat, double gpsLon ,double posErr ,double speed ,double course,double timestamp ,String date_time_str) {



            this.rawAccX = rawAccX;
            this.rawAccY = rawAccY;
            this.rawAccZ = rawAccZ;
            this.speed = speed;
            this.course = course;
            this.posErr = posErr;
            this.velErr = velErr;
            this.corrected_AccX = corrected_AccX;
            this.corrected_AccY = corrected_AccY;
            this.corrected_AccZ = corrected_AccZ;
            this.timestamp = timestamp;
            this.gpsLat = gpsLat;
            this.gpsLon = gpsLon;
            this.gpsAlt = gpsAlt;
            this.date_time_str =date_time_str;

            this.orientx =orientx;
            this.orienty =orienty;
            this.orientz =orientz;

        }




        public String getDate_time_str() {
        return date_time_str;
    }

        public double getTimestamp() {
            return timestamp;
        }

        public double getGpsLat() {
            return gpsLat;
        }

        public double getGpsLon() {
            return gpsLon;
        }

        public double getGpsAlt() {
            return gpsAlt;
        }

        public double getSpeed() {
            return speed;
        }

        public double getCourse() {
            return course;
        }

        public double getPosErr() {
            return posErr;
        }

        public double getVelErr() {
            return velErr;
        }

        public double getRawAccX() {
            return rawAccX;
        }

        public double getRawAccY() {
            return rawAccY;
        }

        public double getRawAccZ() {
            return rawAccZ;
        }

        public double getAzimuth() {
        return azimuth;
    }

        public double getPitch() {
        return pitch;
    }

        public double getRoll() {
        return roll;
    }


        public double getOrientx() {
        return orientx;
    }

        public double getOrienty() {
        return orienty;
    }


        public double getOrientz() {
        return orientz;
    }


}

