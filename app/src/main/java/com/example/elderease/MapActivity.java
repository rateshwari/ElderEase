package com.example.elderease;

// Import necessary Android and Google Maps classes
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// MapActivity displays a map with nearby hospitals.
// It implements OnMapReadyCallback to handle the map when it is ready.
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    // The GoogleMap object.
    private GoogleMap mMap;
    // A constant for the location permission request code.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_map);

        // Find the map fragment in the layout.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // If the fragment is not null, get the map asynchronously.
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // This method is called when the map is ready to be used.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Initialize the GoogleMap object.
        mMap = googleMap;

        // --- Sample Hospital Data ---
        // An array of LatLng objects representing the coordinates of the hospitals.
        LatLng[] hospitals = {
                new LatLng(19.0760, 72.8777), // Mumbai
                new LatLng(19.0974, 72.8742), // Holy Spirit Hospital
                new LatLng(19.1180, 72.8468), // Nanavati Hospital
                new LatLng(19.0989, 72.8325), // Lilavati Hospital
                new LatLng(19.0644, 72.8355)  // Hinduja Hospital
        };

        // An array of strings representing the names of the hospitals.
        String[] names = {
                "Mumbai General Hospital",
                "Holy Spirit Hospital",
                "Nanavati Hospital",
                "Lilavati Hospital",
                "Hinduja Hospital"
        };

        // Add a marker for each hospital to the map.
        for (int i = 0; i < hospitals.length; i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(hospitals[i])
                    .title(names[i]));
        }

        // Move the camera to the center of the markers and set the zoom level.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitals[2], 12));
    }

    // This method is called when the user responds to a permission request.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Check if the permission request is for location.
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // If the permission is granted...
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check if the app has the ACCESS_FINE_LOCATION permission.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Enable the "My Location" layer on the map.
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // If the permission is denied, show a toast message.
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
