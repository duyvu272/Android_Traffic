package dav.com.tracfficchecking;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dav.com.tracfficchecking.model.ImageUserUp;
import dav.com.tracfficchecking.model.UserPostOnCommunication;

public class UploadReport extends AppCompatActivity {
    // Folder path for Firebase Storage.
    public static final  String Storage_Path = "All_Image_Uploads/";

    // Root Database Name for Firebase Database.
    public static final String Database_Path = "User_Post";

    PermissionManager permissionManager;
    // Creating button.
    Button ChooseButton, UploadButton;
    // Creating EditText.
    EditText CurentLoaction;
    EditText Comment;
    // Creating ImageView.
    ImageView SelectImage;
    // Creating URI.
    private Uri FilePathUri;
    String TempEmail;
    public List<ImageUserUp> imageUserPush = new ArrayList<>();
    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_report_activity);
        getSupportActionBar().setTitle("Report");
        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);
        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference();

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path).child(user.getUid());

        //Assign ID'S to button.
        ChooseButton =  findViewById(R.id.btn_chooseImage);
        UploadButton =  findViewById(R.id.btn_upload);

        Comment =  findViewById(R.id.txt_comment);
        CurentLoaction =  findViewById(R.id.txt_locationPost);

        // Assign ID'S to image view.
        SelectImage = findViewById(R.id.iv_LoadImage);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(UploadReport.this);

        TempEmail = user.getEmail();


        // Adding click listener to Choose image button.
        ChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Creating intent.
                Intent intent = new Intent();

                // Setting intent type as image to select image from phone storage.
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code);

            }
        });


        // Adding click listener to Upload image button.
        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Calling method to upload selected image on Firebase storage.
                UploadFileToFirebaseStorage();

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();

            try {

                // Getting selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);

                // Setting up bitmap selected image into ImageView.
                SelectImage.setImageBitmap(bitmap);

                // After selecting image change choose button above text.
                ChooseButton.setText("Image Selected");

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadFileToFirebaseStorage() {
        if(!isConnected()){
            Toast.makeText(this, "No internet access. pls check your network", Toast.LENGTH_SHORT).show();
        }else
        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");

            // Showing progressDialog.
            progressDialog.show();

            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Getting image name from EditText and store into string variable.
                            String location = CurentLoaction.getText().toString().trim();
                            String TempComment = Comment.getText().toString().trim();
                          SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",Locale.getDefault());
//
//                            Date today = Calendar.getInstance().getTime();
//                            String date = df.format(today);
                            long today = Calendar.getInstance().getTimeInMillis();
                            String date =Long.toString(today);

                            // Getting image upload ID.
                            String ImageUploadId = databaseReference.push().getKey();

                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();
                            int numReport = 0;
                            // Showing toast message after done uploading.
                            Toast.makeText(getApplicationContext(), "Data Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            String rootKey = user.getUid();

                            @SuppressWarnings("VisibleForTests")
                            UserPostOnCommunication imageUploadInfo = new UserPostOnCommunication(TempEmail,location, taskSnapshot.getDownloadUrl().toString(),TempComment,date,ImageUploadId,numReport,rootKey);


                            // Adding image upload id s child element into databaseReference.
                            databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(getApplicationContext(),"Error"+ exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            // Setting progressDialog Title.
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                            if(progress == 100) {
                                finish();
                            }


                        }
                    });
        }
        else {

            Toast.makeText(getApplicationContext(), "Please Select Image or write your content", Toast.LENGTH_LONG).show();

        }
    }
    public boolean isConnected() {
        ConnectivityManager manager =(ConnectivityManager) this.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }




}
