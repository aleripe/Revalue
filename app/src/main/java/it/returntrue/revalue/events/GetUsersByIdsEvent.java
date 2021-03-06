/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.events;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import it.returntrue.revalue.api.UserModel;

/**
 * Represents a data bus event for GetUsersByIds API call
 * */
@SuppressWarnings("ALL")
public class GetUsersByIdsEvent {
    public static class OnStart {
        private final ArrayList<Integer> mUsersIds;
        private final Cursor mCursor;

        public OnStart(ArrayList<Integer> usersIds, Cursor cursor) {
            mUsersIds = usersIds;
            mCursor = cursor;
        }

        public ArrayList<Integer> getUsersIds() {
            return mUsersIds;
        }

        public Cursor getCursor() {
            return mCursor;
        }
    }

    public static class OnSuccess {
        private final List<UserModel> mUsers;
        private final Cursor mCursor;

        public OnSuccess(List<UserModel> users, Cursor cursor) {
            mUsers = users;
            mCursor = cursor;
        }

        public List<UserModel> getUsers() {
            return mUsers;
        }

        public Cursor getCursor() {
            return mCursor;
        }
    }

    public static class OnFailure {
        public OnFailure() { }
    }
}