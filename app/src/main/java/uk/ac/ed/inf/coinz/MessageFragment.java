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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MessageFragment extends Fragment {
    private ListView listMessages;
    private TextView NoMessages;
    private List<DocumentSnapshot> list;
    private String tag = "MessageFragment";
    private ArrayList<String[]> coinsInformation;

    private FirebaseFirestore db;
    CollectionReference firestoreChat;
    private final String email = new CurrentUser().getEmail();
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
        listMessages = view.findViewById(R.id.ListMessages);
        NoMessages = view.findViewById(R.id.TextViewNoMessages);
        NoMessages.setEnabled(false);
        NoMessages.setInputType(InputType.TYPE_NULL);

        db = FirebaseFirestore.getInstance();
        coinsInformation = new ArrayList<>();


        db.collection("user:" + email)
                .document("Coinz").collection("Chat").get()
                .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        //addToListView(null,false);
                        //  listener.processResultFromFireStore(null,false);
                        Log.d(tag, "No documents to show");

                    }
                });

        firestoreChat = db.collection("user:" + email)
                .document("Coinz").collection("Chat");
        realtimeUpdateListener();
    }

    private void realtimeUpdateListener() {
        firestoreChat.addSnapshotListener(
                (@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                 @javax.annotation.Nullable FirebaseFirestoreException e) -> {
                    if (e != null) {
                        System.err.println("Listen failed: " + e);
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
//This is a reduntant step. It was done for not having warning only.
                        list = queryDocumentSnapshots.getDocuments();
                        addToListView(list);
                    }

                });


    }

    private void addToListView(List<DocumentSnapshot> l) {
        if (l != null) {
            for (DocumentSnapshot d : l) {
                Object idObj = d.get("id");
                Object valueObj = d.get("value");
                Object currencyObj = d.get("currency");
                Object sendByObj = d.get("sendBy");
                Object exchangeRateObj = d.get("exchangeRate");

                if (idObj != null && valueObj != null && currencyObj != null &&
                        sendByObj != null && exchangeRateObj != null) {
                    String[] coinInf = new String[4];
                    String id = idObj.toString();
                    coinInf[0] = id;
                    double value = Double.parseDouble(valueObj.toString());
                    coinInf[1] = value + "";
                    String currency = currencyObj.toString();
                    coinInf[2] = currency;
                    Double exchangeRate = Double.parseDouble(exchangeRateObj.toString());
                    coinInf[3] = exchangeRate + "";
                    String sendBy = sendByObj.toString();

                    double golds = value * exchangeRate;
                    coins.add(golds + " GOLDS you will get." + "Send by " + sendBy);
                    coinsInformation.add(coinInf);
                } else {
                    Log.d(tag, "[addToListView] method " +
                            "'get' produced NullPointException at d.get()");
                }
            }
        } else {
            Log.d(tag, "[addToListView] List<DocumentSnapshot> l is empty");
        }
        setList();

    }

    private void setList() {
        if (getContext() != null) {
            ArrayAdapter adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, coins);
            listMessages.setAdapter(adapter);
            listMessages.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
                    openDialog(position)
            );
            if (!coins.isEmpty()) {
                NoMessages.setVisibility(View.GONE);
            } else {
                NoMessages.setVisibility(View.VISIBLE);
            }
        }
    }

    private void openDialog(int index) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Message")
                    .setTitle("Do you want to accept this coin ?");

            builder.setPositiveButton(("Accept"), (DialogInterface dialog, int id) -> {
                String CoinID = coinsInformation.get(index)[0];
                db.collection("user:" + email).document("Coinz").collection("Wallet")
                        .whereEqualTo("id", CoinID).get().addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {

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
                        db.collection("user:" + email).document("Coinz")
                                .collection("NotCollected").document(coinInf[0]).delete();

                        firestoreChat.whereEqualTo("id", coinInf[0]).get()
                                .addOnSuccessListener((QuerySnapshot queryDS) -> {

                                    if (!queryDS.isEmpty()) {
                                        DocumentReference doc = queryDS.
                                                getDocuments().get(0).getReference();
                                        doc.delete();
                                        coins = new ArrayList<>();
                                        coinsInformation = new ArrayList<>();
                                    }

                                });
                    }

                });
            });


            builder.setNegativeButton(("Cancel"), (dialog, which) ->
                    dialog.cancel()
            );
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }
}

