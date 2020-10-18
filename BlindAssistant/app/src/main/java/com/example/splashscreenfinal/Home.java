package com.example.splashscreenfinal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity {

    LinearLayout root;
    FirebaseDatabase mdatabase = FirebaseDatabase.getInstance();
    DatabaseReference mref;
    double latitude=0.0, longitude=0.0;
    EditText editText1,editText2,editText3,editText4;
    private AwesomeValidation awesomeValidation;
    PrefManager prefManager;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    String permissions[] = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
    };
    boolean permissionAsked;
    boolean selectedCancelForGooglePermission;
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    GoogleAccountCredential mCredential;
    String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM
    };
    List<String> listPermissionsNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);
        if(prefManager.isFormFilled()){
            launchSlider();
        }
        setContentView(R.layout.activity_home);
        root = (LinearLayout) findViewById(R.id.root);
        if(checkAndRequestPermissions()){
            init();
        }
    }

    public void init(){
        selectedCancelForGooglePermission = false;
        permissionAsked = false;
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        mref = mdatabase.getReference();
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.contactNumber);
        editText3 = findViewById(R.id.editText3);
        editText4 = findViewById(R.id.editText4);
        awesomeValidation.addValidation(this, R.id.editText1, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.editText3, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.contactNumber, "^[2-9]{2}[0-9]{8}$", R.string.mobileerror);
        awesomeValidation.addValidation(this, R.id.editText4, "^[2-9]{2}[0-9]{8}$", R.string.mobileerror);
    }

    private void launchSlider() {
        Intent i = new Intent(Home.this, Slider.class);
        startActivity(i);
        finish();
    }

    private  boolean checkAndRequestPermissions() {
        listPermissionsNeeded = new ArrayList<String>();
        for(int i=0;i<permissions.length;i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permissions[i]);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        showMessage(root, "All Permissions Granted");
        return true;
    }

    public boolean checkInternetStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo()!= null && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        }
        return false;
    }

    public void signIn(View view){
        permissionAsked=false;
        if(!prefManager.getEMAIL_ID().equals("")||!(prefManager.getEMAIL_ID()==null)){
            mCredential.setSelectedAccountName(prefManager.getEMAIL_ID());
        }
        checkStatus(root);
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        Toast.makeText(this,"GMAIL DIALOG BOX",Toast.LENGTH_SHORT).show();
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(Home.this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void checkStatus(View view) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if(!checkInternetStatus()) {
            showMessageOk("Kindly turn on the Modile Data/Wifi.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }
            );
        } else if (mCredential.getSelectedAccountName() == null || mCredential.getSelectedAccountName().equals("")) {
            chooseAccount(view);
        } else {
            showMessage(root, "Requesting to access GMail.");
            new MakeRequestTask(this, mCredential).execute();
        }
    }

    private void chooseAccount(View view) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS)==PackageManager.PERMISSION_GRANTED) {
            String accountName = mCredential.getSelectedAccountName();
            if (accountName!=null) {
                showMessage(root, mCredential.getSelectedAccountName());
                checkStatus(view);
            } else {
                showMessage(root, "Kindly select an email account to proceed");
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            showMessage(root, "Kindly provide the required permission");
            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION_GET_ACCOUNTS);
        }
    }

    private void showMessage(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    public AlertDialog showDialog(String title, String msg, String positiveLabel,
                                  DialogInterface.OnClickListener positiveOnClick,
                                  String negativeLabel, DialogInterface.OnClickListener negativeOnClick,
                                  boolean isCancellable){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(isCancellable);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        builder.setNegativeButton(negativeLabel, negativeOnClick);
        AlertDialog alert = builder.create();
        alert.show();
        return  alert;
    }

    private void showMessageOk(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
//                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showMessageOkCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCredential.setSelectedAccountName(null);
                        prefManager.setEMAIL_ID(null);
                        Toast.makeText(getApplicationContext(), "Called Delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data!=null && data.getExtras()!=null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        prefManager.setEMAIL_ID(mCredential.getSelectedAccountName());
                        checkStatus(root);
                    }
                } else {
                    showMessageOk("You will have to select an Account to proceed.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                                }
                            }
                    );
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    prefManager.setEMAIL_ID(mCredential.getSelectedAccountName());
                    showMessage(root, "Account Confirmed");
//                    startActivity(new Intent(getApplicationContext(), Email.class));
                } else {
                    showMessageOkCancel("Kindly select ALLOW, which will permit the application to access your GMAIL Account.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkStatus(root);
                                }
                            }
                    );
                }
                break;

            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    showMessage(root, "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.");
                } else {
                    checkStatus(root);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                Map<String, Integer> perms = new HashMap<>();
                for(int i=0;i<grantResults.length;i++) {
                    if(grantResults[i]==PackageManager.PERMISSION_DENIED) {
                        perms.put(permissions[i], grantResults[i]);
                    }
                }
                if(perms.size()==0) {
                    showMessage(root,"All permissions granted");
                    init();
                } else {
                    for(Map.Entry<String, Integer> entry: perms.entrySet()) {
                        String permName = entry.getKey();
                        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permName)){
                            showDialog("Grant Permission",
                                    "This Application requires all the permission at once",
                                    "Yes, Grant Permission", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
                                            dialog.cancel();
                                            checkAndRequestPermissions();
                                        }
                                    }
                                    , "No, Exit App", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    }, true);
                        } else {
                            showDialog("Settings",
                                    "You have denied permissions. Allow all permissions at [Setting] > [Permissions]",
                                    "Go to Settings",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent gotoSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package",getPackageName(),null));
                                            gotoSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(gotoSettings);
                                            dialog.cancel();
                                            finish();
                                        }
                                    }
                                    , "No, Exit App",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    }, true);
                            break;
                        }
                    }
                }
            break;
            case REQUEST_PERMISSION_GET_ACCOUNTS:
                if (grantResults.length > 0) {
                    boolean getAccountsAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    if (getAccountsAccepted) {
                        Snackbar.make(root, "Permission To Choose Account Granted.", Snackbar.LENGTH_LONG).show();
                        chooseAccount(root);
                    } else {
                        Snackbar.make(root, "Permission Denied, You cannot access the Accounts.", Snackbar.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
                                showMessageOk("You need to grant access to this permissions because it helps in choosing an account for Reading/Sending Emails.",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION_GET_ACCOUNTS);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public void submit(View v){
        submitForm();
    }

    private void submitForm() {
        if (awesomeValidation.validate() && !prefManager.getEMAIL_ID().equals("")) {
            String bName = editText1.getText().toString();
            prefManager.setBlindName(bName);
            String cBlind = editText2.getText().toString();
            prefManager.setBlindContact(cBlind);
            String gName = editText3.getText().toString();
            String gContact = editText4.getText().toString();
            prefManager.setGuardianContact(gContact);
            Log.d("TAG",bName+" "+gName+" "+gContact);
            mref.child("Blind").child(cBlind).child("BlindName").setValue(bName);
            mref.child("Blind").child(cBlind).child("Location").child("Latitude").setValue(latitude);
            mref.child("Blind").child(cBlind).child("Location").child("Longitude").setValue(longitude);
            mref.child("Blind").child(cBlind).child("Location").child("isLocation").setValue("false");
            mref.child("Blind").child(cBlind).child("Guardian").child("Name").setValue(gName);
            mref.child("Blind").child(cBlind).child("Guardian").child("Contact").setValue(gContact);
            Intent intent = new Intent(this,Slider.class);
            prefManager.setFormFilled(true);
            startActivity(intent);
            finish();
        } else {
            showMessage(root, "Gmail Account Not Selected");
        }
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, Integer> {

        private static final long MAX_RESULTS_PER_REQUEST = 1;
        private Gmail mService = null;
        private Exception mLastError = null;
        private View view = root;
        private Home activity;

        MakeRequestTask(Home activity, GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(transport, jsonFactory, credential).setApplicationName(getResources().getString(R.string.app_name)).build();
            this.activity = activity;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return readEmail();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return 0;
            }
        }

        private int readEmail() throws IOException {
            List<String> labelsId = new ArrayList<>();
            labelsId.add("INBOX");
            ListMessagesResponse response=null;
            response = mService.users().messages().list("me").setMaxResults(MAX_RESULTS_PER_REQUEST).setLabelIds(labelsId).execute();
            List<Message> messages = response.getMessages();
            int result = (messages.size()>0)?1:0;
            return result;
        }


        @Override
        protected void onPreExecute() {}

        @Override
        protected void onPostExecute(Integer integer) {
            if(integer==1 && !permissionAsked){
                showMessage(root, "Account Confirmed");
            }
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    permissionAsked=true;
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), REQUEST_AUTHORIZATION);
                } else {
                    showMessage(view, "The following error occurred:\n" + mLastError);
                }
            } else {
                showMessage(view, "Accessing Request Cancelled.");
            }
        }

    }

}