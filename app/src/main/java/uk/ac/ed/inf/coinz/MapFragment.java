package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
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


public class MapFragment extends Fragment  implements OnMapReadyCallback, LocationEngineListener,
        PermissionsListener, DownloadResponse, DownloadResponseFromFireStore {

    private final String tag = "MapFragment";
    private String downloadDate = localdate(); // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences

    private MapView mapView;
    private  MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private View view;
    private Button buttonCollect;
    private final String email=new CurrentUser().getEmail();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context mContext;

    private HashMap<Long,String> markersIDs=new HashMap<>();
    boolean exist;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       return getLayoutInflater().inflate(R.layout.fragment_map, container, false);

    }
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mContext=getContext();
        this.view=view;
        Mapbox.getInstance(getActivity(), getString(R.string.access_token));
        mapView = (MapView) view.findViewById(R.id.mapboxMapView);
        buttonCollect=view.findViewById(R.id.buttonCollect);
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
        }

        if(downloadDate.equals(localdate())){
            DownloadFromFireStore dowFs= new DownloadFromFireStore();
            dowFs.listener=this;
            dowFs.doInBackground(db);

        }else{
            DownloadFileTask dowJ= new DownloadFileTask(this);
            dowJ.listener=this;
            dowJ.execute("http://homepages.inf.ed.ac.uk/stg/coinz/"+localdate()+"/coinzmap.geojson");

        }
        map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                LatLng originLatLng = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
                double distance=marker.getPosition().distanceTo(originLatLng);
                if(distance<=150){
                    buttonCollect.setVisibility(View.VISIBLE);
                    buttonCollect.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View view) {
                                    marker.remove();
                                    buttonCollect.setVisibility(View.GONE);

                                   Map<String, Object> user = new HashMap<>();
                                   String IDofMarker=markersIDs.get(marker.getId());

                                    db.collection("user:"+email)
                                    .document("Coinz")
                                    .collection("NotCollected")
                                    .document(IDofMarker).delete();

                                    user.put("id",IDofMarker);
                                    user.put("currency",marker.getTitle());
                                    user.put("value",marker.getSnippet());
                                    db.collection("user"+email)
                                           .document("Coinz")
                                           .collection("Collected")
                                           .document(IDofMarker).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void avoid) {

                                          Log.d(tag,"Sign up was successful");
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                          Log.d(tag, "Error adding document", e);
                                      }
                                  });
                                }
                            } );
                    return false;
                }else{
                    buttonCollect.setVisibility(View.GONE);
                    return false;
                }
            }
        });
        map.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                buttonCollect.setVisibility(View.GONE);
            }
        });
        }

    @Override
    public void processResult(String result) {

        if (!downloadDate.equals(localdate())) {
            try {
                JSONObject json = new JSONObject(result);
                String shil = json.getJSONObject("rates").getString("DOLR");
                String dolr = json.getJSONObject("rates").getString("DOLR");
                String quid = json.getJSONObject("rates").getString("QUID");
                String peny = json.getJSONObject("rates").getString("PENY");
                downloadDate = localdate();
                SharedPreferences settings = getContext().getSharedPreferences(preferencesFile,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("lastDownloadDate", downloadDate);
                editor.putString("SHIL", shil);
                editor.putString("DOLR", dolr);
                editor.putString("QUID", quid);
                editor.putString("PENY", peny);
// Apply the edits!
                editor.apply();
//
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
   public void processResultFromFireStore( List<DocumentSnapshot> list,boolean notnull){
        if(notnull){
        for (DocumentSnapshot d : list) {
            String id = d.get("id").toString();
            String value = d.get("value").toString();
            String currency = d.get("currency").toString();
            String marker_color = d.get("marker-color").toString();
            String market_symbol = d.get("marker-symbol").toString();
            double lat = Double.parseDouble(d.get("lat").toString());
            double lng = Double.parseDouble(d.get("lng").toString());
            Icon i = IconDraw.drawableToIcon(mContext, R.drawable.ic_place, Color.parseColor(marker_color));
            Marker mp = map.addMarker(new MarkerOptions().title(currency)
                    .snippet(value).icon(i)
                    .position(new LatLng(lat, lng)));
            markersIDs.put(mp.getId(), id);
        }

        }
    }


//        for(Feature feature : featuresColl.features()){
//
//            Point p = (Point) feature.geometry();
//            String id=feature.properties().get("id").getAsString();
//            db.collection("user:"+email).document("Coinz").
//                    collection("NotCollected").whereEqualTo("id",id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                    if (task.isSuccessful()) {
//
//                        String value = feature.properties().get("value").getAsString();
//                        String currency = feature.properties().get("currency").getAsString();
//                        String marker_colorHex = feature.properties().get("marker-color").getAsString();
//                        int marker_colorDec = Integer.parseInt(marker_colorHex.substring(1), 16);
//                        Icon i = drawableToIcon(getContext(), R.drawable.ic_place, Color.parseColor(marker_colorHex));
//                        String market_symbol = feature.properties().get("marker-symbol").getAsString();
//                        Marker mp = map.addMarker(new MarkerOptions().title(currency + ":" + value)
//                                .snippet(market_symbol).icon(i)
//                                .position(new LatLng(p.coordinates().get(1), p.coordinates().get(0))));
//
//
//                        markersIDs.put(mp.getId(), id);
//                        for (DocumentSnapshot document : task.getResult()) {
//                            Log.d(tag, document.getId() + " => " + document.getData());
//                        }
//                    } else {
//                        Log.w(tag, "Error getting documents.", task.getException());
//                    }
//                }
//            });






                    //getResult().exists();
           // DocumentSnapshot dc;
          //   docRef.get().continueWithTask(new Continuation<DocumentSnapshot, Task<DocumentSnapshot>>() {
          //      @Override
           //     public Task<DocumentSnapshot> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
           //         return task ;
           //     }
          //  });








    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {
            Log.d(tag, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
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
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
// Open a dialogue with the user
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        SharedPreferences settings = getContext().getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
// use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");
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

        SharedPreferences settings = getContext().getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
// We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
// Apply the edits!
        editor.apply();


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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public String readFile(){
    String path = getContext().getFilesDir().getAbsolutePath();
    File file = new File(path + "/coinzmap.geojson.txt");
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
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } finally {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String contents = new String(bytes);
        return contents;
}
public String localdate(){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDate localDate = LocalDate.now();
    return dtf.format(localDate);
    }

}



