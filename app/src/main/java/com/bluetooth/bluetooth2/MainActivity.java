package com.bluetooth.bluetooth2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 2;

    private final static String STOPPED_REASON_SERVICE_IS_NOT_WORKING = "Service is ready to work.";
    private final static String BLUETOOTH_REASON_SERVICE_IS_NOT_WORKING = "Turn on the bluetooth to make the service work.";
    private final static String NETWORK_IS_UNREACHABLE_REASON_SERVICE_IS_NOT_WORKING =
            "Network is unreachable.\nTurn on the Internet to make the service work.";

    public final static String STOPPED_SERVICE_BROADCAST_RECEIVER = "stopped_service";
    public final static String NETWORK_IS_UNREACHABLE_SERVICE_BROADCAST_RECEIVER = "Network is unreachable";

    private final static String BOUND_SAVED_STATE = "bound_save";
    private final static String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mBound = false;
    private String mState = "Ready start";

    private TextView mMessage;
    private Button mStart;
    private Button mFinish;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (STOPPED_SERVICE_BROADCAST_RECEIVER.equals(action)){
                mBound = false;
                if(mBluetoothAdapter.isEnabled())showServiceNotWorkView(STOPPED_REASON_SERVICE_IS_NOT_WORKING);
                else showServiceNotWorkView(BLUETOOTH_REASON_SERVICE_IS_NOT_WORKING);
            }
            if (NETWORK_IS_UNREACHABLE_SERVICE_BROADCAST_RECEIVER.equals(action)) {
                mBound = false;
                showServiceNotWorkView(NETWORK_IS_UNREACHABLE_REASON_SERVICE_IS_NOT_WORKING);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            mStart = findViewById(R.id.start);
            mFinish = findViewById(R.id.finish);
            mMessage = findViewById(R.id.message);

            if (savedInstanceState != null) {
                mBound = savedInstanceState.getBoolean(BOUND_SAVED_STATE, false);
            } else {
                mBound = isMyServiceRunning(BluetoothService.class);
            }

            if (mBound)showServiceWorkView();
            else {
                showServiceNotWorkView(STOPPED_REASON_SERVICE_IS_NOT_WORKING);
                Utils.initializeReceiverAddress(this);
            }


            if (!mBluetoothAdapter.isEnabled()) {
                showServiceNotWorkView(BLUETOOTH_REASON_SERVICE_IS_NOT_WORKING);
            }

            if (!checkPermissions()) {
                Toast.makeText(this, "Give me permission: ACCESS_FINE_LOCATION", Toast.LENGTH_LONG).show();
                requestMultiplePermissions();
            }

            IntentFilter serviceIntentFilter = new IntentFilter();
            serviceIntentFilter.addAction(STOPPED_SERVICE_BROADCAST_RECEIVER);
            serviceIntentFilter.addAction(NETWORK_IS_UNREACHABLE_SERVICE_BROADCAST_RECEIVER);
            getApplicationContext().registerReceiver(mBroadcastReceiver, serviceIntentFilter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BOUND_SAVED_STATE, mBound);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT || requestCode == 1111) {
            if (resultCode == Activity.RESULT_OK) {
                startSearchBluetoothDevices();
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                if (!mBound) {
                    if (checkPermissions()) {
                        startSearchBluetoothDevices();
                    } else {
                        Toast.makeText(this, "Give me permission: ACCESS_FINE_LOCATION", Toast.LENGTH_LONG).show();
                        requestMultiplePermissions();
                    }
                }
                break;
            case R.id.finish:
                if (mBound) {
                    Intent intentFinish = new Intent(this, BluetoothService.class);
                    stopService(intentFinish);
                    mBound = false;
                    showServiceNotWorkView(STOPPED_REASON_SERVICE_IS_NOT_WORKING);
                }
                break;
        }
    }

    private void startSearchBluetoothDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            moveToStartedState();
            mBound = true;
            showServiceWorkView();
        } else {
            enableBT();
            showServiceNotWorkView(BLUETOOTH_REASON_SERVICE_IS_NOT_WORKING);
        }
    }

    private void enableBT() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
    }

    public void showServiceWorkView(){
        mStart.setEnabled(false);
        mFinish.setEnabled(true);
        mState = "Service is looking for devices with bluetooth enabled.";
        mMessage.setText(mState);
    }

    public void showServiceNotWorkView(String reason){
        mStart.setEnabled(true);
        mFinish.setEnabled(false);
        mState = reason;
        mMessage.setText(mState);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void moveToStartedState() {
        Intent intent = new Intent(this, BluetoothService.class);
        if (Utils.isPreAndroidO())
            startService(intent);
        else startForegroundService(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int RequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (RequestCode == MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    Log.i(TAG, "Permission denied without 'NEVER ASK AGAIN': "
                            + permissions[0]);
                    showRequestPermissionsSnackbar();
                } else {
                    Log.i(TAG, "Permission denied with 'NEVER ASK AGAIN': "
                            + permissions[0]);
                    showLinkToSettingSnackbar();
                }
            } else {
                startSearchBluetoothDevices();
            }
        }
    }

    public void showRequestPermissionsSnackbar() {
        Snackbar.make(findViewById(R.id.main_activity_container),
                R.string.permission_relation,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                    }
                }).show();
    }

    public void showLinkToSettingSnackbar() {
        Snackbar.make(findViewById(android.R.id.content),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle item selection
        switch (item.getItemId()) {
            case R.id.change_recipient:
                Intent intent = new Intent(this, ChangeRecipient.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(mBroadcastReceiver);
    }
}

