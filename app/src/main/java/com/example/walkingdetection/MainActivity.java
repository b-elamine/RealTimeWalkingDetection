package com.example.walkingdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {


    TextView result;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private boolean sensorPresent;
    private circularBuffer buffer;
    private SensorThread sensorThread;
    private LineChart mChart;
    private LineChart mChartSd;


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
        //result.setText("Standard deviation : "+buffer.getProcessedData().getStandardDeviation());

        mChart = (LineChart) findViewById(R.id.chart);
        mChartSd = (LineChart) findViewById(R.id.chartsd);

        init_Charts(mChart);

        sensorThread.start(); //start collecting processing and plotting ..

    }

    private void init_Charts(LineChart chart){
        // enable description text
        chart.getDescription().setEnabled(true);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart.setData(data);
        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setDrawBorders(false);
    }

    private void addEntry(SensorEvent event) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 80) + 10f), 0);
            data.addEntry(new Entry(set.getEntryCount(), (float) Math.sqrt(Math.pow(event.values[0],2)+Math.pow(event.values[1],2)+Math.pow(event.values[2],2))-5), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(150);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1.5f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
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
                System.out.println("_______________std_______________   " +
                        df.format(buffer.getProcessedData().getStandardDeviation()) +
                        "   "
                        + buffer.getProcessedData().getStepCount());
            }

            addEntry(event);


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
