package com.picmob.android.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.R;
import com.picmob.android.fragments.EventsFragment;
import com.picmob.android.fragments.FriendsFragment;
import com.picmob.android.fragments.GalleryFragment;
import com.picmob.android.fragments.HomeFragment;
import com.picmob.android.fragments.RequestFragment;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.fcm.FcmViewModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.General;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity {
    @BindView(R.id.bottom_menu)
    BottomBar bottom_menu;

    private static final String TAG = "HomeActivity";

    private FcmViewModel viewModel;
    private UserAuthPojo pojo;

    public String fcmToken = null;
    private General general;
    private long pressedTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        SpannableString spannableString = new SpannableString("Picmob");
        general = new General(this);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if (UtilsFunctions.isNetworkAvail(this))
            getFcmToken();

        pojo = new Gson().fromJson(CustomSharedPreference.getInstance(this).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);

        viewModel = ViewModelProviders.of(this).get(FcmViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.app_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH));
        }
        if (AppConstants.REQUEST_RESPONSE_UPDATE) {
            switchFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
            bottom_menu.setDefaultTabPosition(2);
        } else if (AppConstants.FEED_UPDATE) {
            switchFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
            bottom_menu.setDefaultTabPosition(0);
        } else if (AppConstants.GALLERY_UPDATE) {
            switchFragment(new GalleryFragment(), GalleryFragment.class.getSimpleName());
            bottom_menu.setDefaultTabPosition(4);
        } else if (AppConstants.DM_UPDATE) {
            switchFragment(new FriendsFragment(), FriendsFragment.class.getSimpleName());
            bottom_menu.setDefaultTabPosition(3);
        } else if (AppConstants.EVENT_LIST_UPDATE) {
            switchFragment(new EventsFragment(), EventsFragment.class.getSimpleName());
            bottom_menu.setDefaultTabPosition(1);
        } else {
            switchFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
            bottom_menu.setDefaultTabPosition(0);
        }

        if (getIntent().getExtras() != null && getIntent().hasExtra(AppConstants.TAB)) {

           /* if (getIntent().getExtras().containsKey(AppConstants.MSG)) {
                switchFragment(new RequestFragment(), RequestFragment.class.getSimpleName());
                bottom_menu.setDefaultTabPosition(2);
            }*/
            int tab = getIntent().getIntExtra(AppConstants.TAB, 0);
            LogCapture.e(TAG, "onCreate: " + tab);
            switchTab(tab);
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        bottom_menu.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                switch (tabId) {
                    case R.id.home:
                        Log.e(TAG, "onItemSelected: " + 0);
                        setScreenTitle("Feeds");
                        switchFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
//                        switchFragment(new DeviceScanFragment(), DeviceScanFragment.class.getSimpleName());
                        break;
                    case R.id.events:
                        Log.e(TAG, "onItemSelected: " + 1);
                        setScreenTitle("Events");
                        switchFragment(new EventsFragment(), EventsFragment.class.getSimpleName());
                        break;
                    case R.id.request:
                        setScreenTitle("Picmob");
                        Log.e(TAG, "onItemSelected: " + 2);
                        switchFragment(new RequestFragment(), RequestFragment.class.getSimpleName());
                        break;
                    case R.id.friends:
                        setScreenTitle("Friends");
                        switchFragment(new FriendsFragment(), FriendsFragment.class.getSimpleName());
                        break;
                    case R.id.gallery:
                        setScreenTitle("Gallery");
                        switchFragment(new GalleryFragment(), GalleryFragment.class.getSimpleName());
                        break;
                }
            }
        });

       /* bottom_menu.setItemSelected(R.id.home,true);

        bottom_menu.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i){
                    case R.id.home:
                        Log.e(TAG, "onItemSelected: "+0 );
                        switchFragment(new HomeFragment(),HomeFragment.class.getSimpleName());
                        break;
                    case R.id.events:
                        Log.e(TAG, "onItemSelected: "+1 );
                        switchFragment(new EventsFragment(),EventsFragment.class.getSimpleName());
                        break;
                    case R.id.request:
                        Log.e(TAG, "onItemSelected: "+2 );
                        switchFragment(new RequestFragment(),RequestFragment.class.getSimpleName());
                        break;
                    case R.id.friends:
                        switchFragment(new FriendsFragment(),FriendsFragment.class.getSimpleName());
                        break;
                    case R.id.gallery:
                        switchFragment(new GalleryFragment(),GalleryFragment.class.getSimpleName());
                        break;
                }
            }
        });*/

        Log.e(TAG, "onCreate: " + (pojo != null ? pojo.getToken() : "Null"));
    }

    private void switchTab(int tab) {
        switch (tab) {
            case 0:
                bottom_menu.setDefaultTabPosition(0);
                setScreenTitle("Feeds");
                switchFragment(new HomeFragment(), HomeFragment.class.getSimpleName());
                break;
            case 1:
                bottom_menu.setDefaultTabPosition(1);
                Log.e(TAG, "onItemSelected: " + 1);
                setScreenTitle("Events");
                switchFragment(new EventsFragment(), EventsFragment.class.getSimpleName());
                break;
            case 2:
                bottom_menu.setDefaultTabPosition(2);
                setScreenTitle("Picmob");
                Log.e(TAG, "onItemSelected: " + 2);
                switchFragment(new RequestFragment(), RequestFragment.class.getSimpleName());
                break;
            case 3:
                bottom_menu.setDefaultTabPosition(3);
                setScreenTitle("Friends");
                switchFragment(new FriendsFragment(), FriendsFragment.class.getSimpleName());
                break;
            case 4:
                bottom_menu.setDefaultTabPosition(4);
                setScreenTitle("Gallery");
                switchFragment(new GalleryFragment(), GalleryFragment.class.getSimpleName());
                break;
        }
    }


    Observer<Resource<ApiResponseModel>> fcmObserver = apiResponseModelResource -> {
        switch (apiResponseModelResource.status) {
            case ERROR:
                UtilsFunctions.showToast(HomeActivity.this, AppConstants.shortToast, apiResponseModelResource.message);
                break;
            case SUCCESS:
                Log.e(TAG, "notification token update status :" + apiResponseModelResource.data.getMessage());
                break;
        }
    };


    private void setScreenTitle(String title) {
        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
    }

    private void getFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        fcmToken = task.getResult();

                        if (fcmToken != null) {
                            LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
                            map.put(AppConstant.NOTIFY_TOKEN, fcmToken);

                            if (UtilsFunctions.isNetworkAvail(HomeActivity.this)) {
                                viewModel.updateFcmToken(pojo.getToken(), pojo.getId().toString(), map);
                                viewModel.getFcmData().observe(HomeActivity.this, fcmObserver);
                            }
                            Log.e(TAG, "onCreate: FcmToken=>" + fcmToken);
                        }
                        String msg = getString(R.string.msg_token_fmt, fcmToken);
                        Log.e(TAG, msg);
                    }
                });
    }

    private void switchFragment(Fragment fragment, String tagFragmentName) {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Fragment currentFragment = mFragmentManager.getPrimaryNavigationFragment();
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }
        Fragment fragmentTemp = mFragmentManager.findFragmentByTag(tagFragmentName);

        if (AppConstants.REQUEST_RESPONSE_UPDATE && tagFragmentName.equals(RequestFragment.class.getSimpleName())) {
            fragmentTemp = null;
            AppConstants.REQUEST_RESPONSE_UPDATE = false;
        }
        if (AppConstants.FEED_UPDATE && tagFragmentName.equals(HomeFragment.class.getSimpleName())) {
//            HomeFragment.class.getSimpleName()
            fragmentTemp = null;
            AppConstants.FEED_UPDATE = false;
        }
        if (AppConstants.GALLERY_UPDATE && tagFragmentName.equals(GalleryFragment.class.getSimpleName())) {
            fragmentTemp = null;
            AppConstants.GALLERY_UPDATE = false;
        }
        if (AppConstants.DM_UPDATE && tagFragmentName.equals(FriendsFragment.class.getSimpleName())) {
            fragmentTemp = null;
            AppConstants.DM_UPDATE = false;
        }
        if (AppConstants.EVENT_LIST_UPDATE && tagFragmentName.equals(EventsFragment.class.getSimpleName())) {
            fragmentTemp = null;
            AppConstants.EVENT_LIST_UPDATE = false;
        }
        if (fragmentTemp == null) {
            LogCapture.e(TAG, "switchFragment: null");
            fragmentTemp = fragment;
            fragmentTransaction.add(R.id.fragmentContainer, fragmentTemp, tagFragmentName);
        } else {
            LogCapture.e(TAG, "switchFragment: " + fragmentTemp.getTag());
            fragmentTransaction.show(fragmentTemp);
            fragmentTransaction.detach(fragmentTemp);
            fragmentTransaction.attach(fragmentTemp);
        }
        fragmentTransaction.setPrimaryNavigationFragment(fragmentTemp);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.logout:
                FirebaseMessaging.getInstance().deleteToken();
                CustomSharedPreference.getInstance(this).clear();
                FirebaseMessaging.getInstance().deleteToken();
                stopService(new Intent(getApplicationContext(), LocationService.class));
                Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                i.putExtra(AppConstants.LOGOUT, AppConstants.LOGOUT);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getExtras() != null && intent.hasExtra(AppConstants.TAB)) {
            int tab = intent.getIntExtra(AppConstants.TAB, 0);
            LogCapture.e(TAG, "onNewIntent: " + tab);
            switchTab(tab);
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.back_again_to_exit), Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }
}
