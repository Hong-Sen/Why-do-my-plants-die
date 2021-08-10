package kr.sswu.whydomyplantsdie.Model;

import java.util.Map;

public class AlarmModel {
    public String uid;
    public String userid;
    public String image;
    public String imageUrl;
    public String plantName;
    public String water;
    public String cycle;
    public String btnOnoff;
    public Map<String, AlarmModel.AlarmItem> alarmItems;

    public static class AlarmItem {
        public String uid;
        public String userid;
        public String image;
        public String imageUrl;
        public String plantName;
        public String water;
        public String cycle;
        public String btnOnoff;
        public String alarmItem;
    }

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

    public void setWater(String water) {
        this.water = water;
    }

    public String getWater() {
        return water;
    }

    /*
    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

     */

    public void setBtnOnoff(String btnOnoff) { this.btnOnoff = btnOnoff; }

    public String getBtnOnoff() { return btnOnoff; }
}