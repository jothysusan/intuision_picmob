package com.picmob.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.picmob.android.R;
import com.picmob.android.activity.GalleryViewActivity;
import com.picmob.android.adapters.GalleryAdapter;
import com.picmob.android.listeners.GalleryListener;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.gallery.GalleryPojo;
import com.picmob.android.mvvm.gallery.GalleryViewModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.UtilsFunctions;
import com.google.gson.Gson;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryFragment extends Fragment implements GalleryListener {

    @BindView(R.id.rvGallery)
    ShimmerRecyclerViewX rvGallery;
    @BindView(R.id.swipy)
    SwipyRefreshLayout swipy;
    @BindView(R.id.tvNoData)
    TextView tvNoData;

    private UserAuthPojo pojo;
    private GalleryViewModel viewModel;
    private GalleryAdapter mAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        ButterKnife.bind(this, root);
        viewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pojo = new Gson().fromJson(CustomSharedPreference.getInstance(getActivity()).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);

        swipy.setColorSchemeColors(getResources().getColor(R.color.purple_300),
                getResources().getColor(R.color.purple_500));
        swipy.setOnRefreshListener(direction -> {
            if (direction == SwipyRefreshLayoutDirection.TOP) {
                getGallery();
                swipy.setRefreshing(false);
            }
        });
        getGallery();
    }

    private void getGallery() {
        if (UtilsFunctions.isNetworkAvail(getActivity())) {
            tvNoData.setVisibility(View.GONE);
            rvGallery.showShimmerAdapter();
            viewModel.getGalleryList(pojo.getToken().toString(), pojo.getId().toString());
            viewModel.getGalleryData().observe(this, galleryObserver);
        }
    }


    Observer<Resource<List<GalleryPojo>>> galleryObserver = new Observer<Resource<List<GalleryPojo>>>() {
        @Override
        public void onChanged(Resource<List<GalleryPojo>> listResource) {
            switch (listResource.status) {

                case ERROR:
                    rvGallery.hideShimmerAdapter();
                    UtilsFunctions.showToast(getActivity(), AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    if (AppConstants.GALLERY_UPDATE)
                        AppConstants.GALLERY_UPDATE = false;
                    List<GalleryPojo> gList = new ArrayList<>(listResource.data);
                    if (gList.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                    rvGallery.setHasFixedSize(true);
                    rvGallery.setDrawingCacheEnabled(true);
                    rvGallery.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    mAdapter = new GalleryAdapter(getActivity(), GalleryFragment.this, gList);
                    rvGallery.setAdapter(mAdapter);
                    rvGallery.hideShimmerAdapter();
                    mAdapter.notifyDataSetChanged();
                    break;

            }
        }
    };


    @Override
    public void onGalleryClick(GalleryPojo pojo) {
        String msgDetail = new Gson().toJson(pojo);
        Intent i = new Intent(getActivity(), GalleryViewActivity.class);
        i.putExtra(AppConstants.SCREEN, AppConstants.GALLERY_SCREEN);
        i.putExtra(AppConstants.MESSAGE_DETAILS, msgDetail);
        startActivity(i);
    }

    @Override
    public void onResume() {
        if (UtilsFunctions.isNetworkAvail(getActivity()))
            if (AppConstants.GALLERY_UPDATE)
                getGallery();
        super.onResume();
    }
}
