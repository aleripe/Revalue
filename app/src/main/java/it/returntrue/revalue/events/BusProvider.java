package it.returntrue.revalue.events;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public final class BusProvider {
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public static Bus bus() {
        return BUS;
    }

    private BusProvider() {}
}