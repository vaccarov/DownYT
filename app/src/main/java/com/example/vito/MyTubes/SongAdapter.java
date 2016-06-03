
package com.example.vito.MyTubes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {

    private LayoutInflater songInf;
    MainActivity ma;

    public SongAdapter(MainActivity mac){
        ma = mac;
        songInf=LayoutInflater.from(mac);
    }

    @Override
    public int getCount() {
        return ma.getSongs().size();
    }
    @Override
    public Object getItem(int position) {
        return null;
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        RelativeLayout songLay = (RelativeLayout)songInf.inflate(R.layout.song, parent, false);
        //get title and artist views
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        Song currSong = ma.getSongs().get(position); //get song using position
        songView.setText(currSong.getTitle()); //get title and artist strings
        artistView.setText(currSong.getArtist());
        songLay.setTag(position); //set position as tag
        return songLay;
    }
}
