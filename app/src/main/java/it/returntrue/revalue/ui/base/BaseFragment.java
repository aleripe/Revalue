package it.returntrue.revalue.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.Tracker;

import it.returntrue.revalue.RevalueApplication;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.preferences.SessionPreferences;

public class BaseFragment extends Fragment {
    protected RevalueApplication mApplication;
    protected Tracker mTracker;
    protected SessionPreferences mSessionPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets application context
        mApplication = (RevalueApplication)getActivity().getApplicationContext();

        // Gets analytics tracker
        mTracker = mApplication.getTracker();

        // Creates preferences managers
        mSessionPreferences = new SessionPreferences(getContext());

        // Sets option menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stops listening to bus events
        BusProvider.bus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listens to bus events
        BusProvider.bus().register(this);
    }
}