package com.greenhero.greenheroes;

public class User {
    private String mUserId;
    private String mUserName;
    private String mEmailId;
    private String mPassword;
    private int mTotalTrees;
    private String mprofilePic;

    public User() {
    }

    public User(String UserId, String UserName, String EmailId, String Password) {
      mUserId = UserId;
      mUserName = UserName;
      mEmailId = EmailId;
      mPassword = Password;
      mTotalTrees = 0;
    }



    public User(String mUserId, String mUserName, String mprofilePic) {
        this.mUserId = mUserId;
        this.mUserName = mUserName;
        this.mprofilePic = mprofilePic;
    }

    public User(String mUserId, String mUserName,String mEmailId, String mprofilePic, Boolean bool) {
        this.mUserId = mUserId;
        this.mUserName = mUserName;
        this.mprofilePic = mprofilePic;
        this.mEmailId= mEmailId;
    }



    public String getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getEmailId() {
        return mEmailId;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getTotalTrees(){return mTotalTrees;}

    public String getProfilePic() { return mprofilePic; }
}
