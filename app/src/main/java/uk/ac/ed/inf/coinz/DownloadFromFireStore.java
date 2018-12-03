package uk.ac.ed.inf.coinz;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class DownloadFromFireStore {

    public DownloadResponseFromFireStore listener = null;
    public boolean notnull = true;

    List<DocumentSnapshot> list;

    protected void doInBackground(FirebaseFirestore firebaseFirestore) {
        String email = new CurrentUser().getEmail();
        List<DocumentSnapshot> list;
        firebaseFirestore.collection("user:" + email).document("Coinz").
                collection("NotCollected").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            listener.processResultFromFireStore(list, true);

                        }
                    }
                });


    }
}