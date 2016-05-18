package com.example.vito.MyTubes;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by melissabeuze on 11/05/16.
 */
public class GlobalState extends Application {


    public static String CAT = "IME4";
    SharedPreferences prefs;

    //fonction qui cree des toast
    public void alerter(String s){
        Toast t = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
        t.show();
        Log.i(CAT, s);
    }

    private String convertStreamToString(InputStream in) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String requete(String qs) {
        Log.i(CAT,"requete1");
        if (qs != null)
        {
            Log.i(CAT,"requete2");
            String API_URL = "http://www.youtubeinmp3.com/fetch/?";
            String API_FORMAT ="format=JSON";
            String API_VIDEO = "video=";

            String urlData = API_URL + API_FORMAT + "&" + API_VIDEO + qs;
            Log.i(CAT, "urlData: "+ urlData);
            try {
                URL url = new URL(urlData);
                Log.i(CAT, "url utilisée : " + url.toString());
                HttpURLConnection urlConnection = null;
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = null;
                in = new BufferedInputStream(urlConnection.getInputStream());
                String txtReponse = convertStreamToString(in);
                urlConnection.disconnect();
                return txtReponse;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }
}
