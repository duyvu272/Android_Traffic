package dav.com.tracfficchecking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.net.UriCompat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;


import dav.com.tracfficchecking.model.LocationTrafficJam;
import de.hdodenhof.circleimageview.CircleImageView;

public class UploadPositionImage extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    final String TAG = "GPS";
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    GoogleApiClient gac;
    LocationRequest locationRequest;
    EditText comment;
    ImageView takeImageFromCamera;
    CircleImageView camera;
    Button submit;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri filePathUri;
    Location mLastLocation;
    ProgressDialog progressDialog;
    public static final String Database_Path_Report = "User_Mark_To_Gmap";
    StorageReference storageReference;
    DatabaseReference databaseReference;

    PermissionManager permissionManager;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_post_location_with_image);
        getSupportActionBar().setTitle("Upload position");

        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gac = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        storageReference = FirebaseStorage.getInstance().getReference();
        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path_Report).child(user.getUid());

        progressDialog = new ProgressDialog(this);
        comment = findViewById(R.id.txt_commentWithImage);
        takeImageFromCamera = findViewById(R.id.iv_imageWithLocation);
        camera = findViewById(R.id.civ_camera);
        submit = findViewById(R.id.btn_submitLocationImage);


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Toast.makeText(getApplicationContext(), "" + intent.getData(), Toast.LENGTH_LONG).show();
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
                ;
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
               Toast.makeText(UploadPositionImage.this, "uri : "+filePathUri, Toast.LENGTH_SHORT).show();
                if (!isConnected()) {
                    Toast.makeText(UploadPositionImage.this, "No internet accesss", Toast.LENGTH_SHORT).show();
                } else if (filePathUri == null) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(UploadPositionImage.this);
                    dialog.setTitle("Take a pictrure")
                            .setMessage("You must take a picture to demonstrate \nthis report is real ")
                            .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    finish();
                                }
                            });
                    dialog.show();

                } else {

                    StorageReference filePath = storageReference.child(UploadReport.Storage_Path).child(Objects.requireNonNull(filePathUri.getLastPathSegment()));
                    filePath.putFile(filePathUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            String downloadUri = taskSnapshot.getDownloadUrl().toString();
                            String cmt = comment.getText().toString().trim();
                            double lat = mLastLocation.getLatitude();
                            double lng = mLastLocation.getLongitude();
                            String email = user.getEmail();
                            long today = Calendar.getInstance().getTimeInMillis();
                            String date = Long.toString(today);
                            LocationTrafficJam jam = new LocationTrafficJam(email, lat, lng, date, cmt, downloadUri.toString());
                            // Getting image upload ID.
                            String upload = databaseReference.push().getKey();
                            // Adding image upload id s child element into databaseReference.
                            databaseReference.child(upload).setValue(jam);
                        }

                    });
                    Toast.makeText(getApplicationContext(), "Upload success!!", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,@NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Toast.makeText(getApplicationContext(), "" + data.getData(), Toast.LENGTH_LONG).show();
//            filePathUri = data.getData();

            Bundle extras = data.getExtras();

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            takeImageFromCamera.setImageBitmap(imageBitmap);
            Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
            Toast.makeText(getApplicationContext(), "" + getRealPathFromURI(tempUri), Toast.LENGTH_LONG).show();
            filePathUri = tempUri;

        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }



    //////<----------!!!!!!!!------->
    ///////Google location service API
    @Override
    protected void onStart() {
        gac.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        gac.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null)
            mLastLocation = location;
        //getLatLng(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UploadPositionImage.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        Log.d(TAG, "onConnected");

        Location ll = LocationServices.FusedLocationApi.getLastLocation(gac);
        Log.d(TAG, "LastLocation: " + (ll == null ? "NO LastLocation" : ll.toString()));

        LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission was granted!", Toast.LENGTH_LONG).show();

                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                gac, locationRequest, this);
                    } catch (SecurityException e) {
                        Toast.makeText(getApplicationContext(), "SecurityException:\n" + e.toString(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
        Log.d("DDD", connectionResult.toString());
    }

    public boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Service.CONNECTIVITY_SERVICE);
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


    public void takePicture(View view) {

    }
}
