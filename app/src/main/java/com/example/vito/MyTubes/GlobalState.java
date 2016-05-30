package com.example.vito.MyTubes;

import android.app.Application;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by melissabeuze on 11/05/16.
 */
public class GlobalState extends Application {

    public ArrayList<Song> songList;
    public static String CAT = "IME4";
    public MusicService musicSrv;
    public boolean musicBound=false;
    public MediaController controller;
    public Intent playIntent;
    public boolean playbackPaused=false;

    @Override
    public void onCreate() {
        super.onCreate();
        getSongList();
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
//        setController();
    }
//    private ServiceConnection musicConnection = new ServiceConnection(){
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
//            //get service
//            musicSrv = binder.getService();
//            //pass list
//            musicSrv.setList(songList);
//            musicBound = true;
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicBound = false;
//        }
//    };
//
//    public void onStartFromFragment() {
//        if(playIntent==null){
//            playIntent = new Intent(this, MusicService.class);
//            //erreur
//            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
//        }
//    }

    public void getSongList() {
        songList = new ArrayList<Song>();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = getContentResolver().query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            do { //add songs to list
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }

    public void songPickedFromFragment(int number){
        Log.i("log gs songpicked","num"+number);
        musicSrv.setSong(number);
        musicSrv.playSong();
        if(playbackPaused){
//            setController();
            playbackPaused=false;
        }
//        controller.show(0);
    }

    public void alert(String s){
        Toast.makeText(getApplicationContext(), s,Toast.LENGTH_SHORT).show();
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
            String urlData = qs;

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
