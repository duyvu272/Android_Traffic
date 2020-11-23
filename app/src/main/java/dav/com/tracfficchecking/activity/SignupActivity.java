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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dav.com.tracfficchecking.R;
import dav.com.tracfficchecking.TrafficMap;


public class SignupActivity extends AppCompatActivity {

    public EditText inputEmail, inputPassword,inputUsername,inputPhone,inputRePassword;
    public Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    public static final String Database_Path_User = "Users";
    DatabaseReference databaseReference;
    FirebaseUser userFB = FirebaseAuth.getInstance().getCurrentUser();
    public SignupActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path_User);
        btnSignIn = findViewById(R.id.sign_in_button);
        btnSignUp =  findViewById(R.id.sign_up_button);
        inputUsername = findViewById(R.id.username);
        inputPhone = findViewById(R.id.phonenumber);
        inputRePassword = findViewById(R.id.re_password);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = inputEmail.getText().toString().trim();
                final String password = inputPassword.getText().toString().trim();
                final String username = inputUsername.getText().toString().trim();
                final String phone = inputPhone.getText().toString().trim();
                String rePass = inputRePassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isValidMail(email)){
                    Toast.makeText(getApplicationContext(), "Not Valid Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isValidMobile(phone)){
                    Toast.makeText(getApplicationContext(), "Not Valid phone", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(rePass)) {
                    Toast.makeText(getApplicationContext(), "Input password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(username)){
                    Toast.makeText(getApplicationContext(), "Enter username!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(getApplicationContext(), "Enter your phone number!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!rePass.equalsIgnoreCase(password)){
                    Toast.makeText(getApplicationContext(), "password not match!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //push user data on firebase
//                User user = new User(username,phone,email,password);
//
//                String upload = databaseReference.push().getKey();
//                // Adding image upload id s child element into databaseReference.
//                databaseReference.child(upload).setValue(user);

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(getApplicationContext(), "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
//                                //push user data on firebase
//                                User user = new User(username,phone,email,password);
                                Intent intent = new Intent(SignupActivity.this, TrafficMap.class);
                                intent.putExtra("Username",username);
                                intent.putExtra("phone",phone);
                                intent.putExtra("email",email);
                                intent.putExtra("password",password);
//                                String upload = userFB.getUid();
//                                // Adding image upload id s child element into databaseReference.
//                                databaseReference.child(upload).setValue(user);
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show(

                                    );
                                } else {
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

            }
        });
    }
    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}