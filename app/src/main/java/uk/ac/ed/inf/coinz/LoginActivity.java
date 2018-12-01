package uk.ac.ed.inf.coinz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;

    EditText editTextEmail, editTexPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextEmail=(EditText)  findViewById(R.id.EditTextEmail);
        editTexPassword=(EditText) findViewById(R.id.EditTextPassword);
        mAuth = FirebaseAuth.getInstance();
       findViewById(R.id.ButtonSignUp).setOnClickListener(this);

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    //    updateUI(currentUser);
    }


    @Override
    public void onClick(View veiw) {
        switch(veiw.getId()){
            case R.id.ButtonSignUp:
                registerUser();
                break;
        } }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTexPassword.getText().toString().trim();
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTexPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTexPassword.setError("Minimum length of password should be 6");
            editTexPassword.requestFocus();
            return;

        }

        if (password.isEmpty()) {
            editTexPassword.setError("Password is required");
            editTexPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"User Registered Successfull",
                                Toast.LENGTH_SHORT).show();
                    }
                    }

                });

    }
}
