package com.bluetooth.bluetooth2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothService extends Service {

    private static final String TAG = "BTTT";

    private Timer timer;
    private BluetoothAdapter mBluetoothAdapter;
    private DatagramSocket mSocket;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d(TAG, "onReceive(): "+ action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, device.getName() + " : " + device.getAddress());
                sendAddresses(("/c/" + device.getAddress()).getBytes());
            }
        }
    };

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // Device search Bluetooth
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                bluetoothSearch();
            }
        }, 0, 12000);
        return super.onStartCommand(intent, flags, startId);
    }

    private void bluetoothSearch() {

        if (!mBluetoothAdapter.isEnabled()) {
            timer.cancel();
            stopSelf();
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (mBluetoothAdapter.startDiscovery()) { //scan of about 12 seconds
            Log.d(TAG, "Start search");
        }

    }

    private void sendAddresses(final byte[] message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isOnline()) {
                    try {
                        System.out.println(InetAddress.getByAddress(receiverAddress.ipAddress).toString()+
                                receiverAddress.port);
                        DatagramPacket packet = new DatagramPacket(message, message.length,
                                InetAddress.getByAddress(receiverAddress.ipAddress),
                                receiverAddress.port);
                        mSocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return true;
        //return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.d(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
