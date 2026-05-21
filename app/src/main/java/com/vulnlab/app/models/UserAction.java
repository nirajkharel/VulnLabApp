package com.vulnlab.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAction implements Parcelable {

    public String targetActivity;
    public String payloadData;
    public String actionType;

    public UserAction() {}

    protected UserAction(Parcel in) {
        targetActivity = in.readString();
        payloadData    = in.readString();
        actionType     = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(targetActivity);
        dest.writeString(payloadData);
        dest.writeString(actionType);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<UserAction> CREATOR = new Creator<UserAction>() {
        @Override
        public UserAction createFromParcel(Parcel in) { return new UserAction(in); }
        @Override
        public UserAction[] newArray(int size) { return new UserAction[size]; }
    };
}
