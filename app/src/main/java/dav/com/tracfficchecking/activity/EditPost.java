package dav.com.tracfficchecking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import dav.com.tracfficchecking.R;

public class EditPost extends AppCompatActivity {
    EditText text,text2;
    Button save, cancel;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase;
    HashMap<String,Object> updateContent = new HashMap<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post_activity);
        text = findViewById(R.id.edt_content);
        save =findViewById(R.id.accept_edit_post);
        cancel = findViewById(R.id.cancel_edit_post);
        Intent t = getIntent();
        final String content = t.getStringExtra("content");
        String keyPost =t.getStringExtra("idPost");
        String userID =t.getStringExtra("UserId");
        text.setText(content);
        mDatabase = FirebaseDatabase.getInstance().getReference("User_Post").child(userID).child(keyPost);
        final String ct = text.getText().toString();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText t = findViewById(R.id.edt_content);
                String content = t.getText().toString();
                updateContent.put("content",content);
                mDatabase.updateChildren(updateContent);
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }
}
