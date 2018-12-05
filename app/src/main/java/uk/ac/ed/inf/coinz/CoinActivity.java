package uk.ac.ed.inf.coinz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class CoinActivity extends AppCompatActivity {
//    private TextView textViewCoin;
//    private Button buttonSpareChange, buttonExchangeBank;
//    private String coinID;
//    private String CoinInformation ;
//    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final String email=new CurrentUser().getEmail();
//    private String tag="CoinActivity";
//
//
  @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
//        coinID=getIntent().getStringExtra("CoinID");
//        CoinInformation= getIntent().getStringExtra("CoinInformation");
    setContentView(R.layout.activity_coin);
//        textViewCoin = (TextView) findViewById(R.id.textViewCoin);
//        findViewById(R.id.buttonSpareChange).setOnClickListener(this);
//        findViewById(R.id.buttonExchangeBank).setOnClickListener(this);
//        textViewCoin.setEnabled(false);
//        textViewCoin.setInputType(InputType.TYPE_NULL);
//        textViewCoin.setText(CoinInformation);
//
   }
}
//
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.buttonExchangeBank:
//                ExchangeCoin();
//                getSupportFragmentManager().beginTransaction().replace(R.id.some,
//                        new WalletFragment()).commit();
//                break;
//        }
//    }
//
//
//  private void ExchangeCoin(){
//      DownloadFromFireStore dowFs= new DownloadFromFireStore();
//      dowFs.listener=this;
//      dowFs.doInBackgroundQuery(db,"Bank",coinID);
//}
//
//    @Override
//    public void processResultFromFireStore(List<DocumentSnapshot> list, boolean notnull){
//        if(!notnull){
//            Map<String, Object> user = new HashMap<>();
//            user.put("id",coinID);
//            db.collection("user:"+email)
//                    .document("Coinz")
//                    .collection("Bank")
//                    .document(coinID).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void avoid) {
//                    Log.d(tag,"Sign up was successful");
//                    Toast.makeText(getApplicationContext(),
//                            "Coins was exchanged", Toast.LENGTH_SHORT).show();
//
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d(tag, "Error adding document", e);
//                    Toast.makeText(getApplicationContext(),
//                            e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//            }
//            else{
//            Toast.makeText(getApplicationContext(),
//                   "This coin was already exchanged" , Toast.LENGTH_SHORT).show();
//        }
//    }
//
//}