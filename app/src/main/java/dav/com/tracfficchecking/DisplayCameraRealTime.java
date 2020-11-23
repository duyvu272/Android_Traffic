package dav.com.tracfficchecking;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayerConfig;

public class DisplayCameraRealTime extends AppCompatActivity implements MediaPlayer.MediaPlayerCallback {
    MediaPlayer mediaPlayer;

Button  button;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.display_camera_activity);
        getSupportActionBar().setTitle("Camera real time");

        mediaPlayer = findViewById(R.id.mp_video);
        button = findViewById(R.id.btnBackMap);
        mediaPlayer.getSurfaceView().setZOrderOnTop(true);
        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");

        progressDialog = new ProgressDialog(DisplayCameraRealTime.this);
        progressDialog.setMessage("please wait");
        progressDialog.show();
        if(mediaPlayer != null){
            mediaPlayer.getConfig().setConnectionUrl(url);
            if( mediaPlayer.getConfig().getConnectionUrl().isEmpty())
                return;

            MediaPlayerConfig config = new MediaPlayerConfig();
            config.setConnectionUrl(mediaPlayer.getConfig().getConnectionUrl());
            config.setConnectionNetworkProtocol(-1);
            config.setConnectionDetectionTime(2000);
            config.setConnectionBufferingTime(500);
            config.setDecodingType(1);
            config.setRendererType(1);
            config.setSynchroEnable(1);
            config.setSynchroNeedDropVideoFrames(1);
            config.setEnableColorVideo(1);
            config.setDataReceiveTimeout(30000);
            config.setNumberOfCPUCores(0);


            mediaPlayer.Open(config,DisplayCameraRealTime.this);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent1 = new Intent(DisplayCameraRealTime.this,TrafficMap.class);
                mediaPlayer.onStop();
                //mediaPlayer.onDestroy();
                finish();
                //startActivity(intent1);
            }
        });

    }

    @Override
    public int Status(int i) {
        if(i == MediaPlayer.PlayerNotifyCodes.PLP_PLAY_SUCCESSFUL.ordinal())
            progressDialog.dismiss();
        return 0;
    }

    @Override
    public int OnReceiveData(ByteBuffer byteBuffer, int i, long l) {
        return 0;
    }




}

