package com.picmob.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.picmob.android.R;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.friends.FriendsPojo;
import com.picmob.android.mvvm.login.LoginViewModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.xwray.passwordview.PasswordView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.picmob.android.utils.ExceptionHandler;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.etUsrName)
    EditText etUsrName;
    @BindView(R.id.etPswrd)
    PasswordView etPswrd;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.btnFbLogin)
    LoginButton btnFbLogin;
    @BindView(R.id.btnGLogin)
    SignInButton btnGLogin;
    @BindView(R.id.tvForgetPswrd)
    TextView tvForgetPswrd;

    private UtilsFunctions utilsFunctions;

    private LoginViewModel loginViewModel;
    private static final String TAG = "LoginActivity";

    private static final String EMAIL = "email";
    private static final String PROFILE = "public_profile";
    private static final String AUTH_TYPE = "rerequest";
    private FirebaseAuth mAuth;

    private String email, usrId, firstName, lastName, photoUrl, token, zip, usrName;

    private FirebaseUser currentUser;

    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    private int loginOption = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        boolean login = CustomSharedPreference.getInstance(this).getBoolean(AppConstants.LOGIN);
        if (login) {
            Intent intent = new Intent(this, HomeActivity.class);
            if (getIntent().hasExtra(AppConstants.TYPE)) {
                int type = Integer.parseInt(getIntent().getStringExtra(AppConstants.TYPE));
                if (type == AppConstants.REQUEST || type == AppConstants.RESPOND) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(AppConstants.TAB, 2);
                } else if (type == AppConstants.NEW_MESSAGE) {
                    FriendsPojo pojo = new FriendsPojo();
                    pojo.setId(Integer.parseInt(getIntent().getStringExtra(AppConstants.USER_ID)));
                    pojo.setUsername(getIntent().getStringExtra(AppConstants.USERNAME));
                    intent = new Intent(this, DmActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(AppConstants.MESSAGE_DETAILS, new Gson().toJson(pojo));
                }
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            startActivity(intent);
            finish();
        }

        utilsFunctions = new UtilsFunctions();
        mAuth = FirebaseAuth.getInstance();

        UtilsFunctions.isNetworkAvail(LoginActivity.this);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        if (UtilsFunctions.isNetworkAvail(LoginActivity.this)) {
            initFacebook();
            initGoogle();
        }

        if (getIntent().hasExtra(AppConstants.LOGOUT)) {
            if (currentUser != null)
                mAuth.signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.e(TAG, "onComplete: " + task.isSuccessful());
                }
            });

            LoginManager.getInstance().logOut();
        }
    }

    private void initGoogle() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("246964316341-ivlqegitmgf1t2mbg5ieph54o83e63ec.apps.googleusercontent.com")
//                 getResources().getString(R.string.default_web_client_id)
//        getString(R.string.default_web_client_id)
//
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @OnClick(R.id.btnGLogin)
    void googleLogin() {
        if (UtilsFunctions.isNetworkAvail(LoginActivity.this)) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, AppConstants.GOOGLE_SIGN_IN);
        }
    }

    @OnClick(R.id.tvForgetPswrd)
    void forgetPassword() {
        if (UtilsFunctions.isNetworkAvail(LoginActivity.this)) {
            Intent i = new Intent(this, VerificationAndResetPasswordActivity.class);
            i.putExtra(AppConstants.FORGET_PASSWORD, AppConstants.FORGET_PASSWORD);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        }
    }

    @OnClick(R.id.tvReg)
    void Register() {
        if (UtilsFunctions.isNetworkAvail(LoginActivity.this))
            startActivity(new Intent(this, RegistrationActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }


    private void initFacebook() {
        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
/*
        if (!loggedOut) {

            Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName()+" profile username==>"+Profile.getCurrentProfile().getName());
            //Using Graph API
            getUserProfile(AccessToken.getCurrentAccessToken());
        }

        AccessTokenTracker fbTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                if (accessToken2 == null) {
                    Toast.makeText(getApplicationContext(),"User Logged Out.",Toast.LENGTH_LONG).show();
                }
            }
        };
        fbTracker.startTracking();*/

        mCallbackManager = CallbackManager.Factory.create();
        btnFbLogin.setPermissions(Arrays.asList(PROFILE, EMAIL));
        btnFbLogin.setAuthType(AUTH_TYPE);

        btnFbLogin.registerCallback(
                mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

                        Log.e(TAG, "onSuccess: " + new Gson().toJson(loginResult.getAccessToken()));

                        token = loginResult.getAccessToken().getToken();

                        if (!loggedOut) {
                            getUserProfile(AccessToken.getCurrentAccessToken());
                        }
                    }

                    @Override
                    public void onCancel() {
                        setResult(RESULT_CANCELED);
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.e(TAG, "onError: " + e.getLocalizedMessage());
                    }
                });

    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("TAG", object.toString());
                        try {
                            firstName = object.getString("first_name");
                            lastName = object.getString("last_name");
                            email = object.getString("email");
                            usrId = object.getString("id");
                            photoUrl = "https://graph.facebook.com/" + usrId + "/picture?type=normal";
                            disconnectFromFacebook();
                            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
                            map.put(AppConstant.FB_TOKEN, token);
                            map.put(AppConstant.FB_ID, usrId);
                            if (UtilsFunctions.isNetworkAvail(LoginActivity.this))
                                loginSocial(map);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Log.e(TAG, "handleFacebookAccessToken:" + currentAccessToken);


        AuthCredential credential = FacebookAuthProvider.getCredential(currentAccessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
                            currentUser = mAuth.getCurrentUser();
                            Log.e(TAG, "onComplete: " + currentUser.getEmail() + " ==>" + currentUser.getDisplayName() + "==>" + currentUser.getUid());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                new GraphRequest
                        .Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        LoginManager.getInstance().logOut();
                    }
                })
                .executeAsync();
    }

    @OnClick(R.id.btnLogin)
    void login() {

        if (etUsrName.getText().toString() == null || etUsrName.getText().toString().length() < 1)
            UtilsFunctions.showToast(this, AppConstants.shortToast, "Invalid username!");
        else if (etPswrd.getText().toString() == null || etPswrd.getText().toString().length() < 1)
            UtilsFunctions.showToast(this, AppConstants.shortToast, "Invalid password!");
        else {
            LinkedTreeMap<String, Object> loginObj = new LinkedTreeMap<>();
            loginObj.put(AppConstant.USR_NAME, etUsrName.getText().toString());
            loginObj.put(AppConstant.PSWRD, etPswrd.getText().toString());

            utilsFunctions.showDialog(this);
            if (UtilsFunctions.isNetworkAvail(LoginActivity.this)) {

                loginViewModel.loginUser(loginObj);

                LogCapture.e(TAG, "login: " + new Gson().toJson(loginObj));

                loginViewModel.getLoginData().observe(this, userLoginObserver);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showRegisterBottomsheet(String usr, String zip) {
        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_register, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        BottomSheetBehavior bottomSheetBehavior =
                BottomSheetBehavior.from((View) (dialogView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setFitToContents(true);

        EditText etUsrName = dialogView.findViewById(R.id.etUsrName);
        EditText etZip = dialogView.findViewById(R.id.etZip);
        Button btnReg = dialogView.findViewById(R.id.btnReg);


        if (usr != null && zip != null) {
            etUsrName.setText(usr);
            etUsrName.setError("Enter any other username");
            etZip.setText(zip);
        }


        etZip.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!UtilsFunctions.checkUserName(etUsrName.getText().toString()))
                        UtilsFunctions.showToast(LoginActivity.this, AppConstants.shortToast, getString(R.string.invalid_usrname));
                    else if (!UtilsFunctions.checkPhone(etZip.getText().toString()))
                        UtilsFunctions.showToast(LoginActivity.this, AppConstants.shortToast, getString(R.string.invalid_zip));
                    else {
                        addValues(etZip, etUsrName);
                        dialog.dismiss();
                        utilsFunctions.showDialog(LoginActivity.this);
                    }
                    return true;
                }
                return false;
            }
        });


        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UtilsFunctions.checkUserName(etUsrName.getText().toString()))
                    UtilsFunctions.showToast(LoginActivity.this, AppConstants.shortToast, getString(R.string.invalid_usrname));
                else if (!UtilsFunctions.checkPhone(etZip.getText().toString()))
                    UtilsFunctions.showToast(LoginActivity.this, AppConstants.shortToast, getString(R.string.invalid_zip));
                else {
                    addValues(etZip, etUsrName);
                    dialog.dismiss();
                    utilsFunctions.showDialog(LoginActivity.this);
                }
            }
        });

        dialog.show();
    }

    private void addValues(EditText etZip, EditText etUsrName) {
        zip = etZip.getText().toString();
        usrName = etUsrName.getText().toString();

        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
        map.put(AppConstant.F_NAME, firstName);
        map.put(AppConstant.L_NAME, lastName);
        map.put(AppConstant.EMAIL, email);
        if (loginOption == 1) {
            map.put(AppConstant.GOOGLE_TOKEN, token);
            map.put(AppConstant.GOOGLE_ID, usrId);
        } else {
            map.put(AppConstant.FB_TOKEN, token);
            map.put(AppConstant.FB_ID, usrId);
        }
        map.put(AppConstant.USR_NAME, usrName);
        map.put(AppConstant.LOCATION, zip);

        Log.e(TAG, "onEditorAction: " + new Gson().toJson(map));
        if (UtilsFunctions.isNetworkAvail(LoginActivity.this))
            registerSocial(map);
    }

    private void registerSocial(LinkedTreeMap<String, Object> map) {

        Log.e(TAG, "registerSocial: " + new Gson().toJson(map));

        loginViewModel.socialRegister(map);
        loginViewModel.getUserRegisterLiveData().observe(LoginActivity.this, socialRegisterObs);
    }


    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.e(TAG, "handleSignInResult: " + new Gson().toJson(account));

            if (account != null) {
                firstName = account.getGivenName();
                lastName = account.getFamilyName();
                email = account.getEmail();
                usrId = account.getId();
                photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
                token = account.getIdToken();

                LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
                map.put(AppConstant.GOOGLE_ID, usrId);
                map.put(AppConstant.GOOGLE_TOKEN, token);
                loginOption = 1;
                if (UtilsFunctions.isNetworkAvail(LoginActivity.this))
                    loginSocial(map);
                addFirebaseAuth(token);
            }

        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
        }

    }

    private void loginSocial(LinkedTreeMap<String, Object> map) {
        utilsFunctions.showDialog(LoginActivity.this);
        loginViewModel.loginSocial(map);
        loginViewModel.getUsercheckLiveData().observe(LoginActivity.this, socialLoginObserver);
    }

    private void addFirebaseAuth(String token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            currentUser = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        Log.e(TAG, "onStart: " + currentUser);

//        updateUI(currentUser);
    }


    Observer<Resource<UserAuthPojo>> socialLoginObserver = apiResponseModelResource -> {
        switch (apiResponseModelResource.status) {
            case ERROR:
                utilsFunctions.hideDialog();
                if (apiResponseModelResource.message.contains("User not found"))
                    showRegisterBottomsheet(null, null);
                break;
            case SUCCESS:
                utilsFunctions.hideDialog();
                LogCapture.e(TAG, "login: " + apiResponseModelResource.data.getToken());
                CustomSharedPreference.getInstance(this).putBoolean(AppConstants.LOGIN, true);
                CustomSharedPreference.getInstance(this).putString(AppConstants.USR_DETAIL, new Gson().toJson(apiResponseModelResource.data));
                Intent i = new Intent(this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                break;
        }
    };


    Observer<Resource<UserAuthPojo>> userLoginObserver = userAuthPojoResource -> {

        switch (userAuthPojoResource.status) {
            case ERROR:
                utilsFunctions.hideDialog();
                UtilsFunctions.showToast(this, AppConstants.shortToast, userAuthPojoResource.message);
                break;
            case SUCCESS:
                utilsFunctions.hideDialog();


                LogCapture.e(TAG, "login: " + userAuthPojoResource.data.getToken());
                CustomSharedPreference.getInstance(this).putBoolean(AppConstants.LOGIN, true);
                CustomSharedPreference.getInstance(this).putString(AppConstants.USR_DETAIL, new Gson().toJson(userAuthPojoResource.data));
                Intent i = new Intent(this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                break;
        }
    };


    Observer<Resource<UserAuthPojo>> socialRegisterObs = userAuthPojoResource -> {

        switch (userAuthPojoResource.status) {
            case ERROR:
                utilsFunctions.hideDialog();

                String msg = userAuthPojoResource.message;

                UtilsFunctions.showToast(this, AppConstants.shortToast, userAuthPojoResource.message);

                if (msg.contains("already taken")) {
                    showRegisterBottomsheet(usrName, zip);
                }
                break;
            case SUCCESS:
                utilsFunctions.hideDialog();

                LogCapture.e(TAG, "login: " + userAuthPojoResource.data.getToken());
                CustomSharedPreference.getInstance(this).putBoolean(AppConstants.LOGIN, true);
                CustomSharedPreference.getInstance(this).putString(AppConstants.USR_DETAIL, new Gson().toJson(userAuthPojoResource.data));
                Intent i = new Intent(this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                break;
        }
    };
}
