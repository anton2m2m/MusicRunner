package com.example.anton.musicrunner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;

/**
 * Created by Anton on 2015-05-11.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    public RemoteControlReceiver()    {

    }

    public interface IDateCallback {
        void call();
    }

    private IDateCallback callerActivity;

    public  RemoteControlReceiver(Activity activity) {
        callerActivity = (IDateCallback)activity;
    }

    @Override
    public void onReceive(Context arg0, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
           // Log.e(TAG, "ACTION_MEDIA_BUTTON");
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                // Handle key press.
            }



            if (/*KeyEvent.KEYCODE_MEDIA_PLAY*/ 126 == event.getKeyCode()) { // KEYCODE_MEDIA_PLAY undefined for API < 11
              //  Log.e(TAG, "KEYCODE_MEDIA_PLAY");
            }
        }
    }
}

