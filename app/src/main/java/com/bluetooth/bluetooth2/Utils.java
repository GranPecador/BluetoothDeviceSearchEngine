package com.bluetooth.bluetooth2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class Utils {

    private static SharedPreferences mSharedPreferences;
    private static String mFilePref = "com.bluetooth.bluetooth2";

    public static void initializeReceiverAddress(Context context) {
        mSharedPreferences = context.getSharedPreferences(mFilePref, MODE_PRIVATE);
        getReceiverIpAddress();
        getReceiverPort();
    }

    public static void getReceiverIpAddress() {
        ReceiverAddress.ipAddressText =
                mSharedPreferences.getString("ip_address", "192.168.1.4");
        String[] addr = (ReceiverAddress.ipAddressText).split("\\.");
        ReceiverAddress.ipAddressBytes = new byte[]{(byte)Integer.parseInt(addr[0]),
                (byte)Integer.parseInt(addr[1]),
                (byte)Integer.parseInt(addr[2]),
                (byte)Integer.parseInt(addr[3])};
    }

    public static void getReceiverPort() {
        ReceiverAddress.port = mSharedPreferences.getInt("port", 12053);
    }

    public static void setReceiverIpAddress(String address){
        SharedPreferences.Editor preferencesEditor = mSharedPreferences.edit();
        preferencesEditor.putString("ip_address", address);
        preferencesEditor.apply();
        getReceiverIpAddress();
    }

    public static void setReceiverPort(int port){
        SharedPreferences.Editor preferencesEditor = mSharedPreferences.edit();
        preferencesEditor.putInt("port", port);
        preferencesEditor.apply();
        getReceiverPort();
    }



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
