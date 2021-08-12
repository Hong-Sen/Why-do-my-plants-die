package kr.sswu.whydomyplantsdie.Model;

public class AlarmModel {
    public String uid;
    public String userid;
    public String image;
    public String imageUrl;
    public String plantName;
    public String heart;
    public String cycle;

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUserid(String fid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setCycle(String cycle) { this.cycle = cycle; }

    public String getCycle() { return cycle; }

    public void setHeart(String heart) {
        this.heart = heart;
    }

    public String getHeart() {
        return heart;
    }
}