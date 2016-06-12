package it.returntrue.revalue.events;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import it.returntrue.revalue.api.UserModel;

public class GetUsersByIdsEvent {
    public static class OnStart {
        private ArrayList<Integer> mUsersIds;
        private Cursor mCursor;

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
        private List<UserModel> mUsers;
        private Cursor mCursor;

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