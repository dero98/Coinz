package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
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

    public String tag="DowloadFromFireStore";
    public boolean notnull = true;

    List<DocumentSnapshot> list;


    public DownloadFromFireStore(Context context) {
        this.context =  context;
    }


    protected void doInBackground(FirebaseFirestore firebaseFirestore,String path) {
        String email = new CurrentUser().getEmail();
        List<DocumentSnapshot> list;
        firebaseFirestore.collection("user:" + email).document("Coinz").
                collection(path).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            listener.processResultFromFireStore(list,true);
                            Log.d(tag,"Documents download succeed");

                        }else{
                            listener.processResultFromFireStore(null,false);
                            Log.d(tag,"No documents to show");

                        }
                    }
                });


    }

    protected void doInBackgroundQuery(FirebaseFirestore firebaseFirestore,String path,String id) {
        String email = new CurrentUser().getEmail();
        List<DocumentSnapshot> list;
        firebaseFirestore.collection("user:" + email).document("Coinz").
                collection(path).whereEqualTo("id",id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    listenerQ.processQueryFromFireStore(list, true);
                    Log.d(tag,"Sign up was successful");

                }else{
                    listenerQ.processQueryFromFireStore(null,false);

                }
            }
        });
    }
    protected void doInBackgroundQueryLastUpload(FirebaseFirestore firebaseFirestore,String date) {
        String email = new CurrentUser().getEmail();
        List<DocumentSnapshot> list;
        firebaseFirestore.collection("user:" + email).whereEqualTo("lastDownloadDate",date).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    listenerQ.processQueryFromFireStore(null,true);
                    Log.d(tag,"lastDownloadDate is "+ date);

                }else{
                    HashMap<String,Object> coinz=new HashMap<>();
                    coinz.put("lastDownloadDate",date);

                    firebaseFirestore.collection("user:" + email).add(coinz);
                    new DownloadFileTask(null).saveToFirestore(readFile());
                    listenerQ.processQueryFromFireStore(null, false);

                }
            }
        });



    }
    public String readFile(){
        String path = context.getFilesDir().getAbsolutePath();
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
}