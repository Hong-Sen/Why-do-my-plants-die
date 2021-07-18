package kr.sswu.whydomyplantsdie.Model;

import java.util.HashMap;
import java.util.Map;

public class ContentDTO {

    public String explain;
    public String imageUrl;
    public String uid;
    public String userId;
    public String timestamp;
    public int likeCount = 0;
    public Map<String, Boolean> LIKES = new HashMap<>();
    public Map<String, Comment> comments;

    public static class Comment {

        public String uid;
        public String userId;
        public String comment;
    }

    @Override
    public String toString() {
        return "uid = " + uid + " , userid = " + userId;
    }
}

