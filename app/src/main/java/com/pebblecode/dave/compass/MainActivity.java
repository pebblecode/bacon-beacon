package com.pebblecode.dave.compass;

import android.bluetooth.BluetoothDevice;
import android.hardware.Sensor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import android.content.Context;

import com.pebblecode.dave.compass.Classes.BeaconDevice;
import com.pebblecode.dave.compass.Classes.BeaconFactory;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int mAzimuth = 0; // degree

    private SensorManager mSensorManager = null;

    private Sensor mGravity;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private int mInterval = 500;
    private Handler mHandler;

    boolean haveGravity = false;
    boolean haveAccelerometer = false;
    boolean haveMagnetometer = false;

    private BluetoothAdapter mBluetoothAdapter;

    private int mainBearing = 0;


    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        float[] gData = new float[3]; // gravity or accelerometer
        float[] mData = new float[3]; // magnetometer
        float[] rMat = new float[9];
        float[] iMat = new float[9];
        float[] orientation = new float[3];

        public void onAccuracyChanged( Sensor sensor, int accuracy ) {}

        @Override
        public void onSensorChanged( SensorEvent event ) {
            float[] data;
            switch ( event.sensor.getType() ) {
                case Sensor.TYPE_GRAVITY:
                    gData = event.values.clone();
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    gData = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mData = event.values.clone();
                    break;
                default: return;
            }

            if ( SensorManager.getRotationMatrix( rMat, iMat, gData, mData ) ) {
                mAzimuth= (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;

            }
        }
    };
    private ImageView image;
    TextView tvHeading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageView);
        tvHeading = (TextView) findViewById(R.id.textView);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        this.mGravity = this.mSensorManager.getDefaultSensor( Sensor.TYPE_GRAVITY );
        this.haveGravity = this.mSensorManager.registerListener( mSensorEventListener, this.mGravity, SensorManager.SENSOR_DELAY_GAME );

        this.mAccelerometer = this.mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        this.haveAccelerometer = this.mSensorManager.registerListener( mSensorEventListener, this.mAccelerometer, SensorManager.SENSOR_DELAY_GAME );

        this.mMagnetometer = this.mSensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
        this.haveMagnetometer = this.mSensorManager.registerListener( mSensorEventListener, this.mMagnetometer, SensorManager.SENSOR_DELAY_GAME );

        // if there is a gravity sensor we do not need the accelerometer
        if( this.haveGravity )
            this.mSensorManager.unregisterListener( this.mSensorEventListener, this.mAccelerometer );

        if ( ( haveGravity || haveAccelerometer ) && haveMagnetometer ) {
            // ready to go
            tvHeading.setText("ready");
        } else {
            // unregister and stop
            tvHeading.setText(":(");
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter.startLeScan(mLeScanCallback);

        mHandler = new Handler();
        startRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                RotateToTarget(mAzimuth - mainBearing);
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    int lastRotation;

    private void RotateToTarget(int newRotation) {
        RotateAnimation ra = new RotateAnimation(
                lastRotation, //current
                -newRotation, // target
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        lastRotation = -newRotation;

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);

    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    List<BeaconDevice> bdevices = BeaconFactory.BuildBeaconMap();
                    int bearing = 0;

                    for(int i = 0; i<bdevices.size(); i++){
                        if(device.getAddress().equals(bdevices.get(i).getDeviceId())){
                            bearing = bdevices.get(i).getBearing();
                            if(rssi < 90 && bearing > 0){
                                mainBearing = bearing;
                            }
                        }
                    }

                    Log.d(TAG, "bearing:" + bearing + ", " + device.getAddress() + ", " + rssi);
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();


    }

}
