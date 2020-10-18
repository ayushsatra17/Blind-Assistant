package com.example.splashscreenfinal;



import android.annotation.TargetApi;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class Timer extends AppCompatActivity implements TextToSpeech.OnInitListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
    public int counter = 0;
    private static final int SPEECH_REQUEST_CODE = 0;
    Ringtone ringtone;
    private GestureDetector gestureDetector;


    TextView counttime;
    long timeSeconds;
    TextToSpeech t1;

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        this.gestureDetector = new GestureDetector(this,this);
        gestureDetector.setOnDoubleTapListener(this);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    //displaySpeechRecognizer();
                    instructions();
                }
            }
        });
        counttime = findViewById(R.id.count);
        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onError(String utteranceId) {
                Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDone(String utteranceId) {
                switch (utteranceId){
                    case "401":
                        displaySpeechRecognizer();
                        break;
                    case "409":
                        break;
                    case "404":
                        displaySpeechRecognizer();
                        break;
                    default:
                        break;
                }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void instructions() {
        t1.speak("Single tap to set or stop timer, long press to repeat the instructions.",TextToSpeech.QUEUE_ADD,null,"101");
    }

    public void startTimer(long timeSeconds) {
        new CountDownTimer(timeSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                counter++;
                counttime.setText(String.valueOf(counter));
            }

            @Override
            public void onFinish() {
                counttime.setText("Finished");
                Intent intent = new Intent(Timer.this, Show_timer.class);
                startActivity(intent);
            }
        }.start();
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent,SPEECH_REQUEST_CODE);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            if(spokenText.contains("seconds") || spokenText.contains("minutes")) {
                String timer[] = spokenText.split(" ");
                long digit = Long.parseLong(timer[0]);
                String parameter = timer[1];
                Toast.makeText(this, "Value of digit:" + digit + parameter, Toast.LENGTH_SHORT).show();
                if (parameter.equals("seconds") || parameter.equals("second")) {
                    timeSeconds = digit * 1000;
                    Log.d("TAG", "Seconds" + timeSeconds);
                    startTimer(timeSeconds);
                }
                else if (parameter.equals("minutes") || parameter.equals("minute")) {
                    timeSeconds = digit * 60 * 1000;
                    startTimer(timeSeconds);
                }
            }
            else{
                t1.speak("Invalid input! Please give the input again.",TextToSpeech.QUEUE_ADD,null,"404");
                Toast.makeText(getApplicationContext(), "Invalid input!", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int status) {

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        t1.speak("Give the timer input.",TextToSpeech.QUEUE_ADD,null,"401");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onLongPress(MotionEvent e) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            instructions();
        }

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        t1.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        t1.stop();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}