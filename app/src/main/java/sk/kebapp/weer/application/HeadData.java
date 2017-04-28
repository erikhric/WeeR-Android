package sk.kebapp.weer.application;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import org.opencv.core.Point;

/**
 * Created by erikhric on 06/09/16.
 */
public class HeadData implements SensorEventListener {

    private double roll = 180.0f;
    private double pitch = 180.0f;
    private double yaw;
    private boolean refreshedSinceLastSent = true;

    private float[] euler;

    private Point steeringPointA, steeringPointB;

    private double upperLenght, lowerLenght;

    private boolean isMarkerVisible = false;
    private double lastSteerValue = 0;
    private double lastAccelValue = 0;


    private boolean hasGyroscope;

    private HeadTracker mHeadTracker;
    private HeadTransform mHeadTransform;
    /// We don't want to alloc new memory each time, so we reuse this point
    private Point anglePoint;

    private double prevRoll, prevPitch, prevYaw;

    public HeadData(Context context) {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        euler = new float[3];
        anglePoint = new Point();

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // Sensor FOUND
            this.hasGyroscope = true;
            mHeadTracker = new HeadTracker(context);
            mHeadTransform = new HeadTransform();
        } else {
            //Sensor NOT FOUND
            this.hasGyroscope = false;
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void refreshData() {
        if (hasGyroscope) {
            mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);
            mHeadTransform.getEulerAngles(euler, 0);

            prevRoll = roll;
            prevPitch = pitch;

            roll = Math.toDegrees(euler[1]) + 180f;
            pitch = Math.toDegrees(euler[0]) + 180f;
            yaw = Math.toDegrees(euler[2]);

            refreshedSinceLastSent = true;
        }
    }

    public double getzRotation() {
        return -yaw;
    }

    public void setSteeringPoints(Point A, Point B) {
        steeringPointA = A;
        steeringPointB = B;
    }

    public void setHorizontalLenghs(double upperLenght, double lowerLenght) {
        this.upperLenght = upperLenght;
        this.lowerLenght = lowerLenght;
    }

    public void startTracking() {
        if (hasGyroscope)
            mHeadTracker.startTracking();
    }

    public void stopTracking() {
        if (hasGyroscope)
            mHeadTracker.stopTracking();
    }

    //TODO: this shitcode JSON saved us some miliseconds
    @Override
    public String toString() {
        return "{\"roll\": " + (roll) +
                ",\"pitch\": " + (pitch) +
                ",\"yaw\": " + (yaw) +
                ",\"steer\": " + steerAngle() +
                ",\"forward\": " + verticalSkew()
                + "}";
    }

    public double verticalSkew() {
        if (!isMarkerVisible) {
            return lastAccelValue;
        }
        lastAccelValue = (lowerLenght - upperLenght) * (100 / (lowerLenght + upperLenght));
        return lastAccelValue;
    }

    public double steerAngle() {
        if (steeringPointA == null || steeringPointB == null) return 0;

        if (!isMarkerVisible) {
            return lastSteerValue;
        }

        anglePoint.x = steeringPointB.x - steeringPointA.x;
        anglePoint.y = steeringPointB.y - steeringPointA.y;

        lastSteerValue = Math.toDegrees(Math.atan2(anglePoint.y, anglePoint.x)) - yaw;
        return lastSteerValue;
    }

    public void setMarkerVisible(boolean markerVisible) {
        isMarkerVisible = markerVisible;
    }

    public double getRollDiff() {
        return (hasGyroscope) ? roll - prevRoll : 0;
    }

    public double getPitchDiff() {
        return pitch - prevPitch;
    }

    public double getYaw() {
        return yaw;
    }

    float[] mGeomagnetic = null;
    float[] mGravity = null;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {

                prevRoll = roll;
                prevPitch = pitch;

                prevYaw = yaw;

                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                roll = -Math.toDegrees(orientation[0]) + 180f; // orientation contains: azimut, pitch and roll
                yaw = Math.toDegrees(orientation[1]);
                pitch = -Math.toDegrees(orientation[2]) + 180f;


                ///TODO: really sorry for dis
                if ((roll > 240 && prevRoll < 120)) {
                    prevRoll += 360.0;
                }

                if ((prevRoll > 240 && roll < 120)) {
                    roll += 360.0;
                }

                if ((pitch > 240 && prevPitch < 120)) {
                    prevPitch += 360.0;
                }

                if ((prevPitch > 240 && pitch < 120)) {
                    pitch += 360.0;
                }

                roll = (roll + prevRoll) / 2;
                pitch = (pitch + prevPitch) / 2;
                yaw = (yaw + prevYaw) / 2;

                if (pitch > 360)
                    pitch -= 360;

                if (yaw >= 360)
                    yaw -= 360;

                refreshedSinceLastSent = true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean isRefreshedSinceLastSent() {
        return refreshedSinceLastSent;
    }

    public void sent() {
        refreshedSinceLastSent = false;
    }
}
