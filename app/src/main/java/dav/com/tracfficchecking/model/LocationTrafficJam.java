package dav.com.tracfficchecking.model;

public class LocationTrafficJam {
    private String email;
    private double lat, lng;
    private String date;
    private String title;
    private String url;

    public LocationTrafficJam()  {
    }

    public LocationTrafficJam(String email, double lat, double lng, String date, String title, String url) {
        this.email = email;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
        this.title = title;
        this.url = url;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }



}


