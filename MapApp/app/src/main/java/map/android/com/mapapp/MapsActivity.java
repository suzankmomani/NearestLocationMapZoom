package map.android.com.mapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;

import Objects.MapLocation;
import Objects.SortPlaces;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int REQUEST_LOCATION = 1;
    private static final int ACCESS_FINE_LOCATION = 2;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void startGoogleApiClient() {
        buildGoogleApiClient();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();


        handleMyLocationButton();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {

            // permission has been granted, continue as usual
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            /*draw all the markers including my location, and zoom */
            if (mMap != null) {
                updateMapUI();
            }

            /*start location updates so if the user changed it's location update the map*/
            initLocationRequest();
            if (mCurrentLocation == null)
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void showGrantPermissionScreen() {
        final LinearLayout grantPermissionScreen = (LinearLayout) findViewById(R.id.grantPermissionScreen);
        TextView showPermissionPopup = (TextView) findViewById(R.id.showPermissionPopup);

        grantPermissionScreen.setVisibility(View.VISIBLE);
        showPermissionPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGoogleApiClient();

            }
        });
    }

    private void hideGrantPermissionScreen() {
        final LinearLayout grantPermissionScreen = (LinearLayout) findViewById(R.id.grantPermissionScreen);
        grantPermissionScreen.setVisibility(View.GONE);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                hideGrantPermissionScreen();
                startGoogleApiClient();
            } else {
                // Permission was denied or request was cancelled
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Check Permissions Now

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Display UI and wait for user interaction
                    } else {
                        ActivityCompat.requestPermissions(
                                this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                ACCESS_FINE_LOCATION);
                    }
                    showGrantPermissionScreen();
                } else {
                    hideGrantPermissionScreen();
                    startGoogleApiClient();
                }

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        startGoogleApiClient();


        drawPlacesMarkersOnMap();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.e("location ", " lat: " + latLng.latitude + " lng: " + latLng.longitude);
            }
        });

    }

    private void handleMyLocationButton() {
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    LatLng ll = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 20);
                    mMap.animateCamera(update);

                    return true;
                }
            });
        }
    }

    private LatLng getNearestMarker() {

        ArrayList<MapLocation> mapLocations = getPlacesList();

        LatLng myLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        //sort the list, give the Comparator the current location
        Collections.sort(mapLocations, new SortPlaces(myLoc));
        return new LatLng(mapLocations.get(0).getLat(), mapLocations.get(0).getLng());

    }

    private void drawPlacesMarkersOnMap() {
        ArrayList<MapLocation> mapLocations = getPlacesList();
        for (MapLocation loc :
                mapLocations) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(loc.getLat(), loc.getLng()))
                    .title(loc.getName()));

        }
    }

    private ArrayList<MapLocation> getPlacesList() {
        ArrayList<MapLocation> places = new ArrayList<MapLocation>();

        places.add(new MapLocation("Cafe 4", 32.886613739467826, 43.894992247223854));
        places.add(new MapLocation("Cafe 3", 22.87299280926598, 43.77114996314049));
        places.add(new MapLocation("Cafe 2", 30.62794534081762, 36.17052298039198));
        places.add(new MapLocation("Cafe 1", 31.660850015075493, 36.4123160764575));
        places.add(new MapLocation("Cafe 5", 32.31544321813735, 35.928798280656345));
//        places.add(new MapLocation("Cafe 6", 37.41509908060492, -122.09613550454378));
//        places.add(new MapLocation("Cafe 7", 37.385857958257915, -122.08916913717985));


        return places;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation == null) {
            //remove previous current location marker and add new one at current position
            if (mCurrentLocation != null) {
                myMarker.remove();
            }
            updateMapUI();
        }
    }

    private void updateMapUI() {
        if (mCurrentLocation != null) {
            myMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                    .title("My location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            zoomOnMyLocationAndNearestPlace();
        }

    }

    private void zoomOnMyLocationAndNearestPlace() {
        LatLng myLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(getNearestMarker());
        builder.include(myLoc);
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);
        mMap.animateCamera(cu);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
}
