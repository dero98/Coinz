package uk.ac.ed.inf.coinz;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class DownloadFromFireStore {

    public DownloadResponseFromFireStore listener = null;
    public String tag="DowloadFromFireStore";
    public boolean notnull = true;

    List<DocumentSnapshot> list;

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
                            listener.processResultFromFireStore(list, true);
                            Log.d(tag,"Sign up was successful");

                        }else{
                            Log.d(tag,"Sign up failed");

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
                    listener.processResultFromFireStore(list, true);
                    Log.d(tag,"Sign up was successful");

                }else{
                    listener.processResultFromFireStore(null,false);

                }
            }
        });



    }
}