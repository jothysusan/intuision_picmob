package com.picmob.android.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.picmob.android.R;
import com.picmob.android.activity.GalleryViewActivity;
import com.picmob.android.activity.ScanActivity;
import com.picmob.android.adapters.GalleryAdapter;
import com.picmob.android.listeners.GalleryListener;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.gallery.GalleryPojo;
import com.picmob.android.mvvm.home.FeedViewModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.GpsUtils;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment implements GalleryListener {

    @BindView(R.id.scanFab)
    ExtendedFloatingActionButton extFab;
    @BindView(R.id.rvHome)
    ShimmerRecyclerViewX rvHome;
    @BindView(R.id.swipy)
    SwipyRefreshLayout swipy;
    @BindView(R.id.tvNoData)
    TextView tvNoData;

    private UserAuthPojo pojo;
    private FeedViewModel viewModel;
    private GalleryAdapter mAdapter;
    private static final String TAG = "HomeFragment";
    private GpsUtils mGpsUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(FeedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, root);
        mGpsUtil = new GpsUtils(this.getContext());
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            mGpsUtil.turnGPSOn(null);
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pojo = new Gson().fromJson(CustomSharedPreference.getInstance(getActivity()).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);

        getFeeds();
        swipy.setColorSchemeColors(getResources().getColor(R.color.purple_300),
                getResources().getColor(R.color.purple_500));
        swipy.setOnRefreshListener((SwipyRefreshLayoutDirection direction) -> {
            if (direction == SwipyRefreshLayoutDirection.TOP) {
                getFeeds();
                swipy.setRefreshing(false);
            }
        });
    }

    private void getFeeds() {
        if (UtilsFunctions.isNetworkAvail(getActivity())) {
            tvNoData.setVisibility(View.GONE);
            rvHome.showShimmerAdapter();
            viewModel.getFeedList(pojo.getToken().toString(), pojo.getId().toString());
            viewModel.getFeedData().observe(getViewLifecycleOwner(), feedObserver);
        }
    }

    Observer<Resource<List<GalleryPojo>>> feedObserver = new Observer<Resource<List<GalleryPojo>>>() {
        @Override
        public void onChanged(Resource<List<GalleryPojo>> listResource) {
            switch (listResource.status) {

                case ERROR:
                    rvHome.hideShimmerAdapter();
                    UtilsFunctions.showToast(getActivity(), AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    if (AppConstants.FEED_UPDATE)
                        AppConstants.FEED_UPDATE = false;
                    List<GalleryPojo> gList = new ArrayList<>(listResource.data);
                    if (gList.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                    rvHome.setHasFixedSize(true);
                    rvHome.setDrawingCacheEnabled(true);
                    rvHome.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    mAdapter = new GalleryAdapter(getActivity(), HomeFragment.this, gList);
                    rvHome.setAdapter(mAdapter);
                    rvHome.hideShimmerAdapter();
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void onGalleryClick(GalleryPojo pojo) {
        String msgDetail = new Gson().toJson(pojo);
        Intent i = new Intent(getActivity(), GalleryViewActivity.class);
        i.putExtra(AppConstants.SCREEN, AppConstants.HOME_SCREEN);
        i.putExtra(AppConstants.MESSAGE_DETAILS, msgDetail);
        startActivity(i);
    }

    @Override
    public void onResume() {
        LogCapture.e(TAG, "onResume: ");
        if (UtilsFunctions.isNetworkAvail(getActivity()))
            if (AppConstants.FEED_UPDATE)
                getFeeds();
        super.onResume();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void requestPermissions() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        ActivityCompat.requestPermissions(this.getActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                AppConstants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }
    @OnClick(R.id.scanFab)
    void openScan() {
        Intent i = new Intent(getActivity(), ScanActivity.class);
        startActivity(i);
    }

}
