package uk.ac.ed.inf.coinz;

//public class CoinFragment extends Fragment implements View.OnClickListener,DownloadResponseFromFireStore  {
//    private TextView textViewCoin;
//    private Button buttonSpareChange, buttonExchangeBank;
//    private String coinID;
//    private String CoinInformation ;
//    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final String email=new CurrentUser().getEmail();
//    private String tag="CoinActivity";
//
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return getLayoutInflater().inflate(R.layout.fragment_wallet, container, false);
//
//    }
//
//    @Override
//    public void onViewCreated(View view,Bundle savedInstanceState){
//        super.onViewCreated(view, savedInstanceState);
//        coinID="bla";
//        CoinInformation="bla";
//        textViewCoin = (TextView) view.findViewById(R.id.textViewCoin);
//        view.findViewById(R.id.buttonSpareChange).setOnClickListener(this);
//        view.findViewById(R.id.buttonExchangeBank).setOnClickListener(this);
//        textViewCoin.setEnabled(false);
//        textViewCoin.setInputType(InputType.TYPE_NULL);
//        textViewCoin.setText(CoinInformation);
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.buttonExchangeBank:
//                ExchangeCoin();
//                Fragment fragment = new CoinFragment();
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.fragment_container, fragment);
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();
//                break;
//        }
//    }
//
//    private void ExchangeCoin(){
//        DownloadFromFireStore dowFs= new DownloadFromFireStore();
//        dowFs.listener=this;
//        dowFs.doInBackgroundQuery(db,"Bank",coinID);
//    }
//
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
//                    Toast.makeText(getContext(),
//                            "Coins was exchanged", Toast.LENGTH_SHORT).show();
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                    Log.d(tag, "Error adding document", e);
//                    Toast.makeText(getContext(),
//                            e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//        else{
//            Toast.makeText(getContext(),
//                    "This coin was already exchanged" , Toast.LENGTH_SHORT).show();
//        }
//    }
//
//}
