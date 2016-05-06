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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Math;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //TextView accelXView, accelYView, accelZView;
    //TextView relAView, relXView, relYView;
    //TextView azimutView, pitchView, rollView;
    TextView rssiView, sigStrView, rssiAverageView;
    TextView stepView, stepDistView;
    WebView mWebView;

    Button AngleButton;
    Button RSSIButton;

    ProgressBar accelBar;
    private float accelBarValue = 0;
    private Handler accelBarHandler = new Handler();

    ProgressBar aziMeasureBar;
    private int aziMeasures = 0;

    private float stepDetectValue = 0;

    private float accelDelX = 0, accelDelY = 0, accelDelZ = 0;
    private float accelOldX, accelOldY, accelOldZ;

    private float azimut, pitch, roll, azimutD;
    private double delPosX, delPosY, longX, latY, delLongX, delLatY;
    private double stepSize; // in m
    private float stepAngle;

    static String serverIP;
    static String name;

    private String AP1 = "00:1a:1e:88:46:41";
    private String AP2 = "00:1a:1e:93:1e:61";
    int RSSI_AP1, RSSI_AP2;
    private double dist_AP1, dist_AP2;

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

    //Ap 1: 40.442178, -79.946980
    //Ap 2; 40.442273, -79.946952

    Intent i;

    private SensorManager sensorManager;
    private WifiManager wifiManager;

    Queue queue = new LinkedList();
    Queue stepQueue = new LinkedList();
    LinkedList RSSIQueue = new LinkedList();

    LinkedList tempAzimuthQueue = new LinkedList();
    LinkedList finalAzimuthQueue = new LinkedList();
    //LinkedList finalAzimuthQueue = new LinkedList();
    //LinkedList tempPitchQueue = new LinkedList();
    //LinkedList finalPitchQueue = new LinkedList();
    //LinkedList tempRollQueue = new LinkedList();
    //LinkedList finalRollQueue = new LinkedList();
    int cycleCount = 0;

    int errorBucket;
    int goodBucket;
    int meanAziSamples;
    int numSamples = 100;
    float totalAzi;
    float meanAzi;
    float prevAzi;
    float currAzi;
    float totalPitch;
    float meanPitch;
    float totalRoll;
    float meanRoll;

    LinkedList accelX = new LinkedList();
    LinkedList accelY = new LinkedList();
    LinkedList accelZ = new LinkedList();

    int stepCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentTime = System.nanoTime();

        rssiView = (TextView) findViewById(R.id.textView);
        sigStrView = (TextView) findViewById(R.id.textView2);
        //accelXView = (TextView) findViewById(R.id.accelX);
        //accelYView = (TextView) findViewById(R.id.accelY);
        //accelZView = (TextView) findViewById(R.id.accelZ);
        //relAView = (TextView) findViewById(R.id.relativeA);
        //relXView = (TextView) findViewById(R.id.relativeX);
        //relYView = (TextView) findViewById(R.id.relativeY);
        //azimutView = (TextView) findViewById(R.id.textView3);
        //pitchView = (TextView) findViewById(R.id.textView4);
        //rollView = (TextView) findViewById(R.id.textView5);
        rssiAverageView = (TextView) findViewById(R.id.rssiAverageView);
        //stepView = (TextView) findViewById(R.id.stepView);
        //stepDistView = (TextView) findViewById(R.id.stepDistView);
        //AP 1
        latY = 40.442178;
        longX = -79.946980;
        //relXView.setText(Float.toString(relativePosX));
        //relYView.setText(Float.toString(relativePosY));
        stepSize = 0.5; // in meters

        name = "sammy";
        serverIP = "52.36.135.251";

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        accelBar = (ProgressBar) findViewById(R.id.progressBar);
        accelBar.setMax(5);

        aziMeasureBar = (ProgressBar) findViewById(R.id.progressBar2);
        aziMeasureBar.setMax(100);

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
        //mWebView.loadUrl("http://52.36.135.251 ");

        RSSIButton = (Button) findViewById(R.id.RSSIButton);
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
                meanRSSI = meanRSSI/ Double.valueOf(meanRSSISamples);

                rssiAverageView.setText("Average RSSI value: " + meanRSSI);
                System.out.println("Average RSSI value: " + meanRSSI);


            }
        });
        AngleButton = (Button) findViewById(R.id.AngleButton);
        AngleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finalAzimuthQueue.add(getAngle());
            }
        });



        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Client.createMap();
                i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"chrpng.brd@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Steps Test");
                //i.putExtra(Intent.EXTRA_TEXT, "#StepDistribution" + stepQueue + "#AccelerationData" + queue);
                i.putExtra(Intent.EXTRA_TEXT, "#Azimuth" + finalAzimuthQueue);
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
                tempAzimuthQueue.clear();
                aziMeasures = 0;
                aziMeasureBar.setProgress(aziMeasures);
                //finalAzimuthQueue.clear();
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
                                //System.out.println(wifiInfo.getSSID());
                                //System.out.println(wifiInfo.getSSID().equals("CMU-SECURE"));
                                //System.out.println(wifiInfo.getBSSID());
                                rssiView.setText("RSSI " + cycleCount);

                                cycleCount++;
                                wifiManager.startScan();
                                List<ScanResult> wifiList = wifiManager.getScanResults();
                                for (ScanResult scanResult : wifiList) {
                                    //level = WifiManager.calculateSignalLevel(scanResult.level, 5);
                                    //AP1 00:1a:1e:88:46:41
                                    //AP2 00:1a:1e:93:1e:61
                                    if(scanResult.BSSID.equals(AP1)) {
//                                        System.out.println(scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level);
                                        RSSI_AP1 = scanResult.level;
                                    }
                                    if(scanResult.BSSID.equals(AP2)) {
                                        //System.out.println(wifiInfo.getMacAddress());
//                                        System.out.println(scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level);
                                        RSSI_AP2 = scanResult.level;
                                    }
                                    sigStrView.setText(": " + RSSI_AP1 + ", " + RSSI_AP2);
                                }

                                level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
                                //setContentView(R.layout.layoutName);
                                //sigStrView.setText("Signal level: " + level);
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
            float[] Rs = new float[9];
            float[] I = new float[9];
            float[] R = new float[9];
            boolean success = SensorManager.getRotationMatrix(Rs, I, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.remapCoordinateSystem(Rs, SensorManager.AXIS_X, SensorManager.AXIS_Z, R);
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];
                azimutD = (float) Math.toDegrees(azimut);
                //displayOrientValues();
                tempAzimuthQueue.add(azimutD);
                aziMeasures++;
                aziMeasureBar.setProgress(aziMeasures);
            }
        }

        if(event.sensor == linaccelerometer) {

            accelDelX = accelOldX - event.values[0];
            accelDelY = accelOldY - event.values[1];
            accelDelZ = accelOldZ - event.values[2];

            if (Math.abs(accelOldX - event.values[0]) > 0.2) {
                accelOldX = event.values[0];
            }
            if (Math.abs(accelOldY - event.values[1]) > 0.2) {
                accelOldY = event.values[1];
            }
            if (Math.abs(accelOldZ - event.values[2]) > 0.2) {
                accelOldZ = event.values[2];
            }
            //displayAccelValues();
            //accelX.add(accelOldX);
            //accelY.add(accelOldY);
            //accelZ.add(accelOldZ);
            accelBarValue = (float) Math.sqrt(Math.pow(accelOldX, 2) + Math.pow(accelOldY, 2) + Math.pow(accelOldZ, 2));
            queue.add(accelBarValue);
            seconds = (double) (System.nanoTime() - currentTime) / 1E9;
            if (accelBarValue > 5f && seconds > 0.2f) { // for cell nexus s
//            if (accelBarValue > 2f && seconds > 0.4f) { // for tablet
                stepDetectValue = 1f;
                currentTime = System.nanoTime();
                //stepView.setText("Steps: " + ++stepCount);
            } else {
                stepDetectValue = 0f;
            }
            stepQueue.add(stepDetectValue);

            if(stepDetectValue == 1f) {
                float z = getAngle();
                System.out.println("Step angle=" + z);

                stepAngle = (float) (Math.toRadians( z) + (Math.PI/6.0));
                delPosX = (float) (stepSize * Math.sin(stepAngle)); //xPos
                delPosY = (float) (stepSize * Math.cos(stepAngle)); //yPos
                delLatY = delPosY * 0.0000090519550080342795929298339582217;
                delLongX = delPosX * 0.0000077606358111266947171575502571295;
                latY += delLatY;
                longX += delLongX;
                String url = constructURL(serverIP,name,latY,longX,delLatY,delLongX);
                dist_AP1 = -1.8547 * RSSI_AP1 - 47.697; // meters
                dist_AP2 = -1.8547 * RSSI_AP2 - 47.697; // meters

                mWebView.loadUrl(url);
                //horizontal: -79.947056 to -79.946890 = 0.000166
                //21.39 meters
                //1 meter = 0.0000077606358111266947171575502571295
                //7.7606358111266947171575502571295e-6

                //vertical: 40.442096 to 40.442265 = 0.000169
                //18.67 meters
                //1 meter = 0.0000090519550080342795929298339582217
                //9.0519550080342795929298339582217e-6

                //Client.constructURL
                //displayRelPosValues();
            }

            finalAzimuthQueue.add(stepAngle);
            accelBar.setProgress((int)accelBarValue);
                /*
                float x_max, x_min;
                float y_max, y_min;
                float z_max, z_min;
                double dist_x, dist_y, dist_z;

                x_max = (float) Collections.max(accelX);
                y_max = (float) Collections.max(accelY);
                z_max = (float) Collections.max(accelZ);
//                System.out.println("x_max: " + x_max+" y_max: " + y_max+" z_max: " + z_max);

                x_min = (float) Collections.min(accelX);
                y_min = (float) Collections.min(accelY);
                z_min = (float) Collections.min(accelZ);
//                System.out.println("x_min: " + x_min+" y_min: " + y_min+" z_min: " + z_min);

                double s = 0.45;
                dist_x = s*Math.pow((x_max - x_min),1.0/4.0);
                dist_y = s*Math.pow((y_max - y_min),1.0/4.0);
                dist_z = s*Math.pow((z_max - z_min),1.0/4.0);

//                System.out.println("x_max - x_min: " + (x_max - x_min)+" y_max - y_min: " + (y_max - y_min)+" z_max - z_min: " + (z_max - z_min));
                System.out.println("X: " + dist_x+" Y: " + dist_y+" Z: " + dist_z);


                Math.pow(dist_x,2.0);
                double dist = Math.pow(Math.pow(dist_x,2.0)+Math.pow(dist_y,2.0)+Math.pow(dist_z,2.0),1.0/2.0);
                stepDistView.setText("X: " + dist_x+" Y: " + dist_y+" Z: " + dist_z);
                System.out.println("size is "+ accelX.size());
                */
        }
    }


    /*
    public void displayAccelValues() {
        accelXView.setText(Float.toString(accelOldX));
        accelYView.setText(Float.toString(accelOldY));
        accelZView.setText(Float.toString(accelOldZ));
    }*/
    /*
    public void displayRelPosValues() {
        relXView.setText(Float.toString((float) posX));
        relYView.setText(Float.toString((float) posY));
        relAView.setText(Float.toString((float) Math.toDegrees(stepAngle)));
    }
    */
    /*
    public void displayOrientValues() {
        azimutView.setText(Float.toString(azimutD));
        pitchView.setText(Float.toString(pitch));
        rollView.setText(Float.toString(roll));
    }
    */
    public float getAngle() {
        if (tempAzimuthQueue.size() < 100) {
            meanAziSamples = tempAzimuthQueue.size();
        } else {
            meanAziSamples = 100;
        }

        totalAzi = 0;
        errorBucket = 0;
        goodBucket = 0;
        currAzi = 0;
        prevAzi = 0;
        for (int i = 0; i < meanAziSamples; i++) {
            currAzi = (float) tempAzimuthQueue.get(i);
            if(i > 0) {
                if((currAzi < prevAzi + 5) && (currAzi > prevAzi - 5)) {
                    totalAzi += currAzi;
                    prevAzi = currAzi;
                    //finalAzimuthQueue.add(currAzi);
                    errorBucket = 0;
                    goodBucket++;
                } else if(errorBucket < 5) {
                    errorBucket++;
                } else {
                    errorBucket = 0;
                    //finalAzimuthQueue.clear();
                    //finalAzimuthQueue.add(currAzi);
                    goodBucket = 1;
                    totalAzi = currAzi;
                    prevAzi = currAzi;
                }
            } else {
                totalAzi += currAzi;
                prevAzi = currAzi;
                goodBucket++;
            }
            ////System.out.println("temp Azi Queue value: " + tempAzimuthQueue.get(i));
            ////System.out.println("temp Azi Queue type: " + tempAzimuthQueue.get(i).getClass());
            //finalAzimuthQueue.add(tempAzimuthQueue.get(i));
            ////totalAzi += (float) 2;
        }
        meanAzi = totalAzi / goodBucket;

        tempAzimuthQueue.clear();
        aziMeasures = 0;
        aziMeasureBar.setProgress(aziMeasures);
        return meanAzi;
    }

    public static String constructURL(String IP,String name,double latt, double longi, double vecy, double vecx){
        String url = "http://";
        url=url+IP;
        url=url+"/insert.php?";
        url=url+"name="+name;
        url=url+"&latt="+latt;
        url=url+"&long="+longi;
        url=url+"&vecx="+vecy;
        url=url+"&vecy="+vecx;
        return url;
    }

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        InputStream is = conn.getInputStream();
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader rd = new BufferedReader(ir);
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}
