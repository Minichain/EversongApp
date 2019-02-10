package com.upf.minichain.eversongapp;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Utils class. Only "static" methods.
 */
public class Utils {
    public static float getAverage(float[] array, int length) {
        float sum = 0;
        for (int i = 0; i < length; i++) {
            sum += array[i];
        }
        return (sum / length);
    }

    public static double getAverage(double[] array, int length) {
        double sum = 0;
        for (int i = 0; i < length; i++) {
            sum += array[i];
        }
        return (sum / length);
    }

    public static float getStandardDeviation(float[] array, int length) {
        float sum = 0;
        float mean = getAverage(array, length);
        for (int i = 0; i < length; i++) {
            sum += Math.pow(array[i] - mean, 2);
        }
        return (sum / (length - 1));
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
