package com.example.splashscreenfinal;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.core.content.ContextCompat;


public class CustomBroadcastReceiver extends BroadcastReceiver {

    PrefManager prefManager;
    SmsManager smsManager = SmsManager.getDefault();


    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
            prefManager = new PrefManager(context);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if(level<=15){
                if(smsManager!=null && ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED){
                    String message = " Low Battery Notification, " +
                                        prefManager.getBlindName() + " - " + prefManager.getBlindContact() + ", " +
                                        "\n" +
                                        "- Blind Assistant";
                    smsManager.sendTextMessage(prefManager.getGuardianContact(), null, message, null, null);
                } else {
                    Toast.makeText(context, "SMS Cannot be sent as SEND SMS Permission Not Granted", Toast.LENGTH_LONG).show();
                }
            }
            smsManager = null;
        }
    }


}
