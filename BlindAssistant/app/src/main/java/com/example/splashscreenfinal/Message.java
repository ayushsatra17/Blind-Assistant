package com.example.splashscreenfinal;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class Message extends Activity implements TextToSpeech.OnInitListener{

    String finalMessage = "", contact = "";
    String contactName="", contactNumber="", name="", finalContactNumber ="", finalContactName ="";
    private static final int PERSON_NAME = 1;
    private static final int MESSAGE_CONTENT = 2;
    private static final int CONFIRM_PERSON_OPTIONS = 10;
    private static final int FINAL_CONFIRMATION_OF_CONTACT_NAME = 100;
    private static final int CONFIRMATION_FINAL_MESSAGE_TO_SEND = 200;
    private static final int USER = 10001;
    private static final int BOT = 10002;
    SmsManager smsManager;
    TextToSpeech speaker;
    HashMap<String,String> contactsWithTheName;
    ImageView sendBtn;
    PrefManager prefManager;
    private LinearLayout chatLayout;
    private EditText queryEditText;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        prefManager = new PrefManager(this);
        smsManager = smsManager.getDefault();

        contactsWithTheName = new HashMap<String,String>();

        speaker = new TextToSpeech(this,this);

        chatLayout = findViewById(R.id.chatLayout);
        sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msg = queryEditText.getText().toString();
                if (msg.trim().isEmpty()) {
                    Toast.makeText(Message.this, "Please enter your query!", Toast.LENGTH_LONG).show();
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
                        displaySpeechRecognizer(2);
                        break;
                    case "403":
                        displaySpeechRecognizer(100);
                        break;
                    case "404":
                        speakContacts(contactsWithTheName);
                        break;
                    case "405":
                        displaySpeechRecognizer(10);
                        break;
                    case "406":
                        displaySpeechRecognizer(200);
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
            Toast.makeText(Message.this, "Please enter your query!", Toast.LENGTH_LONG).show();
        } else {
            showTextView(msg, USER);
            queryEditText.setText("");
        }
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            speaker.setLanguage(Locale.ENGLISH);
            speaker.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
            askForContactName();
        }
    }

    private void displaySpeechRecognizer(int code) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, code);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PERSON_NAME:
                try{
                    if (resultCode == RESULT_OK) {
                        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        contact = results.get(0);
                        contact = toFirstLetterCapital(contact);
                        showTextView(contact,USER);
                        call(contact);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;

            case CONFIRM_PERSON_OPTIONS:
                try{
                    if(resultCode == RESULT_OK){
                        List<String> results = data != null ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : null;
                        finalContactName = toFirstLetterCapital(results.get(0));
                        showTextView(finalContactName,USER);
                        if(contactsWithTheName.containsKey(finalContactName)){
                            showTextView(" Are you sure you want to send the message to " + finalContactName+"?",BOT);
                            speaker.speak(" Are you sure you want to send the message to " + finalContactName+"?", TextToSpeech.QUEUE_ADD, null,"403");
                        }else{
                            showTextView(" Sorry, but that was not " + contact +"\n"+"Try Again.",BOT);
                            speaker.speak(" Sorry, but that was not  " + contact, TextToSpeech.QUEUE_ADD, null);
                            speaker.speak(" Try again ", TextToSpeech.QUEUE_ADD, null);
                            speaker.speak("", TextToSpeech.QUEUE_ADD, null,"404");
//                            speakContacts(contactsWithTheName);
                        }
                    }
                }catch (Exception e){

                }
                break;

            case FINAL_CONFIRMATION_OF_CONTACT_NAME:
                try {
                    if (resultCode == RESULT_OK) {
                        assert data != null;
                        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String resultMessage = null;
                        if (results != null) {
                            resultMessage = results.get(0);
                            showTextView(resultMessage,USER);
                        }
                        if(resultMessage.equalsIgnoreCase("yes.")||resultMessage.equalsIgnoreCase("yes")||resultMessage.equalsIgnoreCase("yeah")|resultMessage.equalsIgnoreCase("okay")){
                            finalContactNumber = contactsWithTheName.get(finalContactName);
                            askForTheMessage();
                        }else{
                            showTextView(" Ending the message thread ",BOT);
                            speaker.speak(" Ending the message thread ", TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                }catch (Exception e){

                }
                break;

            case MESSAGE_CONTENT:
                try{
                    if (resultCode == RESULT_OK) {
                        assert data != null;
                        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (results != null) {
                            finalMessage = results.get(0);
                            showTextView(finalMessage,USER);
                        }
                        showTextView(" Are you sure you want to send the message "+finalMessage+" to "+finalContactName+"?",BOT);
                        speaker.speak(" Are you sure you want to send the message ", TextToSpeech.QUEUE_FLUSH, null);
//                        pause(50);
                        speaker.speak(finalMessage, TextToSpeech.QUEUE_ADD, null);
//                        pause(50);
                        speaker.speak(" to " + finalContactName + "?", TextToSpeech.QUEUE_ADD, null);
                        speaker.speak("", TextToSpeech.QUEUE_ADD, null,"406");
//                        pauseAndCallSTT(CONFIRMATION_FINAL_MESSAGE_TO_SEND,((finalMessage.split(" ")).length*1000)+5000);
                    }
                }catch (Exception e){

                }
                break;

            case CONFIRMATION_FINAL_MESSAGE_TO_SEND:
                try{
                    if (resultCode == RESULT_OK) {
                        List<String> results = data != null ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : null;
                        String resultContact = null;
                        if (results != null) {
                            resultContact = results.get(0);
                            showTextView(resultContact,USER);
                        }
                        if(resultContact.equalsIgnoreCase("yes.")||resultContact.equalsIgnoreCase("yes")||resultContact.equalsIgnoreCase("yeah")|resultContact.equalsIgnoreCase("okay")){
                            showTextView("Sending your Message ...",BOT);
                            speaker.speak("Sending your message",TextToSpeech.QUEUE_FLUSH,null,"");
                            sendSMS(finalContactNumber,finalMessage);
                        }else{
                            askForTheMessage();
                        }
                    }
                }catch (Exception e){

                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void call(String personData) {
        if (personData.startsWith("9")||personData.startsWith("7")||personData.startsWith("8")) {
            if(personData.length()==10){
                finalContactNumber = personData;
                askForTheMessage();
            }else{
                showTextView("Sorry, couldn't understand you! Try again.",BOT);
                speaker.speak( " Sorry, couldn't understand you! Try again. ",TextToSpeech.QUEUE_ADD,null,"401");
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},   //request specific permission from user
                        10);
                return;
            }
        } else {
            Uri uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + personData + "%'";
            Cursor people = getContentResolver().query(uri, projection, selection, null, ContactsContract.Contacts.SORT_KEY_PRIMARY);
            if(people.getCount()>0){
                people.moveToFirst();
                try {
                    Log.d("TAG","Total Rows = "+people.getCount());
                    for(int i=0;i<people.getCount();i++){
                        name = people.getString(0);
                        Log.d("TAG",(i+1)+" "+name+" "+personData+" "+validName(name,personData));
                        if(validName(name,personData)){
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
                    speaker.speak(" Kindly try again ! ",TextToSpeech.QUEUE_ADD,null, "");
                }
            }else{
                speaker.speak(" Sorry, no such contact found ! ",TextToSpeech.QUEUE_ADD,null);
            }
        }
    }

    public void printHashMap(final HashMap<String, String> hm){
        String mTo = "";
        Log.d("TAG","Total contacts with the name: "+hm.size());
        Iterator hmIterator = hm.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            Log.d("TAG","Details -> "+ mapElement.getKey() + " : " + mapElement.getValue());
            mTo += mapElement.getKey() + ", ";
        }
        mTo = mTo.substring(0,mTo.length()-2);
        if(hm.size()>1){
            showTextView(" Which " + contact + " ?",BOT);
            showTextView("Do you want to message "+mTo+"?",BOT);
            speaker.speak(" Which " + contact + " ?",TextToSpeech.QUEUE_ADD,null,"404");
        }else if(hm.size()==1){
            Log.d("TAG"," 1 contact name only ");
            hmIterator = hm.entrySet().iterator();
            finalContactName = (String) ((Map.Entry)hmIterator.next()).getKey();
            showTextView(" Are you sure, you want to message " + finalContactName + " ?",BOT);
            speaker.speak(" Are you sure, you want to message " + finalContactName + " ?",TextToSpeech.QUEUE_ADD,null,"403");
        }else{
            showTextView(" Kindly try again with correct first name ! ",BOT);
            speaker.speak(" Kindly try again with correct first name ! ",TextToSpeech.QUEUE_ADD,null);
            askForContactName();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speakContacts(HashMap<String,String> h){
        int flag = 0;
        Iterator hmIterator = h.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            if (flag == 0) {
                speaker.speak("Do you want to message "+mapElement.getKey()+" ?",TextToSpeech.QUEUE_ADD,null);
                flag = 1;
            } else {
                speaker.speak("Or "+mapElement.getKey()+" ?",TextToSpeech.QUEUE_ADD,null);
            }
        }
        speaker.speak("",TextToSpeech.QUEUE_ADD,null,"405");
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
        return name;
    }

    public boolean validName(String n, String mName){
        Log.d("TAG","Checking: "+n+" "+mName+"\n");
        if(n.contains(" ")){
            String temp[] = n.split(" ");
            if(temp[0].equalsIgnoreCase(mName)||n.equalsIgnoreCase(mName)){
                return true;
            }
        }else if(n.length()==mName.length()||n.equalsIgnoreCase(mName)){
            return true;
        }
        return false;
    }


    public void sendSMS(String cont, String mess){
        speaker.speak(" Sending your message to " + finalContactName ,TextToSpeech.QUEUE_FLUSH,null);
        smsManager.sendTextMessage(cont,null, mess, null, null);
        showTextView("Message Sent.",BOT);
        Toast.makeText(this,"Message sent succFessfully!",Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void askForContactName(){
        showTextView("Whom do you want to send the Message?",BOT);
        speaker.speak(" Whom do you want to send the Message? ",TextToSpeech.QUEUE_ADD,null,"401");
//        pauseAndCallSTT(1,4000);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void askForTheMessage(){
        showTextView("What is the message ?",BOT);
        speaker.speak(" What is the message ? ",TextToSpeech.QUEUE_FLUSH,null,"402");
//        pauseAndCallSTT(MESSAGE_CONTENT,3000);
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
        LayoutInflater inflater = LayoutInflater.from(Message.this);
        return (FrameLayout) inflater.inflate(R.layout.user_msg_layout, null);
    }

    FrameLayout getBotLayout() {
        LayoutInflater inflater = LayoutInflater.from(Message.this);
        return (FrameLayout) inflater.inflate(R.layout.bot_msg_layout, null);
    }

    @Override
    protected void onStop() {
        speaker.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        speaker.shutdown();
        super.onDestroy();
    }
}
