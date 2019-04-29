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
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
            //Log.d(TAG, "onReceive(): "+ action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, device.getName() + " : " + device.getAddress());
                sendAddresses(("/c/" + device.getAddress()).getBytes());
            }
            else {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int adapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (adapterState == BluetoothAdapter.STATE_OFF || adapterState == BluetoothAdapter.STATE_TURNING_OFF){
                        Log.e(TAG, "Bluetooth Adapter is off.");
                        Intent stopServiceIntent = new Intent(MainActivity.STOPPED_SERVICE_BROADCAST_RECEIVER);
                        sendBroadcast(stopServiceIntent);
                        stopForeground(true);
                        stopSelf();
                    }
                }
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
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Utils.isPreAndroidO())
            HandlerNotifications.PreO.createNotification(this);
        else HandlerNotifications.O.createNotification(this);

        if (intent.getAction() != null && intent.getAction().equals("stop_service")) {
            Intent stopServiceIntent = new Intent(MainActivity.STOPPED_SERVICE_BROADCAST_RECEIVER);
            sendBroadcast(stopServiceIntent);
            stopForeground(true);
            stopSelf();
        }
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
                        System.out.println(InetAddress.getByAddress(ReceiverAddress.ipAddress).toString()+
                                ReceiverAddress.port);
                        DatagramPacket packet = new DatagramPacket(message, message.length,
                                InetAddress.getByAddress(ReceiverAddress.ipAddress),
                                ReceiverAddress.port);
                        mSocket.send(packet);
                    } catch (IOException e) {
                        sendBroadcast(new Intent(MainActivity.NETWORK_IS_UNREACHABLE_SERVICE_BROADCAST_RECEIVER));
                        stopForeground(true);
                        stopSelf();
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
