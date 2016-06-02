package com.example.vito.MyTubes;

import android.content.Context;
import android.util.Log;
import android.widget.MediaController;

/**
 * Created by Victor on 02/06/2016.
 */

public class MusicController extends MediaController {

    public MusicController(Context c){
        super(c);
    }

    public void hide(){
        Log.i("okokoko", "hide");
    }
}
