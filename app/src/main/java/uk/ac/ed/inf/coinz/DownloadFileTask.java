package uk.ac.ed.inf.coinz;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadFileTask extends AsyncTask<String, Void, String> {

    public DownloadResponse listener = null;
    private MapFragment activity;
    private String tag = "Download";
    private final String email = new CurrentUser().getEmail();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DownloadFileTask(MapFragment activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e) {
            return "Unable to load content. Check your network connection";
        }
    }

    private String loadFileFromNetwork(String urlString) throws IOException {
        return readStream(downloadUrl(new URL(urlString)));
    }

    private InputStream downloadUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000); // milliseconds
        conn.setConnectTimeout(15000); // milliseconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    @NonNull
    private String readStream(InputStream stream)
            throws IOException {
        return readString(stream);
    }

    private static String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream into = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];

        for (int n; 0 < (n = stream.read(buf)); ) {
            into.write(buf, 0, n);
        }

        into.close();
        return new String(into.toByteArray(), "UTF-8"); // Or whatever encoding
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        saveToFirestore(result);
        listener.processResult(result);
        File file = new File(activity.getActivity().getFilesDir(), "coinzmap_geojson.txt");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveToFirestore(String result) {
        DeleteOldData();


        db.collection("user:" + email).document("Coinz").
                collection("NotCollected").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        d.getReference().delete();
                    }
                }
                FeatureCollection featuresColl = (FeatureCollection.fromJson(result));
                Map<String, Object> user = new HashMap<>();
                for (Feature feature : featuresColl.features()) {
                    String id = feature.properties().get("id").getAsString();
                    user.put("id", id);
                    String value = feature.properties().get("value").getAsString();
                    user.put("value", value);
                    String currency = feature.properties().get("currency").getAsString();
                    user.put("currency", currency);
                    String marker_color = feature.properties().get("marker-color").getAsString();
                    user.put("marker-color", marker_color);
                    String marker_symbol = feature.properties().get("marker-symbol").getAsString();
                    user.put("marker-symbol", marker_symbol);
                    Point p = (Point) feature.geometry();
                    double lat = p.coordinates().get(1);
                    double lng = p.coordinates().get(0);
                    user.put("lat", lat);
                    user.put("lng", lng);

                    db.collection("user:" + email).document("Coinz").
                            collection("NotCollected").document(id)
                            .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void avoid) {

                            Log.d(tag, "Document was added successfully");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(tag, "Error adding document", e);
                        }
                    });
                }

            }
        });


    }

    public String localdate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate localDate = LocalDate.now();
        return dtf.format(localDate);
    }

    public void DeleteOldData(){

        db.collection("user:" + email).document("Coinz").
                collection("Wallet").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        if (d.get("Golds") == null) {
                            d.getReference().delete();
                        }
                    }
                }
            }
        });

        db.collection("user:"+email)
                .document("NumCoinsPaid").delete();


        db.collection("user:" + email).document("Coinz").
                collection("Chat").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        d.getReference().delete();
                    }
                }
            }
        });

    }

}