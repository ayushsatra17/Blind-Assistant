package com.example.splashscreenfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class BlindsideLocation extends AppCompatActivity implements TextToSpeech.OnInitListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final boolean TODO =false ;
    int count = 0;
    LocationManager locationManager;
    LocationListener locationListener;
    int changeInLocationCounter = 0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    Location mCurrentLocation;
    private LocationCallback locationCallback;
    LocationRequest locationRequest;
    FirebaseDatabase mdatabase;
    DatabaseReference mref;
    TextView latitudeText, longitudeText;
    SmsManager smsManager;
    private GestureDetector gestureDetector;
    TextToSpeech t1;
    String emergencyMessage="";
    String userContact="", guardianContact="", guardianNumber="";
    int flag = 0;
    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blindside_location);
        prefManager = new PrefManager(this);
        latitudeText = findViewById(R.id.latitude);
        longitudeText = findViewById(R.id.longitude);
        smsManager = SmsManager.getDefault();
        mdatabase = FirebaseDatabase.getInstance();
        mref = mdatabase.getReference();
        this.gestureDetector = new GestureDetector(this, this);
        gestureDetector.setOnDoubleTapListener(this);
        userContact = prefManager.getBlindContact();
        guardianContact = prefManager.getGuardianContact();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mCurrentLocation = location;
                    changeInLocationCounter++;
                    double latitude = mCurrentLocation.getLatitude();
                    latitudeText.setText(latitude + "");
                    double longitude = mCurrentLocation.getLongitude();
                    longitudeText.setText(longitude + "");
                    mref.child("Blind").child(userContact).child("Location").child("Latitude").setValue(latitude);
                    mref.child("Blind").child(userContact).child("Location").child("Longitude").setValue(longitude);
                    if(flag==0){
                        mref.child("Blind").child(userContact).child("Location").child("isLocation").setValue("true");
                        flag=1;
                    }
                }
            }
        };
        startLocationUpdates();
        mref.child("Blind").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                guardianNumber = dataSnapshot.child(userContact).child("Guardian").child("Contact").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}

        });

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

    }

    private void speakwords() {
        t1.speak("Welcome to the emergency module. Double tap to send emergency message for alerting your Guardian. ", TextToSpeech.QUEUE_ADD, null,"");
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onInit(int status) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        emergencyMessage = "I am in trouble. Please open the app to see my live location! ";
        smsManager.sendTextMessage(guardianContact,null,emergencyMessage,null,null);
        Toast.makeText(getApplicationContext(),"Last Known Location Sent To Guardian",Toast.LENGTH_LONG).show();
        mref.child("Blind").child(userContact).child("Location").child("isLocation").setValue("true");
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

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public BlindsideLocation() {
        super();
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
        t1.shutdown();
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

