package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BankFragment extends Fragment {
    private TextView textViewSHIL,textViewPENY,textViewQUID,textViewDOLR;
    private final String preferencesFile = "MyPrefsFile";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_bank, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        textViewSHIL= (TextView) view.findViewById(R.id.textViewSHIL);
        textViewPENY= (TextView) view.findViewById(R.id.textViewPENY);
        textViewQUID= (TextView) view.findViewById(R.id.textViewQUID);
        textViewDOLR= (TextView) view.findViewById(R.id.textViewDOLR);

        textViewDOLR.setEnabled(false);
        textViewDOLR.setInputType(InputType.TYPE_NULL);

        textViewSHIL.setEnabled(false);
        textViewSHIL.setInputType(InputType.TYPE_NULL);

        textViewQUID.setEnabled(false);
        textViewQUID.setInputType(InputType.TYPE_NULL);

        textViewPENY.setEnabled(false);
        textViewPENY.setInputType(InputType.TYPE_NULL);

        SharedPreferences settings = getContext().getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        String valDOLR= settings.getString("DOLR", "");
        textViewDOLR.setText("DOLR value is: "+ valDOLR);
        String valSHIL =settings.getString("SHIL", "");
        textViewSHIL.setText("SHIL value is: "+valSHIL);

        String valQUID =settings.getString("QUID", "");
        textViewQUID.setText("QUID value is: "+valQUID);

        String valPENY =settings.getString("PENY", "");
        textViewPENY.setText("PENY value is: "+valPENY);

    }
}


