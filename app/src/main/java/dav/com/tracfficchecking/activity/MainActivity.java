package dav.com.tracfficchecking.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import dav.com.tracfficchecking.R;
import dav.com.tracfficchecking.model.CameraRealTime;
public class MainActivity extends AppCompatActivity {
    public static final String Database_Path_Cam= "Camera_Real_Time";
    DatabaseReference databaseReference;
    private final int SPLASH_DISPLAY_LENGTH=5000;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path_Cam);
       databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if(!dataSnapshot.exists())
                   readAndPushData();
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //get firebase auth instance

        Intent startActivityIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(startActivityIntent);





    }
    private void readAndPushData() {
        InputStream is = getResources().openRawResource(R.raw.danangcamera);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line =  " ";
        try {
            reader.readLine();
            while((line = reader.readLine())!=null){
                //split ;
                String[] token = line.split(";");
                //Read and push data
                CameraRealTime cam = new CameraRealTime(token[0],Double.parseDouble(token[1]),Double.parseDouble(token[2]));
                String upload = databaseReference.push().getKey();
                // Adding image upload id s child element into databaseReference.
                databaseReference.child(upload).setValue(cam);
            }
        } catch (IOException e) {
            Log.e("MainActivity","Error reading data from line "+line,e);
            e.printStackTrace();
        }
    }
}
