package com.picmob.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.picmob.android.R;
import com.picmob.android.activity.EventDetailsActivity;
import com.picmob.android.activity.EventMapActivity;
import com.picmob.android.adapters.EventsAdapter;
import com.picmob.android.adapters.RecyclerViewItemDecoration;
import com.picmob.android.listeners.EventClickListeners;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.events.EventListPojo;
import com.picmob.android.mvvm.events.EventViewModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.utils.AppConstant;
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

public class EventsFragment extends Fragment implements EventClickListeners {

    @BindView(R.id.swipy)
    SwipyRefreshLayout swipy;
    @BindView(R.id.rvEvents)
    ShimmerRecyclerViewX rvEvents;
    @BindView(R.id.tvNoData)
    TextView tvNoData;
    @BindView(R.id.create_event)
    Button createButton;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private static final String TAG = "EventsFragment";
    RecyclerViewItemDecoration recyclerItemDecoration;
    private EventsAdapter eventsAdapter;
    private List<EventListPojo> eventsList = new ArrayList<>();
    private EventViewModel eventViewModel;
    private UtilsFunctions utilsFunctions;
    private UserAuthPojo pojo;
    LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this, root);
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        rvEvents.setHasFixedSize(true);
        rvEvents.setDrawingCacheEnabled(true);
        rvEvents.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        eventsAdapter = new EventsAdapter(getActivity(), EventsFragment.this, eventsList);
        rvEvents.setAdapter(eventsAdapter);
        rvEvents.hideShimmerAdapter();
        recyclerItemDecoration =
                new RecyclerViewItemDecoration(getContext(), getResources()
                        .getDimensionPixelSize(R.dimen.header_height),
                        true, getSectionCallback(eventsList));
        rvEvents.addItemDecoration(recyclerItemDecoration);
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
                loadEventsData();
                swipy.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEventsData();
    }

    private void loadEventsData() {
        if (UtilsFunctions.isNetworkAvail(getActivity())) {
            map.put(AppConstant.USR_ID, pojo.getId());
            rvEvents.showShimmerAdapter();
            tvNoData.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            createButton.setVisibility(View.GONE);
            eventViewModel.getEventList(pojo.getToken(), map);
            eventViewModel.getEventListLiveData().observe(getViewLifecycleOwner(), eventListObserver);
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            createButton.setVisibility(View.VISIBLE);
        }
    }

    Observer<Resource<List<EventListPojo>>> eventListObserver = new Observer<Resource<List<EventListPojo>>>() {

        @Override
        public void onChanged(Resource<List<EventListPojo>> listResource) {
            switch (listResource.status) {
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    createButton.setVisibility(View.GONE);
                    rvEvents.hideShimmerAdapter();
                    UtilsFunctions.showToast(getContext(), AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    tvNoData.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    eventsList.clear();
                    assert listResource.data != null;
                    eventsList.addAll(listResource.data);
                    createButton.setVisibility(View.VISIBLE);
                    if (eventsList != null && eventsList.size() > 0) {
                        Collections.sort(eventsList, new Comparator<EventListPojo>() {
                            public int compare(EventListPojo obj1, EventListPojo obj2) {
                                boolean b1 = obj1.getIsPrivate();
                                boolean b2 = obj2.getIsPrivate();
                                return Boolean.compare(b1, b2);
                            }
                        });
                        rvEvents.hideShimmerAdapter();
                        eventsAdapter.notifyDataSetChanged();
                    } else {
                        rvEvents.hideShimmerAdapter();
                        tvNoData.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        createButton.setVisibility(View.VISIBLE);
                    }
            }
        }
    };

    private RecyclerViewItemDecoration.SectionCallback getSectionCallback(List<EventListPojo> list) {
        return new RecyclerViewItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int pos) {
                if (list.size() != 0 && pos < list.size()) {
                    boolean v = pos == 0 || (list.get(pos).getIsPrivate() != (list.get(pos - 1).getIsPrivate()));
                    Log.i(TAG, String.valueOf(v));
                    return v;
                } else {
                    return true;
                }
            }

            @Override
            public String getSectionHeaderName(int pos) {
                if (list.size() != 0 && pos < list.size()) {
                    if (list.get(pos).getIsPrivate()) {
                        return getString(R.string.private_event_label);
                    } else {
                        return getString(R.string.public_event_label);
                    }
                } else {
                    return "";
                }
            }
        };
    }

    @OnClick(R.id.create_event)
    void createEvent() {
        if (UtilsFunctions.isNetworkAvail(getActivity())) {
            Intent i = new Intent(getActivity(), EventMapActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onClickEventItem(EventListPojo eventsPojo) {
        Intent i = new Intent(getActivity(), EventDetailsActivity.class);
        i.putExtra(AppConstant.EVENT_ID, eventsPojo.getId());
        startActivity(i);
    }
}
