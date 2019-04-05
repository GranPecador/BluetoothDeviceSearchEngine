package com.bluetooth.bluetooth2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeRecipient extends AppCompatActivity {

    String enterText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_recipient);



        final EditText ipAddress = findViewById(R.id.ipAddress);
        ipAddress.addTextChangedListener(new MaskWatcher("###.###.###.###"));

        final EditText port = findViewById(R.id.port);

        Button saveIPAddress = findViewById(R.id.saveIPAddress);
        saveIPAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), ipAddress.getText(), Toast.LENGTH_SHORT).show();
                //receiverAddress.ipAddress = ipAddress.getText().toString();
                receiverAddress.port = Integer.parseInt(port.getText().toString());
                Toast.makeText(getApplicationContext(), "IP:"+ receiverAddress.ipAddress, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
