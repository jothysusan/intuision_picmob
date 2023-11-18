package com.picmob.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.cm.pc.Country;
import com.cm.pc.CountryPicker;
import com.cm.pc.listeners.OnCountryPickerListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.R;
import com.picmob.android.models.RegisterModel;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.forget_password.ForgetPasswordViewModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.registration.RegisterViewModel;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.xwray.passwordview.PasswordView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.philio.pinentry.PinEntryView;

import com.picmob.android.utils.ExceptionHandler;

public class VerificationAndResetPasswordActivity extends AppCompatActivity {

    @BindView(R.id.layPhone)
    LinearLayout layPhone;
    @BindView(R.id.layCode)
    LinearLayout layCode;
    @BindView(R.id.layPassword)
    LinearLayout layPassword;
    /* @BindView(R.id.spCountryCode)
     CountryCodePicker spCountryCode;*/
    @BindView(R.id.etPhone)
    EditText etPhone;
    @BindView(R.id.pin_entry_view)
    PinEntryView pin_entry_view;
    @BindView(R.id.etPswrd)
    PasswordView etPswrd;
    @BindView(R.id.etCpswrd)
    PasswordView etCpswrd;
    @BindView(R.id.tvText)
    TextView tvText;
    @BindView(R.id.tvEdit)
    TextView tvEdit;
    @BindView(R.id.tvResend)
    TextView tvResend;
    @BindView(R.id.btnOk)
    Button btnOk;
    @BindView(R.id.layCC)
    LinearLayout layCC;
    @BindView(R.id.tvCC)
    TextView tvCC;

    private static final String TAG = "VerificationAndResetPas";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_FORGET_PASSWORD = 0;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    private FirebaseAuth mAuth;
    private static int STATE;
    private static boolean REGISTER = false;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private RegisterModel regModel;
    private UtilsFunctions utilsFunctions;
    private General general;
    private String phoneNo;

    private RegisterViewModel regViewModel;
    private ForgetPasswordViewModel pswrdViewModel;

    private UserAuthPojo userAuthPojo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        mAuth = FirebaseAuth.getInstance();
        utilsFunctions = new UtilsFunctions();

        general = new General(this);

        initiateToolbar(null);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        regViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);
        pswrdViewModel = ViewModelProviders.of(this).get(ForgetPasswordViewModel.class);


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                mVerificationId = verificationId;
                mResendToken = forceResendingToken;
                updateUI(STATE_CODE_SENT, mAuth.getCurrentUser(), null);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                String sb = "onVerificationCompleted:" +
                        phoneAuthCredential;
                LogCapture.e(TAG, sb);
                mVerificationInProgress = false;
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException firebaseException) {
                String sb = "onVerificationFailed==>" +
                        firebaseException;
                LogCapture.e(TAG, sb);
                mVerificationInProgress = false;
                if (firebaseException instanceof FirebaseAuthInvalidCredentialsException) {
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(VerificationAndResetPasswordActivity.this,
                            AppConstants.shortToast, "Invalid phone number!");
                } else if (firebaseException instanceof FirebaseTooManyRequestsException) {
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(VerificationAndResetPasswordActivity.this,
                            AppConstants.shortToast, "Quota exceeded!");
                }
                updateUI(STATE_VERIFY_FAILED, mAuth.getCurrentUser(), null);
            }
        };

        if (getIntent().getExtras().containsKey(AppConstants.REGISTRATION)) {
            regModel = new Gson().fromJson(getIntent().getStringExtra(AppConstants.REGISTRATION), RegisterModel.class);
            REGISTER = true;
            layPassword.setVisibility(View.GONE);
            layPhone.setVisibility(View.GONE);
            sendPhoneNumberVerification(regModel.getPhoneCode() + regModel.getPhoneNumber());
            layCode.setVisibility(View.VISIBLE);
            tvResend.setVisibility(View.INVISIBLE);

            String no = regModel.getPhoneNumber();

            String nos = no.substring(2, no.length() - 2);

            nos = UtilsFunctions.replaceX(nos.length());

            Log.e(TAG, "onCreate: " + nos);

            tvText.setText(getString(R.string.enter_6_digit) + " " + regModel.getPhoneCode() + " - "
                    + no.substring(0, 2) + nos + no.substring(no.length() - 2, no.length()) + " mobile number");

            phoneNo = regModel.getPhoneCode() + regModel.getPhoneNumber();
        }

        if (getIntent().getExtras().containsKey(AppConstants.FORGET_PASSWORD)) {
            updateUI(STATE_FORGET_PASSWORD);
            REGISTER = false;
        }

        etPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                    sendCode();
                    return true;
                }
                return false;
            }
        });

        etCpswrd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                    resetPassword();
                    return true;
                }
                return false;
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                        updateUI(STATE_VERIFY_SUCCESS, null, phoneAuthCredential);
                        updateUI(STATE_SIGNIN_SUCCESS, user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            UtilsFunctions.showToast(this, AppConstants.shortToast, "Invalid code.");
                        }
                        updateUI(STATE_SIGNIN_FAILED);
                    }
                    utilsFunctions.hideDialog();
                });
    }

    @OnClick(R.id.layCC)
    void showCC() {
        CountryPicker countryPicker =
                new CountryPicker.Builder().with(this)
                        .listener(new OnCountryPickerListener() {
                            @Override
                            public void onSelectCountry(Country country) {
                                tvCC.setText(country.getDialCode());
                            }
                        }).style(R.style.Theme_Design_BottomSheetDialog)
                        .canSearch(true)
                        .sortBy(CountryPicker.SORT_BY_NAME)
                        .theme(CountryPicker.THEME_NEW).build();

        countryPicker.showBottomSheet(this);
    }

    @OnClick(R.id.btnOk)
    void doAction() {
        if (btnOk.getTag().toString().contains(AppConstants.BTN_VERIFY)) {
            verifyCode();
        } else if (btnOk.getTag().toString().contains(AppConstants.BTN_SEND)) {
            sendCode();
        } else if (btnOk.getTag().toString().contains(AppConstants.BTN_RESET)) {
            resetPassword();
        }
    }

    private void resetPassword() {
        if (!UtilsFunctions.checkPswrd(Objects.requireNonNull(etPswrd.getText()).toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_pswrd));
        else if (!UtilsFunctions.checkCpswrd(etPswrd.getText().toString(), Objects.requireNonNull(etCpswrd.getText()).toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Cpswrd));
        else {
            LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
            map.put(AppConstant.PSWRD, etPswrd.getText().toString());
            utilsFunctions.showDialog(this);
            Log.e(TAG, "resetPassword: " + new Gson().toJson(userAuthPojo));
            pswrdViewModel.resetPassword(userAuthPojo.getId().toString(), userAuthPojo.getToken(), map);
            pswrdViewModel.getResetPswrdData().observe(this, pswrdResetObs);
        }
    }

    private void verifyCode() {
        if (pin_entry_view.getText().toString().length() == 6) {

            String stng = "buttonResetPassword: " + pin_entry_view.getText().toString();
            LogCapture.e(TAG, stng);
            UtilsFunctions.hideKeyboard(this);
            try {
                verifyPhoneNumberWithCode(mVerificationId, pin_entry_view.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else
            UtilsFunctions.showToast(this, AppConstants.shortToast, "Invalid verification code!");
    }

    private void sendCode() {
        if (UtilsFunctions.checkPhone(etPhone.getText().toString())) {
            utilsFunctions.showDialog(this);
            UtilsFunctions.hideKeyboard(this);
            pswrdViewModel.getUserByPhone(etPhone.getText().toString());
//            phoneNo = spCountryCode.getSelectedCountryCodeWithPlus() + etPhone.getText().toString();
            phoneNo = tvCC.getText().toString() + etPhone.getText().toString();
            pswrdViewModel.getUserLiveData().observe(this, userVerifyObs);
        } else
            UtilsFunctions.showToast(this, AppConstants.shortToast, "Enter valid phone number!");
    }


    @OnClick(R.id.tvEdit)
    void doEdit() {
        if (REGISTER)
            finish();
        else
            updateUI(STATE_FORGET_PASSWORD);
    }

    @OnClick(R.id.tvResend)
    void doResendCode() {
        if (REGISTER)
            resendVerificationCode(regModel.getPhoneCode() + regModel.getPhoneNumber(), mResendToken);
        else
            resendVerificationCode(phoneNo, mResendToken);
    }

    private void verifyPhoneNumberWithCode(@NonNull String str, @NonNull String str2) {
        String sb = "Code=>" + str2;
        LogCapture.e(TAG, sb + " vrify=>" + str);

        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(str, str2));
    }

    private void initiateToolbar(String title) {
        SpannableString spannableString = new SpannableString("Verification");
        if (title != null)
            spannableString = new SpannableString(title);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void sendPhoneNumberVerification(String phoneNumber) {

        LogCapture.e(TAG, "sendPhoneNumberVerification: " + phoneNumber);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        mVerificationInProgress = true;
    }

    private void resendVerificationCode(String str, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(str)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(forceResendingToken)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        utilsFunctions.showDialog(this);
    }


    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }


    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential phoneAuthCredential) {

        switch (uiState) {
            case STATE_FORGET_PASSWORD:
                STATE = STATE_FORGET_PASSWORD;
                layPhone.setVisibility(View.VISIBLE);
                layPassword.setVisibility(View.GONE);
                layCode.setVisibility(View.GONE);
                btnOk.setTag(AppConstants.BTN_SEND);
                btnOk.setText(getString(R.string.send));
                initiateToolbar(getString(R.string.forgot_password));
                break;
            case STATE_INITIALIZED:
                STATE = STATE_INITIALIZED;

                break;
            case STATE_CODE_SENT:
                STATE = STATE_CODE_SENT;
                LogCapture.e(TAG, "updateUI: STATE_CODE_SENT");
                tvResend.setVisibility(View.VISIBLE);
                layPhone.setVisibility(View.GONE);
                layPassword.setVisibility(View.GONE);
                layCode.setVisibility(View.VISIBLE);
                String no = phoneNo;
                String nos = no.substring(2, no.length() - 2);

                nos = UtilsFunctions.replaceX(nos.length());

                Log.e(TAG, "onCreate: " + nos);

                tvText.setText(getString(R.string.enter_6_digit) + " - "
                        + no.substring(0, 2) + nos + no.substring(no.length() - 2, no.length()) + " mobile number");
                btnOk.setTag(AppConstants.BTN_VERIFY);
                btnOk.setText(getString(R.string.verify));
                initiateToolbar(getString(R.string.otp_verification));
                utilsFunctions.hideDialog();
                break;

            case STATE_VERIFY_FAILED:
                LogCapture.e(TAG, "updateUI: STATE_VERIFY_FAILED");
                layCode.setVisibility(View.GONE);
                layPhone.setVisibility(View.VISIBLE);
                layPassword.setVisibility(View.GONE);
                btnOk.setText(R.string.reset_password);
                btnOk.setTag(AppConstants.BTN_RESET);
                UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.verification_failed_message));
                STATE = STATE_VERIFY_FAILED;
                finish();
                break;
            case STATE_VERIFY_SUCCESS:
                STATE = STATE_VERIFY_SUCCESS;
                LogCapture.e(TAG, "updateUI: STATE_VERIFY_SUCCESS");
                if (REGISTER) {
                    if (!(phoneAuthCredential == null || phoneAuthCredential.getSmsCode() == null)) {
                        pin_entry_view.setText(phoneAuthCredential.getSmsCode());
                        utilsFunctions.showDialog(this);
                        regViewModel.registerUser(new Gson().toJsonTree(regModel));
                        regViewModel.getRegData().observe(VerificationAndResetPasswordActivity.this, registerObs);
                    }
                } else {
                    if (!(phoneAuthCredential == null || phoneAuthCredential.getSmsCode() == null)) {
                        pin_entry_view.setText(phoneAuthCredential.getSmsCode());
                    }
                }
                mAuth.signOut();
                break;
            case STATE_SIGNIN_FAILED:
                STATE = STATE_SIGNIN_FAILED;
                LogCapture.e(TAG, "STATE_SIGNIN_FAILED");
                layCode.setVisibility(View.GONE);
                layPhone.setVisibility(View.VISIBLE);
                layPassword.setVisibility(View.GONE);
                btnOk.setText(R.string.reset_password);
                btnOk.setTag(AppConstants.BTN_RESET);
                UtilsFunctions.showToast(this, AppConstants.shortToast, "Invalid verification code!");
                finish();
                break;
            case STATE_SIGNIN_SUCCESS:
                STATE = STATE_SIGNIN_SUCCESS;
                LogCapture.e(TAG, "STATE_SIGNIN_SUCCESS");
                if (REGISTER) {
                    utilsFunctions.showDialog(this);
                    regViewModel.registerUser(new Gson().toJsonTree(regModel));
                    regViewModel.getRegData().observe(VerificationAndResetPasswordActivity.this, registerObs);
                } else {
                    layPhone.setVisibility(View.GONE);
                    layCode.setVisibility(View.GONE);
                    layPassword.setVisibility(View.VISIBLE);
                    btnOk.setTag(AppConstants.BTN_RESET);
                    btnOk.setText(getString(R.string.reset_password));
                    initiateToolbar(getString(R.string.reset_password));
                }
                mAuth.signOut();
                break;
        }
    }


    Observer<Resource<UserAuthPojo>> userVerifyObs = new Observer<Resource<UserAuthPojo>>() {
        @Override
        public void onChanged(Resource<UserAuthPojo> userAuthPojoResource) {
            switch (userAuthPojoResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(VerificationAndResetPasswordActivity.this, AppConstants.shortToast, userAuthPojoResource.message);
                    break;
                case SUCCESS:
                    userAuthPojo = userAuthPojoResource.data;
                    Log.e(TAG, "onChanged: " + new Gson().toJson(userAuthPojo));
                    sendPhoneNumberVerification(phoneNo);
                    break;
            }
        }
    };

    Observer<Resource<ApiResponseModel>> registerObs = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(VerificationAndResetPasswordActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    AppConstants.TRANS = 1;
                    utilsFunctions.hideDialog();
                    Intent i = new Intent(VerificationAndResetPasswordActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    UtilsFunctions.showToast(VerificationAndResetPasswordActivity.this, AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    Log.e(TAG, "onChanged: " + apiResponseModelResource.data);
                    break;
            }
        }
    };

    Observer<Resource<ApiResponseModel>> pswrdResetObs = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(VerificationAndResetPasswordActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    utilsFunctions.hideDialog();
                    AppConstants.TRANS = 1;
                    utilsFunctions.hideDialog();
                    Intent i = new Intent(VerificationAndResetPasswordActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    UtilsFunctions.showToast(VerificationAndResetPasswordActivity.this, AppConstants.shortToast,
                            userAuthPojo.getUsername() + "'s " + apiResponseModelResource.data.getMessage());
                    Log.e(TAG, "onChanged: " + apiResponseModelResource.data);
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            backPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void backPressed() {
        if (REGISTER) {
            finish();
        } else {
            if (STATE == STATE_FORGET_PASSWORD) {
                finish();
            } else {
                updateUI(STATE_FORGET_PASSWORD);
            }
        }
    }


}
