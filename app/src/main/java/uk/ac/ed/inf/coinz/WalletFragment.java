package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletFragment extends Fragment implements DownloadResponseFromFireStore, QueryResponseFromFireStore {

    private FirebaseFirestore db;
    private String tag = "WalletFragment";
    String email = new CurrentUser().getEmail();


    private TextView textViewGolds, textViewNoCoins;
    private String[] cInformation;
    private int numberOfCoinsPaid;

    private ListView listCoins;
    private ArrayList<String> coins;
    private ArrayList<String[]> coinsInformation;
    private double totalNGolds;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_wallet, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textViewGolds = view.findViewById(R.id.editTextGolds);
        textViewGolds.setEnabled(false);
        textViewGolds.setInputType(InputType.TYPE_NULL);
        listCoins = view.findViewById(R.id.ListCoins);
        textViewNoCoins = view.findViewById(R.id.TextViewNoCoins);
        db = FirebaseFirestore.getInstance();
        db.collection("user:" + email).document("NumCoinsPaid").get()
                .addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
                    if (documentSnapshot.exists()) {
                        Object coinsPaid = documentSnapshot.get("NumberOfCoinsPaid");
                        if (coinsPaid != null) {

                            numberOfCoinsPaid = Integer.parseInt(coinsPaid.toString());
                        } else {
                            Log.d(tag, "[onViewCreated] method 'toString' produced " +
                                    "NullPointException at documentSnapshot.get()");
                        }
                    } else {
                        numberOfCoinsPaid = 0;
                    }

                    DownloadFromFireStore dowFs = new DownloadFromFireStore(null);
                    dowFs.listener = WalletFragment.this;
                    dowFs.doInBackground(db, "Wallet");

                });

    }

    @Override
    public void processResultFromFireStore(List<DocumentSnapshot> list, boolean notnull) {
        coins = new ArrayList<>();
        coinsInformation = new ArrayList<>();
        final String preferencesFile = "MyPrefsFile";
        if (getContext() != null) {

            SharedPreferences settings = getContext().getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
// use ”” as the default value (this might be the first time the app is run)
            HashMap<String, Double> currencies = new HashMap<>();
            double valueOfCurrency = Double.parseDouble(settings.getString("DOLR", ""));
            Log.d(tag, "Value of DOLR is " + valueOfCurrency);
            currencies.put("DOLR", valueOfCurrency);

            valueOfCurrency = Double.parseDouble(settings.getString("PENY", ""));
            currencies.put("PENY", valueOfCurrency);

            valueOfCurrency = Double.parseDouble(settings.getString("QUID", ""));
            currencies.put("QUID", valueOfCurrency);

            valueOfCurrency = Double.parseDouble(settings.getString("SHIL", ""));
            currencies.put("SHIL", valueOfCurrency);

            boolean isGoldNull = true;

            if (notnull) {
                for (DocumentSnapshot d : list) {
                    Object goldsObj = d.get("Golds");
                    if (goldsObj != null) {
                        isGoldNull = false;
                        Double tgolds = Double.parseDouble(goldsObj.toString());
                        textViewGolds.setText("Golds: " + convertGolds(tgolds));
                        totalNGolds = tgolds;
                    } else {

                        Object idObj = d.get("id");
                        Object valueObj = d.get("value");
                        Object currencyObj = d.get("currency");
                        if (idObj != null && valueObj != null && currencyObj != null) {

                            String[] coinInf = new String[5];
                            String id = idObj.toString();
                            coinInf[0] = id;
                            double value = Double.parseDouble(valueObj.toString());
                            coinInf[1] = value + "";
                            String currency = currencyObj.toString();
                            coinInf[2] = currency;
                            Log.d(tag, "Currency is " + currency);

                            double exchangeRate = currencies.get(currency);
                            coinInf[3] = exchangeRate + "";
                            double golds = value * exchangeRate;
                            coinInf[4] = golds + "";
                            coins.add(currency + ": " + value);
                            coinsInformation.add(coinInf);

                        } else {
                            Log.d(tag, "[openDialog] method " +
                                    "'get' produced NullPointException at d.get()");
                        }
                    }
                }
                setList();
            }
            if (isGoldNull) {
                textViewGolds.setText("Golds: 0.0");
                totalNGolds = 0;
            }
        } else {
            Log.d(tag, "[processResultFromFireStore] method " +
                    "'getContext' produced NullPointException at " +
                    "getContext().getSharedPreferences" +
                    "(preferencesFile, Context.MODE_PRIVATE)");
        }
    }

    private String convertGolds(Double golds) {
        if (golds >= 10000 && golds < 1000 * 1000) {
            double roundOff = Math.round(golds * 10) / 10;
            String convertedGolds = roundOff / 1000 + "";
            convertedGolds = convertedGolds + "K";
            return convertedGolds;
        }
        if (golds >= 1000 * 1000) {
            double roundOff = Math.round(golds * 10) / 10;
            String convertedGolds = roundOff / 1000000 + "";
            convertedGolds = convertedGolds + "M";
            return convertedGolds;
        }
        return golds + "";
    }


    private void setList() {
        ArrayAdapter adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, coins);
        listCoins.setAdapter(adapter);
        listCoins.setOnItemClickListener((AdapterView<?> parent,
                                          View view, int position, long id) -> {
            cInformation = coinsInformation.get(position);
            openDialog(position);

        });
        if (!coins.isEmpty()) {
            textViewNoCoins.setVisibility(View.GONE);
        } else {
            textViewNoCoins.setVisibility(View.VISIBLE);
        }
    }

    private void openDialog(int index) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String golds = cInformation[4];
            builder.setMessage("Changing")
                    .setTitle("Golds: " + golds);
            builder.setPositiveButton(("Exchange to bank"), (DialogInterface dialog, int id) -> {
                if (numberOfCoinsPaid < 25) {
                    DownloadFromFireStore dowFs2 = new DownloadFromFireStore(null);
                    dowFs2.listenerQ = WalletFragment.this;
                    dowFs2.doInBackgroundQuery(db, "Bank", cInformation[0]);
                } else {
                    Toast.makeText(getContext(), "Reach maximum limit ",
                            Toast.LENGTH_SHORT).show();
                }

            });


            builder.setNeutralButton(("SpareChange"), (DialogInterface dialog, int id) -> {
                if (numberOfCoinsPaid >= 25) {
                    openDialogSpareChange(index);
                } else {
                    int left = 25 - numberOfCoinsPaid;
                    Toast.makeText(getContext(), "You need to pay in bank " + left + " coins",
                            Toast.LENGTH_SHORT).show();
                }

            });
            builder.setNegativeButton(("Cancel"), (DialogInterface dialog, int id) ->
                    dialog.cancel()
            );
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Log.d(tag, "[openDialog] method " +
                    "'getActivity' produced NullPointException at " +
                    "new AlertDialog.Builder(getActivity())");
        }
    }

    private void openDialogSpareChange(int index) {
        if (getContext() != null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

            final EditText edittext = new EditText(getContext());
            alert.setMessage("Enter the email");
            alert.setTitle("Sending the coin");
            alert.setView(edittext);
            alert.setView(edittext);

            alert.setPositiveButton(("Confirm"), (DialogInterface dialog, int whichButton) -> {

                String email_rec = edittext.getText().toString();
                if (!email_rec.equals(email)) {
                    db.collection("user:" + email_rec).get().addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            HashMap<String, Object> message = new HashMap<>();
                            message.put("id", cInformation[0]);
                            message.put("value", cInformation[1]);
                            message.put("currency", cInformation[2]);
                            message.put("exchangeRate", cInformation[3]);
                            message.put("sendBy", email);
                            db.collection("user:" + email_rec)
                                    .document("Coinz").collection("Chat").add(message);


                            db.collection("user:" + email)
                                    .document("Coinz")
                                    .collection("Wallet")
                                    .document(cInformation[0]).delete().addOnSuccessListener((Void aVoid) -> {
                                coins.remove(index);
                                coinsInformation.remove(index);
                                setList();
                                Toast.makeText(getContext(), "Coin was successfully send",
                                        Toast.LENGTH_SHORT).show();

                            });


                        } else {
                            Toast.makeText(getContext(), "Wrong email", Toast.LENGTH_SHORT).show();
                        }

                    });
                } else {
                    Toast.makeText(getContext(), "You couldn't send a coin to your account", Toast.LENGTH_SHORT).show();

                }
            });

            alert.setNegativeButton(("Cancel"), (DialogInterface dialog, int whichButton) ->
                    dialog.cancel()
            );

            alert.show();

        } else {
            Log.d(tag, "[openDialogSpareChange] method " +
                    "'getContext' produced NullPointException at " +
                    "new AlertDialog.Builder(getContext())");
        }
    }


    @Override
    public void processQueryFromFireStore(List<DocumentSnapshot> list, boolean notnull) {
        if (!notnull) {
            Map<String, Object> coin = new HashMap<>();
            coin.put("id", cInformation[0]);
            db.collection("user:" + email)
                    .document("Coinz")
                    .collection("Bank")
                    .document(cInformation[0]).set(coin).addOnSuccessListener((Void avoid) -> {
                db.collection("user:" + email)
                        .document("Coinz")
                        .collection("Wallet")
                        .document(cInformation[0]).delete().addOnSuccessListener((Void aVoid) -> {
                    int index = coinsInformation.indexOf(cInformation);
                    String coinInformation = coins.remove(index);
                    String gds = cInformation[4];
                    double goldsCoin = Double.parseDouble(gds);
                    totalNGolds = totalNGolds + goldsCoin;
                    textViewGolds.setText("Golds: " +
                            convertGolds(totalNGolds));
                    coinsInformation.remove(index);
                    numberOfCoinsPaid++;
                    setList();

                });
                Log.d(tag, "Coins was exchanged");
                Toast.makeText(getContext(),
                        "Coins was exchanged", Toast.LENGTH_SHORT).show();

            }).addOnFailureListener((@NonNull Exception e) -> {
                Log.d(tag, "Error adding document", e);
                Toast.makeText(getContext(),
                        e.getMessage(), Toast.LENGTH_SHORT).show();

            });
        } else {
            Toast.makeText(getContext(),
                    "This coin was already exchanged", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        HashMap<String, Object> tlg = new HashMap<>();
        tlg.put("Golds", totalNGolds);

        db.collection("user:" + email)
                .document("Coinz")
                .collection("Wallet")
                .document("Golds").set(tlg);

        HashMap<String, Object> nocp = new HashMap<>();
        nocp.put("NumberOfCoinsPaid", numberOfCoinsPaid);
        db.collection("user:" + email).document("NumCoinsPaid").set(nocp);
    }
}


