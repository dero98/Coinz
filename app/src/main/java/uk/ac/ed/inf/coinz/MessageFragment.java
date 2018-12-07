package uk.ac.ed.inf.coinz;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MessageFragment extends Fragment {
    private ListView listMessages;
    private TextView NoMessages;
    private String cID;
    private List<DocumentSnapshot> list;
    private String tag = "MessageFragment";
    private ArrayList<String[]> coinsInformation;

    private FirebaseFirestore db;
    CollectionReference firestoreChat;
    private final String email = new CurrentUser().getEmail();
    private ArrayList<String> coinsID = new ArrayList<>();
    private ArrayList<String> coins = new ArrayList<>();
    // Use com.google.firebase.Timestamp objects instead of java.util.Date objects


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_messages, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listMessages = (ListView) view.findViewById(R.id.ListMessages);
        NoMessages = (TextView) view.findViewById(R.id.TextViewNoMessages);
        NoMessages.setEnabled(false);
        NoMessages.setInputType(InputType.TYPE_NULL);

        db = new MainActivity().db;
        coinsInformation = new ArrayList<>();


        db.collection("user:" + email)
                .document("Coinz").collection("Chat").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {

                    //addToListView(null,false);
                    //  listener.processResultFromFireStore(null,false);
                    Log.d(tag, "No documents to show");

                }
            }
        });

        firestoreChat = db.collection("user:" + email)
                .document("Coinz").collection("Chat");
        realtimeUpdateListener();
    }

    private void realtimeUpdateListener() {
        firestoreChat.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                list = queryDocumentSnapshots.getDocuments();
                addToListView(list, true);

            }
        });


    }

    private void addToListView(List<DocumentSnapshot> l, boolean notnull) {
        if (notnull) {
            for (DocumentSnapshot d : l) {
                String[] coinInf = new String[4];
                String id = d.get("id").toString();
                coinInf[0] = id;
                double value = Double.parseDouble(d.get("value").toString());
                coinInf[1] = value + "";
                String currency = d.get("currency").toString();
                coinInf[2] = currency;
                Double exchangeRate = Double.parseDouble(d.get("exchangeRate").toString());
                coinInf[3] = exchangeRate + "";
                String sendBy = d.get("sendBy").toString();

                double golds = value * exchangeRate;
                coins.add(golds + " GOLDS you will get");
                coinsID.add(id);
                coinsInformation.add(coinInf);
            }
        }
        setList();

    }

    private void setList() {
        if (getContext() != null) {
                ArrayAdapter adapter = new ArrayAdapter(requireContext(),
                        android.R.layout.simple_list_item_1, coins);
                listMessages.setAdapter(adapter);
                listMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        cID = coinsID.get(position);
                        String coinInformation = coins.get(position);
                        openDialog(position);


                    }
                });
                if (!coins.isEmpty()) {
                    NoMessages.setVisibility(View.GONE);
                }
            else{
                NoMessages.setVisibility(View.VISIBLE);
            }
        }
    }

    private void openDialog(int index) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Barev")
                    .setTitle("Bank");
            builder.setPositiveButton("Accept ", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    db.collection("user:" + email).document("Coinz").collection("Wallet")
                            .whereEqualTo("id", id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Toast.makeText(getContext(),
                                        "You have this coin in your wallet",
                                        Toast.LENGTH_SHORT).show();

                            } else {
                                String coinInf[] = coinsInformation.get(index);
                                HashMap<String, Object> coin = new HashMap<>();
                                coin.put("id", coinInf[0]);
                                coin.put("value", coinInf[1]);
                                coin.put("currency", coinInf[2]);
                                db.collection("user:" + email).document("Coinz")
                                        .collection("Wallet").document(coinInf[0]).set(coin);
                                firestoreChat.whereEqualTo("id", coinInf[0]).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            DocumentReference doc = queryDocumentSnapshots.
                                                    getDocuments().get(0).getReference();
                                            doc.delete();
                                            coins = new ArrayList<>();
                                            coinsID = new ArrayList<>();
                                            coinsInformation = new ArrayList<>();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });


            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }
}

