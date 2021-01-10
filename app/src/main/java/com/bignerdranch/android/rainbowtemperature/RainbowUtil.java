package com.bignerdranch.android.rainbowtemperature;

import android.graphics.Color;

public class RainbowUtil {
    /* Barometer Range Constants */
    private static final float BAROMETER_RANGE_LOW = 965.f;
    private static final float BAROMETER_RANGE_HIGH = 1035.f;

    /* LED Strip Color Constants*/
    private static int[] sRainbowColors;
    static {
        sRainbowColors = new int[7];
        for (int i = 0; i < sRainbowColors.length; i++) {
            float[] hsv = {i * 360.f / sRainbowColors.length, 1.0f, 1.0f};
            sRainbowColors[i] = Color.HSVToColor(255, hsv);
        }
    }

    /**
     * Return an array of colors for the LED strip based on the given pressure.
     * @param pressure Pressure reading to compare.
     * @return Array of colors to set on the LED strip.
     */
    public static int[] getWeatherStripColors(float pressure) {
        float t = (pressure - BAROMETER_RANGE_LOW) / (BAROMETER_RANGE_HIGH - BAROMETER_RANGE_LOW);
        int n = (int) Math.ceil(sRainbowColors.length * t);
        n = Math.max(0, Math.min(n, sRainbowColors.length));

        int[] colors = new int[sRainbowColors.length];
        for (int i = 0; i < n; i++) {
            int ri = sRainbowColors.length - 1 - i;
            colors[ri] = sRainbowColors[ri];
        }

        return colors;
    }
}
