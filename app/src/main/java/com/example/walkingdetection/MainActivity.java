package com.example.walkingdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class MainActivity extends AppCompatActivity {


    TextView result;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private boolean sensorPresent;
    private circularBuffer buffer;
    private SensorThread sensorThread;

    //Plotting variables
    private LineChart lineChart;
    boolean plot = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //initializing plotting setting
        lineChart = (LineChart) findViewById(R.id.mag_chart);
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("|A| of real time accelerometer data");
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        lineChart.setData(data);


        // SensorThread instance
        sensorThread = new SensorThread((SensorManager) getSystemService(Context.SENSOR_SERVICE), buffer);
        sensorThread.start();
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
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }


        private void addEntry(float value) {
                LineData data = lineChart.getData();
                if (data!=null) {
                    ILineDataSet set = data.getDataSetByIndex(0);
                    if (set==null){
                        set=createSet();
                        data.addDataSet(set);
                    }
                    data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);                    data.notifyDataChanged();
                    lineChart.setMaxVisibleValueCount(20);
                    lineChart.moveViewToX(data.getEntryCount());
                }

        }

        private LineDataSet createSet(){
            LineDataSet set = new LineDataSet(null, "Dynamic Data");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setLineWidth(3f);
            set.setColor(Color.MAGENTA);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setCubicIntensity(0.2f);
            return set;
        }


        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float mag = (float) (Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))));
            System.out.println("Sensor changed.. X :" + event.values[0]);
            buffer.add(mag);
            addEntry(mag);

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
