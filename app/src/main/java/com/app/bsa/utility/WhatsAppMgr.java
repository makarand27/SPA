package com.app.bsa.utility;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class WhatsAppMgr {

    public static boolean sendWhatAppMessage(String message, int phone, PackageManager pm){

        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");

        // Give your message here
        intent.putExtra(Intent.EXTRA_TEXT,"hello");

        // Checking whether Whatsapp
        // is installed or not
//        if (intent.resolveActivity(pm)== null) {
//            Toast.makeText(this,"Please install whatsapp first.",Toast.LENGTH_SHORT).show();
//            return true;
//        }
//        // Starting Whatsapp
//        startActivity(intent);
        return true;
    }
}
