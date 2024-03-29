package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapFragment extends Fragment implements OnMapReadyCallback, LocationEngineListener,
        PermissionsListener, DownloadResponse, DownloadResponseFromFireStore, QueryResponseFromFireStore {

    private final String tag = "MapFragment";
    private String downloadDate = localdate(); // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences

    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private FloatingActionButton buttonCollect;
    private final String email = new CurrentUser().getEmail();
    public FirebaseFirestore db;
    private Boolean newDevice;
    private HashMap<Long, String> markersIDs = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_map, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db =FirebaseFirestore.getInstance();
        Mapbox.getInstance(requireActivity(), getString(R.string.access_token));
        mapView = view.findViewById(R.id.mapboxMapView);
        buttonCollect = view.findViewById(R.id.buttonCollect);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;
// Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
// Make location information available
            enableLocation();


            DownloadFromFireStore dowFs = new DownloadFromFireStore(getContext());
            dowFs.listenerQ = this;
            dowFs.doInBackgroundQueryLastUpload(db, localdate());


        }
        markerMapListener();

    }


    public void markerMapListener() {
        map.setOnMarkerClickListener((@NonNull Marker marker) -> {

                if (downloadDate.equals(localdate())&& originLocation!=null) {
                LatLng originLatLng = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
                double distance = marker.getPosition().distanceTo(originLatLng);
                if (distance <= 25) {
                    buttonCollect.setVisibility(View.VISIBLE);
                    buttonCollect.setOnClickListener((View view) ->
                            removeMarker(marker)
                    );
                    return false;
                } else {
                    buttonCollect.setVisibility(View.GONE);
                    return false;
                }

            } else {
                startActivity(new Intent(getActivity(), MainActivity.class));
                requireActivity().finish();
            }
            return false;
        });
        map.addOnMapClickListener((@NonNull LatLng point) -> {
            if (downloadDate.equals(localdate())) {
                buttonCollect.setVisibility(View.GONE);
            } else {
                startActivity(new Intent(getActivity(), MainActivity.class));
                requireActivity().finish();
            }
        });
    }



    public void removeMarker(Marker marker) {
        Map<String, Object> user = new HashMap<>();
        String IDofMarker = markersIDs.get(marker.getId());
        marker.remove();
        buttonCollect.setVisibility(View.GONE);


        db.collection("user:" + email)
                .document("Coinz")
                .collection("NotCollected")
                .document(IDofMarker).delete();

        user.put("id", IDofMarker);
        user.put("currency", marker.getTitle());
        user.put("value", marker.getSnippet());
        db.collection("user:" + email)
                .document("Coinz")
                .collection("Wallet")
                .document(IDofMarker).set(user).addOnSuccessListener((Void avoid) -> {

            Log.d(tag, "Coins is added to Wallet");
            Log.d(tag, "Id is" + IDofMarker);

        }).addOnFailureListener(e ->
                Log.d(tag, "Error adding document", e)
        );
    }

    @Override
    public void processResult(String result) {

        if (!downloadDate.equals(localdate())) {
            try {
                JSONObject json = new JSONObject(result);
                String shil = json.getJSONObject("rates").getString("SHIL");
                String dolr = json.getJSONObject("rates").getString("DOLR");
                String quid = json.getJSONObject("rates").getString("QUID");
                String peny = json.getJSONObject("rates").getString("PENY");
                downloadDate = localdate();
                if (getContext() != null) {

                    SharedPreferences settings = getContext().getSharedPreferences(preferencesFile,
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("lastDownloadDate", downloadDate);
                    editor.putString("SHIL", shil);
                    editor.putString("DOLR", dolr);
                    editor.putString("QUID", quid);
                    editor.putString("PENY", peny);
                    editor.apply();
                    Log.d(tag, "lastDowloadDate was changed");
                    Log.d(tag, "Currencies were changed");

                } else {
                    Log.d(tag, "[processResult] method 'getContext'produced " +
                            "NullPointException at getContext()." +
                            "getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)");
                }

//
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!newDevice) {
            FeatureCollection featuresColl = FeatureCollection.fromJson(result);
            List<Feature> allFeatures = featuresColl.features();
            if (allFeatures != null) {
                for (Feature feature : allFeatures) {
                    Point p = (Point) feature.geometry();
                    JsonObject jsProperties = feature.properties();
                    if (jsProperties != null && p != null) {
                        String id = jsProperties.get("id").getAsString();
                        String value = jsProperties.get("value").getAsString();
                        String currency = jsProperties.get("currency").getAsString();
                        String marker_colorHex = jsProperties.get("marker-color").getAsString();
                        Icon i = drawableToIcon(getContext(), R.drawable.ic_place, Color.parseColor(marker_colorHex));
                        Marker mp = map.addMarker(new MarkerOptions().title(currency)
                                .snippet(value).icon(i)
                                .position(new LatLng(p.coordinates().get(1), p.coordinates().get(0))));
                        markersIDs.put(mp.getId(), id);
                    }
                }
            } else {
                Log.d(tag, "[processResult] method 'features' produced " +
                        "NullPointException at featuresColl.features()");

            }
        }

    }


    public  Icon drawableToIcon(@NonNull Context context, @DrawableRes int id, @ColorInt int colorRes) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
        if(vectorDrawable!=null){
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            DrawableCompat.setTint(vectorDrawable, colorRes);
            vectorDrawable.draw(canvas);
            return IconFactory.getInstance(context).fromBitmap(bitmap);
        }else{
            Log.d(tag, "[drawableToIcon] method 'vectorDrawable' produced " +
                    "NullPointException at vectorDrawable.getIntrinsicWidth()");
            return null;
        }
    }




    @Override
    public void processResultFromFireStore(List<DocumentSnapshot> list, boolean notnull) {
        if (notnull && getContext() != null) {
            for (DocumentSnapshot d : list) {
                Object idObj = d.get("id");
                Object valueObj = d.get("value");
                Object currencyObj = d.get("currency");
                Object marker_colorObj = d.get("marker-color");
                Object latObj = d.get("lat");
                Object lngObj = d.get("lng");
                if (idObj != null && valueObj != null && currencyObj != null &&
                        marker_colorObj != null && latObj != null && lngObj != null) {

                    String id = idObj.toString();
                    String value = valueObj.toString();
                    String currency = currencyObj.toString();
                    String marker_color = marker_colorObj.toString();
                    double lat = Double.parseDouble(latObj.toString());
                    double lng = Double.parseDouble(lngObj.toString());
                    Icon i = drawableToIcon(getContext(), R.drawable.ic_place, Color.parseColor(marker_color));
                    Marker mp = map.addMarker(new MarkerOptions().title(currency)
                            .snippet(value).icon(i)
                            .position(new LatLng(lat, lng)));
                    markersIDs.put(mp.getId(), id);
                } else {
                    Log.d(tag, "[processResultFromFireStore] method " +
                            "'get' produced NullPointException at d.get()");

                }
            }
        }
    }

    @Override
    public void processQueryFromFireStore(List<DocumentSnapshot> list, boolean wasToDate) {
        if (wasToDate) {
            if (downloadDate.equals(localdate())) {
                newDevice = false;
                DownloadFromFireStore dowFs = new DownloadFromFireStore(null);
                dowFs.listener = this;
                dowFs.doInBackground(db, "NotCollected");
            } else {
                newDevice = true;
                DownloadFileTask dowJ = new DownloadFileTask(this, true);
                dowJ.listener = this;
                dowJ.execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + localdate() + "/coinzmap.geojson");
                DownloadFromFireStore dowFs = new DownloadFromFireStore(null);
                dowFs.listener = this;
                dowFs.doInBackground(db, "NotCollected");
            }


        } else {
            if (downloadDate.equals(localdate())) {
                newDevice = false;
               new DownloadFileTask(null, false)
                .saveToFirestore(readFile());
                processResult(readFile());
            } else {
                newDevice = false;
                DownloadFileTask dowJ = new DownloadFileTask(this, false);
                dowJ.listener = this;
                dowJ.execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + localdate() + "/coinzmap.geojson");
            }
        }
    }


    private void enableLocation() {
        try {
            if (PermissionsManager.areLocationPermissionsGranted(requireActivity())) {
                Log.d(tag, "Permissions are granted");
                initializeLocationEngine();
                initializeLocationLayer();
            } else {
                Log.d(tag, "Permissions are not granted");
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(getActivity());
            }

        } catch (NullPointerException e) {
            e.printStackTrace();

        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(getActivity())
                .obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000); // preferably every 5 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(tag, "mapView is null");
        } else {
            if (map == null) {
                Log.d(tag, "map is null");
            } else {
                locationLayerPlugin = new LocationLayerPlugin(mapView,
                        map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }

    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
// Present toast or dialog.
        Toast.makeText(getContext(), "In order to use this app the " +
                "Location Servicies must be enabled ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            Toast.makeText(getContext(), "Please make sure that the " +
                            "Location Services are enabled for this app ",
                    Toast.LENGTH_SHORT).show();
            Log.d(tag, "[onPermissionResult] is not granted");

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @SuppressWarnings("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();

        }
        mapView.onStart();
        if (getContext() != null) {
            SharedPreferences settings = getContext().getSharedPreferences(preferencesFile,
                    Context.MODE_PRIVATE);
// use ”” as the default value (this might be the first time the app is run)
            downloadDate = settings.getString("lastDownloadDate", "");
            Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");
        } else Log.d(tag, "[onStart] method 'getContext' NullPointException at" +
                " getContext().getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }

        mapView.onStop();

        Log.d(tag, "[onStop] Storing lastDownloadDate of " + downloadDate);
// All objects are from android.context.Context
        if (getContext() != null) {
            SharedPreferences settings = getContext().getSharedPreferences(preferencesFile,
                    Context.MODE_PRIVATE);
// We need an Editor object to make preference changes.
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("lastDownloadDate", downloadDate);
// Apply the edits!
            editor.apply();
        } else Log.d(tag, "[onStop] produces NullPointException at " +
                "getContext().getSharedPreferences(preferencesFile,Context.MODE_PRIVATE)");

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public String readFile() {
        if (getContext() != null) {
            String path = getContext().getFilesDir().getAbsolutePath();
            File file = new File(path + "/coinzmap_geojson.txt");
            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                try {
                    if (in != null) {
                        in.read(bytes);
                    } else Log.d(tag, "[readFile] method 'read' produced NullPointException " +
                            "at in.read(bytes)");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    if (in != null)
                        in.close();
                    else
                        Log.d(tag, "[readFile] method 'close' produced NullPointException at in.close() ");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d(tag, "The data was returned from file ");
            return new String(bytes);

        } else {
            Log.d(tag, "[readFile] method 'getContext' produced NullPointException at " +
                    " getContext().getFilesDir().getAbsolutePath() ");
            return "";
        }
    }

    public String localdate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate localDate = LocalDate.now();
        return dtf.format(localDate);
    }

}



