package it.returntrue.revalue.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.ExternalTokenModel;
import it.returntrue.revalue.events.BusProvider;
import it.returntrue.revalue.events.ExternalLoginEvent;
import it.returntrue.revalue.services.RevalueGcmIntentService;
import it.returntrue.revalue.ui.base.BaseActivity;
import it.returntrue.revalue.utilities.NetworkUtilities;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 9001;

    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Bind(R.id.button_google_sign_in) Button mButtonGoogleSignIn;
    @Bind(R.id.button_facebook_sign_in) Button mButtonFacebookSignIn;
    @Bind(R.id.label_status) TextView mLabelStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup available providers
        setupFacebook();
        setupGoogle();

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
        switch (v.getId()) {
            case R.id.button_google_sign_in:
                googleLogin();
                break;
            case R.id.button_facebook_sign_in:
                facebookLogin();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            handleSignInResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data));
        }

        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        // Prevents from going back to MainActivity
        moveTaskToBack(true);
    }

    @Subscribe
    public void onExternalLoginSuccess(ExternalLoginEvent.OnSuccess onSuccess) {
        // Authenticate the user
        mSessionPreferences.login(
                onSuccess.getTokenModel().getUserId(),
                onSuccess.getTokenModel().getUsername(),
                onSuccess.getTokenModel().getAccessToken(),
                onSuccess.getTokenModel().getAlias(),
                onSuccess.getTokenModel().getAvatar());

        // Registers GCM to send or retrieve messages
        startService(new Intent(LoginActivity.this, RevalueGcmIntentService.class));

        // Starts main activity
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();

        // Closes progress dialog
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }

    @Subscribe
    public void onExternalLoginFailure(ExternalLoginEvent.OnFailure onFailure) {
        // Displays error status
        mLabelStatus.setText(onFailure.getMessage());

        // Closes progress dialog
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }

    private void setupFacebook() {
        // Creates and registers Facebook callback manager
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallbacks());
    }

    private void setupGoogle() {
        // Creates Google API Client
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleCallbacks())
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
    }

    private void googleLogin() {
        if (checkInternetConnection()) {
            // Starts Google login process
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    private void facebookLogin() {
        if (checkInternetConnection()) {
            // Starts Facebook login process
            LoginManager.getInstance().logInWithReadPermissions(this,
                    Arrays.asList("public_profile", "email"));
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            mProgressDialog = ProgressDialog.show(this,
                    getString(R.string.loading), getString(R.string.authenticating_user));
            GoogleSignInAccount account = result.getSignInAccount();
            login(getString(R.string.provider_google), account.getIdToken());
        } else {
            mLabelStatus.setText(R.string.login_failed);
        }
    }

    private void login(String provider, String token) {
        if (checkInternetConnection()) {
            // Creates external token
            ExternalTokenModel externalTokenModel = new ExternalTokenModel();
            externalTokenModel.Provider = provider;
            externalTokenModel.Token = token;

            // Login user
            BusProvider.bus().post(new ExternalLoginEvent.OnStart(externalTokenModel));
        }
    }

    private boolean checkInternetConnection() {
        if (!NetworkUtilities.checkInternetConnection(this)) {
            mLabelStatus.setText(R.string.check_connection);
            return false;
        }

        return true;
    }

    private class FacebookCallbacks implements FacebookCallback<LoginResult> {
        @Override
        public void onSuccess(LoginResult loginResult) {
            mProgressDialog = ProgressDialog.show(LoginActivity.this,
                    getString(R.string.loading), getString(R.string.authenticating_user));
            login(getString(R.string.provider_facebook), loginResult.getAccessToken().getToken());
        }

        @Override
        public void onCancel() {
            mLabelStatus.setText(R.string.login_canceled);
        }

        @Override
        public void onError(FacebookException error) {
            mLabelStatus.setText(R.string.login_failed);
        }
    }

    private class GoogleCallbacks implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            mLabelStatus.setText(R.string.login_failed);
        }
    }
}