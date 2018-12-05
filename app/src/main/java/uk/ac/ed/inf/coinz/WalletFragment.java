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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletFragment extends Fragment implements DownloadResponseFromFireStore,QueryResponseFromFireStore {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String preferencesFile = "MyPrefsFile";
    private String tag="WalletFragment";
    String email = new CurrentUser().getEmail();


   private TextView textViewGolds;
    private String cID;
    private ListView listCoins;
    private ArrayList<String> coinsID ;
    private ArrayList<String> coins;
    private double totalNGolds;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_wallet, container, false);

    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        textViewGolds = (TextView) view.findViewById(R.id.editTextGolds);
        textViewGolds.setEnabled(false);
        textViewGolds.setInputType(InputType.TYPE_NULL);

        listCoins = (ListView) view.findViewById(R.id.ListCoins);

        DownloadFromFireStore dowFs= new DownloadFromFireStore();
        dowFs.listener=this;
        dowFs.doInBackground(db,"Wallet");

    }
    @Override
    public void processResultFromFireStore(List<DocumentSnapshot> list, boolean notnull){
         coins = new ArrayList<>();
         coinsID = new ArrayList<>();

        SharedPreferences settings = getContext().getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
// use ”” as the default value (this might be the first time the app is run)
        HashMap<String,Double> currencies=new HashMap<>();
        double valueOfCurrency=Double.parseDouble(settings.getString("DOLR", ""));
        currencies.put("DOLR",valueOfCurrency);

        valueOfCurrency=Double.parseDouble(settings.getString("PENY", ""));
        currencies.put("PENY",valueOfCurrency);

        valueOfCurrency=Double.parseDouble(settings.getString("QUID", ""));
        currencies.put("QUID",valueOfCurrency);

        valueOfCurrency=Double.parseDouble(settings.getString("SHIL", ""));
        currencies.put("SHIL",valueOfCurrency);
        boolean isGoldNull=true;
        for (DocumentSnapshot d : list) {
            if(d.get("Golds")!=null){
                isGoldNull=false;
                String tgolds=d.get("Golds").toString();
                textViewGolds.setText(tgolds);
                totalNGolds=Double.parseDouble(tgolds);
            }else{
            String id = d.get("id").toString();
            double value = Double.parseDouble(d.get("value").toString());
            String currency = d.get("currency").toString();
            double exchangeRate=currencies.get(currency);
            double golds=value*exchangeRate;
            coins.add(golds + " GOLDS you will get");
            coinsID.add(id);
            }
      }
      if(isGoldNull){
          textViewGolds.setText("Total number of Golds on your account is 0");
          totalNGolds=0;
      }
        setList();

    }
   private void setList(){
       ArrayAdapter adapter= new ArrayAdapter(getContext(),
               android.R.layout.simple_list_item_1 , coins);
       listCoins.setAdapter(adapter);
       listCoins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               cID=coinsID.get(position);
               String coinInformation=coins.get(position);
               openDialog(coinInformation);

           }
       });
   }

    private void openDialog(String coinInformation){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Barev")
                .setTitle("Bank");
        builder.setPositiveButton("ExchangeToBan", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DownloadFromFireStore dowFs2= new DownloadFromFireStore();
                dowFs2.listenerQ=WalletFragment.this;
                dowFs2.doInBackgroundQuery(db,"Bank",cID);


            }
        });


        builder.setNegativeButton("llll", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void processQueryFromFireStore( List<DocumentSnapshot> list,boolean notnull){
        if(!notnull){
            Map<String, Object> user = new HashMap<>();
            user.put("id",cID);
            db.collection("user:"+email)
                    .document("Coinz")
                    .collection("Bank")
                    .document(cID).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void avoid) {
                    db.collection("user:"+email)
                            .document("Coinz")
                            .collection("Wallet")
                            .document(cID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            int index=coinsID.indexOf(cID);
                            String coinInformation=coins.remove(index);
                            String gds= coinInformation.substring(0,coinInformation.indexOf('G'));
                            double goldsCoin=Double.parseDouble(gds);
                            totalNGolds=totalNGolds+goldsCoin;
                            textViewGolds.setText("Total number of Golds on your account is "+
                                    totalNGolds);


                            coinsID.remove(index);
                            setList();

                        }
                    });
                    Log.d(tag,"Sign up was successful");
                    Toast.makeText(getContext(),
                            "Coins was exchanged", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(tag, "Error adding document", e);
                    Toast.makeText(getContext(),
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(getContext(),
                    "This coin was already exchanged" , Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        HashMap<String,Object> user=new HashMap<>();
        user.put("Golds",totalNGolds);

        db.collection("user:"+email)
                .document("Coinz")
                .collection("Wallet")
                .document("Golds").set(user);
    }
}


