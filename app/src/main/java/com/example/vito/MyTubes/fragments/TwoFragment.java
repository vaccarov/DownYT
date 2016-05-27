package com.example.vito.MyTubes.fragments;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vito.MyTubes.GlobalState;
import com.example.vito.MyTubes.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by melissabeuze on 22/05/16.
 */
public class TwoFragment extends Fragment{
    View view;
    GlobalState gs;
    FloatingActionButton myFab;
    EditText dlLink;
    private DownloadManager dm;
    private long enqueue;
    ProgressBar dlProgressBar;


    public EditText edt;

    public TwoFragment() {
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
        view = inflater.inflate(R.layout.fragment_two, container, false);

/*
        edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //what ever you need to do goes here
                Log.i(gs.CAT, "click");
            }
        });
*/
        gs = (GlobalState) getActivity().getApplication();
        myFab = (FloatingActionButton)  view.findViewById(R.id.myFAB);
        dlLink = (EditText) view.findViewById(R.id.download_link);
        dlProgressBar = (ProgressBar) view.findViewById(R.id.dlProgressBar);

        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlProgressBar.setVisibility(View.VISIBLE);
                String link = dlLink.getText().toString();
                Log.i(gs.CAT, link);

                Object[] arg = new String[]{link};

                AsyncTask at = new JSONAsyncTask(); //instanciation
                at.execute(arg); //declenche la requete
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            Log.i(gs.CAT, "success");
                            dlProgressBar.setVisibility(View.GONE);
                            dlLink.setText("");
                        }
                    }
                }
            }
        };

        getActivity().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return view;
    }

    class JSONAsyncTask extends AsyncTask<String, Void, JSONObject> {

        private Exception exception;

        @Override
        protected void onPreExecute() {
            Log.i(gs.CAT, "onPreExecute");
        }

        protected JSONObject doInBackground(String... qs) {
            // Do some validation here

            Log.i(gs.CAT, "doInBackground");
            Log.i(gs.CAT, "qs: "+qs[0]);

            String res = "";
            res = gs.requete(qs[0]);

            Log.i(gs.CAT, "res: " + res);


            try{
                Log.i(gs.CAT, "res: " + res);
                JSONObject oRes = new JSONObject(res);
                return oRes;
            }
            catch(JSONException e){
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(JSONObject response) {

            Log.i(gs.CAT, "onPostExecute");
            String result = "";

            if(response == null) {
                result = "THERE WAS AN ERROR";
            }

            try{
                Log.i("INFO", response.getString("link"));
                String title = response.getString("title");
                String link = response.getString("link");
                result = title + " " + link;
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                //startActivity(browserIntent);

                dm = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
                enqueue = dm.enqueue(request);
            }
            catch(JSONException e){
                e.printStackTrace();
            }

            Log.i(gs.CAT,"result: "+result);
        }
    }

}