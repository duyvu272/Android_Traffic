package dav.com.tracfficchecking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dav.com.tracfficchecking.R;
import dav.com.tracfficchecking.model.User;

public class ChangePasswordActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private EditText inputCurrentPass, inputNewPass, inputRePass;
    private Button btnReset, btnBack;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    FirebaseUser user;
    private String pass;
    static String key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        inputCurrentPass = findViewById(R.id.currentpass);
        inputNewPass = findViewById(R.id.newpass);
        inputRePass = findViewById(R.id.re_newpass);

        btnReset = findViewById(R.id.btn_reset_password);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        Toast.makeText(this, "Email"+user.getEmail(), Toast.LENGTH_SHORT).show();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mDatabase.child("Users").child(user.getUid()).child("password").toString();
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            User u = data.getValue(User.class);
                            if (u.getEmail().equalsIgnoreCase(user.getEmail())) {
                                key = data.getKey();
                                pass = u.getPassword();
                                Toast.makeText(getApplication(), "key" + key, Toast.LENGTH_SHORT).show();
                                String currentPass = inputCurrentPass.getText().toString().trim();
                                final String newPass = inputNewPass.getText().toString().trim();
                                String RePass = inputRePass.getText().toString().trim();

                                if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(RePass)) {
                                    Toast.makeText(getApplication(), "Fill your password", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (!newPass.equals(RePass)) {
                                    Toast.makeText(getApplication(), "Password not match", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                progressBar.setVisibility(View.VISIBLE);
                                user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(ChangePasswordActivity.this, "Update Password Successed", Toast.LENGTH_SHORT).show();
                                            mDatabase.child(key).child("password").setValue(newPass);
                                        }progressBar.setVisibility(View.GONE);
                                    }
                                });
                                mDatabase.child(key).child("password").setValue(newPass);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

}
