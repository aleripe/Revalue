/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.preferences.SessionPreferences;

/**
 * Provides a base implementation for all application fragments
 * */
public class BaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets option menu
        setHasOptionsMenu(true);

        // Listens to bus events
        BusProvider.bus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stops listening to bus events
        BusProvider.bus().unregister(this);
    }

    protected RevalueApplication application() {
        return RevalueApplication.get(getActivity().getApplicationContext());
    }

    protected SessionPreferences session() {
        return SessionPreferences.get(getActivity());
    }
}