package com.picmob.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.picmob.android.R;
import com.picmob.android.activity.GalleryViewActivity;
import com.picmob.android.activity.RequestResponseActivity;
import com.picmob.android.adapters.RecyclerViewItemDecoration;
import com.picmob.android.adapters.RequestResponseAdapter;
import com.picmob.android.listeners.RequestResponseClickListener;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.requests_response.RequestResponseModel;
import com.picmob.android.mvvm.requests_response.RequestResponseViewModel;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.UtilsFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RequestFragment extends Fragment implements RequestResponseClickListener {

    @BindView(R.id.swipy)
    SwipyRefreshLayout swipy;
    @BindView(R.id.rvReq)
    ShimmerRecyclerViewX rvReq;
    @BindView(R.id.vCam)
    LinearLayout vCam;
    @BindView(R.id.vVideo)
    LinearLayout vVideo;
    @BindView(R.id.extFab)
    ExtendedFloatingActionButton extFab;
    @BindView(R.id.tvNoData)
    TextView tvNoData;

    private RequestResponseViewModel resViewModel;
    private static final String TAG = "RequestFragment";
    private List<RequestResponseModel> modelList = new ArrayList<>();
    private UtilsFunctions utilsFunctions;
    private RequestResponseAdapter responseAdapter;
    private UserAuthPojo pojo;
    RecyclerViewItemDecoration recyclerItemDecoration;
    private BroadcastReceiver mMyBroadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView()");
        View root = inflater.inflate(R.layout.fragment_request, container, false);
        ButterKnife.bind(this, root);
        resViewModel = ViewModelProviders.of(this)
                .get(RequestResponseViewModel.class);
        rvReq.setHasFixedSize(true);
        rvReq.setDrawingCacheEnabled(true);
        rvReq.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        responseAdapter = new RequestResponseAdapter(getActivity(), RequestFragment.this, modelList);
        rvReq.setAdapter(responseAdapter);
        rvReq.hideShimmerAdapter();
        recyclerItemDecoration =
                new RecyclerViewItemDecoration(getContext(), getResources()
                        .getDimensionPixelSize(R.dimen.header_height),
                        true, getSectionCallback(modelList));
        rvReq.addItemDecoration(recyclerItemDecoration);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated()");
        utilsFunctions = new UtilsFunctions();
        pojo = new Gson().fromJson(CustomSharedPreference.getInstance(getActivity())
                .getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        swipy.setColorSchemeColors(getResources().getColor(R.color.purple_300),
                getResources().getColor(R.color.purple_500));
        swipy.setOnRefreshListener(direction -> {
            if (direction == SwipyRefreshLayoutDirection.TOP) {
                loadRequestResponseData();
                swipy.setRefreshing(false);
            }
        });
        rvReq.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && extFab.getVisibility() == View.VISIBLE) {
                    extFab.hide();
                } else if (dy < 0 && extFab.getVisibility() != View.VISIBLE) {
                    extFab.show();
                }
            }
        });

        loadRequestResponseData();
    }

    private void loadRequestResponseData() {
        if (UtilsFunctions.isNetworkAvail(getActivity())) {
            tvNoData.setVisibility(View.GONE);
            rvReq.showShimmerAdapter();
            resViewModel.initRequest(pojo.getToken().toString(),
                    pojo.getId().toString());
            resViewModel.getDashboardLiveData().observe(this, new Observer<Resource<List<RequestResponseModel>>>() {
                @Override
                public void onChanged(Resource<List<RequestResponseModel>> listResource) {
                    switch (listResource.status) {
                        case ERROR:
                            rvReq.hideShimmerAdapter();
                            UtilsFunctions.showToast(getActivity(), AppConstants.shortToast, listResource.message);
                            break;
                        case SUCCESS:
                            if (AppConstants.REQUEST_RESPONSE_UPDATE)
                                AppConstants.REQUEST_RESPONSE_UPDATE = false;
                            modelList.clear();
                            if (listResource.data != null) {
                                modelList.addAll(listResource.data);
                                if (modelList.size() == 0) {
                                    tvNoData.setVisibility(View.VISIBLE);
                                }
                            } else {
                                tvNoData.setVisibility(View.VISIBLE);
                            }
                            Collections.sort(modelList, new Comparator<RequestResponseModel>() {
                                public int compare(RequestResponseModel obj1, RequestResponseModel obj2) {
                                    int b1 = obj1.getType();
                                    int b2 = obj2.getType();
                                    return Integer.compare(b1, b2);
                                }
                            });
                            rvReq.hideShimmerAdapter();
                            responseAdapter.notifyDataSetChanged();
                            break;
                    }
                }
            });
        }
    }

    Observer<Resource<List<RequestResponseModel>>> dashboardObserver
            = new Observer<Resource<List<RequestResponseModel>>>() {
        @Override
        public void onChanged(Resource<List<RequestResponseModel>> listResource) {
            switch (listResource.status) {
                case ERROR:
                    rvReq.hideShimmerAdapter();
                    UtilsFunctions.showToast(getActivity(), AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    if (AppConstants.REQUEST_RESPONSE_UPDATE)
                        AppConstants.REQUEST_RESPONSE_UPDATE = false;
                    modelList.clear();
                    if (listResource.data != null) {
                        modelList.addAll(listResource.data);
                        if (modelList.size() == 0) {
                            tvNoData.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                    Collections.sort(modelList, new Comparator<RequestResponseModel>() {
                        public int compare(RequestResponseModel obj1, RequestResponseModel obj2) {
                            int b1 = obj1.getType();
                            int b2 = obj2.getType();
                            return Integer.compare(b1, b2);
                        }
                    });
                    rvReq.hideShimmerAdapter();
                    responseAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private RecyclerViewItemDecoration.SectionCallback getSectionCallback(List<RequestResponseModel> list) {
        return new RecyclerViewItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int pos) {
                if (list.size() != 0 && pos < list.size()) {
                    boolean v = pos == 0 || (!list.get(pos).getType().equals(list.get(pos - 1).getType()));
                    Log.i(TAG, String.valueOf(v));
                    return v;
                } else {
                    return true;
                }
            }

            @Override
            public String getSectionHeaderName(int pos) {
                if (list.size() != 0 && pos < list.size()) {
                    if (list.get(pos).getType() == AppConstants.REQUEST_TYPE) {
                        return getString(R.string.request_label);
                    } else if (list.get(pos).getType() == AppConstants.RESPONSE_TYPE) {
                        return getString(R.string.response_label);
                    } else {
                        return getString(R.string.initiator_label);
                    }
                } else {
                    return "";
                }
            }
        };
    }

    @OnClick(R.id.vVideo)
    void actionVideo() {
        Intent i = new Intent(getActivity(), RequestResponseActivity.class);
        i.putExtra(AppConstants.TRANSACTION, AppConstants.REQUEST);
        i.putExtra(AppConstants.ACTION, AppConstants.VIDEO);
        startActivity(i);
    }

    @OnClick(R.id.vCam)
    void actionPicture() {
        Intent i = new Intent(getActivity(), RequestResponseActivity.class);
        i.putExtra(AppConstants.TRANSACTION, AppConstants.REQUEST);
        i.putExtra(AppConstants.ACTION, AppConstants.PICTURE);
        startActivity(i);
    }


    @OnClick(R.id.extFab)
    void action() {
        Intent i = new Intent(getActivity(), RequestResponseActivity.class);
        i.putExtra(AppConstants.TRANSACTION, AppConstants.REQUEST);
        i.putExtra(AppConstants.ACTION, AppConstants.PICTURE);
        startActivity(i);
    }

    @Override
    public void onClickReqItem(RequestResponseModel responseModel) {
        String msgDetails = new Gson().toJson(responseModel);
        Log.e(TAG, "onClickReqItem: " + msgDetails);
        Intent i = new Intent(getActivity(), GalleryViewActivity.class);
        i.putExtra(AppConstants.SCREEN, AppConstants.REQUEST_SCREEN);
        i.putExtra(AppConstants.MESSAGE_DETAILS, msgDetails);
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UtilsFunctions.isNetworkAvail(getActivity()))
            if (AppConstants.REQUEST_RESPONSE_UPDATE)
                loadRequestResponseData();

        mMyBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Here you can refresh your listview or other UI
                Toast.makeText(context.getApplicationContext(), "Receiver", Toast.LENGTH_SHORT).show();
                loadRequestResponseData();
            }
        };
        try {
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(mMyBroadcastReceiver, new IntentFilter("get_request"));

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMyBroadcastReceiver);
    }
}
