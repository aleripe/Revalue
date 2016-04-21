package it.returntrue.revalue.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.preferences.SessionPreferences;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private SessionPreferences mSessionPreferences;

    @Bind(R.id.button_google_sign_in)Button mButtonGoogleSignIn;
    @Bind(R.id.button_facebook_sign_in)Button mButtonFacebookSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates session preferences manager
        mSessionPreferences = new SessionPreferences(this);

        // Sets layout
        setContentView(R.layout.activity_login);

        // Binds controls
        ButterKnife.bind(this);

        // Sets listeners
        mButtonGoogleSignIn.setOnClickListener(this);
        mButtonFacebookSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mSessionPreferences.setToken("DUMMY");

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}