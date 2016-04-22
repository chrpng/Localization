package weili.localization;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
//import android.app.ProgressDialog;
import java.lang.Math;
import android.widget.Toast;
import java.util.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView accelXView, accelYView, accelZView;
    TextView azimutView, pitchView, rollView;
    TextView rssiView, sigStrView, rssiAverageView, stepView;
    WebView mWebView;

    ProgressBar accelBar;
    private float accelBarValue = 0;
    private Handler accelBarHandler = new Handler();

    private float stepDetectValue = 0;

    private float accelDelX = 0, accelDelY = 0, accelDelZ = 0;
    private float accelOldX, accelOldY, accelOldZ;

    private float azimut, pitch, roll;

    private Sensor accelerometer;
    private Sensor linaccelerometer;
    private Sensor gyroscope;
    float[] mGravity;
    float[] mGeomagnetic;

    long currentTime;
    double seconds;
    double timeElapsed;

    private Sensor gravity;
    private Sensor magnetometer;
    private float[] orientation;
    private float[] Rs;
    private float[] I;

    Intent i;

    private SensorManager sensorManager;
    private WifiManager wifiManager;

    Queue queue = new LinkedList();
    Queue stepQueue = new LinkedList();
    LinkedList RSSIQueue = new LinkedList();
    int stepCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentTime = System.nanoTime();

        rssiView = (TextView) findViewById(R.id.textView);
        sigStrView = (TextView) findViewById(R.id.textView2);
        accelXView = (TextView) findViewById(R.id.accelX);
        accelYView = (TextView) findViewById(R.id.accelY);
        accelZView = (TextView) findViewById(R.id.accelZ);
        azimutView = (TextView) findViewById(R.id.textView3);
        pitchView = (TextView) findViewById(R.id.textView4);
        rollView = (TextView) findViewById(R.id.textView5);
        rssiAverageView = (TextView) findViewById(R.id.rssiAverageView);
        stepView = (TextView) findViewById(R.id.stepView);


        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        accelBar = (ProgressBar) findViewById(R.id.progressBar);
        accelBar.setMax(5);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

         /*if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("http://52.36.135.251 ");

        final Button RSSIButton = (Button) findViewById(R.id.RSSIButton);
        RSSIButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int numSamples = 100;
                int num_milliseconds = 4000;

                for (int i = 0; i<numSamples; i++){
                    int x = wifiManager.getConnectionInfo().getRssi();
                    RSSIQueue.add(x);
                    try{
                        Thread.sleep(num_milliseconds/numSamples,0);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
//                        e.printStackTrace();
                    }
                }


                int meanRSSISamples = RSSIQueue.size();
                double meanRSSI = 0;

                for (int i = 0; i < RSSIQueue.size(); i++) {
                    meanRSSI += (double) ((Integer) RSSIQueue.get(i));
                }
                meanRSSI = meanRSSI/ (new Double(meanRSSISamples));

                rssiAverageView.setText("Average RSSI value: " + meanRSSI);
                System.out.println("Average RSSI value: " + meanRSSI);


            }
        });



        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Client.createMap();
                i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"brenbadia@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "cutebrazilianmidgetmingle.com");
                i.putExtra(Intent.EXTRA_TEXT, "#StepDistribution" + stepQueue + "#AccelerationData" + queue);
                //i.putExtra(Intent.EXTRA_TEXT, "booty booteh");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                queue.clear();
                stepQueue.clear();
            }
        });

        final Context context = this;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //getRotationMatrix (R, I, gravity.);
        //orientation = sensorManager.getOrientation();
        linaccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, linaccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

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
        sensorManager.registerListener(this, linaccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
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
        if (event.sensor == accelerometer) {
            mGravity = event.values;
        }
        if (event.sensor == magnetometer) {
            mGeomagnetic = event.values;
        }
        if (mGravity != null && mGeomagnetic != null) {
            float Rs[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(Rs, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(Rs, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];
                displayOrientValues();
            }
        }

        if(event.sensor == linaccelerometer) {

            accelDelX = accelOldX - event.values[0];
            accelDelY = accelOldY - event.values[1];
            accelDelZ = accelOldZ - event.values[2];

            /*
            if (Math.abs(accelOldX - event.values[0]) < 2) {
                accelDelX = 0;
            }
            if (Math.abs(accelOldY - event.values[1]) < 2) {
                accelDelY = 0;
            }
            if (Math.abs(accelOldZ - event.values[2]) < 2) {
                accelDelZ = 0;
            }*/

            if (Math.abs(accelOldX - event.values[0]) > 0.2) {
                accelOldX = event.values[0];
            }
            if (Math.abs(accelOldY - event.values[1]) > 0.2) {
                accelOldY = event.values[1];
            }
            if (Math.abs(accelOldZ - event.values[2]) > 0.2) {
                accelOldZ = event.values[2];
            }
            displayAccelValues();
            accelBarValue = (float) Math.sqrt(Math.pow(accelOldX, 2) + Math.pow(accelOldY, 2) + Math.pow(accelOldZ, 2));
            queue.add(accelBarValue);
            seconds = (double) (System.nanoTime() - currentTime) / 1E9;
            if (accelBarValue > 5f && seconds > 0.2f) {
                stepDetectValue = 1f;
                currentTime = System.nanoTime();
            } else {
                stepDetectValue = 0f;
            }
            stepQueue.add(stepDetectValue);
            accelBar.setProgress((int)accelBarValue);

            if (accelBarValue > 5.0){
                stepView.setText("Steps: " + ++stepCount);

            }


        }
    }

    public void displayAccelValues() {
        /*
        accelXView.setText(Float.toString(accelDelX));
        accelYView.setText(Float.toString(accelDelY));
        accelZView.setText(Float.toString(accelDelZ));
        */
        accelXView.setText(Float.toString(accelOldX));
        accelYView.setText(Float.toString(accelOldY));
        accelZView.setText(Float.toString(accelOldZ));
    }

    public void displayOrientValues() {
        /*
        accelXView.setText(Float.toString(accelDelX));
        accelYView.setText(Float.toString(accelDelY));
        accelZView.setText(Float.toString(accelDelZ));
        */
        azimutView.setText(Float.toString(azimut));
        pitchView.setText(Float.toString(pitch));
        rollView.setText(Float.toString(roll));
    }
}
