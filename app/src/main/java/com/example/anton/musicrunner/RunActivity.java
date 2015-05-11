package com.example.anton.musicrunner;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.content.Context;
import android.app.AlertDialog;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.*;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.OnInitListener;

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
    int[] slowSongs = new int[]{R.raw.slow1, R.raw.slow2};
    int[] mediumSongs = new int[]{R.raw.medium1, R.raw.medium2};
    int[] fastSongs = new int[]{R.raw.fast1, R.raw.fast2};
    int currentSong = 0;
    Chronometer chronometer;
    long timeWhenStopped = 0; //för att kunna pausa chronometern.
    //Talk variabler

    protected static final int REQUEST_OK = 1;
    private TextToSpeech ttobj;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //För att aktivera enhetens sensor
        time = System.currentTimeMillis();

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();


        //TALK
        ttobj = new TextToSpeech(getApplicationContext(), new OnInitListener(){
           @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {

                    int result = ttobj.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        //om allt funkar så gör vadå? kanske inget.
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }

            }
        });





    }

    /**
     * Ger användaren valet om den vill avsluta löprundan. Detta för att undvika att aktiviteten stängs ner av misstag
     */
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

    /**
     * Medtod för att ta hand om stop knappens anrop.
     */
    public void endRunButton(View v) {
        endRun();
    }

    /**
     * Avslutar löppasset och stoppar musiken
     */
    public void endRun() {
        mp.stop();
        mp.release();
        timeWhenStopped = 0;
        ttobj.stop();
        ttobj.shutdown();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void nextSongButton(View V) {
        nextSong();
    }

    public void nextSong() {
        if (tempo == 1) {
            if (slowSongs.length == currentSong + 1) {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, slowSongs[0]);
                mp.start();
                currentSong = 0;

            } else {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, slowSongs[currentSong + 1]);
                currentSong++;
                mp.start();
            }
        } else if (tempo == 2) {

            if (mediumSongs.length == currentSong + 1) {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, mediumSongs[0]);
                mp.start();
                currentSong = 0;
            } else {

                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, mediumSongs[currentSong + 1]);
                currentSong++;
                mp.start();
            }
        } else if (tempo == 3) {
            if (fastSongs.length == currentSong + 1) {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, fastSongs[0]);
                mp.start();
                currentSong = 0;

            } else {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, fastSongs[currentSong + 1]);
                currentSong++;
                mp.start();
            }
        }
    }

    public void previousSongButton(View V) {
        previousSong();
    }

    public void previousSong() {
        if (tempo == 1) {
            if (currentSong == 0) {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, slowSongs[slowSongs.length - 1]);
                mp.start();
                currentSong = slowSongs.length - 1;

            } else {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, slowSongs[currentSong - 1]);
                currentSong--;
                mp.start();
            }
        } else if (tempo == 2) {

            if (currentSong == 0) {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, mediumSongs[mediumSongs.length - 1]);
                mp.start();
                currentSong = mediumSongs.length - 1;
            } else {

                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, mediumSongs[currentSong - 1]);
                currentSong--;
                mp.start();
            }
        } else if (tempo == 3) {
            if (currentSong == 0) {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, fastSongs[fastSongs.length - 1]);
                mp.start();
                currentSong = fastSongs.length - 1;

            } else {
                mp.stop();
                mp.reset();
                mp = MediaPlayer.create(this, fastSongs[currentSong - 1]);
                currentSong--;
                mp.start();
            }
        }
    }

    /**
     * Körs varje gång ett steg registreras. I intervaller bestäms tempot, mellan det räknas stegen.
     */
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

    /**
     * Byter låt när användaren ändrar tempot den springer i, byter endast låt om tempot ändras.
     */
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



    /**
     * Starten en låt från en av tre listor med låtar. Vilken lista som används bestäms av vilket tempo användaren springer i.
     */
    public void playSong() {
        //int x = (int) (Math.random() * 2);
        int x = 0;
        currentSong = x;
        mp.stop();
        mp.release();
        if (tempo == 1) {
            mp = MediaPlayer.create(this, slowSongs[x]);
        } else if (tempo == 2) {
            mp = MediaPlayer.create(this, mediumSongs[x]);
        } else if (tempo == 3) {
            mp = MediaPlayer.create(this, fastSongs[x]);
        }
        mp.start();
        mp.setOnCompletionListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Startar en ny låt när en låt tagit slut.
     */
    @Override
    public void onCompletion(MediaPlayer mp) { //När en låt spelats klart spelas en ny från samma tempo nivå
        playSong();
    }

    /**
     * När aktiviteten ska köras skapas en sensor som registerar rörelse och ett sensor objekt för att ta emot händelser från sensormanager.
     */
    @Override
    protected void onResume() { //När aktiviteten ska köras skapas en stegräknare som registerar rörelse.
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER); //Skapar en sensor för att registrera steg
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor,
                    SensorManager.SENSOR_DELAY_UI); //Får händelser från sensormanager och optimerar uppdateringsfrekvensen för gränsnittet
        } else {
            Toast.makeText(this, "Count sensor not available!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
    }


    public void talk(View v) {
        contSpeak();
    }

    public void contSpeak() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ((TextView) findViewById(R.id.fillMe)).setText(thingsYouSaid.get(0));
            shownHide(thingsYouSaid);
            contSpeak();
        }


    }
    public void shownHide(ArrayList<String> command) {
        if (command.get(0).equals("next")) {
            //showButton();
            nextSong();

        } else if (command.get(0).equals("back")) {
            //  hideButton();
            previousSong();

        } else if (command.get(0).equals("time")) {
            //  hideButton();
            tellTime();
        } else if (command.get(0).equals("stop")) {
            //  hideButton();
            mp.pause();
            timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
            ttobj.speak("Music Paused", TextToSpeech.QUEUE_ADD, null);
            onPause();
        } else if (command.get(0).equals("start")) {
            //  hideButton();
            onResume();
            chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            chronometer.start();
            mp.start();
            }else if (command.get(0).equals("end")) {

            finalTime();
            try {
                Thread.sleep(12000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            endRun();
        }
    }

        public void finalTime(){

          mp.stop();
          ttobj.speak("You ran for", TextToSpeech.QUEUE_ADD, null);
            tellTime();
          ttobj.speak("Good job", TextToSpeech.QUEUE_ADD, null);

        }
        public void tellTime(){


           long runTime = SystemClock.elapsedRealtime()- chronometer.getBase();

            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss");
            String date = DATE_FORMAT.format(runTime);
            String[] parts = date.split(":");
            String part1 = parts[0]; //min
            String part2 = parts[1]; //sec


            String text = part1 +" Minutes and "+part2+" Seconds ";
            //String text="Hello you bastard";

            ttobj.speak(text, TextToSpeech.QUEUE_ADD, null);
    }



}
