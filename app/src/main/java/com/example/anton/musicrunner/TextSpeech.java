package com.example.anton.musicrunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.*;
import android.util.Log;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by Anton on 2015-03-23.
 */


public class TextSpeech extends Application implements TextToSpeech.OnInitListener{

   private TextToSpeech tts;



    public TextSpeech (){



    }

    public void start(){

        speakOut();

    }


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

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

    private void speakOut() {

        String text="Hello you bastard";

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}

