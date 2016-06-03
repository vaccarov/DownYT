package com.example.vito.MyTubes.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vito.MyTubes.MainActivity;
import com.example.vito.MyTubes.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by melissabeuze on 22/05/16.
 */
public class LyricsFragment extends Fragment{

    MainActivity ma;
    View view;
    String musixmatch_apikey = "5bfe574b1f36bebba4a51420fa3833dc";
    EditText formSong_artist;
    EditText formSong_title;
    TextView lyricsTitle;
    TextView lyricsText;
    String currentSongTitle ="";
    Button lyricsFormBtn;
    RelativeLayout formLyrics;
    TextView currentSongTitleTxtView;
    Button loadLyricsCurrentSongBtn;

    public LyricsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ma = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_lyrics, container, false);

        lyricsTitle = (TextView)  view.findViewById(R.id.lyricsTitle);
        lyricsText = (TextView)  view.findViewById(R.id.lyricsText);
        lyricsText.setMovementMethod(new ScrollingMovementMethod());
        lyricsFormBtn = (Button) view.findViewById(R.id.lyricsFormBtn);
        formSong_artist = (TextInputEditText) view.findViewById(R.id.form_song_artist);
        formSong_title = (TextInputEditText) view.findViewById(R.id.form_song_title);
        formLyrics = (RelativeLayout) view.findViewById(R.id.formLyrics);
        currentSongTitleTxtView = (TextView) view.findViewById(R.id.currentSongTitleTxtView);
        loadLyricsCurrentSongBtn = (Button) view.findViewById(R.id.loadLyricsCurrentSongBtn);

        if(ma.musicSrv.songTitle.length() > 0){
            currentSongTitleTxtView.setText("Load lyrics for the current song playing \""+ma.musicSrv.songTitle+"\"");
            currentSongTitle = ma.musicSrv.songTitle;
            Log.i(ma.CAT,currentSongTitle);
            loadLyricsCurrentSongBtn.setAlpha(1);
            loadLyricsCurrentSongBtn.setEnabled(true);
        }
        else{
            currentSongTitleTxtView.setText("No song playing");
            loadLyricsCurrentSongBtn.setEnabled(false);
            loadLyricsCurrentSongBtn.setAlpha(.5f);
        }

        loadLyricsCurrentSongBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int i = currentSongTitle.indexOf('-');
                    loadLyrics(currentSongTitle.substring(0, i), currentSongTitle.substring(i));
                }
                catch (Exception e){
                    ma.alert("An error has occured");
                }
            }
        });

        lyricsFormBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String artist = formSong_artist.getText().toString();
                String title = formSong_title.getText().toString();
                if(artist.equals("") || title.equals("")){
                    ma.alert("Please fill all the fields.");
                }
                else{
                    loadLyrics(artist, title);
                }
            }
        });
        return view;
    }

    class JSONAsyncTask extends AsyncTask<String, Void, JSONObject> {

        private Exception exception;

        @Override
        protected void onPreExecute() {
        }

        protected JSONObject doInBackground(String... qs) {
            // Do some validation here
            String API_URL = "http://api.musixmatch.com/ws/1.1/matcher.lyrics.get?apikey="+musixmatch_apikey+"&format=JSON";
            String final_api_url = API_URL+qs[0];
            String res = "";
            res = ma.requete(final_api_url);
            try{
                if(res.length() >0){
                    JSONObject oRes = new JSONObject(res);
                    return oRes;
                }
                return null;
            }
            catch(JSONException e){
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(JSONObject response) {
            if(response == null) {
                ma.alert("An error has occured. Please try again.");
            }
            else {
                try {
                    if(response.getJSONObject("message").getJSONObject("header").getInt("status_code") == 200){
                        ma.currentLyrics = response.getJSONObject("message").getJSONObject("body").getJSONObject("lyrics").getString("lyrics_body");
                        formLyrics.setVisibility(View.GONE);
                        lyricsTitle.setText(ma.currentLyricsTrack, TextView.BufferType.EDITABLE);
                        lyricsText.setText(ma.currentLyrics, TextView.BufferType.EDITABLE);
                    }
                    else if(response.getJSONObject("message").getJSONObject("header").getInt("status_code") == 404){
                        ma.alert("No lyrics found.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }


    public void loadLyrics(String artist, String title){
        ma.currentLyricsTrack = artist + title;
        String encodedUrlArtist = null;
        String encodedUrlTitle = null;
        try {
            encodedUrlArtist = URLEncoder.encode(artist, "UTF-8");
            encodedUrlTitle = URLEncoder.encode(title, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // Can be safely ignored because UTF-8 is always supported
        }
        Object[] arg = new String[]{"&q_artist="+encodedUrlArtist+"&q_track="+encodedUrlTitle};
        AsyncTask at = new JSONAsyncTask();
        at.execute(arg);
    }
}
