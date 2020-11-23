package dav.com.tracfficchecking.model;


import androidx.appcompat.app.AppCompatActivity;

public class ImageUserUp extends AppCompatActivity {
    String urlImage;

    public ImageUserUp() {
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public ImageUserUp(String urlImage) {

        this.urlImage = urlImage;
    }
}
