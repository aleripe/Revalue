/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Represents a bus provider for API events
 * */
public final class BusProvider {
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public static Bus bus() {
        return BUS;
    }

    private BusProvider() {}
}