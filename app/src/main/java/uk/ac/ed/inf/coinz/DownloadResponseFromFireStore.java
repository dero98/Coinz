package uk.ac.ed.inf.coinz;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface DownloadResponseFromFireStore {
    void processResultFromFireStore( List<DocumentSnapshot> list,boolean notnull);

}
