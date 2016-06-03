package com.example.vito.MyTubes.fragments;

/**
 * Created by melissabeuze on 22/05/16.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.vito.MyTubes.MainActivity;
import com.example.vito.MyTubes.R;
import com.example.vito.MyTubes.SongAdapter;

public class ListFragment extends Fragment {

    private ListView songView;
    MainActivity ma;

    public ListFragment() {} // Required empty public constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ma = ((MainActivity) getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        songView = (ListView)getView().findViewById(R.id.song_list);
        SongAdapter songAdt = new SongAdapter(ma);
        songView.setAdapter(songAdt);
    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }
}