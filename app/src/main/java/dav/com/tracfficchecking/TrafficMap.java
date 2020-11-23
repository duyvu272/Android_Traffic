package dav.com.tracfficchecking;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dav.com.tracfficchecking.activity.ChangePasswordActivity;
import dav.com.tracfficchecking.activity.DirectionActivity;
import dav.com.tracfficchecking.activity.DisplayUserReportActivity;
import dav.com.tracfficchecking.activity.HeatMapActivity;
import dav.com.tracfficchecking.activity.LoginActivity;
import dav.com.tracfficchecking.activity.UserListRoadActivity;
import dav.com.tracfficchecking.model.CameraRealTime;
import dav.com.tracfficchecking.model.LocationTrafficJam;
import dav.com.tracfficchecking.model.User;
import dav.com.tracfficchecking.model.UserListRoad;

import static android.util.Log.d;


public class TrafficMap extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    final String TAG = "GPS";
    GoogleMap mGoogleMap;

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    Marker mCurrLocationMarker;
    public DatabaseReference mDatabase;
    public DatabaseReference mDatabaseCam;
    public DatabaseReference mDatabaseUser;
    public DatabaseReference mDatabaseListRoad;
    ArrayList<LatLng> listLatLng = new ArrayList<>();
    FirebaseUser userFB = FirebaseAuth.getInstance().getCurrentUser();
    public static final String Database_Path_User = "Users";
    List<String> lstRoad = new ArrayList<>();
    HashMap<String, LocationTrafficJam> hLocal = new HashMap<>();
    HashMap<String, CameraRealTime> hCam = new HashMap<>();
    HashMap<String,User> hUser = new HashMap<>();
    public double latCurrent, lngCurrent;
    ProgressDialog progressDialog;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Marker myMarker;
    private Marker myMarkerCam;
    Dialog dialog;
    public View viewInfo;
    ImageView imageView;
    Button back;
    EditText textSearch;
    ImageButton btnSearch;
    Circle circle;
    PermissionManager permissionManager;
    ImageButton btnSugest;
    boolean check = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.maptraffic_activity);
        textSearch = findViewById(R.id.search);
        btnSearch =(ImageButton) findViewById(R.id.btn_search_map);
        btnSugest = findViewById(R.id.id_suget);
        progressDialog = new ProgressDialog(TrafficMap.this);
        getSupportActionBar().setTitle("Map Location Activity");
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(this);
        mDatabaseUser = FirebaseDatabase.getInstance().getReference(Database_Path_User);
        //receive User Data and push into firebase
        pushUserToFirebase();

        isGooglePlayServicesAvailable();

        displayDialogTraffic();
        if (!isConnected())
            dialogConnected();

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        getReportLocation();
        getCameraMarker();
        getListRoad();
        //GPS location
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        btnSugest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(TrafficMap.this, DirectionActivity.class);
                startActivity(intent);

            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    geoLocate(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayDialogTraffic() {

    }

    //get list road from firebase
    public void getListRoad() {
        mDatabaseListRoad = FirebaseDatabase.getInstance().getReference("User_List_Road").child(userFB.getUid());
        mDatabaseListRoad.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    UserListRoad u = data.getValue(UserListRoad.class);
                    String road = splitAddress(u.getFullAddress());
                    lstRoad.add(road);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //check adrress and split address
    public String splitAddress(String address) {
        char temp;
        String num = "";
        String addr = "";
        for (int i = 0; i < address.length(); i++) {
            temp = address.charAt(i);
            if (temp == ' ') {
                num = address.substring(0, i);
                addr = address.substring(i + 1, address.length());
                break;
            }
        }
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(num);
        if (matcher.matches()) {
            // Toast.makeText(this,""+addr, Toast.LENGTH_SHORT).show();
            return addr;

        }
        // Toast.makeText(this,""+address, Toast.LENGTH_SHORT).show();
        return address;
        //Toast.makeText(this, "Number is"+addressWN, Toast.LENGTH_SHORT).show();
        //return addressWN;
    }


    public String getAddress(String address) {
        char temp;
        String addressWN = "";
        for (int i = 0; i < address.length(); i++) {
            temp = address.charAt(i);
            if (temp == ',') {
                addressWN = address.substring(0, i);
                break;
            }
        }
        return addressWN;
    }


    private void pushUserToFirebase() {

        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(userFB.getUid()==dataSnapshot.getKey()) {
                    check = true;
                    return;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if(!check) {
            Intent intent = getIntent();
            String username = intent.getStringExtra("Username");
            String phone = intent.getStringExtra("phone");
            String email = intent.getStringExtra("email");
            String password = intent.getStringExtra("password");
            String role = "user";
            User user = new User(username, phone, email, password, role);
            String key = userFB.getUid();
            mDatabaseUser.child(key).setValue(user);
        }
    }


//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(newBase);
//        MultiDex.install(this);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        mGoogleMap.setInfoWindowAdapter(null);
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }


    @Override
    protected void onDestroy() {
        mGoogleApiClient.disconnect();
        mGoogleMap.setInfoWindowAdapter(null);
        super.onDestroy();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.054407, 108.202167), 8));
        //addMarkOnMap(googleMap);
        addCameraAndMarkReportOnMap(googleMap);
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
            }
        } else {

            mGoogleMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();

    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(TrafficMap.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        d(TAG, "onConnected");

        Location ll = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        d(TAG, "LastLocation: " + (ll == null ? "NO LastLocation" : ll.toString()));
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
        d("DDD", connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        String id = null;
        mLastLocation = location;

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        latCurrent = mLastLocation.getLatitude();
        lngCurrent = mLastLocation.getLongitude();
        //Place current location marker
        LatLng latLng = new LatLng(latCurrent, lngCurrent);

        mCurrLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Current Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        //move map camera
    }

    //Create method require user must be turn on GPS
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_SHORT).show();
                d(TAG, "c.");
                finish();
            }
            return false;
        }
        Toast.makeText(this, "This device is supported.", Toast.LENGTH_LONG).show();
        d(TAG, "This device is supported.");
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {

                }
        }
    }

    //show alert when GPS off
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this function")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private void dialogConnected() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("No Internet Connection")
                .setMessage("You need to have mobile data or wifi to access data")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {


                    }
                });
        dialog.show();
    }

    //find road by latitude and longitude
    public String getAddressToLatLng(double lat, double lng) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> address = null;
        try {
            address = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String addr = null;
        if (address == null || address.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No andress found";
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        } else
            addr = address.get(0).getAddressLine(0);

        return addr;
    }
    //find road by name;

    public void geoLocate(View view) throws IOException {
        String errorMessage = "";
        Marker marker = null;
        String location = textSearch.getText().toString();
        Geocoder geocoder = new Geocoder(TrafficMap.this);
        List<Address> address = geocoder.getFromLocationName(location, 1);
        //Toast.makeText(this, "List size" + geocoder.isPresent(), Toast.LENGTH_SHORT).show();
        if (address == null || address.size() == 0) {
            Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
            textSearch.setText(" ");
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found";
                Log.e(TAG, errorMessage);
            }
        } else {
          //  marker.remove();
            Address add = address.get(0);
            String locality = add.getLocality();
            Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();
            double lat = add.getLatitude();
            double lng = add.getLongitude();
            goToLocationZoom(lat, lng, 15);

            marker = mGoogleMap.addMarker(new MarkerOptions()
                    .title(locality)
                    .position(new LatLng(lat, lng))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .snippet("Your Current Position"));
            //markOther.add(marker.getId());

            textSearch.setText("");
        }
    }


    //Create darius for market
    private Circle drawcircle(LatLng latLng) {
        CircleOptions options = new CircleOptions()
                .center(latLng)
                .radius(20)
                .fillColor(0xA6FF4040)
                .strokeColor(Color.GREEN)
                .strokeWidth(1);
        return mGoogleMap.addCircle(options);
    }

    //zoom location
    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }

    //resize
    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    // get report location data from Firebase realtime databse
    public void getReportLocation() {
        final long millis = new java.util.Date().getTime();
        mDatabase = FirebaseDatabase.getInstance().getReference(UploadPositionImage.Database_Path_Report);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot : data.getChildren()) {
                        final LocationTrafficJam localati = snapshot.getValue(LocationTrafficJam.class);
                       // if (Long.parseLong(localati.getDate()) > (millis - 600000)) {
                            double lat = localati.getLat();
                            double lng = localati.getLng();
                            String address = getAddressToLatLng(lat,lng);
                            listLatLng.add(new LatLng(lat,lng));
                            myMarker = mGoogleMap.addMarker(new MarkerOptions()
                                    .title(getAddress(address))
                                    .position(new LatLng(lat, lng))
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("icontraffic", 100, 100))));
                            circle = drawcircle(new LatLng(lat, lng));
                            hLocal.put(myMarker.getId(), localati);
                            String addrress = splitAddress(getAddressToLatLng(lat, lng));
                            for (int i = 0; i < lstRoad.size(); i++) {
                                if (addrress.equalsIgnoreCase(lstRoad.get(i))) {
                                    String road = getAddress(getAddressToLatLng(lat, lng));
                                    AlertDialog.Builder builder = new AlertDialog.Builder(TrafficMap.this);
                                    builder.setTitle("Thông báo tình trạng đường");
                                    builder.setCancelable(true);
                                    builder.setMessage("Tuyến đường " + road + " gần tuyến đường bạn quan tâm đang gặp vấn đề kẹt xe.\n" +
                                            "Hãy chọn đường đi phù hợp để thuận tiện di chuyển.");
                                    builder.setPositiveButton("OK", null);
                                    builder.show();

                               // }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    //Show dialog with image when user click on info windows
    public void showDialogInfo(String url) {
        dialog = new Dialog(TrafficMap.this);
        dialog.setContentView(R.layout.show_image_click_info_activity);
        dialog.setTitle("Show large image");
        back = dialog.findViewById(R.id.btnBackMap1);
        imageView = dialog.findViewById(R.id.iv_show_image);
        Glide.with(TrafficMap.this).load(url).into(imageView);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }
    // show marker camera and marker location report
    public void addCameraAndMarkReportOnMap(final GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //check marker show/hide info windows
                if (marker.isInfoWindowShown())
                    marker.hideInfoWindow();
                if (hCam.get(marker.getId()) != null) {
                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            viewInfo = getLayoutInflater().inflate(R.layout.info_window_camera_activity, null);
                            //ImageView imageView1 = viewInfo.findViewById(R.id.iv_camera);
                            TextView txtAddress = viewInfo.findViewById(R.id.tv_cam_location);
                            String address = getAddressToLatLng(hCam.get(marker.getId()).getLat(), hCam.get(marker.getId()).getLng());
                            splitAddress(address);
                            //Toast.makeText(TrafficMap.this, "New Adress"+addressWN, Toast.LENGTH_SHORT).show();
                            txtAddress.setText(address);
                            //Glide.with(TrafficMap.this).asBitmap().load(Uri.fromFile(new File(hCam.get(marker.getId()).getUrlCam()))).into(imageView1);
                            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    Intent intent = new Intent(TrafficMap.this, DisplayCameraRealTime.class);
                                    String urlCam = hCam.get(marker.getId()).getUrlCam();
                                    intent.putExtra("URL", urlCam);
                                    startActivity(intent);
                                }
                            });
                            return viewInfo;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            return null;
                        }
                    });
                } else if (hLocal.get(marker.getId()) != null) {
                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            View v = getLayoutInflater().inflate(R.layout.info_windows_activity, null);
                            ImageView img = v.findViewById(R.id.iv_download);
                            TextView cmt = v.findViewById(R.id.tv_comment_download);
                            TextView date = v.findViewById(R.id.tv_date_time);
                            cmt.setText(marker.getTitle());
                            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                                    Long.parseLong(hLocal.get(marker.getId()).getDate()),
                                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                            date.setText(timeAgo);
                            Glide.with(TrafficMap.this).load(hLocal.get(marker.getId()).getUrl()).thumbnail(0.5f).into(img);
                            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    showDialogInfo(hLocal.get(marker.getId()).getUrl());
                                }
                            });
                            return v;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            return null;
                        }
                    });
                } else
                    mGoogleMap.setInfoWindowAdapter(null);
                return false;
            }
        });
    }

    //click on marker
    public void getCameraMarker() {
        mDatabaseCam = FirebaseDatabase.getInstance().getReference("Camera_Real_Time");
        mDatabaseCam.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    CameraRealTime cam = data.getValue(CameraRealTime.class);


                    double lat = cam.getLat();
                    double lng = cam.getLng();
                    //  Toast.makeText(TrafficMap.this, "Lat "+lat, Toast.LENGTH_LONG).show();
                    myMarkerCam = mGoogleMap.addMarker(new MarkerOptions()
                            .title("Camera real time")
                            .position(new LatLng(lat, lng))
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("camera1", 150, 150))));
                    hCam.put(myMarkerCam.getId(), cam);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void postProblem(View view) {
//        if (!isLocationEnabled())
//            showAlert();
//        else {

            Intent intent = new Intent(TrafficMap.this, UploadReport.class);
            startActivity(intent);
//        }
    }

    public void sublimation(View view) {
        if (!isLocationEnabled())
            showAlert();
        else {
            Intent intent = new Intent(TrafficMap.this, UploadPositionImage.class);
            startActivity(intent);
        }
    }

    public void onRoadClick(View view) {
        Intent intent = new Intent(TrafficMap.this, UserListRoadActivity.class);
        startActivity(intent);
    }


    //Create option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.types_of_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Create type of Map
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.signout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TrafficMap.this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.changepass:
                Intent intent1 = new Intent(TrafficMap.this, ChangePasswordActivity.class);
                startActivity(intent1);
                break;
            case R.id.heatMap:
                Intent intent2 = new Intent(TrafficMap.this, HeatMapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("location",listLatLng);
                //Toast.makeText(TrafficMap.this, "Size" +listLatLng.size(), Toast.LENGTH_SHORT).show();
                intent2.putExtras(bundle);
                startActivity(intent2);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void communication(View view) {
        Intent intent = new Intent(TrafficMap.this, DisplayUserReportActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

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


}
