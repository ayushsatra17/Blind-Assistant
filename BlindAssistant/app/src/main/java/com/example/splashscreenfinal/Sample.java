package com.example.splashscreenfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Locale;

public class Sample extends AppCompatActivity implements TextToSpeech.OnInitListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    CustomBroadcastReceiver customBroadcastReceiver;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    TextToSpeech t1;
    GestureDetector gestureDetector;
    Intent i, toDynamic;
    PrefManager prefManager;
    String gestureData[];
    int unusedGestureIndex = Integer.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        customBroadcastReceiver = new CustomBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(this.customBroadcastReceiver, intentFilter);

        prefManager = new PrefManager(this);

        this.gestureDetector = new GestureDetector(this,this);
        gestureDetector.setOnDoubleTapListener(this);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                    speakwords();
                }
            }
        });

        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {}

            @Override
            public void onDone(String s) {
                switch (s){
                    case "0":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(unusedGestureIndex==0) {
                                    t1.speak("", TextToSpeech.QUEUE_FLUSH, null, "1");
                                } else {
                                    t1.speak(" Single Tap is set for " + prefManager.getSingleTap(), TextToSpeech.QUEUE_FLUSH, null, "1");
                                }
                            }
                        });
                        break;
                    case "1":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(unusedGestureIndex==1)
                                    t1.speak("", TextToSpeech.QUEUE_FLUSH, null, "2");
                                else
                                    t1.speak(" Double Tap is set for " + prefManager.getDoubleTap(),TextToSpeech.QUEUE_FLUSH,null,"2");
                            }
                        });
                        break;
                    case "2":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(unusedGestureIndex==2)
                                    t1.speak("",TextToSpeech.QUEUE_FLUSH,null,"3");
                                else
                                    t1.speak(" Long Press is set for " + prefManager.getLongPress(),TextToSpeech.QUEUE_FLUSH,null,"3");
                            }
                        });
                        break;
                    case "3":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(unusedGestureIndex==3)
                                    t1.speak("",TextToSpeech.QUEUE_FLUSH,null,"4");
                                else
                                    t1.speak(" Swipe Up is set for " + prefManager.getSwipeUp(),TextToSpeech.QUEUE_FLUSH,null,"4");
                            }
                        });
                        break;
                    case "4":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(unusedGestureIndex==4)
                                    t1.speak("",TextToSpeech.QUEUE_FLUSH,null,"5");
                                else
                                    t1.speak(" Swipe Right is set for " + prefManager.getSwipeRight(),TextToSpeech.QUEUE_FLUSH,null,"5");
                            }
                        });
                        break;
                    case "5":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(unusedGestureIndex==5)
                                    t1.speak("",TextToSpeech.QUEUE_FLUSH,null,"6");
                                else
                                    t1.speak(" Swipe Down is set for " + prefManager.getSwipeDown(),TextToSpeech.QUEUE_FLUSH,null,"6");
                            }
                        });
                        break;
                    case "6":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(unusedGestureIndex==6)
                                    t1.speak("",TextToSpeech.QUEUE_FLUSH,null,"");
                                else
                                    t1.speak(" Swipe Left is set for " + prefManager.getSwipeLeft(),TextToSpeech.QUEUE_FLUSH,null,"");
                            }
                        });
                        break;
                    case "101":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                                startActivity(toDynamic);
                            }
                        });
                        break;
                    case "404":
                        t1.speak("Use" + getUnusedGesture() + " for repeating Instructions ", TextToSpeech.QUEUE_FLUSH, null, "");
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

    private void speakwords() {
        Toast.makeText(getApplicationContext(), "Name: " + prefManager.getBlindName(), Toast.LENGTH_SHORT);
        t1.speak(" Hello " + prefManager.getBlindName() + " , you're at the Navigation Screen. ", TextToSpeech.QUEUE_FLUSH, null,"404");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.item1:
                voiceSettings();
                return  true;
            case R.id.item2:
                changeGestureSetting();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void voiceSettings(){
        t1.stop();
        final String[] speakerSettings = {"Very slow", "Slow", "Moderate", "Fast","Very fast"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Speaker settings");
        builder.setItems(speakerSettings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0:
                        prefManager.setSPEECH_RATE("0.2f");
                        t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                        t1.speak("Speech Rate changed to Very Slow mode", TextToSpeech.QUEUE_FLUSH, null, "");
                        break;
                    case 1:
                        prefManager.setSPEECH_RATE("0.4f");
                        t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                        t1.speak("Speech Rate changed to Slow mode", TextToSpeech.QUEUE_FLUSH, null, "");
                        break;
                    case 2:
                        prefManager.setSPEECH_RATE("0.6f");
                        t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                        t1.speak("Speech Rate changed to Moderate mode", TextToSpeech.QUEUE_FLUSH, null, "");
                        break;
                    case 3:
                        prefManager.setSPEECH_RATE("0.8f");
                        t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                        t1.speak("Speech Rate changed to Fast mode", TextToSpeech.QUEUE_FLUSH, null, "");
                        break;
                    case 4:
                        prefManager.setSPEECH_RATE("1f");
                        t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                        t1.speak("Speech Rate changed to Very Fast mode", TextToSpeech.QUEUE_FLUSH, null, "");
                        break;
                    default:
                        t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                        break;
                }
            }
        });
        builder.show();
    }

    public void changeGestureSetting() {
        nullifyGestureSettings();
        prefManager.setIsGestureSet(false);
        t1.speak(" Taking you to the Gesture Setting Module. ", TextToSpeech.QUEUE_FLUSH, null, "101");
        toDynamic = new Intent(Sample.this, Dynamic.class);
    }

    public void nullifyGestureSettings() {
        prefManager.setSingleTap("");
        prefManager.setDoubleTap("");
        prefManager.setLongPress("");
        prefManager.setSwipeRight("");
        prefManager.setSwipeLeft("");
        prefManager.setSwipeDown("");
        prefManager.setSwipeUp("");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Toast.makeText(getApplicationContext(), "Single Tapped", Toast.LENGTH_SHORT);
        if(prefManager.getSingleTap().equals("")){
            unusedGestureIndex = 0;
            t1.speak("", TextToSpeech.QUEUE_FLUSH,null,"0");
        } else if (getGestureActivity(prefManager.getSingleTap())!=null){
            i = new Intent(Sample.this, getGestureActivity(prefManager.getSingleTap()));
            startActivity(i);
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Toast.makeText(getApplicationContext(), "Double Tapped", Toast.LENGTH_SHORT);
        if(prefManager.getDoubleTap().equals("")){
            unusedGestureIndex = 1;
            t1.speak("", TextToSpeech.QUEUE_FLUSH,null,"0");
        }else if(getGestureActivity(prefManager.getLongPress())!=null){
            i = new Intent(Sample.this, getGestureActivity(prefManager.getDoubleTap()));
            startActivity(i);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Toast.makeText(getApplicationContext(), "Long Pressed", Toast.LENGTH_SHORT);
        if (prefManager.getLongPress().equals("")) {
            unusedGestureIndex = 2;
            t1.speak("", TextToSpeech.QUEUE_FLUSH,null,"0");
        } else {
            if (getGestureActivity(prefManager.getLongPress()) != null) {
                i = new Intent(Sample.this, getGestureActivity(prefManager.getLongPress()));
                startActivity(i);
            }
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    result = true;
                }
            }
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    onSwipeBottom();
                } else {
                    onSwipeTop();
                }
                result = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    private void onSwipeRight() {
        Toast.makeText(getApplicationContext(), "Swiped Right", Toast.LENGTH_SHORT);
        if(prefManager.getSwipeRight().equals("")){
            unusedGestureIndex = 4;
            t1.speak("", TextToSpeech.QUEUE_FLUSH,null,"0");
        }else if(getGestureActivity(prefManager.getSwipeRight())!=null){
            i = new Intent(Sample.this, getGestureActivity(prefManager.getSwipeRight()));
            startActivity(i);
        }
    }

    private void onSwipeLeft() {
        Toast.makeText(getApplicationContext(), "Swiped Left", Toast.LENGTH_SHORT);
        if(prefManager.getSwipeLeft().equals("")){
            unusedGestureIndex = 6;
            t1.speak("", TextToSpeech.QUEUE_FLUSH,null,"0");
        } else if(getGestureActivity(prefManager.getSwipeLeft())!=null){
            i = new Intent(Sample.this, getGestureActivity(prefManager.getSwipeLeft()));
            startActivity(i);
        }
    }

    private void onSwipeTop() {
        Toast.makeText(getApplicationContext(), "Swiped Up", Toast.LENGTH_SHORT);
        if(getGestureActivity(prefManager.getSwipeUp()).equals("")){
            unusedGestureIndex = 3;
            t1.speak(" Swipe Up is the unused Gesture which can be used to know the gestures you have set. ",TextToSpeech.QUEUE_FLUSH,null,"0");
        } else if(getGestureActivity(prefManager.getSwipeUp())!=null){
            i = new Intent(Sample.this, getGestureActivity(prefManager.getSwipeUp()));
            startActivity(i);
        }
    }

    private void onSwipeBottom() {
        Toast.makeText(getApplicationContext(), "Swiped Down", Toast.LENGTH_SHORT);
        if(prefManager.getSwipeDown().equals("")){
            unusedGestureIndex = 5;
            t1.speak("", TextToSpeech.QUEUE_FLUSH,null,"0");
        }else if(getGestureActivity(prefManager.getSwipeDown())!=null){
            i = new Intent(Sample.this, getGestureActivity(prefManager.getSwipeDown()));
            startActivity(i);
        }
    }

    public Class getGestureActivity(String module){
        if(module.equals("EmergencyService")){
            return BlindsideLocation.class;
        }else if(module.equals("Phone")){
            return Phone.class;
        } else if(module.equals("Clock")) {
            return Clock.class;
        } else if(module.equals("Music")){
            return Music.class;
        } else if(module.equals("Email")){
            return Email.class;
        } else {
            return null;
        }
    }

    public String getUnusedGesture() {
        if(prefManager.getSingleTap().equals("")) {
            return "Single Tap";
        } else if(prefManager.getDoubleTap().equals("")) {
            return "Double Tap";
        } else if(prefManager.getLongPress().equals("")) {
            return "Long Press";
        } else if(prefManager.getSwipeUp().equals("")) {
            return "Swipe Up";
        } else if(prefManager.getSwipeDown().equals("")) {
            return "Swipe Down";
        } else if(prefManager.getSwipeRight().equals("")) {
            return "Swipe Right";
        } else {
            return "Swipe Left";
        }
    }

    public void instructSetGestures(){}

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onInit(int status) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    protected void onStop() {
        t1.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        t1.shutdown();
        unregisterReceiver(this.customBroadcastReceiver);
        super.onDestroy();
    }
}