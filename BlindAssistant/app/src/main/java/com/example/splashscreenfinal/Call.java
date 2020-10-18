package com.example.splashscreenfinal;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Call extends AppCompatActivity implements TextToSpeech.OnInitListener, CharSequence, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final int CALL_CODE = 1;
    private static final int CONFIRM_PHONE_CALL_TO_PERSON = 2;
    private static final int CONFIRMATION = 100;
    private static final int TTS_INSTALL_CODE = 1000;
    private static final int USER = 10001;
    private static final int BOT = 10002;

    GestureDetector gestureDetector;

    private LinearLayout chatLayout;
    private EditText queryEditText;

    private TextToSpeech speaker;
    String contactNumber="", name="", contactName="", exactFinalContactName="", displayContactNameForBot="";
    HashMap<String, String> contactsWithTheName;
    Intent callIntent;
    ImageView sendBtn;
    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        prefManager = new PrefManager(this);
        this.gestureDetector = new GestureDetector(this,this);
        gestureDetector.setOnDoubleTapListener(this);

        chatLayout = findViewById(R.id.chatLayout);
        sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msg = queryEditText.getText().toString();
                if (msg.trim().isEmpty()) {
                    Toast.makeText(Call.this, "Please enter your query!", Toast.LENGTH_LONG).show();
                } else {
                    showTextView(msg, USER);
                    queryEditText.setText("");
                }
            }
        });


        queryEditText = findViewById(R.id.queryEditText);
        queryEditText.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        sendMessage(sendBtn);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });

        speaker = new TextToSpeech(this,this);
        speaker.setOnUtteranceProgressListener(new UtteranceProgressListener() {
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
                        displaySpeechRecognizer(1);
                        break;
                    case "402":
                        speakContacts(contactsWithTheName);
                        break;
                    case "403":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displaySpeechRecognizer(100);
                            }
                        });
                        break;
                    case "404":
                        displaySpeechRecognizer(2);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void sendMessage(View view) {
        String msg = queryEditText.getText().toString();
        if (msg.trim().isEmpty()) {
            Toast.makeText(Call.this, "Please enter your query!", Toast.LENGTH_LONG).show();
        } else {
            showTextView(msg, USER);
            queryEditText.setText("");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            speaker.setLanguage(Locale.UK);
            showTextView("Say call and a person name to connect to the person.",BOT);
            speaker.speak(" Say call and a person name to connect to the person. ",TextToSpeech.QUEUE_FLUSH,null,"401");
            contactsWithTheName = new HashMap<>();
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "Text To Speech Not Activated", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TTS_INSTALL_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                speaker = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }

        if (requestCode == CALL_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            spokenText = removeFullStop(spokenText);
            showTextView(spokenText, USER);
            String choiceOfContact[] = spokenText.split(" ");
            if (choiceOfContact[0].equalsIgnoreCase("call")) {
                for (int i = 1; i < choiceOfContact.length; i++) {
                    contactName += choiceOfContact[i] + " ";
                }
                contactName = toFirstLetterCapital(contactName);
                if (!contactName.equalsIgnoreCase("")) {
                    call();
                } else {
                    showTextView("Try calling a person from your Contacts.", BOT);
                    speaker.speak(" Try calling a person from your Contacts. ", TextToSpeech.QUEUE_ADD, null, "401");
                }
            } else {
                showTextView("Sorry, couldn't recognise your request. " + "\n" + " Try Again.", BOT);
                speaker.speak("Couldn't Recognise what you want!", TextToSpeech.QUEUE_FLUSH, null, "401");
            }
        }

        if (requestCode == CONFIRM_PHONE_CALL_TO_PERSON && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            showTextView(spokenText, USER);
            exactFinalContactName = toFirstLetterCapital(spokenText);
            if (contactsWithTheName.containsKey(exactFinalContactName)) {
                showTextView(" Are you sure, you want to call " + exactFinalContactName + " ? ", BOT);
                speaker.speak(" Are you sure, you want to call " + exactFinalContactName + " ? ", TextToSpeech.QUEUE_ADD, null, "403");
            } else {
                showTextView(" Sorry, but that wasn't " + contactName + " !" + "\n" + " Try Again!", BOT);
                speaker.speak(" Sorry, but that wasn't " + contactName + " !", TextToSpeech.QUEUE_ADD, null, "");
                speaker.speak(" Try again ! ", TextToSpeech.QUEUE_ADD, null, "402");
            }
        }

        if (requestCode == CONFIRMATION && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            showTextView(spokenText, USER);
            if (spokenText.equalsIgnoreCase("yes.") || spokenText.equalsIgnoreCase("yes") || spokenText.equalsIgnoreCase("okay") || spokenText.equalsIgnoreCase("Yeah")) {
                callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + contactsWithTheName.get(toFirstLetterCapital(exactFinalContactName))));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (contactsWithTheName.get(toFirstLetterCapital(exactFinalContactName)) != null) {
                    showTextView("Calling " + exactFinalContactName, BOT);
                    speaker.speak("Calling " + exactFinalContactName + " ...", TextToSpeech.QUEUE_ADD, null, "");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(callIntent);
                        }
                    }, 3000);
                } else {
                    showTextView(" Kindly tell me which " + exactFinalContactName + " ? ", BOT);
                    speaker.speak(" Kindly tell me which " + exactFinalContactName + " ? ", TextToSpeech.QUEUE_ADD, null, "403");
                }
            } else {
                showTextView("Ending your Call Request!", BOT);
                speaker.speak("Ending your Call Request!", TextToSpeech.QUEUE_ADD, null, "");
            }
        }

    }

    private void displaySpeechRecognizer(int code) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, code);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speakContacts(HashMap<String,String> h){
        int flag = 0;
        Iterator hmIterator = h.entrySet().iterator();
//        Toast.makeText(this,displayContactNameForBot,Toast.LENGTH_SHORT).show();
//        showTextView(" Do you want to call "+displayContactNameForBot+"?",BOT);
//        Log.d("TAG",displayContactNameForBot+" in Speak Contacts");
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            if (flag == 0) {
                speaker.speak(" Do you want to call "+mapElement.getKey()+" ? ",TextToSpeech.QUEUE_ADD,null, "");
                flag = 1;
            } else {
                speaker.speak(" Or "+mapElement.getKey()+" ?",TextToSpeech.QUEUE_ADD,null, "");
            }
        }
        speaker.speak("",TextToSpeech.QUEUE_ADD,null,"404");
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void call() {
        if(contactName.matches("[0-9]+")){
            if(isValidPhoneNumber(contactName)){
                if (contactName.startsWith("9")||contactName.startsWith("7")||contactName.startsWith("8")) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CALL_PHONE},   //request specific permission from user
                                10);
                        return;
                    }
                    showTextView("Calling " + contactName + "...",BOT);
                    callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + contactName));
                    startActivity(callIntent);
                }
            }else{
                contactName = "";
                showTextView("Not a valid phone number."+"\n"+"Try Again.",BOT);
                speaker.speak("Not a valid phone number. Try again",TextToSpeech.QUEUE_FLUSH,null,"401");
            }
        } else {
            Uri uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
//            contactName = toFirstLetterCapital(contactName);
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + contactName + "%'";
//            Toast.makeText(getApplicationContext()," Contact Name is "+contactName,Toast.LENGTH_LONG).show();
            Cursor people = getContentResolver().query(uri, projection, selection, null, ContactsContract.Contacts.SORT_KEY_PRIMARY);
            if(people.getCount()>0){
                people.moveToFirst();
                try {
                    Log.d("TAG","Total Rows = "+people.getCount());
                    for(int i=0;i<people.getCount();i++){
                        name = people.getString(0);
                        Log.d("TAG",(i+1)+" "+name+" "+contactName+" "+validName(name));
                        if(validName(name)){
                            contactNumber = people.getString(1);
                            if (contactNumber.contains("+91")) {
                                contactNumber = contactNumber.replace("+91", "");
                            }
                            if(contactNumber.contains(" ")){
                                String temp[] = contactNumber.split(" ");
                                contactNumber = "";
                                for(String elem: temp){
                                    contactNumber+=elem;
                                }
                            }
                            if(!contactsWithTheName.containsKey(name)) {
                                contactsWithTheName.put(name, contactNumber);
                            }
                            people.moveToNext();
                        }else{
                            people.moveToNext();
                            continue;
                        }
                    }
                    printHashMap(contactsWithTheName);
                }catch (Exception ex){
                    showTextView("Kindly try calling again",BOT);
                    speaker.speak("Kindly try again!",TextToSpeech.QUEUE_ADD,null,"401");
//                    Toast.makeText(this,"Sorry no such contact",Toast.LENGTH_SHORT);
                }
            }else{
                showTextView("Sorry, no such contact found.",BOT);
                speaker.speak(" Sorry, no such contact found ! ",TextToSpeech.QUEUE_ADD,null, "");
            }
        }
    }

    public boolean isValidPhoneNumber(String s){
        int flag = 1;
        for(int i=0;i<s.length();i++){
            if(!Character.isDigit(s.charAt(i))){
                return false;
            }
        }
        return (flag==1) && (s.length()==10);
    }

    public String removeFullStop(String s){
        if(s.contains(".")){
            s = s.substring(0,s.indexOf("."));
        }
        return s;
    }

    public void printHashMap(final HashMap<String, String> hm){
        Log.d("TAG","Total contacts with the name: "+hm.size());
        Iterator hmIterator = hm.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            Log.d("TAG","Details -> "+mapElement.getKey() + " : " + mapElement.getValue());
            displayContactNameForBot +=  mapElement.getKey() + ", ";
        }
        displayContactNameForBot = displayContactNameForBot.substring(0,displayContactNameForBot.length()-2);
        Log.d("TAG",displayContactNameForBot);
        if(hm.size()>1){
            showTextView(" Which " + contactName + " ? ",BOT);
            showTextView("Do you want to call " + displayContactNameForBot + " ?",BOT);
            speaker.speak(" Which " + contactName + " ? ",TextToSpeech.QUEUE_FLUSH,null,"402");
        }else if(hm.size()==1){
            hmIterator = hm.entrySet().iterator();
            exactFinalContactName = (String) ((Map.Entry)hmIterator.next()).getKey();
            showTextView(" Are you sure, you want to call " + exactFinalContactName + " ? ",BOT);
            speaker.speak(" Are you sure, you want to call " + exactFinalContactName + " ? ",TextToSpeech.QUEUE_FLUSH,null,"403");
        }else{
            showTextView("Kindly try again with a correct name from your contacts!",BOT);
            speaker.speak(" Kindly try again with a correct name from your contacts !",TextToSpeech.QUEUE_FLUSH,null,"401");
        }
    }

    public String toFirstLetterCapital(String s){
        String name = "";
        String a[] = s.split(" ");
        for(int i=0;i<a.length;i++){
            if(i==a.length-1){
                name += Character.toUpperCase(a[i].charAt(0))+a[i].substring(1);
            }else{
                name += Character.toUpperCase(a[i].charAt(0))+a[i].substring(1)+" ";
            }
        }
//        Toast.makeText(this," Contact name is: "+name,Toast.LENGTH_SHORT).show();
        return name;
    }

    public boolean validName(String n){
//        Log.d("TAG","Checking: "+n+" "+contactName+"\n");
        if(n.contains(" ")){
            String temp[] = n.split(" ");
            if(temp[0].equalsIgnoreCase(contactName)||n.equalsIgnoreCase(contactName)){
                return true;
            }
        }else if(n.length()==contactName.length()){
            return true;
        }
        return false;
    }

    private void showTextView(String message, int type) {
        FrameLayout layout;
        switch (type) {
            case USER:
                layout = getUserLayout();
                break;
            case BOT:
                layout = getBotLayout();
                break;
            default:
                layout = getBotLayout();
                break;
        }
        layout.setFocusableInTouchMode(true);
        chatLayout.addView(layout); // move focus to text view to automatically make it scroll up if softfocus
        TextView tv = layout.findViewById(R.id.chatMsg);
        tv.setText(message);
        layout.requestFocus();
        queryEditText.requestFocus(); // change focus back to edit text to continue typing
    }

    FrameLayout getUserLayout() {
        LayoutInflater inflater = LayoutInflater.from(Call.this);
        return (FrameLayout) inflater.inflate(R.layout.user_msg_layout, null);
    }

    FrameLayout getBotLayout() {
        LayoutInflater inflater = LayoutInflater.from(Call.this);
        return (FrameLayout) inflater.inflate(R.layout.bot_msg_layout, null);
    }

    @Override
    protected void onDestroy() {
        speaker.shutdown();
        super.onDestroy();
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt(int index) {
        return 0;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
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
    public void onLongPress(MotionEvent e) {
        Intent i = new Intent(this, Message.class);
        startActivity(i);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
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
    protected void onStop() {
        speaker.stop();
        super.onStop();
    }
}
