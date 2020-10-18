package com.example.splashscreenfinal;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Locale;
import androidx.core.view.GestureDetectorCompat;
import cdflynn.android.library.checkview.CheckView;

public class Dynamic extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener  {

    TextToSpeech t1;
    private GestureDetectorCompat gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    String modules[] = new String[6];
    int count;
    CheckView check;
    CheckView check1;
    CheckView check2;
    CheckView check3;
    CheckView check4;
    CheckView check5;
    CheckView[] tickAnimationArray;
    PrefManager prefManager;
    boolean callDestroygesture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);
        if(prefManager.getIsGestureSet()){
            launchSampleScreen();
            finish();
        }
        setContentView(R.layout.activity_dynamic);
        initialise();
        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    t1.setLanguage(Locale.ENGLISH);
                    t1.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                    if(prefManager.getIsGestureSet()==false){
                        startFuntionSetup();
                    }
                }
            }
        });
        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {}

            @Override
            public void onDone(String s) {
                switch (s){
                    case "1":
                        if(count<=5){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    disableGestureListener();
                                    speakwords(modules[count]);
                                }
                            });
                        }
                        break;
                    case "100":
                        if(count<=5){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    enableGestureListener();
                                }
                            });
                        }
                        break;
                    case "200":
                        count++;
                        if(count<=5){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    disableGestureListener();
                                    speakwords(modules[count]);
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    t1.speak(" You are all set to use all the Modules now. ", TextToSpeech.QUEUE_FLUSH, null, "1000");
                                }
                            });
                        }
                        break;
                    case "1000":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prefManager.setIsGestureSet(true);
                                callDestroygesture = false;
                                Intent i = new Intent(Dynamic.this, Sample.class);
                                startActivity(i);
                                finish();
                            }
                        });
                        break;
                    default:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {}
                        });
                        break;
                }
            }

            @Override
            public void onError(String s) {}
        });
    }

    public void initialise(){
        gestureDetector = new GestureDetectorCompat(this, this);
        count = 0;
        check=findViewById(R.id.check);
        check1 =findViewById(R.id.check1);
        check2 =findViewById(R.id.check2);
        check3 =findViewById(R.id.check3);
        check4 =findViewById(R.id.check4);
        check5 =findViewById(R.id.check5);
        tickAnimationArray = new CheckView[6];
        tickAnimationArray[0] = check;
        tickAnimationArray[1] = check1;
        tickAnimationArray[2] = check2;
        tickAnimationArray[3] = check3;
        tickAnimationArray[4] = check4;
        tickAnimationArray[5] = check5;
        modules[0]="Email";
        modules[1]="Music";
        modules[2]="Clock";
        modules[3]="Phone";
        modules[4]="NearbyPlaces";
        modules[5]="EmergencyService";
        callDestroygesture = true;
    }

    public void launchSampleScreen(){
        Intent i = new Intent(Dynamic.this, Sample.class);
        startActivity(i);
    }

    public void enableGestureListener(){
        if(gestureDetector==null){
            Toast.makeText(this,"Gesture Enabled",Toast.LENGTH_SHORT).show();
            gestureDetector = new GestureDetectorCompat(this, this);
            gestureDetector.setOnDoubleTapListener(this);
        }
    }

    public void disableGestureListener(){
        if(gestureDetector!=null){
            Toast.makeText(this,"Gesture Disabled",Toast.LENGTH_SHORT).show();
            gestureDetector = null;
        }
    }

    private void speakwords(String s) {
        t1.speak(" Which gesture do you want to set for the " + s + " module? ",TextToSpeech.QUEUE_FLUSH,null,"100");
    }

    public void startFuntionSetup(){
        t1.speak(" This is Gesture Setting module. ",TextToSpeech.QUEUE_FLUSH,null,"1");
        count = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        t1.stop();
    }

    @Override
    protected void onDestroy() {
        t1.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
//        disableGestureListener();
        if(!prefManager.getSingleTap().equals("")||prefManager.getSingleTap()==null){
            disableGestureListener();
            t1.speak(" Sorry, but you've already set single tap for " + prefManager.getSingleTap(),TextToSpeech.QUEUE_FLUSH,null,"1");
        } else {
//            h.put("Single Tap",modules[count]);
            prefManager.setSingleTap(modules[count]);
            tickAnimationArray[count].check();
            t1.speak(" Single Tap is now set for " + prefManager.getSingleTap(),TextToSpeech.QUEUE_FLUSH,null,"200");
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
//        disableGestureListener();
        if(!prefManager.getDoubleTap().equals("")){
            disableGestureListener();
            t1.speak(" Sorry, but you've already set double tap for " + prefManager.getDoubleTap(),TextToSpeech.QUEUE_FLUSH,null,"1");
        } else {
            prefManager.setDoubleTap(modules[count]);
            tickAnimationArray[count].check();
            t1.speak(" Double Tap is now set for " + prefManager.getDoubleTap(),TextToSpeech.QUEUE_FLUSH,null,"200");
        }
        return true;
    }


    @Override
    public void onLongPress(MotionEvent e) {
//        disableGestureListener();
        if(!prefManager.getLongPress().equals("")){
            disableGestureListener();
            t1.speak(" Sorry, but you've already set long press for " + prefManager.getLongPress(),TextToSpeech.QUEUE_FLUSH,null,"1");
        } else {
            prefManager.setLongPress(modules[count]);
            t1.speak(" Long Press is now set for " + prefManager.getLongPress(),TextToSpeech.QUEUE_FLUSH,null,"200");
            tickAnimationArray[count].check();
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
//        disableGestureListener();
        if(!prefManager.getSwipeRight().equals("")){
            disableGestureListener();
            t1.speak(" Sorry, but you've already set right swipe for " + prefManager.getSwipeRight(),TextToSpeech.QUEUE_FLUSH,null,"1");
        } else {
            prefManager.setSwipeRight(modules[count]);
            tickAnimationArray[count].check();
            t1.speak(" Right Swipe is now set for " + prefManager.getSwipeRight(),TextToSpeech.QUEUE_FLUSH,null,"200");
        }
    }

    private void onSwipeLeft() {
//        disableGestureListener();
        if(!prefManager.getSwipeLeft().equals("")){
            disableGestureListener();
            t1.speak(" Sorry, but you've already set left swipe for " + prefManager.getSwipeLeft(),TextToSpeech.QUEUE_FLUSH,null,"1");
        } else {
            prefManager.setSwipeLeft(modules[count]);
            tickAnimationArray[count].check();
            t1.speak(" Left Swipe is now set for " + prefManager.getSwipeLeft(),TextToSpeech.QUEUE_FLUSH,null,"200");
        }
    }

    private void onSwipeTop() {
//        disableGestureListener();
        if(!prefManager.getSwipeUp().equals("")){
            disableGestureListener();
            t1.speak(" Sorry, but you've already set swipe up for " + prefManager.getSwipeUp(),TextToSpeech.QUEUE_FLUSH,null,"1");
        } else {
            prefManager.setSwipeUp(modules[count]);
            tickAnimationArray[count].check();
            t1.speak(" Swipe Up is now set for " + prefManager.getSwipeUp(),TextToSpeech.QUEUE_FLUSH,null,"200");
        }
    }

    private void onSwipeBottom() {
//        disableGestureListener();
        if(!prefManager.getSwipeDown().equals("")){
            disableGestureListener();
            t1.speak(" Sorry, but you've already set swipe down for " + prefManager.getSwipeDown(),TextToSpeech.QUEUE_FLUSH,null,"1");
        } else {
            prefManager.setSwipeDown(modules[count]);
            tickAnimationArray[count].check();
            t1.speak(" Swipe Down is now set for " + prefManager.getSwipeDown(),TextToSpeech.QUEUE_FLUSH,null,"200");
        }
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

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
        if(callDestroygesture) {
            destroyPrefGestures();
        }
        super.onStop();
    }

    public void destroyPrefGestures() {
        prefManager.setSingleTap("");
        prefManager.setDoubleTap("");
        prefManager.setLongPress("");
        prefManager.setSwipeUp("");
        prefManager.setSwipeDown("");
        prefManager.setSwipeLeft("");
        prefManager.setSwipeRight("");
    }

}
