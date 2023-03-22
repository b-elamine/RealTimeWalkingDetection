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

import java.text.DecimalFormat;

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
        buffer = new circularBuffer(200, 50);

        // SensorThread instance
        sensorThread = new SensorThread((SensorManager) getSystemService(Context.SENSOR_SERVICE), buffer);
        sensorThread.start();
        //result.setText("Standard deviation : "+buffer.getProcessedData().getStandardDeviation());
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
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float mag = (float) (Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))));

            float x_norm = (float)(x-(9.81*x/mag));
            float y_norm = (float)(y-(9.81*y/mag));
            float z_norm = (float)(z-(9.81*z/mag));
            float mag_norm = (float) (Math.sqrt((Math.pow(x_norm, 2) + Math.pow(y_norm, 2) + Math.pow(z_norm, 2))));

            System.out.println("X, Y, Z : " + x_norm+", "+ y_norm+ ", +"+ z_norm +", ");
            buffer.add(mag_norm);
            if (buffer.getProcessedData()!=null) {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(3);
                result.setText("STD : " + df.format(buffer.getProcessedData().getStandardDeviation()));
                System.out.println("_______________std_______________   " + df.format(buffer.getProcessedData().getStandardDeviation()) + "   " + buffer.getProcessedData().getStepCount());
            }

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
