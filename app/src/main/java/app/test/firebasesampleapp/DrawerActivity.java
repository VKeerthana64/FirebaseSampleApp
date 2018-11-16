package app.test.firebasesampleapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    DrawerLayout drawer;

    private LocationManager locationManager;
    private String provider;
    private MyLocationListener mylistener;
    private Criteria criteria;

    public RecyclerView recyclerListView;
    public UserAdapter myAdapter;
    public EditText editTextName;
    public EditText editTextlat;
    public EditText editTextlon,editTextdesc;
    public static TextView textViewEmptyView;
    ImageView buttonAdd;
    public ProgressBar myProgressBar;
    DatabaseReference databaseReference;

    @SuppressLint({"NewApi", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        // creating layout
        creatingLayouts();
        auth = FirebaseAuth.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                DrawerActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(DrawerActivity.this);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);   //default

        // user defines the criteria

        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        provider = locationManager.getBestProvider(criteria, false);

        // the last known location of this provider
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        mylistener = new MyLocationListener();

        if (location != null) {
            mylistener.onLocationChanged(location);
        } else {
            // leads to the settings because there is no last known location
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        // location updates: at least 1 meter and 200millsecs change
        locationManager.requestLocationUpdates(provider, 200, 1, mylistener);
        String a="loc"+location.getLatitude() + location.getLongitude();
        myAdapter.myloc(location.getLatitude(),location.getLongitude());
    }


    public void creatingLayouts(){
        myProgressBar=(ProgressBar) findViewById(R.id.loader);
        textViewEmptyView = (TextView) findViewById(R.id.tvEmptyView);
        editTextName = (EditText) findViewById(R.id.nameEditText);
        editTextlat=(EditText) findViewById(R.id.countryEditText);
        editTextlon=(EditText) findViewById(R.id.weightEditText);
        editTextdesc=(EditText) findViewById(R.id.descEditText);
        buttonAdd = (ImageView) findViewById(R.id.addButton);
        recyclerListView=(RecyclerView) findViewById(R.id.recylerview_list);
        recyclerListView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter= new UserAdapter(this);
        updateAdapter();
        recyclerListView.setAdapter(myAdapter);
    }

    //add new user to database
    public void btnAddOnClick(View v) {

        String name = editTextName.getText().toString().trim();
        String lat=editTextlat.getText().toString().trim();
        String desc=editTextdesc.getText().toString().trim();
        double lon=Double.parseDouble(editTextlon.getText().toString().trim());
        User user= new User(name, lat, lon,desc);

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please enter name",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(lat)) {
            Toast.makeText(getApplicationContext(), "Please enter lat",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(desc)) {
            Toast.makeText(getApplicationContext(), "Please enter description",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        updateDatabase(user);

    }


    // adding new user to end  the user using on firebase database
    public void updateDatabase(User user){

        databaseReference.child("users").push().setValue(user);

        editTextName.setText(null);
        editTextlat.setText(null);
        editTextlon.setText(null);
        editTextdesc.setText(null);

        updateAdapter();

    }

    //update adapter
    public void updateAdapter(){

        final List<User> listUsers= new ArrayList<>();
        databaseReference.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                listUsers.add(dataSnapshot.getValue(User.class));
                displayUsers(listUsers);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Canceled",Toast.LENGTH_SHORT).show();
            }
        });


    }


    //display the user on Adapter
    public void displayUsers(List<User> ls){
        myProgressBar.setVisibility(View.GONE);
        textViewEmptyView.setVisibility(View.GONE);
        recyclerListView.setVisibility(View.VISIBLE);
        editTextName.setText(null);
        editTextlat.setText(null);
        editTextdesc.setText(null);
        myAdapter.setData(ls);
        myAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        }
        else if (id == R.id.nav_manage)
        {
        }
        else if (id == R.id.nav_signout) {
            auth.signOut();

//            clearApplicationData();

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // Initialize the location fields



            Toast.makeText(DrawerActivity.this,  ""+location.getLatitude()+location.getLongitude(),
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(DrawerActivity.this, provider + "'s status changed to "+status +"!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(DrawerActivity.this, "Provider " + provider + " enabled!",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(DrawerActivity.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
