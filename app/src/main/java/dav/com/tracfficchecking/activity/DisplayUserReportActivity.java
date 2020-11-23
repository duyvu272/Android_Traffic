package dav.com.tracfficchecking.activity;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dav.com.tracfficchecking.R;
import dav.com.tracfficchecking.adapter.RecyclerViewAdapterUserPost;
import dav.com.tracfficchecking.model.ImageUserUp;
import dav.com.tracfficchecking.model.UserPostOnCommunication;

public class DisplayUserReportActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    ProgressDialog progressDialog;
    List<UserPostOnCommunication> list = new ArrayList<>();
    ImageUserUp imageUserUp = new ImageUserUp();
    Button buttonOption;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_post_activity);

        recyclerView =  findViewById(R.id.recyclerView);
        buttonOption = findViewById(R.id.btn_option_post_menu);

        // Setting RecyclerView size true.
        recyclerView.setHasFixedSize(true);

        // Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // Assign activity this to progress dialog.
        progressDialog = new ProgressDialog(DisplayUserReportActivity.this);

        // Setting up message in Progress dialog.
        progressDialog.setMessage("Loading data");

        // Showing progress dialog.
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("User_Post");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    for(DataSnapshot dataSnapshot : postSnapshot.getChildren()) {
                        UserPostOnCommunication userReport = dataSnapshot.getValue(UserPostOnCommunication.class);
                        list.add(userReport);
                    }
                }
                adapter = new RecyclerViewAdapterUserPost(getApplicationContext(), list);
                recyclerView.setAdapter(adapter);
                // Hiding the progress dialog.
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Hiding the progress dialog.
                progressDialog.dismiss();

            }
        });
    }
}
