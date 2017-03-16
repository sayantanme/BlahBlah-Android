package com.example.sayantanchakraborty.blahblah;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sayantanchakraborty on 15/02/17.
 */

public class SettingsTabActivity extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        return rootView;

    }
}
