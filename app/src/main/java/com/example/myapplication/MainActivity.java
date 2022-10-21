package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    TextView timerText;
    Button startStopButton;

    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;

    boolean timerStarted = false;

    private int Activity_Permission_Code = 1;
    private Button BtnRequest;

    TextView tv_steps;

    SensorManager sensorManager;

    boolean running = false;

    int stepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        timerText = (TextView) findViewById(R.id.timerText);
        startStopButton = (Button) findViewById(R.id.startStopBtn);

        timer = new Timer();

        BtnRequest = (Button) findViewById(R.id.test_permission);


        BtnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "You already have granted permission", Toast.LENGTH_SHORT).show();
                } else {
                    requestActivityRecognition();
                }
            }
        });

        tv_steps = (TextView) findViewById(R.id.tv_Steps);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


    }


    public void ResetTapped (View view){
        AlertDialog.Builder resetAlert = new AlertDialog.Builder(this);
        resetAlert.setTitle("Reset Timer");
        resetAlert.setMessage("Are you sure you want to reset the timer?");
        resetAlert.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(timerTask !=null){
                    timerTask.cancel();
                    setButtonUI("start", R.color.green);
                    time = 0.0;
                    timerStarted = false;
                    timerText.setText(formatTime(0,0,0));
                }
                running = false;
                stepCount = 0;
                tv_steps.setText(String.valueOf(stepCount));
            }
        });

        resetAlert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        resetAlert.show();

    }

    public void startStopTapped (View view){
        if (timerStarted == false)
        {
            timerStarted = true;
            running = true;
            setButtonUI("Stop",R.color.red);
            startTimer();
        }
        else
        {
            timerStarted = false;
            running = false;
            setButtonUI("Start",R.color.green);
            timerTask.cancel();
        }

    }
    private void setButtonUI(String start, int p){
        startStopButton.setText(start);
        startStopButton.setTextColor(ContextCompat.getColor(this, p));
    }

    private void startTimer(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        timerText.setText(getTimerText());
                    }
                });

            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }
    private String getTimerText() {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;

        int minutes = ((rounded % 86400) % 3600) / 60;

        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds,minutes,hours);
    }

    private String formatTime(int seconds, int minutes, int hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }

    private void requestActivityRecognition (){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Allow 8you to access your physical activity?")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACTIVITY_RECOGNITION}, Activity_Permission_Code );
                        }
                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACTIVITY_RECOGNITION}, Activity_Permission_Code );
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Activity_Permission_Code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = false;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else{
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running) {
            stepCount = (int) (stepCount+event.values[0]);
            tv_steps.setText(String.valueOf(stepCount));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}