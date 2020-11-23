package dav.com.tracfficchecking.activity;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dav.com.tracfficchecking.R;
import dav.com.tracfficchecking.adapter.RecyclerViewAdapterListRoad;
import dav.com.tracfficchecking.model.UserListRoad;

public class UserListRoadActivity extends AppCompatActivity {
    EditText txtRoad;
    Button btnAdd;
    ListView lv;
    private  Geocoder geo;
    List<Address> list1;
    private DatabaseReference mDatabase;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    RecyclerView recyclerView;
    Adapter adapterLR;
    ProgressDialog progressDialog;

    List<UserListRoad> lstUserListRoad = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_road_activity);
        getSupportActionBar().setTitle("List Road");
        mDatabase = FirebaseDatabase.getInstance().getReference("User_List_Road").child(user.getUid());
        //Spinner spinner = findViewById(R.id.city_spinner);
        txtRoad = findViewById(R.id.txtRoad);
        btnAdd = findViewById(R.id.btn_add_road);
        //lv = findViewById(R.id.rv_list_road);

        recyclerView = findViewById(R.id.rv_list_road);


        // Setting RecyclerView size true.
        recyclerView.setHasFixedSize(true);

        // Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // Assign activity this to progress dialog.
        progressDialog = new ProgressDialog(UserListRoadActivity.this);

        // Setting up message in Progress dialog.
        progressDialog.setMessage("Loading data");

        // Showing progress dialog.
        progressDialog.show();

// Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.city_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//// Apply the adapter to the spinner
//        spinner.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = txtRoad.getText().toString();
                geo = new Geocoder(UserListRoadActivity.this);
                try {
                    list1 = geo.getFromLocationName(location,1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(list1.size()==0 || list1 == null){
                    Toast.makeText(getApplicationContext(), "The address does not Exits", Toast.LENGTH_SHORT).show();
                    txtRoad.setText("");
                }else{
                    String address = list1.get(0).getAddressLine(0);
//                    String area = list.get(0).getLocality();
//                    String city = list.get(0).getAdminArea();
//                    String country = list.get(0).getCountryName();

                    String fullAddress = address; //+","+area+","+city+","+ country;
                    String key = mDatabase.push().getKey();
                    UserListRoad listRoad = new UserListRoad(fullAddress,key);
                    mDatabase.child(key).setValue(listRoad);
                    txtRoad.setText("");
                }
            }
        });


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lstUserListRoad.clear();
                    for(DataSnapshot roadSnapshot : dataSnapshot.getChildren() ){
                        UserListRoad u = roadSnapshot.getValue(UserListRoad.class);
                        lstUserListRoad.add(u);

                    }

                Toast.makeText(UserListRoadActivity.this, "Size "+lstUserListRoad.size(), Toast.LENGTH_SHORT).show();

                adapterLR = (Adapter) new RecyclerViewAdapterListRoad(getApplicationContext(), lstUserListRoad);

                recyclerView.setAdapter((RecyclerView.Adapter) adapterLR);

                // Hiding the progress dialog.
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void onPause() {

        super.onPause();
        recyclerView.setOnClickListener(null);


    }

    @Override
    public void onResume() {

        super.onResume();
        if(lstUserListRoad != null){
            //lv.setOnItemClickListener((AdapterView.OnItemClickListener) btnAdd);
        }
    }
}
