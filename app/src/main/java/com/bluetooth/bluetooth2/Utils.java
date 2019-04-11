package com.bluetooth.bluetooth2;

import android.os.Build;

import java.util.Random;

public class Utils {

    public static boolean isPreAndroidO(){
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1;
    }

    public static boolean isNotPreAndroidP(){
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1;
    }

    public static int getRandomNumber() {
        return new Random().nextInt(1000000);
    }
}
