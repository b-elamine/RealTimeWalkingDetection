package com.example.walkingdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingdetection.tools.circularBuffer;

public class MainActivity extends AppCompatActivity {


    TextView result;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private boolean sensorPresent;
    private circularBuffer buffer;
    private SensorThread sensorThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = findViewById(R.id.result);
        result.setText("Results of the activity");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) { //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
            }
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorPresent = true;
        } else {
            result.setText("No sensor found !");
            sensorPresent = false;
        }
        // Initialize the buffer with window size and overlap size
        buffer = new circularBuffer(50, 25);

        // SensorThread instance
        sensorThread = new SensorThread((SensorManager) getSystemService(Context.SENSOR_SERVICE), buffer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorThread.stopSensor();
    }


    public class SensorThread extends Thread implements SensorEventListener {

        private final SensorManager sensorManager;
        private final Sensor sensor;
        private final circularBuffer buffer;

        public SensorThread(SensorManager sensorManager, circularBuffer buffer) {
            this.sensorManager = sensorManager;
            this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            this.buffer = buffer;
        }

        @Override
        public void run() {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float mag = (float) (Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(y, 2))));

            buffer.add(mag);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }

        public void stopSensor() {
            sensorManager.unregisterListener(this);
        }
    }
}
