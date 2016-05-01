package it.returntrue.revalue.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenModel {
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("alias")
    @Expose
    private String alias;
    @SerializedName("avatar")
    @Expose
    private String avatar;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}