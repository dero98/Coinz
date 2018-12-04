package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WalletFragment extends Fragment implements DownloadResponseFromFireStore {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String preferencesFile = "MyPrefsFile";
    ListView listCoins;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_wallet, container, false);

    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        listCoins = (ListView) view.findViewById(R.id.ListCoins);

        DownloadFromFireStore dowFs= new DownloadFromFireStore();
        dowFs.listener=this;
        dowFs.doInBackground(db,"Collected");



    }
    @Override
    public void processResultFromFireStore(List<DocumentSnapshot> list, boolean notnull){
        ArrayList<String> coins = new ArrayList<>();
        ArrayList<String> coinsID = new ArrayList<>();

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

        for (DocumentSnapshot d : list) {
            String id = d.get("id").toString();
            String value = d.get("value").toString();
            String currency = d.get("currency").toString();
            coins.add(currency+":"+value+"Exchange Rate is: "+currencies.get(currency));
            coinsID.add(id);
      }



        ArrayAdapter adapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_list_item_1 , coins);
        listCoins.setAdapter(adapter);
        listCoins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cID=coinsID.get(position);
                String coinInformation=coins.get(position);
                Intent intent = new Intent(getActivity(), CoinActivity.class);
                intent.putExtra("CoinID",cID);
                intent.putExtra("CoinInformation",coinInformation);
                getActivity().finish();
                startActivity(intent);
            }
        });

    }


}
