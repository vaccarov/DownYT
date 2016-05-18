package com.example.vito.MyTubes;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

/**
 * Created by melissabeuze on 10/05/16.
 */
public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    GlobalState gs;
    Button btnDL;
    EditText dlLink;
    TextView response_text;
    ProgressBar progress_bar;
    private DownloadManager dm;
    private long enqueue;

    String API_URL = "www.youtubeinmp3.com/fetch/?";
    String API_FORMAT ="format=JSON";
    String API_VIDEO = "video=";

    class JSONAsyncTask extends AsyncTask<String, Void, JSONObject> {

        private Exception exception;

        @Override
        protected void onPreExecute() {
            Log.i(gs.CAT, "onPreExecute");
            progress_bar.setVisibility(View.VISIBLE);
            response_text.setText("");
        }

        protected JSONObject doInBackground(String... qs) {
            // Do some validation here

            Log.i(gs.CAT, "doInBackground");
            Log.i(gs.CAT, "qs: "+qs[0]);

            String res = "";
            res = gs.requete(qs[0]);

            /*if (qs[0] != null)
            {
                Log.i(gs.CAT,"requete2");
                String API_URL = "http://www.youtubeinmp3.com/fetch/?";
                String API_FORMAT ="format=JSON";
                String API_VIDEO = "video=";

                String urlData = API_URL + API_FORMAT + "&" + API_VIDEO;
                Log.i(gs.CAT, "urlData: "+ urlData);
                try {
                    URL url = new URL(urlData + qs[0]);
                    Log.i(gs.CAT, "url utilisée : " + url.toString());
                    HttpURLConnection urlConnection = null;
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = null;
                    in = new BufferedInputStream(urlConnection.getInputStream());
                    String txtReponse = gs.convertStreamToString(in);
                    urlConnection.disconnect();
                    res = txtReponse;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/


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
            String result;

            if(response == null) {
                result = "THERE WAS AN ERROR";
            }
            progress_bar.setVisibility(View.GONE);

            try{
                Log.i("INFO", response.getString("link"));
                String title = response.getString("title");
                String link = response.getString("link");
                result = title + " " + link;
                response_text.setText(result);
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                //startActivity(browserIntent);

                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
                enqueue = dm.enqueue(request);
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        btnDL = (Button) findViewById(R.id.dl_button);
        dlLink = (EditText) findViewById(R.id.download_link);
        response_text = (TextView) findViewById(R.id.response_text);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        btnDL.setOnClickListener(this);
        gs = (GlobalState) getApplication();

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
                        }
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onClick(View view) {
        String link = dlLink.getText().toString();
        Log.i(gs.CAT, link);

        Object[] arg = new String[]{link};

        AsyncTask at = new JSONAsyncTask(); //instanciation
        at.execute(arg); //declenche la requete
    }
}
