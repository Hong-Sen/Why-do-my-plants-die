package kr.sswu.whydomyplantsdie.Model;

public class AlarmModel {
    private String image, fid, name, email, title;

    public AlarmModel(String fid, String image, String name, String email, String title) {
        this.fid = fid;
        this.image = image;
        this.name = name;
        this.email = email;
        this.title = title;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getFid() {
        return fid;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}