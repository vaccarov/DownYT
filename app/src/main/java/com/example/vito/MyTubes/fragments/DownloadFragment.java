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
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vito.MyTubes.MainActivity;
import com.example.vito.MyTubes.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by melissabeuze on 22/05/16.
 */
public class DownloadFragment extends Fragment{
    View view;
    MainActivity ma;
    FloatingActionButton myFab;
    EditText dlLink;
    private DownloadManager dm;
    private long enqueue;
    ProgressBar dlProgressBar;
    TextView errorMsg;

    public EditText edt;

    public DownloadFragment() {
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
        view = inflater.inflate(R.layout.fragment_download, container, false);
        myFab = (FloatingActionButton)  view.findViewById(R.id.myFAB);
        dlLink = (EditText) view.findViewById(R.id.download_link);
        dlProgressBar = (ProgressBar) view.findViewById(R.id.dlProgressBar);
        errorMsg = (TextView) view.findViewById(R.id.errorMsg);

        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            errorMsg.setText("");
            dlProgressBar.setVisibility(View.VISIBLE);
            String link = dlLink.getText().toString();
            Log.i(ma.CAT, link);

            if(link.length() == 0){
                errorMsg.setText("Please, fill the field.");
            }
            else{
                Object[] arg = new String[]{link};

                AsyncTask at = new JSONAsyncTask(); //instanciation
                at.execute(arg); //declenche la requete
            }
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            Log.i(ma.CAT, "success");
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
        }

        protected JSONObject doInBackground(String... qs) {
            // Do some validation here
            String API_URL = "http://www.youtubeinmp3.com/fetch/?";
            String API_FORMAT ="format=JSON";
            String API_VIDEO = "video=";
            String res = "";
            res = ma.requete(API_URL + API_FORMAT + "&" + API_VIDEO + qs[0]);

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
            Log.i(ma.CAT, "onPostExecute");
            String result = "";

            if(response == null) {
                errorMsg.setText("No video was found.");
            }
            else {
                try {
                    Log.i("INFO", response.getString("link"));
                    String title = response.getString("title");
                    String link = response.getString("link");
                    result = title + " " + link;
                    Log.i(ma.CAT, result);

                    dm = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));

                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
                    enqueue = dm.enqueue(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}