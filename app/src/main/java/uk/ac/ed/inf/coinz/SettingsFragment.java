package uk.ac.ed.inf.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    String email = new CurrentUser().getEmail();
    private TextView textViewName;
    private TextView textViewSurname;
    private String tag = "SettingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_settings, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        textViewName = view.findViewById(R.id.textViewNameSettings);
        textViewSurname = view.findViewById(R.id.textViewSurnameSettings);
        textViewSurname.setEnabled(false);
        textViewSurname.setInputType(InputType.TYPE_NULL);
        textViewName.setEnabled(false);
        textViewName.setInputType(InputType.TYPE_NULL);
        view.findViewById(R.id.buttonLogout).setOnClickListener(this);

        db.collection("user:" + email).document("User profile")
                .get().addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
            if (documentSnapshot.exists()) {
                Object nameObj = documentSnapshot.get("name");
                Object surnameObj = documentSnapshot.get("surname");

                if (nameObj != null && surnameObj != null) {
                    textViewName.setText(nameObj.toString());
                    textViewSurname.setText(surnameObj.toString());
                } else {
                    Log.d(tag, "[onViewCreated] method " +
                            "'toString' produced NullPointException " +
                            "at documentSnapshot.get.toString");
                }
            } else {
                Log.d(tag, "Error on loading user's Name and Surname");
            }


        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLogout:
                if (getActivity() != null) {
                    getActivity().finish();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                } else {
                    Log.d(tag, "[onClick] method " +
                            "'finish' produced NullPointException " +
                            "at getActivity.finish()");
                }
                break;
        }

    }

}


