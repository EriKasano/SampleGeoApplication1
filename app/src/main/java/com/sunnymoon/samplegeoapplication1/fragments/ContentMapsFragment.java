package com.sunnymoon.samplegeoapplication1.fragments;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sunnymoon.samplegeoapplication1.R;
import com.sunnymoon.samplegeoapplication1.maps.marker.SingleFocusedMarker;
import com.sunnymoon.utils.AttrRetrieval;

public class ContentMapsFragment extends Fragment
        implements OnMapReadyCallback {
    private static final String TAG = "ContentMapsFragment";

    public static final String ARGS_LATITUDE = "latitude";
    public static final String Args_LONGITUDE = "longitude";

    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeDetectionClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    @BindView(R.id.map_view)
    MapView mapView;
    private GoogleMap map;
    private SingleFocusedMarker marker;

    @BindView(R.id.select_current_location)
    FloatingActionButton selectCurLocButton;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private Location mLastKnownLocation;

    public ContentMapsFragment() {
        // Required empty public constructor
    }
    public static ContentMapsFragment newInstance() {
        final ContentMapsFragment fragment = new ContentMapsFragment();
        return fragment;
    }

    public static ContentMapsFragment newInstance(double lat, double lng){
        final Bundle args = new Bundle();
        args.putDouble(ARGS_LATITUDE, lat);
        args.putDouble(Args_LONGITUDE, lng);

        final ContentMapsFragment fragment = new ContentMapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View content = inflater.inflate(R.layout.fragment_content_maps, container, false);
        ButterKnife.bind(this, content);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Construct a GeoDataClient.
        geoDataClient = Places.getGeoDataClient(getActivity(), null);

        // Construct a PlaceDetectionClient.
        placeDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        return content;
    }


    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission granted");
            mLocationPermissionGranted = true;
            updateLocationUI();
        } else {
            Log.d(TAG, "requesting permissions");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        Log.d(TAG, "updateLocationUI");
        Log.d(TAG, "mLocationPermissionGranted=" + mLocationPermissionGranted);
        if (map == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                selectCurLocButton.show();
            } else {
                selectCurLocButton.hide();
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //UI settings.
        UiSettings settings = map.getUiSettings();
        settings.setMapToolbarEnabled(false);

        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if(reason != REASON_DEVELOPER_ANIMATION)
                    selectCurLocButton.setColorFilter(null);
            }
        });

        // Add a marker and move the camera.
        final Bundle args = getArguments();
        marker = new SingleFocusedMarker(map);
        if(args != null) {
            final LatLng sydney = new LatLng(
                    args.getDouble(ARGS_LATITUDE),
                    args.getDouble(Args_LONGITUDE));
            marker.moveTo(sydney);
            marker.setTitle("You'are here.");
        }else {
            final LatLng sydney = new LatLng(-34, 151);
            marker.moveTo(sydney);
            marker.setTitle("Marker in Sydney");
        }

        // Add a listener so that marker will move when the map is clicked.
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectCurLocButton.setColorFilter(null);
                marker.moveTo(latLng);
                marker.setTitle("lat: " + latLng.latitude + ", lng: " + latLng.longitude);
            }
        });
        getLocationPermission();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.select_current_location)
    void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                final Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            final LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude());

                            marker.moveTo(latLng);
                            marker.setTitle("You're here.");
                            selectCurLocButton.setColorFilter( AttrRetrieval.fetchPrimaryColor(getActivity()));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            if(selectCurLocButton.getVisibility() == View.VISIBLE)
                                selectCurLocButton.hide();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mapView != null)
            mapView.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        if(mapView != null)
            mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null)
            mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapView != null)
            mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mapView != null)
            mapView.onLowMemory();
    }
}
