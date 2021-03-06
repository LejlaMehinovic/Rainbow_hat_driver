package com.bignerdranch.android.rainbowtemperature;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Gpio;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import java.io.IOException;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    Context context=this;

    // Default LED brightness
    public static final int LEDSTRIP_BRIGHTNESS = 1;
    private AlphanumericDisplay mDisplay;
    private Apa102 mLedstrip;
    private Bmx280SensorDriver mEnvironmentalSensorDriver;
    private SensorManager mSensorManager;
    private final int REQUEST_PERMISSION_MENAGE_DRIVERS=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Weather Station Started");

        mSensorManager = getSystemService(SensorManager.class);

        // Initialize 14-segment display
        try {
            mDisplay = RainbowHat.openDisplay();
            mDisplay.setEnabled(true);
            mDisplay.display("1234");
            Log.d(TAG, "Initialized I2C Display");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing display", e);
        }

        // Initialize LED strip
        try {
            mLedstrip = RainbowHat.openLedStrip();
            mLedstrip.setBrightness(LEDSTRIP_BRIGHTNESS);
            int[] colors = new int[7];
            Arrays.fill(colors, Color.RED);
            mLedstrip.write(colors);
            // Because of a known APA102 issue, write the initial value twice.
            mLedstrip.write(colors);

            Log.d(TAG, "Initialized SPI LED strip");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing LED strip", e);
        }

        // Initialize temperature/pressure sensors
        try {
            mEnvironmentalSensorDriver = RainbowHat.createSensorDriver();
            // Register the drivers with the framework
            mEnvironmentalSensorDriver.registerTemperatureSensor();
            mEnvironmentalSensorDriver.registerPressureSensor();

            Log.d(TAG, "Initialized I2C BMP280");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing BMP280", e);
        }


    }
    @Override
    protected void onStart() {
        super.onStart();

        // Register the BMP280 temperature sensor
        Sensor temperature = mSensorManager.getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).get(0);
        mSensorManager.registerListener(mSensorEventListener, temperature, SensorManager.SENSOR_DELAY_NORMAL);
        // Register the BMP280 pressure sensor
        Sensor pressure = mSensorManager.getDynamicSensorList(Sensor.TYPE_PRESSURE).get(0);
        mSensorManager.registerListener(mSensorEventListener,pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onStop() {
        super.onStop();

        mSensorManager.unregisterListener(mSensorEventListener);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDisplay != null) {
            try {
                mDisplay.clear();
                mDisplay.setEnabled(false);
                mDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mDisplay = null;
            }
        }

        if (mLedstrip != null) {
            try {
                mLedstrip.setBrightness(0);
                mLedstrip.write(new int[7]);
                mLedstrip.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED strip", e);
            } finally {
                mLedstrip = null;
            }
        }

        if (mEnvironmentalSensorDriver != null) {
            try {
                mEnvironmentalSensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing sensors",  e);
            } finally {
                mEnvironmentalSensorDriver = null;
            }
        }
    }

    private void updateTemperatureDisplay(float temperature) {
        if (mDisplay != null) {
            try {
                mDisplay.display(temperature);
            } catch (IOException e) {
                Log.e(TAG, "Error updating display",e);
            }
        }
    }

    private void updateBarometerDisplay(float pressure) {

        if (mLedstrip != null) {
            try {
                int[] colors = RainbowUtil.getWeatherStripColors(pressure);
                mLedstrip.write(colors);
            } catch (IOException e) {
                Log.e(TAG, "Error updating ledstrip", e);
            }
        }
    }

    // Callback when SensorManager delivers new data.
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float value = event.values[0];

            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                updateTemperatureDisplay(value);
            } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                updateBarometerDisplay(value);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "accuracy changed: " + accuracy);
        }




    };





}

