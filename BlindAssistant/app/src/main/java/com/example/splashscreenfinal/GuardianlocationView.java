package com.example.splashscreenfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GuardianlocationView extends AppCompatActivity {
String blindContact;
    FirebaseDatabase mdatabase = FirebaseDatabase.getInstance();
    DatabaseReference mref;
    TextView txt;
    TextView subl,city,pc,division,country,latitude,longitude;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardianlocation_view);
        blindContact = getIntent().getStringExtra("Contact_Blind");
        mref = mdatabase.getReference();
        subl=findViewById(R.id.subl);
        city=findViewById(R.id.city);
        pc=findViewById(R.id.pc);
        division=findViewById(R.id.division);
        country=findViewById(R.id.country);
        latitude=findViewById(R.id.latitude);
        longitude=findViewById(R.id.longitude);
        btn=findViewById(R.id.btn);
        txt=findViewById(R.id.txt);

        getLocation();
    }

    private void getLocation() {
        // Fetches value from firebase and shows location
        mref.child("Blind").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.child(blindContact).child("Location").child("isLocation").getValue(String.class);
                Toast.makeText(getApplicationContext(),"Maps: " + value,Toast.LENGTH_SHORT).show();
//                && ((value.equals("True")) || (value.equals("true")))
                if ((dataSnapshot != null)) {
                    Double lat = dataSnapshot.child(blindContact).child("Location").child("Latitude").getValue(Double.class);
                    Double lng = dataSnapshot.child(blindContact).child("Location").child("Longitude").getValue(Double.class);
                        latitude.setText(lat.toString());
                        longitude.setText(lng.toString());
                       getLocalityName(lat,lng);

                } else {

                    Toast.makeText(getApplicationContext(),"Object - Null",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLocalityName(Double lat, Double lng) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String cityx = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String countryx = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        String gh=addresses.get(0).getPremises();
        String xyz=addresses.get(0).getSubAdminArea();
        String idk=addresses.get(0).getSubLocality();
        subl.setText(idk);
        city.setText(cityx);
        pc.setText(postalCode);
        division.setText(state);
        country.setText(countryx);
        txt.setText(address);


        // Only if available else return NULL
    }
    public void maplocation(View v){
        Intent i=new Intent(this,GuardianMap.class);
        i.putExtra("Contact_Blind", blindContact);
        startActivity(i);
    }


}

