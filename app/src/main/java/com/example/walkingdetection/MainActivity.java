package com.example.walkingdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingdetection.tools.circularBuffer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    TextView result;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private boolean sensorPresent;
    private circularBuffer buffer;

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
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorPresent = true;
        } else {
            result.setText("No sensor found !");
            sensorPresent = false;
        }
        // Initialize the buffer with window size and overlap size
        buffer = new circularBuffer(50, 25);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float mag = (float) (Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(y, 2))));

            buffer.add(mag);
            if (buffer.getProcessedData() != null) {
                result.setText(buffer.getProcessedData().getStepCount());
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            sensorManager.registerListener(this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this, "Sensor Not found", Toast.LENGTH_SHORT).show();
        }

    }
}