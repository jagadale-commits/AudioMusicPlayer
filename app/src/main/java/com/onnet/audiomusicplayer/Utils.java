package com.onnet.audiomusicplayer;

import android.content.Context;
import android.widget.Toast;

public class Utils {

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, 60000).show();
    }
}
