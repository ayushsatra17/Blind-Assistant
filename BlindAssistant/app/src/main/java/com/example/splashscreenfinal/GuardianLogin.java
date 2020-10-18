package com.example.splashscreenfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

public class GuardianLogin extends AppCompatActivity {
    Button button;
    EditText guardianContact;
    EditText blindContact;
    FirebaseDatabase mdatabase;
    DatabaseReference mref;
    private AwesomeValidation awesomeValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guardian_login);
        mdatabase = FirebaseDatabase.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        mref = mdatabase.getReference();
        button=findViewById(R.id.button);
        guardianContact=findViewById(R.id.Gcontact);
        blindContact=findViewById(R.id.Bcontact);
        awesomeValidation.addValidation(this, R.id.Gcontact, "^[2-9]{2}[0-9]{8}$", R.string.mobileerror);
        awesomeValidation.addValidation(this, R.id.Bcontact, "^[2-9]{2}[0-9]{8}$", R.string.mobileerror);
    }

    public void submit(View v) {
        final String gCon = guardianContact.getText().toString();
        final String bCon = blindContact.getText().toString();
        if (awesomeValidation.validate()) {
            mref.child("Blind").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Toast.makeText(getApplicationContext(), "Entered Change", Toast.LENGTH_SHORT).show();
                    String number = dataSnapshot.child(bCon).child("Guardian").child("Contact").getValue(String.class);
//                    Toast.makeText(getApplicationContext(), "Number" + " " + number, Toast.LENGTH_SHORT).show();
                    if (number == null) {
                        Toast.makeText(getApplicationContext(), "Match Not Found", Toast.LENGTH_SHORT).show();
                    } else {
                        if (number.equals(gCon)) {
                            Toast.makeText(getApplicationContext(), "Match Found!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), GuardianSideLocation.class);
                            intent.putExtra("Contact_Blind", bCon);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "You're not the Registered Guardian for this Blind!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }

            });


        }

    }
}
