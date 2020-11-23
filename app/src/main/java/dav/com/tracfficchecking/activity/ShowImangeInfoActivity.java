package dav.com.tracfficchecking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import dav.com.tracfficchecking.R;

public class ShowImangeInfoActivity extends AppCompatActivity {
    ImageView img;
    Button button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image_click_info_activity);
        img = findViewById(R.id.iv_show_image);
        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        //Toast.makeText(this, "URL" +url, Toast.LENGTH_SHORT).show();
        Glide.with(ShowImangeInfoActivity.this).load(url).into(img);

        button = findViewById(R.id.btnBackMap1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           img = null;
                finish();
            }
        });


    }
}
