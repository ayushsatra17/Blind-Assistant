package com.example.splashscreenfinal;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;


import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AlarmRIng extends AppCompatActivity implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
    //    TimePicker alarmTimePicker;
//    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    TextToSpeech t1;
    TextView text;
    int counter=-1;

    int hours;
    int minutes;
    private static final int SPEECH_REQUEST_CODE = 0;
    GestureDetectorCompat gestureDetectorCompat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_r_ing);
        //   text=findViewById(R.id.hour);
        //tp=findViewById(R.id.tp);

        gestureDetectorCompat = new GestureDetectorCompat(this,this);
        gestureDetectorCompat.setOnDoubleTapListener(this);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    //displaySpeechRecognizer();
                    speakwords();
                }
            }
        });

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onError(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                switch (utteranceId){
                    case "401":
                        // displaySpeechRecognizer();
                        break;
                    case "409":
                        displaySpeechRecognizer();
                        break;
                    case "410":
                        displaySpeechRecognizer();
                        break;
                    case "411":
                        displaySpeechRecognizer();
                        break;
                    case "412":
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakwords() {
        //t1.speak("Double tap for instructions.",TextToSpeech.QUEUE_FLUSH,null,"401");
        instructions();
    }





    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            String timer[]=spokenText.split(" ");
            String time=timer[0];
            if(time.contains(":")){
                int colonIndex = time.indexOf(":");
                hours = Integer.parseInt(time.substring(0,colonIndex));
                minutes = Integer.parseInt(time.substring(colonIndex+1));
                Log.d("TAG","minutes:"+minutes);
                Log.d("TAG",timer[1]);
            }
            if(timer[1].contains("p.m.")||timer[1].contains("pm")||timer[1].contains("P.M.")||timer[1].contains("PM") || timer[1].contains("p.m")) {
                if(hours>=1 && hours<=11){
                    hours+=12;
                    Log.d("TAG","Hours:"+hours);


                }
            }
            else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    t1.speak("Invalid input, please provide the input again!",TextToSpeech.QUEUE_FLUSH,null,"411");
                }

            }
            counter++;

            setAlarm(counter);
//            if(.contains(":")){
//                int colonIndex = item.indexOf(":");
//                hours = Integer.parseInt(item.substring(0,colonIndex));
//                minutes = Integer.parseInt(item.substring(colonIndex+1));
//                requestedHour = hours;
//                requestedMin = minutes;
//                break;
//            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setAlarm(int counter){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND,0);
        if(System.currentTimeMillis()<calendar.getTimeInMillis()){
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //creating a new intent specifying the broadcast receiver
            Intent i = new Intent(this, AlarmReceiver.class);
            //creating a pending intent using the intent
            PendingIntent pi = PendingIntent.getBroadcast(this, counter, i, 0);
            //setting the repeating alarm that will be fired every day
            am.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pi);
            //am.setInexact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pi);
            Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t1.speak("Alarm set for"+hours+"hours and"+minutes+"minutes",TextToSpeech.QUEUE_FLUSH,null,"401");
            }

        }else{
            Toast.makeText(this, "Time specified is incorrect", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t1.speak("Please give correct time input",TextToSpeech.QUEUE_FLUSH,null,"410");
            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cancelAlarm(){
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, AlarmReceiver.class);
        //creating a pending intent using the intent
        PendingIntent pi = PendingIntent.getBroadcast(this, counter, i, 0);

        am.cancel(pi);
        Toast.makeText(this,"Alarm Cancelled",Toast.LENGTH_SHORT).show();
        t1.speak("Alarm cancelled!", TextToSpeech.QUEUE_FLUSH, null, "412");


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        t1.speak("Time to set an alarm?",TextToSpeech.QUEUE_FLUSH,null,"409");

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // cancelAlarm();
        instructions();
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void instructions() {
        t1.speak("Single tap to set or stop an alarm, fling to cancel alarm and long press to repeat the instructions.",TextToSpeech.QUEUE_FLUSH,null,"402");

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
        instructions();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        cancelAlarm();
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


