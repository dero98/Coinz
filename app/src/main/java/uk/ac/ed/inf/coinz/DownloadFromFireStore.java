package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DownloadFromFireStore {

    public DownloadResponseFromFireStore listener = null;
    public QueryResponseFromFireStore listenerQ = null;
    private Context context;
    private String tag = "DowloadFromFireStore";



      DownloadFromFireStore(Context context) {
        this.context = context;
    }


    protected void doInBackground(FirebaseFirestore firebaseFirestore, String path) {
        String email = new CurrentUser().getEmail();
        firebaseFirestore.collection("user:" + email).document("Coinz").
                collection(path).get()
                .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) ->{

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list= queryDocumentSnapshots.getDocuments();
                            listener.processResultFromFireStore(list, true);
                            Log.d(tag, "Documents download succeed");

                        } else {
                            listener.processResultFromFireStore(null, false);
                            Log.d(tag, "No documents to show");

                        }

                });


    }

    protected void doInBackgroundQuery(FirebaseFirestore firebaseFirestore, String path, String id) {
        String email = new CurrentUser().getEmail();
        firebaseFirestore.collection("user:" + email).document("Coinz").
                collection(path).whereEqualTo("id", id).get()
                .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {

            if (!queryDocumentSnapshots.isEmpty()) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                listenerQ.processQueryFromFireStore(list, true);
                Log.d(tag, "Sign up was successful");

            } else {
                listenerQ.processQueryFromFireStore(null, false);

            }
        });
    }

    protected void doInBackgroundQueryLastUpload(FirebaseFirestore firebaseFirestore, String date) {
        String email = new CurrentUser().getEmail();
        firebaseFirestore.collection("user:" + email)
                .whereEqualTo("lastDownloadDate", date).get()
                .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        listenerQ.processQueryFromFireStore(null, true);
                        Log.d(tag, "[doInBackgroundQueryLastUpload] lastDownloadDate is " + date);

                    } else {
                        HashMap<String, Object> dateUpldate = new HashMap<>();
                        dateUpldate.put("lastDownloadDate", date);
                        firebaseFirestore.collection("user:" + email)
                                .document("DownloadDate").set(dateUpldate);

                       // new DownloadFileTask(null, false)
                       //         .saveToFirestore(readFile());
                        listenerQ.processQueryFromFireStore(null, false);
                    }
                });


    }

    private String readFile() {
        if (context != null) {
            String path = context.getFilesDir().getAbsolutePath();
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

}