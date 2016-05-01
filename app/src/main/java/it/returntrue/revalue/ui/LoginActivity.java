package it.returntrue.revalue.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.returntrue.revalue.R;
import it.returntrue.revalue.api.ExternalTokenModel;
import it.returntrue.revalue.api.RevalueService;
import it.returntrue.revalue.api.TokenModel;
import it.returntrue.revalue.preferences.SessionPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 9001;

    private final List<String> facebookPermissions = Arrays.asList("public_profile", "email");
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2/Revalue.Api/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private SessionPreferences mSessionPreferences;
    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Bind(R.id.button_google_sign_in) Button mButtonGoogleSignIn;
    @Bind(R.id.button_facebook_sign_in) Button mButtonFacebookSignIn;
    @Bind(R.id.label_sign_in_status) TextView mLabelSignInStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates session preferences manager
        mSessionPreferences = new SessionPreferences(this);

        // Creates and registers Facebook callback manager
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallbacks());

        // Creates Google API Client
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleCallbacks())
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

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

    private void googleLogin() {
        // Starts Google login process
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void facebookLogin() {
        // Starts Facebook login process
        LoginManager.getInstance().logInWithReadPermissions(this, facebookPermissions);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            mProgressDialog = ProgressDialog.show(this, "Loading", "Authenticating user...");
            GoogleSignInAccount account = result.getSignInAccount();
            login(getString(R.string.provider_google), account.getIdToken());
        } else {
            mLabelSignInStatus.setText(getString(R.string.login_failed));
        }
    }

    private void login(String provider, String token) {
        // Creates external token
        ExternalTokenModel externalTokenModel = new ExternalTokenModel();
        externalTokenModel.Provider = provider;
        externalTokenModel.Token = token;

        // Calls API to log user
        RevalueService service = retrofit.create(RevalueService.class);
        Call<TokenModel> call = service.ExternalLogin(externalTokenModel);
        call.clone().enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                // Response is OK, let's authenticate the user
                if (response != null) {
                    TokenModel tokenModel = response.body();

                    if (tokenModel != null) {
                        mSessionPreferences.login(tokenModel.getUserName(),
                                tokenModel.getAccessToken(), tokenModel.getAlias(),
                                tokenModel.getAvatar());

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else {
                        mLabelSignInStatus.setText(getString(R.string.token_unavailable));
                    }
                }
                else {
                    mLabelSignInStatus.setText(getString(R.string.response_null));
                }

                if (mProgressDialog != null) mProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<TokenModel> call, Throwable t) {
                mLabelSignInStatus.setText(getString(R.string.call_failed));
                if (mProgressDialog != null) mProgressDialog.dismiss();
            }
        });
    }

    private class FacebookCallbacks implements FacebookCallback<LoginResult> {
        @Override
        public void onSuccess(LoginResult loginResult) {
            mProgressDialog = ProgressDialog.show(LoginActivity.this, "Loading", "Authenticating user...");
            login(getString(R.string.provider_facebook), loginResult.getAccessToken().getToken());
        }

        @Override
        public void onCancel() {
            mLabelSignInStatus.setText(getString(R.string.login_canceled));
        }

        @Override
        public void onError(FacebookException error) {
            mLabelSignInStatus.setText(getString(R.string.login_failed));
        }
    }

    private class GoogleCallbacks implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            mLabelSignInStatus.setText(getString(R.string.login_failed));
        }
    }
}