package com.picmob.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.cm.pc.Country;
import com.cm.pc.CountryPicker;
import com.cm.pc.listeners.OnCountryPickerListener;
import com.google.gson.Gson;
import com.picmob.android.R;

import java.util.Objects;

import com.picmob.android.mvvm.registration.RegisterViewModel;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.General;
import com.picmob.android.utils.UtilsFunctions;
import com.google.gson.internal.LinkedTreeMap;
import com.xwray.passwordview.PasswordView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.picmob.android.utils.ExceptionHandler;

public class RegistrationActivity extends AppCompatActivity {

    @BindView(R.id.etUsrName)
    EditText etUsrName;
    @BindView(R.id.etFname)
    EditText etFname;
    @BindView(R.id.etLname)
    EditText etLname;
    @BindView(R.id.etMail)
    EditText etMail;
    @BindView(R.id.etPhone)
    EditText etPhone;
    @BindView(R.id.etZip)
    EditText etZip;
    @BindView(R.id.etPswrd)
    PasswordView etPswrd;
    @BindView(R.id.etCpswrd)
    PasswordView etCpswrd;
    @BindView(R.id.layCC)
    LinearLayout layCC;
    @BindView(R.id.tvCC)
    TextView tvCC;
//    @BindView(R.id.spCountryCode)
//    CountryCodePicker spCountryCode;


    private RegisterViewModel registerViewModel;
    private UtilsFunctions utilsFunctions;
    private static final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        ButterKnife.bind(this);
        SpannableString spannableString = new SpannableString("Registration");
        General general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        utilsFunctions = new UtilsFunctions();
        registerViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);
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

    @OnClick(R.id.btnReg)
    void register() {

        if (!UtilsFunctions.checkUserName(etUsrName.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_usrname));
        else if (!UtilsFunctions.checkName(etFname.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Fname));
        else if (!UtilsFunctions.checkName(etLname.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Lname));
        else if (!UtilsFunctions.checkEmail(etMail.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Email));
        else if (!UtilsFunctions.checkPhone(etPhone.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Phone));
        else if (!UtilsFunctions.checkPhone(etZip.getText().toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_zip));
        else if (!UtilsFunctions.checkPswrd(Objects.requireNonNull(etPswrd.getText()).toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_pswrd));
        else if (!UtilsFunctions.checkCpswrd(etPswrd.getText().toString(), Objects.requireNonNull(etCpswrd.getText()).toString()))
            UtilsFunctions.showToast(this, AppConstants.shortToast, getString(R.string.invalid_Cpswrd));
        else {
            utilsFunctions.showDialog(this);

            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
            map.put(AppConstant.USR_NAME, etUsrName.getText().toString());
            map.put(AppConstant.PHONE, etPhone.getText().toString());
            map.put(AppConstant.EMAIL, etMail.getText().toString());
            map.put(AppConstant.LOCATION, etZip.getText().toString());

            Log.e(TAG, "register: " + new Gson().toJson(map));

            if (UtilsFunctions.isNetworkAvail(RegistrationActivity.this)) {
                registerViewModel.validateRegister(map);
                registerViewModel.getValidateData().observe(this, registerModelResource -> {

                    switch (registerModelResource.status) {
                        case ERROR:
                            utilsFunctions.hideDialog();
                            UtilsFunctions.showToast(this, AppConstants.longToast, registerModelResource.message);
                            break;
                        case SUCCESS:
                            LinkedTreeMap<String, Object> treeMap = new LinkedTreeMap<>();
                            treeMap.put(AppConstant.USR_NAME, etUsrName.getText().toString());
                            treeMap.put(AppConstant.F_NAME, etFname.getText().toString());
                            treeMap.put(AppConstant.L_NAME, etLname.getText().toString());
                            treeMap.put(AppConstant.PHONE, etPhone.getText().toString());
                            treeMap.put(AppConstant.EMAIL, etMail.getText().toString());
                            treeMap.put(AppConstant.LOCATION, etZip.getText().toString());
                            treeMap.put(AppConstant.PSWRD, etPswrd.getText().toString());
                            treeMap.put(AppConstant.PHONE_CODE, tvCC.getText().toString());

                            utilsFunctions.hideDialog();
                            Intent i = new Intent(this, VerificationAndResetPasswordActivity.class);
                            i.putExtra(AppConstants.REGISTRATION, new Gson().toJson(treeMap));
                            startActivity(i);
                            break;
                    }
                });
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
