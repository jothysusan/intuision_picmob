package com.picmob.android.fragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.picmob.android.utils.AppConstants.CONTACT;
import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mikelau.views.shimmer.ShimmerRecyclerViewX;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.picmob.android.R;
import com.picmob.android.activity.DmActivity;
import com.picmob.android.adapters.FriendsAdapter;
import com.picmob.android.listeners.FriendsListener;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.friends.FriendsPojo;
import com.picmob.android.mvvm.friends.FriendsViewModel;
import com.picmob.android.mvvm.login.UserAuthPojo;
import com.picmob.android.mvvm.utils.AppConstant;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.CustomSharedPreference;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.LimitColumn;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FriendsFragment extends Fragment implements FriendsListener {

    @BindView(R.id.extFab)
    ExtendedFloatingActionButton extFab;
    private static final String TAG = "FriendsFragment";

    @BindView(R.id.swipy)
    SwipyRefreshLayout swipy;

    @BindView(R.id.rvFriends)
    ShimmerRecyclerViewX rvFriends;
    @BindView(R.id.tvNoData)
    TextView tvNoData;

    private UserAuthPojo pojo;
    private FriendsViewModel viewModel;
    private UtilsFunctions utilsFunctions;
    private FriendsAdapter mAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, root);
        viewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pojo = new Gson().fromJson(CustomSharedPreference.getInstance(getActivity()).getString(AppConstants.USR_DETAIL), UserAuthPojo.class);
        utilsFunctions = new UtilsFunctions();
        getFriendsList();

        swipy.setColorSchemeColors(getResources().getColor(R.color.purple_300),
                getResources().getColor(R.color.purple_500));
        swipy.setOnRefreshListener((SwipyRefreshLayoutDirection direction) -> {
            if (direction == SwipyRefreshLayoutDirection.TOP) {
                getFriendsList();
                swipy.setRefreshing(false);
            }
        });

        rvFriends.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    }

    private void getFriendsList() {
        if (UtilsFunctions.isNetworkAvail(getActivity())) {
            tvNoData.setVisibility(View.GONE);
            rvFriends.showShimmerAdapter();
            viewModel.getFriendsList(pojo.getToken().toString(), pojo.getId().toString());
            viewModel.getFriendsListData().observe(this, friendsListObs);
        }
    }

    Observer<Resource<List<FriendsPojo>>> friendsListObs = new Observer<Resource<List<FriendsPojo>>>() {
        @Override
        public void onChanged(Resource<List<FriendsPojo>> listResource) {
            switch (listResource.status) {
                case ERROR:
                    rvFriends.hideShimmerAdapter();
                    UtilsFunctions.showToast(getActivity(), AppConstants.shortToast, listResource.message);
                    break;
                case SUCCESS:
                    List<FriendsPojo> fList = new ArrayList<>(listResource.data);
                    if (fList.size() == 0) {
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                    rvFriends.setHasFixedSize(true);
                    rvFriends.setDrawingCacheEnabled(true);
                    rvFriends.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    mAdapter = new FriendsAdapter(getActivity(), FriendsFragment.this, fList);
                    rvFriends.setAdapter(mAdapter);
                    rvFriends.hideShimmerAdapter();
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @OnClick(R.id.extFab)
    void openInvite() {
        if (UtilsFunctions.isNetworkAvail(getActivity()))
            checkPersmission();
    }


    private void checkPersmission() {
        if (UtilsFunctions.hasMultiplePermissions(Objects.requireNonNull(getActivity()), CONTACT)) {
            showInviteSheet();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{CONTACT}, STORE_PERMISSION);
        }
    }


    private void showInviteSheet() {
        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_invite, null);
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        BottomSheetBehavior bottomSheetBehavior =
                BottomSheetBehavior.from((View) (dialogView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setFitToContents(true);

        LinearLayout vContact = dialogView.findViewById(R.id.vContact);
        LinearLayout vApps = dialogView.findViewById(R.id.vApps);

        vContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactIntent();
                dialog.dismiss();
            }
        });


        vApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntent();
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    Observer<Resource<ApiResponseModel>> resourceObserver = new Observer<Resource<ApiResponseModel>>() {
        @Override
        public void onChanged(Resource<ApiResponseModel> apiResponseModelResource) {
            switch (apiResponseModelResource.status) {
                case ERROR:

                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(getActivity(), AppConstants.shortToast, apiResponseModelResource.message);
                    break;
                case SUCCESS:
                    if (AppConstants.DM_UPDATE)
                        AppConstants.DM_UPDATE = false;
                    utilsFunctions.hideDialog();
                    UtilsFunctions.showToast(getActivity(), AppConstants.shortToast, apiResponseModelResource.data.getMessage());
                    Log.e(TAG, "onChanged: " + apiResponseModelResource.data);
                    break;
            }
        }
    };


    private void shareIntent() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Exsiting new app!");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Get picmob app from www.example.com");
        getActivity().startActivity(Intent.createChooser(sharingIntent, "Share Option"));
    }

    private void contactIntent() {
        new MultiContactPicker.Builder(this) //Activity/fragment context
                .hideScrollbar(true) //Optional - default: false
                .showTrack(false) //Optional - default: true
                .searchIconColor(Color.WHITE) //Option - default: White
                .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
                .handleColor(ContextCompat.getColor(getActivity(), R.color.purple_500)) //Optional - default: Azure Blue
                .bubbleColor(ContextCompat.getColor(getActivity(), R.color.purple_300)) //Optional - default: Azure Blue
                .bubbleTextColor(Color.WHITE) //Optional - default: White
                .setTitleText("Select Contacts") //Optional - default: Select Contacts
                .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                .limitToColumn(LimitColumn.PHONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                .showPickerForResult(AppConstants.CONTACT_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == STORE_PERMISSION) {
            checkPersmission();
            LogCapture.e(TAG, "Permission Granted, Now you can use local drive .CONFIRMED");
        } else {
            LogCapture.e(TAG, "Permission Denied, You cannot use local drive .");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                ArrayList<ContactResult> results = new ArrayList<>(MultiContactPicker.obtainResult(data));
                List<LinkedTreeMap<String, Object>> list = new ArrayList<>();

                for (ContactResult contactResult : results) {
                    LinkedTreeMap<String, Object> linkedTreeMap = new LinkedTreeMap<>();
                    linkedTreeMap.put(AppConstant.NAME, contactResult.getDisplayName());
                    linkedTreeMap.put(AppConstant.PHONE, contactResult.getPhoneNumbers().get(0).getNumber());
                    linkedTreeMap.put(AppConstant.IDENTIFIER, contactResult.getContactID());
                    list.add(linkedTreeMap);
                    Log.e(TAG, "onActivityResult: " + contactResult.getDisplayName() + "===" + contactResult.getContactID());
                }
                LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
                map.put(AppConstant.ID, pojo.getId());
                map.put(AppConstant.CONTACTS, list);

                utilsFunctions.showDialog(getActivity());
                viewModel.sendInvite(pojo.getToken(), map);
                viewModel.getInviteData().observe(this, resourceObserver);


            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("User closed the picker without selecting items.");
            }
        }
    }

    @Override
    public void onClickFriend(FriendsPojo pojo) {
        Intent i = new Intent(getActivity(), DmActivity.class);
        i.putExtra(AppConstants.MESSAGE_DETAILS, new Gson().toJson(pojo));
        startActivity(i);
    }

    @Override
    public void onResume() {
        if (UtilsFunctions.isNetworkAvail(getActivity()))
            if (AppConstants.DM_UPDATE)
                getFriendsList();
        super.onResume();
    }
}
