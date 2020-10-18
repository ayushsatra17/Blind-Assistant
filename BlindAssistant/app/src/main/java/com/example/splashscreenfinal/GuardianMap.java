package com.example.splashscreenfinal;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuardianMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseDatabase mdatabase = FirebaseDatabase.getInstance();
    DatabaseReference mref;
    LatLng lt;
    String blindContact;
    private ArrayList<LatLng> points;
    Polyline line;
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mref = mdatabase.getReference();
        points = new ArrayList<>();
        blindContact = getIntent().getStringExtra("Contact_Blind");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mref = mdatabase.getReference();
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
                    Log.d("Lat = ", "" + lat);
                    Log.d("Lng = ", "" + lng);
                    mMap.clear();
                    setmarkers(lat, lng);
                } else {
                    Intent i = new Intent(GuardianMap.this,GuardianSideLocation.class);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(),"Object - Null",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setmarkers(Double lat,Double lng){
        lt = new LatLng(lat,lng);
        PolylineOptions options = null;
        if(points.size()>1){
            options = new PolylineOptions().width(8).color(Color.BLUE).geodesic(true);
            for(int i=0;i<points.size();i++){
                LatLng point = points.get(i);
                options.add(point);
            }
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon)).position(points.get(0)).title(getLocalityName(points.get(0).latitude,points.get(0).longitude)));
            line = mMap.addPolyline(options);
        }
        if(flag==0){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lt,20));
            flag = 1;
        }

        mMap.addMarker(new MarkerOptions().position(lt).title("Blind Position"));
    }


    public String getLocalityName(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addresses!=null){
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            return city+", "+state+"-"+postalCode+", "+country;
        }
        return "Blind Position";
    }

}

