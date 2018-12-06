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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    private ArrayList<String []> coinsInformation;
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

        DownloadFromFireStore dowFs= new DownloadFromFireStore(null);
        dowFs.listener=this;
        dowFs.doInBackground(db,"Wallet");

    }
    @Override
    public void processResultFromFireStore(List<DocumentSnapshot> list, boolean notnull){
         coins = new ArrayList<>();
         coinsID = new ArrayList<>();
         coinsInformation =new ArrayList<>();

        SharedPreferences settings = getContext().getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
// use ”” as the default value (this might be the first time the app is run)
        HashMap<String,Double> currencies=new HashMap<>();
        double valueOfCurrency=Double.parseDouble(settings.getString("DOLR", ""));
        Log.d(tag,"Value of DOLR is "+ valueOfCurrency);
        currencies.put("DOLR",valueOfCurrency);

        valueOfCurrency=Double.parseDouble(settings.getString("PENY", ""));
        currencies.put("PENY",valueOfCurrency);

        valueOfCurrency=Double.parseDouble(settings.getString("QUID", ""));
        currencies.put("QUID",valueOfCurrency);

        valueOfCurrency=Double.parseDouble(settings.getString("SHIL", ""));
        currencies.put("SHIL",valueOfCurrency);
        boolean isGoldNull=true;
        if(notnull){
        for (DocumentSnapshot d : list) {
            if(d.get("Golds")!=null){
                isGoldNull=false;
                String tgolds=d.get("Golds").toString();
                textViewGolds.setText(tgolds);
                totalNGolds=Double.parseDouble(tgolds);
            }else{
                String [] coinInf =new String[4];
                String id = d.get("id").toString();
                coinInf[0]=id;
            double value = Double.parseDouble(d.get("value").toString());
            coinInf[1]=value+"";
                String currency = d.get("currency").toString();
                coinInf[2]=currency;
                Log.d(tag,"Currency is "+ currency);

            double exchangeRate=currencies.get(currency);
                coinInf[3]=exchangeRate+"";
                double golds=value*exchangeRate;
            coins.add(golds + " GOLDS you will get");
            coinsID.add(id);
            coinsInformation.add(coinInf);

            }
      }
        setList();
        }
        if(isGoldNull){
                textViewGolds.setText("Total number of Golds on your account is 0");
                totalNGolds=0;
            }
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
               openDialog(position);

           }
       });
   }

    private void openDialog(int index){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Barev")
                .setTitle("Bank");
        builder.setPositiveButton("Exchange to bank", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DownloadFromFireStore dowFs2= new DownloadFromFireStore(null);
                dowFs2.listenerQ=WalletFragment.this;
                dowFs2.doInBackgroundQuery(db,"Bank",cID);
            }
        });


        builder.setNeutralButton("SpareChange", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                openDialogSpareChange(index);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openDialogSpareChange(int index){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        final EditText edittext = new EditText(getContext());
        alert.setMessage("Enter the email");
        alert.setTitle("Sending the coin");
        alert.setView(edittext);
        alert.setView(edittext);

        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String email_rec = edittext.getText().toString();
                db.collection("user:"+email_rec).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            HashMap<String,Object> message=new HashMap<>();
                            String coinInformation[] =coinsInformation.get(index);
                            message.put("id",coinInformation[0]);
                            message.put("value",coinInformation[1]);
                            message.put("currency",coinInformation[2]);
                            message.put("exchangeRate",coinInformation[3]);
                            message.put("sendBy",email);
                            db.collection("user:"+email_rec)
                                    .document("Coinz").collection("Chat").add(message);

                        }else{
                            Toast.makeText(getContext(), "Wrong email", Toast.LENGTH_SHORT).show();

                        }
                    }});

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();



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
                            coinsInformation.remove(index);
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


