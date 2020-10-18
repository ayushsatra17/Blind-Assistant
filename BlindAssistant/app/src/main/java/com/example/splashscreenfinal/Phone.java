package com.example.splashscreenfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.w3c.dom.Text;

import java.util.Locale;

public class Phone extends AppCompatActivity implements GestureDetector.OnDoubleTapListener,GestureDetector.OnGestureListener {

    TextToSpeech phoneTTS;
    Intent call, message;
    GestureDetectorCompat gestureDetectorCompat;
    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        prefManager = new PrefManager(this);
        gestureDetectorCompat = new GestureDetectorCompat(this,this);
        gestureDetectorCompat.setOnDoubleTapListener(this);
        phoneTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    phoneTTS.setLanguage(Locale.ENGLISH);
                    phoneTTS.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                    phoneTTS.speak(" Welcome to the Phone Module ",TextToSpeech.QUEUE_FLUSH,null,"1");
                }
            }
        });
        phoneTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                switch (s){
                    case "1":
                        phoneTTS.speak(" Single Tap to Call ",TextToSpeech.QUEUE_FLUSH,null,"2");
                        break;
                    case "2":
                        phoneTTS.speak(" Double Tap to Send a Message and long press to repeat the instructions. ",TextToSpeech.QUEUE_FLUSH,null,"");
                        break;
                    case "100":
                        startActivity(call);
                        break;
                    case "200":
                        startActivity(message);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        call = new Intent(Phone.this, Call.class);
        phoneTTS.speak(" Taking you to the Call Module ", TextToSpeech.QUEUE_FLUSH, null, "100");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        message = new Intent(Phone.this, Message.class);
        phoneTTS.speak(" Taking you to the Message Module ", TextToSpeech.QUEUE_FLUSH, null, "200");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        phoneTTS.speak(" Single Tap to Call ",TextToSpeech.QUEUE_FLUSH,null,"2");


    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    protected void onStop() {
        phoneTTS.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        phoneTTS.shutdown();
        super.onDestroy();
    }

}
