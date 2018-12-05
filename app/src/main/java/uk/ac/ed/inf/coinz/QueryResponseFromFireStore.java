package uk.ac.ed.inf.coinz;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface QueryResponseFromFireStore {
    void processQueryFromFireStore(List<DocumentSnapshot> list, boolean notnull);

}
