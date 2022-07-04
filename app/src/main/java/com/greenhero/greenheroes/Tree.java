package com.greenhero.greenheroes;

public class Tree {
    private String mUserId,mUserName,mEmailId,mTreeName, mDate, mTime,mImgUrl;

    private double mlat,mlng;


    public Tree() {
    }



    public Tree(String UserId, String UserName, String EmailId, String TreeName, String Date, String Time, String imgUrl, double lat, double lng ) {
      mUserId = UserId;
      mUserName = UserName;
      mEmailId = EmailId;
      mTreeName = TreeName;
      mDate = Date;
      mTime = Time;
      mImgUrl = imgUrl;
      mlat = lat;
      mlng = lng;
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

    public String getTreeName() {
        return mTreeName;
    }

    public String getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }

    public String getImgUrl() {
        return mImgUrl;
    }

    public double getlat() {
        return mlat;
    }

    public double getlng() {
        return mlng;
    }
}
