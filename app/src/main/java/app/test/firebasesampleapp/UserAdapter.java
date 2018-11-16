package app.test.firebasesampleapp;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private final Context mContext;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private List<User> userList = new ArrayList<>();
    double srcLat,srcLng;

    public UserAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<User> users) {
        userList.clear();
        userList.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public UserAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserAdapter.MyViewHolder holder, final int position) {
        final User user = userList.get(position);

        holder.myTextView.setText(user.getName());
        holder.myCountryTextView.setText(user.getDesc());
        holder.myWeight.setText(String.valueOf(user.getlat()));
        holder.myWeight.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, " " + userList.get(position).toString(), Toast.LENGTH_SHORT).show();

//                MapsActivity.values(userList.get(position).getlat(),userList.get(position).getlon());

//                Intent intent = new Intent(mContext, MapsActivity.class);
//                intent.putExtra("name", userList.get(position).getName());
//                mContext.startActivity(intent);

                showMapFromLocation(userList.get(position).getlat(),userList.get(position).getlon());
            }
        });


        // delete user from firebase database based upon the key
        holder.myButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Query deleteQuery = databaseReference.child("users").orderByChild("name").equalTo(userList.get(position).toString());
                databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User userTemp = snapshot.getValue(User.class);
                            if (user.getName().equals(userTemp.getName())) {
                                databaseReference.child("users").child(snapshot.getKey().toString()).removeValue();
                                userList.remove(position);
                                notifyDataSetChanged();
                                if (userList.size() == 0) {
                                    DrawerActivity.textViewEmptyView.setVisibility(View.VISIBLE);
                                }
                                break;

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

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void myloc(double latitude, double longitude) {
        srcLat =latitude;
        srcLng = longitude;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView myTextView;
        public TextView myCountryTextView;
        public TextView myWeight;
        public ImageView myButtonDelete;

        public MyViewHolder(View itemView) {
            super(itemView);
            myTextView = (TextView) itemView.findViewById(R.id.tvName);
            myCountryTextView = (TextView) itemView.findViewById(R.id.tvCountry);
            myWeight=(TextView) itemView.findViewById(R.id.tvWeight);
            myButtonDelete = (ImageView) itemView.findViewById(R.id.deleteButton);
        }
    }

    private void showMapFromLocation(String lat, double lon) {
        double destLat = Double.parseDouble(lat), destLng = lon;

        Log.d("zxcccccccccc","dsffffffff" + srcLat+ srcLng);
        try
        {
                String desLocation = "&daddr=" + Double.toString(destLat) + ","
                        + Double.toString(destLng);
                String currLocation = "saddr=" + Double.toString(srcLat) + ","
                        + Double.toString(srcLng);
                // "d" means driving car, "w" means walking "r" means by bus
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?" + currLocation
                                + desLocation + "&dirflg=d"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.setClassName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity");
                mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error when showing google map directions, E: " + e);
        }
    }


}