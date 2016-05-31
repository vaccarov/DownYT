package com.example.vito.MyTubes.fragments;

/**
 * Created by melissabeuze on 22/05/16.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.vito.MyTubes.GlobalState;
import com.example.vito.MyTubes.R;
import com.example.vito.MyTubes.SongAdapter;

public class ListFragment extends Fragment {

    Bundle args;
    GlobalState gs;
    private ListView songView;

    public ListFragment() {} // Required empty public constructor

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //gs.onStartFromFragment();
        songView = (ListView)getView().findViewById(R.id.song_list);
        Log.i("songlist",gs.songList.toString());
        SongAdapter songAdt = new SongAdapter(getActivity(), gs.songList);
        songView.setAdapter(songAdt);
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                gs.songPickedFromFragment(position);
            }
        });
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gs = (GlobalState) getActivity().getApplication();
        args = getArguments();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }
}