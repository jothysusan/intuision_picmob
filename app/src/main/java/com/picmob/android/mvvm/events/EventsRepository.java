package com.picmob.android.mvvm.events;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.picmob.android.mvvm.Resource;
import com.picmob.android.mvvm.common.ApiResponseModel;
import com.picmob.android.mvvm.webservices.APIError;
import com.picmob.android.mvvm.webservices.ApiClient;
import com.picmob.android.mvvm.webservices.ApiInterface;
import com.picmob.android.mvvm.webservices.ErrorUtils;
import com.picmob.android.utils.LogCapture;

import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsRepository {
    private static final String TAG = "EventsRepository";

    public MutableLiveData<Resource<List<UserVicinityPojo>>> getUserInVicinity(String token, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<List<UserVicinityPojo>>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).getUserVicinity(headerMap, map).enqueue(new Callback<List<UserVicinityPojo>>() {
            @Override
            public void onResponse(Call<List<UserVicinityPojo>> call, Response<List<UserVicinityPojo>> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(Objects.requireNonNull(response.body())));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<List<UserVicinityPojo>> call, Throwable t) {
                mutableLiveData.setValue(Resource.error(t.getMessage(), null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });


        return mutableLiveData;
    }

    public MutableLiveData<Resource<List<UserVicinityPojo>>> getRegisteredUsers(String token, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<List<UserVicinityPojo>>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).getRegisteredUsers(headerMap, map).enqueue(new Callback<List<UserVicinityPojo>>() {
            @Override
            public void onResponse(Call<List<UserVicinityPojo>> call, Response<List<UserVicinityPojo>> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(Objects.requireNonNull(response.body())));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<List<UserVicinityPojo>> call, Throwable t) {
                mutableLiveData.setValue(Resource.error(t.getMessage(), null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> sendEvent(String token, CreateOrUpdateEventPojo createOrUpdateEventPojo, boolean isUpdate) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).sendEvent(headerMap, createOrUpdateEventPojo).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (isUpdate)
                        mutableLiveData.postValue(Resource.success(new ApiResponseModel("Event Updated", true)));
                    else
                        mutableLiveData.postValue(Resource.success(new ApiResponseModel("Event Created", true)));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.postValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.postValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<Resource<List<EventListPojo>>> getEvenList(String token, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<List<EventListPojo>>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).getEventList(headerMap, map).
                enqueue(new Callback<List<EventListPojo>>() {
                    @Override
                    public void onResponse(Call<List<EventListPojo>> call, Response<List<EventListPojo>> response) {
                        if (response.isSuccessful()) {
                            mutableLiveData.setValue(Resource.success(Objects.requireNonNull(response.body())));
                        } else {
                            APIError error = ErrorUtils.parseError(response);
                            LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                            if (error.message() != null)
                                mutableLiveData.setValue(Resource.error(error.message(), null));
                            else
                                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<EventListPojo>> call, Throwable t) {

                        mutableLiveData.setValue(Resource.error(t.getMessage(), null));
                        LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
                    }
                });
        return mutableLiveData;
    }

    public MutableLiveData<Resource<EventDetailsPojo>> fetchEvent(String token, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<EventDetailsPojo>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).fetchEventDetail(headerMap, map).enqueue(new Callback<EventDetailsPojo>() {
            @Override
            public void onResponse(Call<EventDetailsPojo> call, Response<EventDetailsPojo> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(Objects.requireNonNull(response.body())));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.setValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<EventDetailsPojo> call, Throwable t) {

                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> deleteEvent(String token, int eventID) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).deleteEvent(headerMap, eventID).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Event Deleted", true)));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.postValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.postValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> deleteEventImage(String token, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).deleteEventImage(headerMap, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Image Deleted", true)));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.postValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.postValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> attachImageToEvent(String token, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).attachImageToEvent(headerMap, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Image Attached", true)));
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    LogCapture.e(TAG, "ErrorResponse=> " + new Gson().toJson(error));
                    if (error.message() != null)
                        mutableLiveData.postValue(Resource.error(error.message(), null));
                    else
                        mutableLiveData.postValue(Resource.error("Something went wrong!", null));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }
}
