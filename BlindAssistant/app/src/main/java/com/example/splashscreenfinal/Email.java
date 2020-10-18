package com.example.splashscreenfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import cdflynn.android.library.checkview.CheckView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.os.Bundle;

public class Email extends AppCompatActivity {

    private static final String FINAL_NAME = "FinalName";
    HashMap<String, String> contactAndEmail;
    CheckView toCheckView;
    CheckView subjectCheckView;
    ScrollView root;
    CheckView messageCheckView;
    TextToSpeech speaker;
    PrefManager pref;
    GoogleAccountCredential mCredential;
    String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM
    };
    EditText emailTo, emailSubject, emailBody;
    boolean nameExists;
    public static String toAddress = "", toAddressName="", toSubject = "", toBody = "";
    public static final String TO_EMAIL = "ToEmail";
    public static final String SUBJECT = "Subject";
    public static final String BODY = "Body";
    public static final String TO_EMAIL_NAME_REP = "NameRep";
    public static final String FINAL_SEND_CONFIRMATION = "SendConfirmation";
    public static final int TO_EMAIL_CODE = 100;
    public static final int SUBJECT_CODE = 200;
    public static final int BODY_CODE = 300;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        init();
    }

    public void init(){
//        toCheckView = findViewById(R.id.toCheckView);
//        toCheckView.check();
//        subjectCheckView = findViewById(R.id.subjectCheckView);
//        subjectCheckView.check();
//        messageCheckView = findViewById(R.id.bodyCheckView);
//        messageCheckView.check();
        nameExists = false;
        root = (ScrollView) findViewById(R.id.root);
        emailTo = (EditText) findViewById(R.id.emailTo);
        emailSubject = (EditText) findViewById(R.id.emailSubject);
        emailBody = (EditText) findViewById(R.id.body);
        pref = new PrefManager(getApplicationContext());
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(pref.getEMAIL_ID());
        speaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    speaker.setLanguage(Locale.ENGLISH);
                    speaker.setSpeechRate(1);
                    speaker.speak(" Welcome to the Email Module",TextToSpeech.QUEUE_FLUSH,null,TO_EMAIL);
                }
            }
        });
        speaker.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDone(String s) {
                switch(s){
                    case TO_EMAIL:
                        speaker.speak("Whom do you want to send the Email?", TextToSpeech.QUEUE_FLUSH, null, "START");
                        break;
                    case "START":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displaySpeechRecognizer(TO_EMAIL_CODE);
                            }
                        });
                        break;
                    case TO_EMAIL_NAME_REP:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                speakContacts(contactAndEmail);
                            }
                        });
                        break;
                    case FINAL_NAME:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displaySpeechRecognizer(101);
                            }
                        });
                        break;
                    case FINAL_SEND_CONFIRMATION:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displaySpeechRecognizer(999);
                            }
                        });
                        break;
                    case SUBJECT:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                emailTo.setText(toAddress);
//                                Toast.makeText(getApplicationContext(), "Email ID Part - Done "+toAddress, Toast.LENGTH_LONG).show();
                                displaySpeechRecognizer(SUBJECT_CODE);
                            }
                        });
                        break;
                    case BODY:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displaySpeechRecognizer(BODY_CODE);
                            }
                        });
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(String s) {}
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    showMessage(root, "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.");
                } else {
//                    Toast.makeText(this,"API BOX",Toast.LENGTH_SHORT).show();
                    new MakeRequestTask(Email.this, mCredential).execute();
                }
                break;
            case TO_EMAIL_CODE:
                if(resultCode==RESULT_OK){
                    List<String> results1 = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String toPersonName = results1.get(0);
//                    Toast.makeText(getApplicationContext(), toPersonName, Toast.LENGTH_SHORT).show();
                    if(!toPersonName.equals("")){
                        getFinalName(toPersonName);
                    } else {
                        speaker.speak("Couldn't Recognise You!", TextToSpeech.QUEUE_FLUSH, null, TO_EMAIL);
//                        speaker.speak("", TextToSpeech.QUEUE_FLUSH, null, TO_EMAIL);
                    }
                }
                break;
            case 999:
                if(resultCode==RESULT_OK){
                    List<String> results2 = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String yesNo = results2.get(0);
                    if(yesNo.equalsIgnoreCase("yes.")||yesNo.equalsIgnoreCase("yes")||yesNo.equalsIgnoreCase("okay")||yesNo.equalsIgnoreCase("Yeah")){
                        new MakeRequestTask(this, mCredential).execute();
                    } else {
                        speaker.speak("Email Thread Ended", TextToSpeech.QUEUE_FLUSH, null, "");
                        emailBody.setText("");
                        emailSubject.setText("");
                        emailTo.setText("");
                        toAddressName="";
                        toSubject="";
                        toBody="";
//                        toCheckView.uncheck();
//                        subjectCheckView.uncheck();
//                        messageCheckView.uncheck();
                    }
                }
                break;
            case 101:
                if(resultCode==RESULT_OK){
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String tempname = capitalizeFirstLetterOfEveryWord(results.get(0));
                    if(contactAndEmail.containsKey(tempname)){
                        toAddress = contactAndEmail.get(tempname);
                        emailTo.setText(toAddress);
//                        toCheckView.check();
//                        Toast.makeText(getApplicationContext(), "Email>1 : Done",Toast.LENGTH_SHORT).show();
                        speaker.speak("What is the Subject ?",TextToSpeech.QUEUE_FLUSH,null,SUBJECT);
                    } else {
                        speaker.speak("That wasn't "+ toAddressName +" Try again.", TextToSpeech.QUEUE_FLUSH, null, TO_EMAIL_NAME_REP);
                    }
                }
                break;
            case SUBJECT_CODE:
                if(resultCode==RESULT_OK){
                    List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String subJ = capitalizeFirstLetterOfEveryWord(result.get(0));
                    if(!subJ.equals("")){
                        toSubject=subJ;
                        emailSubject.setText(capitalizeFirstLetterOfEveryWord(subJ));
//                        subjectCheckView.check();
                        speaker.speak(" What is the message? ", TextToSpeech.QUEUE_FLUSH, null, BODY);
                    } else {
                        speaker.speak(" Couldn't Recognize you. What is the subject? ", TextToSpeech.QUEUE_FLUSH, null, SUBJECT);
                    }
                }
                break;
            case BODY_CODE:
                if(resultCode==RESULT_OK){
                    List<String> resultB = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String bodyJ = resultB.get(0);
                    if(!bodyJ.equals("")){
                        toBody=bodyJ;
//                        Toast.makeText(getApplicationContext(), toBody, Toast.LENGTH_SHORT).show();
                        emailBody.setText(getBodyText(toBody));
//                        messageCheckView.check();
                        speaker.speak(" Email prepared for " + toAddressName + "." + " Should i send it? ", TextToSpeech.QUEUE_FLUSH, null, FINAL_SEND_CONFIRMATION);
                    } else {
                        speaker.speak(" Couldn't Recognize you. What is the subject? ", TextToSpeech.QUEUE_FLUSH, null, BODY);
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displaySpeechRecognizer(int code) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, code);
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getFinalName(String name){
        nameExists = false;
        name = capitalizeFirstLetterOfEveryWord(name);
        toAddressName = name;
//        Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
        try{
            HashSet<String> contactNameHashSet = new HashSet<String>();
            Uri contactURI = ContactsContract.Contacts.CONTENT_URI;
            String mColumnProjection[] = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
            String mSelectionClause = ContactsContract.Contacts.DISPLAY_NAME + " like '%"+name+"%'";
            Cursor contactCursor = getContentResolver().query(contactURI, mColumnProjection, mSelectionClause, null, null);

            Uri emailURI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
            String mEmailSelectionClause = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
            Cursor emailCursor=null;

            String id="", tempName="", emailID="", mEmailSelectionArguments[]=null;
            contactAndEmail = new HashMap<String, String>();

            if(contactCursor!=null && contactCursor.getCount()>0){
                nameExists = true;
                while(contactCursor.moveToNext()){
//                    if(!contactNameHashSet.contains(contactCursor.getString(1))){
                    id = contactCursor.getString(0);
                    tempName = contactCursor.getString(1);
                    mEmailSelectionArguments = new String[] {id};
                    Log.d("TAG", "Name:"+tempName+" ID:"+id);
                    emailCursor = getContentResolver().query(emailURI, null,  mEmailSelectionClause, mEmailSelectionArguments, null);
                    if(emailCursor!=null && emailCursor.getCount()>=1){
                        if(!contactAndEmail.containsKey(tempName)) {
                            emailCursor.moveToFirst();
                            emailID = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            Log.d("TAG", tempName + " " + emailID + " " + id);
                            contactAndEmail.put(tempName, emailID + "");
                        }
                    }
//                        contactNameHashSet.add(tempName);
//                    }
                }
            } else {
                speaker.speak("Try again with a name from your contacts!",TextToSpeech.QUEUE_FLUSH,null,TO_EMAIL);
                Log.d("TAG", "No Contact");
            }
            Log.d("TAG", "Size and Boolean: "+contactAndEmail.size()+" "+nameExists);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                printHashMap(contactAndEmail);
            }
        } catch (Exception e) {
//            Log.d("TAG", e.printStackTrace());
            Thread.dumpStack();
            e.printStackTrace();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    speaker.speak("Some error occurred. Kindly try again.",TextToSpeech.QUEUE_ADD,null,TO_EMAIL);
                speaker.speak("Some error occurred. Kindly try again.",TextToSpeech.QUEUE_ADD,null,"");
            }
        }
    }

    public String getBodyText(String s){
        String x[] = s.split(" "), result="";
        x[0] = Character.toUpperCase(x[0].charAt(0))+x[0].substring(1);
        for(String z: x){
            result+=z+" ";
        }
        return result;
    }

    public String capitalizeFirstLetterOfEveryWord(String s){
        char[] ch = s.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            int k = i;
            while (i < ch.length && ch[i] != ' ')
                i++;
            ch[k] = (char) (ch[k]>='a' && ch[k]<='z' ? ((int) ch[k] - 32) : ((int) ch[k]));
            for(int j=k+1;j<i;j++){
                ch[j] = (char) (ch[j]>='a' && ch[j]<='z' ? ((int) ch[j]) : ((int) ch[j])+32);
            }
        }
//        Toast.makeText(getApplicationContext(), "Caps Name: "+new String(ch), Toast.LENGTH_SHORT).show();
        return new String(ch);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void printHashMap(final HashMap<String, String> hm){
        if(nameExists && contactAndEmail.size()==0){
            speaker.speak(" No Email ID for any " + toAddressName + " found. Try Again",TextToSpeech.QUEUE_FLUSH,null, TO_EMAIL);
        } else if(hm.size()>1) {
            speaker.speak(" Which " + toAddressName + " ? ",TextToSpeech.QUEUE_FLUSH,null,TO_EMAIL_NAME_REP);
        } else if(hm.size()==1) {
            String onlyValue="";
            for(String key: hm.keySet()){
                onlyValue = key;
            }
            toAddress = hm.get(onlyValue);
//            Toast.makeText(getApplicationContext(), "Email ID: "+toAddress, Toast.LENGTH_SHORT).show();
            speaker.speak("Email Confirmed for" + toAddressName,TextToSpeech.QUEUE_FLUSH,null,"");
            emailTo.setText(toAddress);
//            toCheckView.check();
            speaker.speak(" What is the subject for your email to " + onlyValue,TextToSpeech.QUEUE_FLUSH,null,SUBJECT);
        } else {
            speaker.speak("Kindly try again with a correct name from your contacts!",TextToSpeech.QUEUE_FLUSH,null,TO_EMAIL);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speakContacts(HashMap<String,String> h){
        int flag = 0;
        Iterator hmIterator = h.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            if (flag == 0) {
                speaker.speak(" Do you want to email "+mapElement.getKey()+" ? ",TextToSpeech.QUEUE_ADD,null,"");
                flag = 1;
            } else {
                speaker.speak(" Or "+mapElement.getKey()+" ?",TextToSpeech.QUEUE_ADD,null,"");
            }
        }
        speaker.speak("",TextToSpeech.QUEUE_ADD,null,FINAL_NAME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.stop();
        speaker.shutdown();
    }

    @Override
    protected void onStop() {
        super.onStop();
        speaker.stop();
        speaker.shutdown();
    }

    private void showMessage(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    public void sendEmail(View view){
        if(!toAddress.equals("")&&!toBody.equals("")&&!toSubject.equals("")){
            new MakeRequestTask(Email.this, mCredential);
        }
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, String> {

        private Gmail mService = null;
        private Exception mLastError = null;
        private View view = root;
        private Email activity;

        MakeRequestTask(Email activity, GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(transport, jsonFactory, credential).setApplicationName(getResources().getString(R.string.app_name)).build();
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return sendEmail();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String sendEmail() throws IOException {
            // getting Values for to Address, from Address, Subject and Body
            String user = "me";
            String to = toAddress;
            String from = mCredential.getSelectedAccountName();
            String subject = toSubject;
            String body = toBody;
            MimeMessage mimeMessage;
            String response = "";
            try {
                mimeMessage = createEmail(to, from, subject, body);
                response = sendMessage(mService, user, mimeMessage);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return response;
        }

        // Method to create email Params
        private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage email = new MimeMessage(session);
            InternetAddress tAddress = new InternetAddress(to);
            InternetAddress fAddress = new InternetAddress(from);
            email.setFrom(fAddress);
            email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
            email.setSubject(subject);
            // Create Multipart object and add MimeBodyPart objects to this object
            Multipart multipart = new MimeMultipart();
            // Changed for adding attachment and text
            // email.setText(bodyText);
            BodyPart textBody = new MimeBodyPart();
            textBody.setText(bodyText);
            multipart.addBodyPart(textBody);
            //Set the multipart object to the message object
            email.setContent(multipart);
            return email;
        }

        // Method to send email
        private String sendMessage(Gmail service, String userId, MimeMessage email) throws MessagingException, IOException {
            Message message = createMessageWithEmail(email);
            // GMail's official method to send email with oauth2.0
            message = service.users().messages().send(userId, message).execute();
            //            Log.d("TAG","Message id: " + message.getId());
//            Log.d("TAG",message.toPrettyString());
            return message.getId();
        }

        private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            email.writeTo(bytes);
            String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
            Message message = new Message();
            message.setRaw(encodedEmail);
            return message;
        }

        @Override
        protected void onPreExecute() {
//            progressDialog.show();
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String output) {
//            progressDialog.hide();
            if (output == null || output.length() == 0) {
                showMessage(root, "Try Again!");
            } else {
                showMessage(root, "Mail sent Successfully");
                speaker.speak("Email to "+toAddressName+" has been sent", TextToSpeech.QUEUE_FLUSH, null, "");
                emailBody.setText("");
                emailSubject.setText("");
                emailTo.setText("");
                toAddressName="";
                toSubject="";
                toBody="";
//                toCheckView.uncheck();
//                subjectCheckView.uncheck();
//                messageCheckView.uncheck();
            }
        }

        @Override
        protected void onCancelled() {
            showMessage(view, "Request Cancelled.");
        }

    }

}