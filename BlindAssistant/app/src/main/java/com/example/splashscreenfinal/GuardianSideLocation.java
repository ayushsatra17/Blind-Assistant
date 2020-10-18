package com.example.splashscreenfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GuardianSideLocation extends AppCompatActivity {

    TextView text;
    FirebaseDatabase mdatabase;
    DatabaseReference mref;
    String blindContact = "";
    ImageView image;
    Button btn;
    TextView body;
    Button location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_side_location);
        mdatabase = FirebaseDatabase.getInstance();
        mref = mdatabase.getReference();
        text=findViewById(R.id.text);
        image=findViewById(R.id.image);
        btn=findViewById(R.id.btn);
        body=findViewById(R.id.body);
        location=findViewById(R.id.location);
        blindContact = getIntent().getStringExtra("Contact_Blind");

        mref.child("Blind").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.child(blindContact).child("Location").child("isLocation").getValue(String.class);
                Toast.makeText(getApplicationContext(),"Allow Offline/Online: " + value,Toast.LENGTH_SHORT).show();
                if (value.equals("false")) {
                    image.setImageResource(R.drawable.error);
                    text.setText("Oops, the blind is offline...");
                    body.setText("Blind has not sent an emergency message yet!");
                    btn.setVisibility(View.INVISIBLE);
                } else {
                    location.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.VISIBLE);
                    text.setText("Blind is online!");
                    image.setImageResource(R.drawable.finallocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }

        });

    }

    public void clicked(View v) {
        Intent i = new Intent(GuardianSideLocation.this, GuardianMap.class);
        i.putExtra("Contact_Blind", blindContact);
        startActivity(i);
    }
    public void viewLocation(View v) {
        Intent i = new Intent(GuardianSideLocation.this, GuardianlocationView.class);
        i.putExtra("Contact_Blind", blindContact);
        startActivity(i);
    }

}