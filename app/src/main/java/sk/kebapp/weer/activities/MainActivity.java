package sk.kebapp.weer.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import sk.kebapp.weer.R;
import sk.kebapp.weer.application.ConnectionSettings;

public class MainActivity extends AppCompatActivity {

    private enum ConnectionType {
        RTMP, USB, UDP
    }

    SharedPreferences mSharedPreferences;
    int permissionCheck = -1;

    private ConnectionType connectionType = ConnectionType.RTMP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        Button b = (Button) findViewById(R.id.simulatedStereoButton);
        final MainActivity c = this;
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStereo(false);
            }
        });

        b = (Button) findViewById(R.id.realStereoButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStereo(true);
            }
        });

//        b = (Button) findViewById(R.id.markerTrackerButton);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MarkerTracker.intentTo(c);
//            }
//        });

        EditText e = (EditText) findViewById(R.id.ipeditText);
        e.setText(mSharedPreferences.getString("lastIp", "192.168."));

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setChecked(ConnectionSettings.useOCV);

        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int lastMode = mSharedPreferences.getInt("lastMode", 0);

        int selectedButton = 0;
        switch (lastMode) {
            case 0:
                selectedButton = R.id.radioButtonUSB;
                break;
            case 1:
                selectedButton = R.id.radioButtonRTMP;
                break;
            case 2:
                selectedButton = R.id.radioButtonUDP;
                break;
        }

        RadioButton rb = (RadioButton) findViewById(selectedButton);
        rb.setChecked(true);
    }

    public void trackingCheckboxClicked(View clickedView) {

        if (permissionCheck == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    public void updateVisibility(View clickedView) {
        setSettings();
        EditText editText = (EditText) findViewById(R.id.ipeditText);

        editText.setVisibility((connectionType == ConnectionType.RTMP) ? View.VISIBLE : View.INVISIBLE);

        editText = (EditText) findViewById(R.id.portEditText);
        editText.setVisibility((connectionType == ConnectionType.RTMP) ? View.VISIBLE : View.INVISIBLE);

        View colon = findViewById(R.id.textView);
        colon.setVisibility((connectionType == ConnectionType.RTMP) ? View.VISIBLE : View.INVISIBLE);
    }

    private void setSettings() {
        EditText b = (EditText) findViewById(R.id.ipeditText);
        ConnectionSettings.ip = b.getText().toString();
        b = (EditText) findViewById(R.id.portEditText);
        ConnectionSettings.port = b.getText().toString();

        CheckBox c = (CheckBox) findViewById(R.id.checkBox);
        ConnectionSettings.useOCV = c.isChecked();

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ConnectionSettings.useOCV = false;
            c.setChecked(false);
        }
        mSharedPreferences.edit().putString("lastIp", ConnectionSettings.ip).commit();

        RadioButton rb = (RadioButton) findViewById(R.id.radioButtonRTMP);
        if (rb.isChecked()) {
            connectionType = ConnectionType.RTMP;
            mSharedPreferences.edit().putInt("lastMode", 1).commit();
            return;
        }
        rb = (RadioButton) findViewById(R.id.radioButtonUSB);
        if (rb.isChecked()) {
            connectionType = ConnectionType.USB;
            ConnectionSettings.ip = "127.0.0.1";
            ConnectionSettings.port = "8888";
            mSharedPreferences.edit().putInt("lastMode", 0).commit();
            return;
        }
        rb = (RadioButton) findViewById(R.id.radioButtonUDP);
        if (rb.isChecked()) {
            connectionType = ConnectionType.UDP;
            mSharedPreferences.edit().putInt("lastMode", 2).commit();
            return;
        }
    }

    private void startStereo(boolean real) {
        setSettings();
        String url = null;
        switch (connectionType) {
            case RTMP:
                url = "rtmp://" + ConnectionSettings.ip + ":1935/live/test";
                break;
            case USB:
                url = "rtmp://127.0.0.1:1935/live/test";
                break;
            case UDP:
                ///TODO: real udp address is udp://127.0.0.1:1935
                url = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8";
//                url = "udp://127.0.0.1:1935";
                break;
        }

        if (real)
            RealStereoscopicActivity.intentTo(this, url);
        else
            SimulatedStereoscopicActivity.intentTo(this, url);
    }

}
