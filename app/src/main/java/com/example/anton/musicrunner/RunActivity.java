package com.example.anton.musicrunner;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.app.AlertDialog;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.*;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.Toast;

/**
 * Created by Anton on 2015-03-23.
 */
public class RunActivity extends Activity implements SensorEventListener, OnCompletionListener {

    boolean firstTime = true;
    private static final long UPDATEMILLIS = 30000;
    private static final int slow = 76; // 76 //Gränsvärden för hur många steg som max  får tas under 30 sek får en viss musiknivå
    private static final int medium = 108; // 108 //
    private SensorManager sensorManager;
    boolean activityRunning; //True ifall stegsensorn registrerar steg
    private int steps;
    private long time;
    private int tempo = 0; //Vilken musiknivå det är
    MediaPlayer mp = new MediaPlayer();
    int[] slowSongs = new int[] { R.raw.slow1};
    int[] mediumSongs = new int[] { R.raw.medium1 };
    int[] fastSongs = new int[] { R.raw.fast1};

    /*    int[] slowSongs = new int[] { R.raw.slow1, R.raw.slow2, R.raw.slow3 };
    int[] mediumSongs = new int[] { R.raw.medium1, R.raw.medium2, R.raw.medium3 };
    int[] fastSongs = new int[] { R.raw.fast1, R.raw.fast2, R.raw.fast3 }; */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //För att aktivera enhetens sensor
        time = System.currentTimeMillis();
        Chronometer chronometer;
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    /**Ger användaren valet om den vill avsluta löprundan. Detta för att undvika att aktiviteten stängs ner av misstag */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Exit");
        builder.setMessage("Do you want to end your run?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                endRun();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    /**Medtod för att ta hand om stop knappens anrop.*/
    public void endRunButton(View v) {
        endRun();
    }
    /**Avslutar löppasset och stoppar musiken*/
    public void endRun() {
        mp.stop();
        mp.release();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    /**Körs varje gång ett steg registreras. I intervaller bestäms tempot, mellan det räknas stegen.*/
    @Override
    public void onSensorChanged(SensorEvent event) { //Körs varje gång hårdvaran registrerar ett steg
        if (activityRunning) {
            if (firstTime == false) {//om det inte är första gången aktiviteten körs
                if (System.currentTimeMillis() - time > UPDATEMILLIS) { //Var 30 sek bestäms tempot
                    tempo();
                } else {
                    steps++;
                }
            }
            if (firstTime == true) { //om det är första gången aktiviteten körs
                if (System.currentTimeMillis() - time > 10000) { //första gången dröjer det 10 sek innan tempot bestäms
                    steps = steps * 3;
                    firstTime = false;
                    tempo();
                } else {
                    steps++;
                }
            }
        }
    }
    /**Byter låt när användaren ändrar tempot den springer i, byter endast låt om tempot ändras.*/
    public void tempo() { //Byter låt ifall användaren ändrar tempot den springer i. Körs var 30 sek.
        if (steps <= slow && tempo != 1) { //byter endast låt om tempot ändras.
            tempo = 1;
            playSong();
        } else if (steps > slow && steps <= medium && tempo != 2) {
            tempo = 2;
            playSong();
        } else if (steps > medium && tempo != 3) {
            tempo = 3;
            playSong();
        }
        steps = 0;
        time = System.currentTimeMillis();
    }
    /**Starten en låt från en av tre listor med låtar. Vilken lista som används bestäms av vilket tempo användaren springer i.*/
    public void playSong() {
        //int x = (int) (Math.random() * 2);
        int x = 0;
        mp.stop();
        mp.release();
        if (tempo == 1) {
            mp = MediaPlayer.create(this, slowSongs[x]);
        }
        else if (tempo == 2) {
            mp = MediaPlayer.create(this, mediumSongs[x]);
        }
        else if (tempo == 3) {
            mp = MediaPlayer.create(this, fastSongs[x]);
        }
        mp.start();
        mp.setOnCompletionListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    /** Startar en ny låt när en låt tagit slut.*/
    @Override
    public void onCompletion(MediaPlayer mp) { //När en låt spelats klart spelas en ny från samma tempo nivå
        playSong();
    }
    /**När aktiviteten ska köras skapas en sensor som registerar rörelse och ett sensor objekt för att ta emot händelser från sensormanager.*/
    @Override
    protected void onResume() { //När aktiviteten ska köras skapas en stegräknare som registerar rörelse.
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER); //Skapar en sensor för att registrera steg
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor,
                    SensorManager.SENSOR_DELAY_UI); //Får händelser från sensormanager och optimerar uppdateringsfrekvensen för gränsnittet
        }
        else {
            Toast.makeText(this, "Count sensor not available!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
    }
}
