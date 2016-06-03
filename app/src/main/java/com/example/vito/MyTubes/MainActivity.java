package com.example.vito.MyTubes;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.vito.MyTubes.fragments.DownloadFragment;
import com.example.vito.MyTubes.fragments.ListFragment;
import com.example.vito.MyTubes.fragments.LyricsFragment;

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
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl{

    private static final int PERMISSIONS = 13; // pour identifier la permission
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private DownloadManager dm;
    private long enqueue;
    MusicController controller;
    public ArrayList<Song> songList;
    public static String CAT = "CAT";
    public MusicService musicSrv;
    public boolean musicBound=false;
    public Intent playIntent;
    private boolean playbackPaused=false;
    public String currentLyricsTrack = "";
    public String currentLyrics = "";
    String title_download="";
    View precView = null;

    private int[] tabIcons = {
            R.drawable.ic_library_music_white_24dp,
            R.drawable.ic_cloud_download_black_24dp,
            R.drawable.ic_queue_music_white_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allowPermission();
        setupTabIcons();
    }

    private void setController(){
        controller = new MusicController(this);
        //set previous and next button listeners
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        //set and show
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.viewpager));
//        controller.setPadding(30, 0, 30, 180);
        controller.setEnabled(true);
    }

    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
    }

    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    public void songPicked(View view){
        controller.show();
        if(precView != null && view != precView){
            ImageView iv = (ImageView) precView.findViewById(R.id.playingIcon);
            iv.setVisibility(View.GONE);
            precView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.songSelected));
        }
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.songUnselected));
        ImageView iv = (ImageView) view.findViewById(R.id.playingIcon);
        iv.setVisibility(View.VISIBLE);
        precView = view;
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            playbackPaused=false;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            setController();
        }
        Intent in = getIntent();
        String receivedAction = in.getAction();
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            String shortLink = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            String idLink = shortLink.substring(shortLink.length() - 11);
            String correctLink = "https://www.youtube.com/watch?v=" + idLink;
            new tacheDeFond().execute(new String[]{correctLink});
            this.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
            // lancement normal
        }

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String themeColor = sharedPref.getString("listPref", "");
        CharSequence[] keys = getApplicationContext().getResources().getTextArray(R.array.listArray);
        CharSequence[] values = getApplicationContext().getResources().getTextArray(R.array.listValues);
        // loop and find index...
        int len = values.length;
        for (int i = 0; i < len; i++) {
            if (values[i].equals(themeColor)) {
                Log.i(CAT,"Preference: "+ (String) keys[i]);
            }
        }
        Log.i(CAT, "themeColor: "+themeColor);
    }

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
                        alert(title_download + " Downloaded");
                    }
                }
            }
        }
    };


    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    public ArrayList<Song> getSongs() {
        return songList;
    }

    private class tacheDeFond extends AsyncTask<String, Void, JSONObject>{
        @Override
        protected JSONObject doInBackground(String... qs) {
            String API_URL = "http://www.youtubeinmp3.com/fetch/?format=JSON&video=";
            String url = API_URL + qs[0];
            String res = requete(url);
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
        @Override
        protected void onPostExecute(JSONObject response) {
            String result = "";
            if(response == null) {
                Log.i(CAT, "ERROR onPostExecute");
            }
            else {
                try {
                    String title = response.getString("title");
                    String link = response.getString("link");
                    title_download = title;
                    dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
                    enqueue = dm.enqueue(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void setUpToolbars() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ListFragment(), "List");
        adapter.addFragment(new DownloadFragment(), "Download");
        adapter.addFragment(new LyricsFragment(), "Lyrics");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    public void setSongs() {
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
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
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
        if (qs != null)
        {
            String urlData = qs;
            try {
                URL url = new URL(urlData);
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
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
            case  R.id.action_choose_theme:
                Intent versPref = new Intent(this, PrefActivity.class);
                startActivity(versPref);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void allowPermission() {
        int read = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (read != PackageManager.PERMISSION_GRANTED && write != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS);
        } else {
            setSongs();
            setUpToolbars();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setSongs();
                setUpToolbars();
            } else {
                Toast.makeText(getApplicationContext(),"We need access for reading memory",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void alert(String s){
        Toast.makeText(this.getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
}
