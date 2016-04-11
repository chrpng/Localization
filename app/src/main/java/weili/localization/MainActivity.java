package weili.localization;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Button;
import android.widget.TextView;
import java.lang.Math;
import android.widget.Toast;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView accelXView, accelYView, accelZView;
    TextView rssiView, sigStrView;

    private float accelDelX = 0, accelDelY = 0, accelDelZ = 0;
    private float accelOldX, accelOldY, accelOldZ;

    private Sensor accelerometer;

    private SensorManager sensorManager;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rssiView = (TextView) findViewById(R.id.textView);
        sigStrView = (TextView) findViewById(R.id.textView2);
        accelXView = (TextView) findViewById(R.id.accelX);
        accelYView = (TextView) findViewById(R.id.accelY);
        accelZView = (TextView) findViewById(R.id.accelZ);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Client.createMap();
            }
        });

        final Context context = this;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int level;

                                int numberOfLevels = 5;
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                                rssiView.setText("RSSI value: " + wifiInfo.getRssi());

                                level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
                                //setContentView(R.layout.layoutName);

                                sigStrView.setText("Signal level: " + level);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //System.out.println("event sensor: " + event.sensor);
        if(event.sensor == accelerometer) {
            accelDelX = accelOldX - event.values[0];
            accelDelY = accelOldY - event.values[1];
            accelDelZ = accelOldZ - event.values[2];


            if (Math.abs(accelOldX - event.values[0]) < 2) {
                accelDelX = 0;
            }
            if (Math.abs(accelOldY - event.values[1]) < 2) {
                accelDelY = 0;
            }
            if (Math.abs(accelOldZ - event.values[2]) < 2) {
                accelDelZ = 0;
            }

            accelOldX = event.values[0];
            accelOldY = event.values[1];
            accelOldZ = event.values[2];

            displayAccelValues();
        }
    }

    public void displayAccelValues() {
        accelXView.setText(Float.toString(accelDelX));
        accelYView.setText(Float.toString(accelDelY));
        accelZView.setText(Float.toString(accelDelZ));
    }
}
