package uk.ac.ed.inf.coinz;

import android.content.Context;
import android.content.SharedPreferences;
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

public class BankFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_bank, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        TextView textViewSHIL, textViewPENY, textViewQUID, textViewDOLR;
        final String preferencesFile = "MyPrefsFile";
        String tag = "BankFragment";
        textViewSHIL = view.findViewById(R.id.textViewSHIL);
        textViewPENY = view.findViewById(R.id.textViewPENY);
        textViewQUID = view.findViewById(R.id.textViewQUID);
        textViewDOLR = view.findViewById(R.id.textViewDOLR);

        textViewDOLR.setEnabled(false);
        textViewDOLR.setInputType(InputType.TYPE_NULL);

        textViewSHIL.setEnabled(false);
        textViewSHIL.setInputType(InputType.TYPE_NULL);

        textViewQUID.setEnabled(false);
        textViewQUID.setInputType(InputType.TYPE_NULL);

        textViewPENY.setEnabled(false);
        textViewPENY.setInputType(InputType.TYPE_NULL);
        if (getContext() != null) {

            SharedPreferences settings = getContext().getSharedPreferences(preferencesFile,
                    Context.MODE_PRIVATE);
            String valDOLR = settings.getString("DOLR", "");
            String textDOLR = getString(R.string.dolrVal) + valDOLR;
            textViewDOLR.setText(textDOLR);

            String valSHIL = settings.getString("SHIL", "");
            String textSHIL = getString(R.string.shilVal) + valSHIL;
            textViewSHIL.setText(textSHIL);

            String valQUID = settings.getString("QUID", "");
            String textQUID = getString(R.string.quidVal) + valQUID;
            textViewQUID.setText(textQUID);

            String valPENY = settings.getString("PENY", "");
            String textPENY = getString(R.string.quidVal) + valPENY;
            textViewPENY.setText(textPENY);
        } else {
            Log.d(tag, "[onViewCreated] method 'getContext'produced " +
                    "NullPointException at getContext()." +
                    "getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)");
        }
    }
}


