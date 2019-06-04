package com.bluetooth.bluetooth2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeRecipient extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_recipient);



        final EditText ipAddress = findViewById(R.id.ipAddress);
        ipAddress.setHint("Now ipAddress: \n"+ ReceiverAddress.ipAddressText);
        ipAddress.addTextChangedListener(new MaskWatcher("###.###.###.###"));

        final EditText port = findViewById(R.id.port);
        port.setHint("Now port: "+ ReceiverAddress.port);

        final Button saveIPAddress = findViewById(R.id.saveIPAddress);
        saveIPAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempAddress = ipAddress.getText().toString();
                if (!tempAddress.isEmpty())
                    Utils.setReceiverIpAddress(ipAddress.getText().toString());
                ipAddress.setText("");
                ipAddress.setHint("Now ipAddress: \n"+ ReceiverAddress.ipAddressText);
                String tempPort = port.getText().toString();
                if (!tempPort.isEmpty())
                    Utils.setReceiverPort(Integer.parseInt(tempPort));
                port.setText("");
                port.setHint("Now port: "+ ReceiverAddress.port);
                if(port.hasFocus()) port.clearFocus();
                ipAddress.clearFocus();

                Toast.makeText(getApplicationContext(), "IP:"+ ReceiverAddress.ipAddressText, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
