package com.picmob.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.picmob.android.R;
import com.picmob.android.adapters.DmAdapter;
import com.picmob.android.listeners.VideoClickListener;
import com.picmob.android.models.DmModel;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.dm.DmPojo;
import com.picmob.android.mvvm.dm.DmViewModel;
import com.picmob.android.mvvm.friends.FriendsPojo;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.General;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.picmob.android.utils.ExceptionHandler;

public class DmActivity extends BaseActivity implements VideoClickListener {

    @BindView(R.id.rvChat)
    ShimmerRecyclerViewX rvChat;
    @BindView(R.id.swipy)
    SwipyRefreshLayout swipy;

    private DmViewModel viewModel;
    private UtilsFunctions utilsFunctions;
    private UserAuthPojo pojo;
    private FriendsPojo friendsPojo;
    private List<DmModel> dmList = new ArrayList<>();
    private DmAdapter dmAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm);
        ButterKnife.bind(this);
        SpannableString spannableString = new SpannableString("Chat");
        General general = new General(this);

        viewModel = ViewModelProviders.of(this).get(DmViewModel.class);

        pojo = new Gson().fromJson(CustomSharedPreference.getInstance(this).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);

        if (getIntent().hasExtra(AppConstants.MESSAGE_DETAILS)) {
            friendsPojo = new Gson().fromJson(getIntent().getStringExtra(AppConstants.MESSAGE_DETAILS), FriendsPojo.class);
            spannableString = new SpannableString(friendsPojo.getUsername());
        }

        spannableString.setSpan(general.mediumtypeface(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(spannableString);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        getMessages();

        swipy.setColorSchemeColors(getResources().getColor(R.color.purple_300),
                getResources().getColor(R.color.purple_500));
        swipy.setOnRefreshListener((SwipyRefreshLayoutDirection direction) -> {
            if (direction == SwipyRefreshLayoutDirection.TOP) {
                getMessages();
                swipy.setRefreshing(false);
            }
        });

        dmAdapter = new DmAdapter(this, dmList, this);
    }


    @OnClick(R.id.btnSendMessage)
    void sendMesg() {
        if (UtilsFunctions.isNetworkAvail(DmActivity.this)) {
            Intent i = new Intent(this, RequestResponseActivity.class);
            i.putExtra(AppConstants.TRANSACTION, AppConstants.NEW_MESSAGE);
            i.putExtra(AppConstants.FRIEND_DETAILS, new Gson().toJson(friendsPojo));
            startActivity(i);
        }
    }

    ;

    private void getMessages() {

        if (UtilsFunctions.isNetworkAvail(DmActivity.this)) {

            if (dmList != null && dmList.size() > 0)
                dmList.clear();

            rvChat.showShimmerAdapter();
            viewModel.getMessages(pojo.getToken(), pojo.getId().toString(), friendsPojo.getId().toString());
            viewModel.getDmSData().observe(this, dmSObs);
            viewModel.getDmRData().observe(this, dmRObs);
        }
    }


    Observer<Resource<List<DmPojo>>> dmSObs = new Observer<Resource<List<DmPojo>>>() {
        @Override
        public void onChanged(Resource<List<DmPojo>> listResource) {
            switch (listResource.status) {

                case ERROR:
                    rvChat.hideShimmerAdapter();
                    UtilsFunctions.showToast(DmActivity.this, AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:

                    for (DmPojo pojo : listResource.data) {
                        DmModel model = new DmModel();
                        model.setId(pojo.getId());
                        if (pojo.getMediaURL() != null && !pojo.getMediaURL().isEmpty())
                            model.setMediaURL(pojo.getMediaURL());
                        model.setDateTime(pojo.getDateTime());
                        model.setMessage(pojo.getMessage());
                        model.setSend(true);
                        model.setSenderId(pojo.getSenderId());
                        model.setReceiverId(pojo.getReceiverId());
                        dmList.add(model);
                    }

                    Collections.sort(dmList, new Comparator<DmModel>() {
                        @Override
                        public int compare(DmModel o1, DmModel o2) {
                            return Integer.compare(o1.getId(), o2.getId());
                        }
                    });

                    rvChat.setHasFixedSize(true);
                    rvChat.setDrawingCacheEnabled(true);
                    rvChat.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    rvChat.setAdapter(dmAdapter);
                    rvChat.hideShimmerAdapter();
                    rvChat.scrollToPosition(dmList.size() - 1);
                    break;
            }
        }
    };

    Observer<Resource<List<DmPojo>>> dmRObs = new Observer<Resource<List<DmPojo>>>() {
        @Override
        public void onChanged(Resource<List<DmPojo>> listResource) {
            switch (listResource.status) {

                case ERROR:
                    rvChat.hideShimmerAdapter();
                    UtilsFunctions.showToast(DmActivity.this, AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:

                    for (DmPojo pojo : listResource.data) {
                        DmModel model = new DmModel();
                        model.setId(pojo.getId());
                        if (pojo.getMediaURL() != null && !pojo.getMediaURL().isEmpty())
                            model.setMediaURL(pojo.getMediaURL());
                        model.setDateTime(pojo.getDateTime());
                        model.setMessage(pojo.getMessage());
                        model.setSend(false);
                        model.setSenderId(pojo.getSenderId());
                        model.setReceiverId(pojo.getReceiverId());
                        dmList.add(model);
                    }

                    Collections.sort(dmList, new Comparator<DmModel>() {
                        @Override
                        public int compare(DmModel o1, DmModel o2) {
                            return Integer.compare(o1.getId(), o2.getId());
                        }
                    });
                    rvChat.hideShimmerAdapter();
                    dmAdapter.notifyDataSetChanged();
                    rvChat.scrollToPosition(dmList.size() - 1);
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if (AppConstants.TRANS == 1) {
            getMessages();
            AppConstants.TRANS = 0;
        }
        super.onResume();
    }

    @Override
    public void onVideoClick(String videoURL) {
        if (UtilsFunctions.isNetworkAvail(DmActivity.this)) {
            Intent i = new Intent(this, VideoPlayerActivity.class);
            i.putExtra(AppConstants.VIDEO_URL, videoURL);
            startActivity(i);
        }
    }
}
