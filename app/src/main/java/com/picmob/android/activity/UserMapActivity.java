package com.picmob.android.activity;

import static com.picmob.android.utils.AppConstants.GPS_REQUEST;
import static com.picmob.android.utils.AppConstants.ZOOM_THRESHOLD;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.picmob.android.R;
import com.picmob.android.adapters.UserListAdapter;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.requests_response.RequestResponseViewModel;
import com.picmob.android.mvvm.requests_response.UserListPojo;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.ExceptionHandler;
import com.picmob.android.utils.General;
import com.picmob.android.utils.GpsUtils;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.MapClusterItem;
import com.picmob.android.utils.UtilsFunctions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserMapActivity extends BaseActivity implements OnMapReadyCallback {


    @BindView(R.id.btnSnd)
    Button btnSnd;
    @BindView(R.id.continuousSlider)
    Slider radiusSlider;
    @BindView(R.id.tvUserCount)
    TextView tvUserCount;


    private GoogleMap mMap;
    private RequestResponseViewModel viewModel;
    private UserAuthPojo userAuthPojo;
    private List<UserListPojo> userList = new ArrayList<>();
    private static final String TAG = "UserMapActivity";

    private List<String> userIdList = new ArrayList<>();
    private String mMsg, mMedia;
    FusedLocationProviderClient mFusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GpsUtils mGpsUtil;
    private boolean locationPermissionGranted;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private int radius = 50;
    private int userCount = 0;
    private UtilsFunctions utilsFunctions;
    private ClusterManager<MapClusterItem> clusterManager;
    private Circle circle;
    private LatLng position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        SpannableString spannableString = new SpannableString("Select User");
        General general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        utilsFunctions = new UtilsFunctions();

        radiusSlider.setValueFrom(AppConstants.START_RADIUS);
        radiusSlider.setValueTo(AppConstants.END_RADIUS);
        radiusSlider.setValue(AppConstants.DEFAULT_RADIUS);
        radiusSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // Nothing to do
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                getUserList();
            }
        });

        radiusSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                radius = (int) slider.getValue();
            }
        });

        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        viewModel = ViewModelProviders.of(this).get(RequestResponseViewModel.class);

        if (getIntent().getExtras() != null) {
            mMsg = getIntent().getStringExtra(AppConstants.MSG);
            if (getIntent().getExtras().containsKey(AppConstants.MEDIA))
                mMedia = getIntent().getStringExtra(AppConstants.MEDIA);
        }
        userAuthPojo = new Gson().fromJson(CustomSharedPreference.getInstance(this).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGpsUtil = new GpsUtils(this);
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        // Initializing LocationRequest object with appropriate methods
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            utilsFunctions.hideDialog();
            lastKnownLocation = locationResult.getLastLocation();
            getUserList();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude())));
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    };

    private void getUserList() {
        utilsFunctions.showDialog(this);
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
        map.put(AppConstant.ID, userAuthPojo.getId());
        if (lastKnownLocation != null) {
            map.put(AppConstant.LATITUDE, String.valueOf(lastKnownLocation.getLatitude()));
            map.put(AppConstant.LONGITUDE, String.valueOf(lastKnownLocation.getLongitude()));
        } else {
            map.put(AppConstant.LATITUDE, "");
            map.put(AppConstant.LONGITUDE, "");
        }
        map.put(AppConstant.RADIUS, radius);

        viewModel.getUserListByLocation(userAuthPojo.getToken(), map);

        viewModel.getUserListData().observe(this, listResource -> {
            utilsFunctions.hideDialog();
            switch (listResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(UserMapActivity.this, AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    userList.clear();
                    userList.addAll(Objects.requireNonNull(listResource.data));
                    addMarkers(userList);
                    userCount = userList.size();
                    if (userCount > 0) {
                        btnSnd.setEnabled(true);
                    }
                    tvUserCount.setVisibility(View.VISIBLE);
                    tvUserCount.setText(getResources().getQuantityString(R.plurals.user_radius_info,
                            userCount, userCount, radius));
                    break;
            }
        });
    }

    // method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getDeviceLocation();
            } else {
                Log.d(TAG, "Location Permission denied. Use defaults.");
                getUserList();
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
        updateLocationUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                getDeviceLocation();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "GPS denied. Use defaults.");
            getUserList();
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                if (isLocationEnabled()) {
                    Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                    locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                // Set the map's camera position to the current location of the device.
                                lastKnownLocation = task.getResult();
                                Log.e(TAG, "onComplete(): lastKnownLocation -" + lastKnownLocation);
                                if (lastKnownLocation != null) {
                                    utilsFunctions.hideDialog();
                                    getUserList();
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(
                                            new LatLng(lastKnownLocation.getLatitude(),
                                                    lastKnownLocation.getLongitude())));
                                } else {
                                    requestNewLocationData();
                                }
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
                } else {
                    mGpsUtil.turnGPSOn(null);
                }
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void addMarkers(List<UserListPojo> userList) {

        mMap.clear();
        clusterManager.clearItems();
        Collections.sort(userList, new UserListPojo.Sortbylocation());

        userIdList.clear();
        Map<String, UserListPojo> userMap = new HashMap<>();

        //here map key values store in marker snippets
        clusterManager.setOnClusterItemClickListener(item -> {
            showUserListBottomSheet(Collections.singletonList(userMap.get(item.getSnippet())));
            return false;
        });

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MapClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<MapClusterItem> cluster) {
                // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
                // inside of bounds, then animate to center of the bounds.

                // Create the builder to collect all essential cluster items for the bounds.
                LatLngBounds.Builder builder = LatLngBounds.builder();
                List<UserListPojo> userListPojo = new ArrayList<>();
                for (MapClusterItem item : cluster.getItems()) {
                    builder.include(item.getPosition());
                    userListPojo.add(userMap.get(item.getSnippet()));
                }
                showUserListBottomSheet(userListPojo);

                // Animate camera to the bounds
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        clusterManager.getMarkerCollection().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = (UserMapActivity.this).getLayoutInflater()
                        .inflate(R.layout.custom_infoview, null);

                TextView tvUsr = view.findViewById(R.id.tvUsr);
                TextView tvId = view.findViewById(R.id.tvId);

                tvUsr.setText(marker.getTitle());
                tvId.setText(marker.getSnippet());
                return view;
            }
        });

        for (int i = 0; i < userList.size(); i++) {
            UserListPojo userData = userList.get(i);
            userIdList.add(userData.getId().toString());
            userMap.put(userData.getId().toString(), userData);
            LogCapture.e(TAG, "addMarkers: " +
                    userData.getCurrentLatitude() + " " +
                    userData.getCurrentLongitude());
            double latitude = Double.parseDouble(userData.getCurrentLatitude());
            double longitude = Double.parseDouble(userData.getCurrentLongitude());

            position = new LatLng(latitude, longitude);

            clusterManager.addItem(new MapClusterItem(latitude, longitude,
                    userData.getUsername(),
                    userData.getId().toString()));
        }
        clusterManager.cluster();
        double iMeter = radius * 1609.34;// Converting Miles into Meters...
        if (circle != null)
            circle.remove();
        if (lastKnownLocation == null) {
            if (position != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                circle = mMap.addCircle(new CircleOptions()
                        .center(position)
                        .radius(iMeter)
                        .strokeColor(Color.RED)
                        .strokeWidth(0));
            }
        } else {
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .radius(iMeter)
                    .strokeColor(Color.RED)
                    .strokeWidth(0));
        }
        float currentZoomLevel = getZoomLevel(circle);
        Log.e("Zoom Level:", currentZoomLevel + "");
        mMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel), 2000, null);

        mMap.setMinZoomPreference(7.5f);

    }

    public float getZoomLevel(Circle circle) {
        float zoomLevel = 0;
        if (circle != null) {
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel + .5f;
    }

    private void showUserListBottomSheet(List<UserListPojo> userList) {
        LogCapture.e(TAG, "showUserListBottomSheet: " + new Gson().toJson(userList));

        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_user_list, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        BottomSheetBehavior bottomSheetBehavior =
                BottomSheetBehavior.from((View) (dialogView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setFitToContents(true);

        RecyclerView rvUsers = dialogView.findViewById(R.id.rvUser);
        ToggleButton tbSelect = dialogView.findViewById(R.id.tbSelect);
        Button btnSend = dialogView.findViewById(R.id.btnSend);
        rvUsers.setHasFixedSize(true);
        rvUsers.setDrawingCacheEnabled(true);
        rvUsers.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        UserListAdapter userAdapter = new UserListAdapter(userList, UserMapActivity.this);

        rvUsers.setAdapter(userAdapter);

        if (userList.size() == 1) {
            tbSelect.setVisibility(View.GONE);
        }
        tbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> setUserChecked(isChecked, userList, userAdapter));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogCapture.e(TAG, "onClick: " + new Gson().toJson(userList));
                sendRequestToSelectedUser(userList);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sendRequestToSelectedUser(List<UserListPojo> userList) {

        List<Integer> idsList = new ArrayList<>();

        for (UserListPojo user : userList) {
            if (user.isChecked())
                idsList.add(user.getId());
        }

        if (idsList.size() > 0) {
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            map.put(AppConstant.INITIATOR_UID, userAuthPojo.getId().toString());
            map.put(AppConstant.REP_UID, idsList);
            map.put(AppConstant.MSG, mMsg);
            map.put(AppConstant.MEDIA_URL, mMedia);
            map.put(AppConstant.DATETIME, new SimpleDateFormat("dd-MMM-yyyy hh:mm aa").format(new Date()));

            LogCapture.e(TAG, "sendRequestToSelectedUser: " + new Gson().toJson(map));
            if (UtilsFunctions.isNetworkAvail(UserMapActivity.this))
                sendRequest(map);
            else
                showUserListBottomSheet(userList);

        } else {
            UtilsFunctions.showToast(UserMapActivity.this, AppConstants.shortToast, "Choose atleast one user to send request!");
            showUserListBottomSheet(userList);
        }

    }

    private void setUserChecked(boolean isChecked, List<UserListPojo> userList, UserListAdapter userAdapter) {
        for (UserListPojo user : userList) {
            user.setChecked(isChecked);
        }
        userAdapter.notifyDataSetChanged();
    }


    @OnClick(R.id.btnSnd)
    void sendAll() {
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
        map.put(AppConstant.INITIATOR_UID, userAuthPojo.getId().toString());
        map.put(AppConstant.REP_UID, userIdList);
        map.put(AppConstant.MSG, mMsg);
        map.put(AppConstant.DATETIME, new SimpleDateFormat("dd-MMM-yyyy hh:mm aa").format(new Date()));
        if (mMedia != null)
            map.put(AppConstant.MEDIA_URL, mMedia);
        LogCapture.e(TAG, "sendAll: " + new Gson().toJson(map));
        sendRequest(map);
    }


    private void sendRequest(LinkedTreeMap<String, Object> map) {
        utilsFunctions.showDialog(UserMapActivity.this);
        viewModel.sendRequest(userAuthPojo.getToken(), map);
        viewModel.sendRequestData().observe(UserMapActivity.this, requestObserver);
    }


    Observer<Resource<ApiResponseModel>> requestObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            utilsFunctions.hideDialog();
            switch (apiResponseModelResource.status) {
                case ERROR:
                    UtilsFunctions.showToast(UserMapActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    UtilsFunctions.showToast(UserMapActivity.this, AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    Intent i = new Intent(UserMapActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    break;
            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<MapClusterItem>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);


        // Initialize renderer
        ZoomBasedRenderer renderer = new ZoomBasedRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(renderer);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private class ZoomBasedRenderer extends DefaultClusterRenderer<MapClusterItem> implements GoogleMap.OnCameraIdleListener {
        private Float zoom = 9.5f;
        private Float oldZoom;

        public ZoomBasedRenderer(Context context, GoogleMap map, ClusterManager<MapClusterItem> clusterManager) {
            super(context, map, clusterManager);
        }

        /**
         * The {@link ClusterManager} will call the {@link this.onCameraIdle()} implementation of
         * any Renderer that implements {@link GoogleMap.OnCameraIdleListener} <i>before</i>
         * clustering and rendering takes place. This allows us to capture metrics that may be
         * useful for clustering, such as the zoom level.
         */
        @Override
        public void onCameraIdle() {
            // Remember the previous zoom level, capture the new zoom level.
            oldZoom = zoom;
            zoom = mMap.getCameraPosition().zoom;
            Log.i(TAG, "ZOOM LEVEL: " + zoom);
        }

        /**
         * You can override this method to control when the cluster manager renders a group of
         * items as a cluster (vs. as a set of individual markers).
         * <p>
         * In this case, we want single markers to show up as a cluster when zoomed out, but
         * individual markers when zoomed in.
         *
         * @param cluster cluster to examine for rendering
         * @return true when zoom level is less than the threshold (show as cluster when zoomed out),
         * and false when the the zoom level is more than or equal to the threshold (show as marker
         * when zoomed in)
         */
        @Override
        protected boolean shouldRenderAsCluster(@NonNull Cluster<MapClusterItem> cluster) {
            // Show as cluster when zoom is less than the threshold, otherwise show as marker
            return zoom < ZOOM_THRESHOLD;
        }


        /**
         * Returns true if the transition between the two zoom levels crossed a defined threshold,
         * false if it did not.
         *
         * @param oldZoom zoom level from the previous time the camera stopped moving
         * @param newZoom zoom level from the most recent time the camera stopped moving
         * @return true if the transition between the two zoom levels crossed a defined threshold,
         * false if it did not.
         */
        private boolean crossedZoomThreshold(Float oldZoom, Float newZoom) {
            if (oldZoom == null || newZoom == null) {
                return true;
            }
            return (oldZoom < ZOOM_THRESHOLD && newZoom > ZOOM_THRESHOLD) ||
                    (oldZoom > ZOOM_THRESHOLD && newZoom < ZOOM_THRESHOLD);
        }
    }

}