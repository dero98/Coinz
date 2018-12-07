package uk.ac.ed.inf.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment implements  View.OnClickListener {
    String email = new CurrentUser().getEmail();
    private TextView textViewName;
    private TextView textViewSurname;
    private FirebaseFirestore db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_settings, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        db = new MainActivity().db;
        textViewName = (TextView) view.findViewById(R.id.textViewNameSettings);
        textViewSurname = (TextView) view.findViewById(R.id.textViewSurnameSettings);
        textViewSurname.setEnabled(false);
        textViewSurname.setInputType(InputType.TYPE_NULL);
        textViewName.setEnabled(false);
        textViewName.setInputType(InputType.TYPE_NULL);
        view.findViewById(R.id.buttonLogout).setOnClickListener(this);

        db.collection("user:" + email).document("User profile")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    textViewName.setText(documentSnapshot.get("name").toString());
                    textViewSurname.setText(documentSnapshot.get("surname").toString());
                }else{

                }
            }


        });
    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.buttonLogout:
                getActivity().finish();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                break;
    }

}

}


