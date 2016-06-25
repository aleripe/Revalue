/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

/**
 * Represents a data bus event for new user login needed
 * */
@SuppressWarnings("ALL")
public class LoginRequestedEvent {
    public static class OnStart { }

    public static class OnSuccess { }

    public static class OnFailure { }
}