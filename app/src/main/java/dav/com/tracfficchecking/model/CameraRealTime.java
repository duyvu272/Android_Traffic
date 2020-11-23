package dav.com.tracfficchecking.model;

public class CameraRealTime {
    private String urlCam;
    private double lat;
    private double lng;

    public CameraRealTime(String urlCam, double lat, double lng) {
        this.urlCam = urlCam;
        this.lat = lat;
        this.lng = lng;
    }

    public CameraRealTime() {
    }

    public String getUrlCam() {
        return urlCam;
    }

    public void setUrlCam(String urlCam) {
        this.urlCam = urlCam;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
