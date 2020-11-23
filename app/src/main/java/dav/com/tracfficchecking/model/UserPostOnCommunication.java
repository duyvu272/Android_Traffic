package dav.com.tracfficchecking.model;

public class UserPostOnCommunication {
    private String emailUser;
    private String locationUserUp;
    private String imageUrl;
    private String content;
    private String date;
    private String keyUserPost;
    private int report;
    private  String rootKey;

    public UserPostOnCommunication(String emailUser, String locationUserUp, String imageUrl,
                                   String content,String date,String keyUserPost,int report,String rootKey
    ) {
        this.emailUser = emailUser;
        this.locationUserUp = locationUserUp;
        this.imageUrl = imageUrl;
        this.content = content;
        this.date = date;
        this.keyUserPost = keyUserPost;
        this.report = report;
        this.rootKey = rootKey;
    }

    public int getReport() {
        return report;
    }

    public void setReport(int report) {
        this.report = report;
    }

    public String getRootKey() {
        return rootKey;
    }

    public void setRootKey(String rootKey) {
        this.rootKey = rootKey;
    }

    public UserPostOnCommunication() {
    }

    public String getKeyUserPost() {
        return keyUserPost;
    }

    public void setKeyUserPost(String keyUserPost) {
        this.keyUserPost = keyUserPost;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocationUserUp() {
        return locationUserUp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
