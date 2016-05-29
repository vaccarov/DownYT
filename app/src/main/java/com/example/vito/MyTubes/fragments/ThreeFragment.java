package com.example.vito.MyTubes.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.vito.MyTubes.GlobalState;
import com.example.vito.MyTubes.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by melissabeuze on 22/05/16.
 */
public class ThreeFragment extends Fragment{
    View view;
    GlobalState gs;
    Button loadLyricsBtn;
    public EditText edt;
    TextView lyricsErrorMsg;
    String musixmatch_apikey = "5bfe574b1f36bebba4a51420fa3833dc";
    EditText dialogSong_artist;
    EditText dialogSong_title;
    TextView lyricsTitle;
    TextView lyricsText;
    String currentSongTitle ="";
    String currentLyricsTrack = "";

    public ThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_three, container, false);
        gs = (GlobalState) getActivity().getApplication();
        loadLyricsBtn = (Button) view.findViewById(R.id.loadLyrics);
        lyricsErrorMsg = (TextView) view.findViewById(R.id.lyricsErrorMsg);
        lyricsTitle = (TextView)  view.findViewById(R.id.lyricsTitle);
        lyricsText = (TextView)  view.findViewById(R.id.lyricsText);
        lyricsText.setMovementMethod(new ScrollingMovementMethod());


        loadLyricsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(gs.CAT,"here");
                if(gs.musicSrv.songTitle.length() == 0){
                    lyricsErrorMsg.setText("Please, fill the field.");
                }
                else{
                    currentSongTitle = gs.musicSrv.songTitle;
                    int i = currentSongTitle.indexOf(' ');
                    String artist = currentSongTitle.substring(0, i); //should be the artist
                    String title = currentSongTitle.substring(i);
                    String encodedUrlArtist = null;
                    String encodedUrlTitle = null;

                    try {
                        encodedUrlArtist = URLEncoder.encode(artist, "UTF-8");
                        encodedUrlTitle = URLEncoder.encode(title, "UTF-8");
                    } catch (UnsupportedEncodingException ignored) {
                        // Can be safely ignored because UTF-8 is always supported
                    }

                    currentLyricsTrack = currentSongTitle;

                    Object[] arg = new String[]{"&q_artist="+encodedUrlArtist+"&q_track="+encodedUrlTitle};
                    Log.i(gs.CAT,"arg: "+arg);

                    AsyncTask at = new JSONAsyncTask(); //instanciation
                    at.execute(arg); //declenche la requete
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

            Log.i(gs.CAT,"final_api_url: "+final_api_url);
            String res = "";
            res = gs.requete(final_api_url);

            try{
                if(res.length() >0){
                    Log.i(gs.CAT,"res: "+res);
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
            Log.i(gs.CAT, "onPostExecute");

            if(response == null) {
                lyricsErrorMsg.setText("An error has occured");
            }
            else {
                try {
                    if(response.getJSONObject("message").getJSONObject("header").getInt("status_code") == 200){
                        String lyrics = response.getJSONObject("message").getJSONObject("body").getJSONObject("lyrics").getString("lyrics_body");
                        Log.i(gs.CAT, "lyrics found: "+lyrics);
                        loadLyricsBtn.setVisibility(View.GONE);
                        lyricsErrorMsg.setVisibility(View.GONE);
                        lyricsTitle.setText(currentLyricsTrack);
                        lyricsText.setText(lyrics);
                    }
                    else{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        // Get the layout inflater
                        final LayoutInflater inflater = getActivity().getLayoutInflater();

                        // Inflate and set the layout for the dialog
                        // Pass null as the parent view because its going in the dialog layout
                        final View inflator = inflater.inflate(R.layout.dialog_lyrics, null);
                        builder.setView(inflator)
                        // Add action buttons
                        .setPositiveButton(R.string.lyricsDialogLoad, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialogSong_artist = (EditText) inflator.findViewById(R.id.dialog_song_artist);
                                dialogSong_title = (EditText) inflator.findViewById(R.id.dialog_song_title);

                                String encodedUrlArtist = null;
                                String encodedUrlTitle = null;

                                try {
                                    encodedUrlArtist = URLEncoder.encode(dialogSong_artist.getText().toString(), "UTF-8");
                                    encodedUrlTitle = URLEncoder.encode(dialogSong_title.getText().toString(), "UTF-8");
                                } catch (UnsupportedEncodingException ignored) {
                                    // Can be safely ignored because UTF-8 is always supported
                                }

                                currentLyricsTrack = dialogSong_artist.getText().toString() + " " +dialogSong_title.getText().toString();
                                Object[] arg = new String[]{"&q_artist="+encodedUrlArtist+"&q_track="+encodedUrlTitle};

                                Log.i(gs.CAT, "Arg after dialog: "+arg);
                                AsyncTask at = new JSONAsyncTask(); //instanciation
                                at.execute(arg); //declenche la requete
                            }
                        })
                        .setNegativeButton(R.string.lyricsDialogCancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}