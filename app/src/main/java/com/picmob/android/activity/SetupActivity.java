package com.picmob.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.picmob.android.R;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.UtilsFunctions;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetupActivity extends BaseActivity {

    @BindView(R.id.spUser)
    Spinner spUser;
    @BindView(R.id.spGroup)
    Spinner spGroup;
    @BindView(R.id.spRole)
    Spinner spRole;
    @BindView(R.id.btnContinue)
    Button btnContinue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Messanger");

        btnContinue.setOnClickListener(v -> {
            if (spUser.getSelectedItemPosition() == 0)
                UtilsFunctions.showToast(SetupActivity.this, AppConstants.shortToast, "Choose username!");
            else if (spGroup.getSelectedItemPosition() == 0)
                UtilsFunctions.showToast(SetupActivity.this, AppConstants.shortToast, "Choose group!");
            else if (spRole.getSelectedItemPosition() == 0)
                UtilsFunctions.showToast(SetupActivity.this, AppConstants.shortToast, "Choose your role!");
            else {
                Intent i = new Intent(SetupActivity.this, MessangerActivity.class);
                i.putExtra(AppConstants.grp, spGroup.getSelectedItem().toString());
                i.putExtra(AppConstants.usr, spUser.getSelectedItem().toString());
                i.putExtra(AppConstants.role, spRole.getSelectedItem().toString());
                i.putExtra(AppConstants.sendGrpMsg,spRole.getSelectedItemPosition());
                startActivity(i);
            }
        });
    }
}
