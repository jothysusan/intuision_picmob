package com.picmob.android.mvvm.requests_response;

import androidx.annotation.NonNull;
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

public class RequestResponseRepository {

    private static final String TAG = "RequestResponseReposito";

    public MutableLiveData<Resource<List<RequestPojo>>> getRequests(String token, String id) {

        final MutableLiveData<Resource<List<RequestPojo>>> mutableLiveData = new MutableLiveData<>();

        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();

        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);

        ApiClient.getClient().create(ApiInterface.class).getRequests(headerMap, id).enqueue(new Callback<List<RequestPojo>>() {
            @Override
            public void onResponse(@NonNull Call<List<RequestPojo>> call, @NonNull Response<List<RequestPojo>> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.setValue(Resource.success(response.body()));
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
            public void onFailure(@NonNull Call<List<RequestPojo>> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<List<RequestPojo>>> getResponse(String token, String id) {
        final MutableLiveData<Resource<List<RequestPojo>>> mutableLiveData = new MutableLiveData<>();

        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);

        ApiClient.getClient().create(ApiInterface.class).getResponses(headerMap, id).enqueue(new Callback<List<RequestPojo>>() {
            @Override
            public void onResponse(@NonNull Call<List<RequestPojo>> call, @NonNull Response<List<RequestPojo>> response) {
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
            public void onFailure(@NonNull Call<List<RequestPojo>> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }


    public MutableLiveData<Resource<ApiResponseModel>> sendResponse(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> linkedTreeMap) {
        LogCapture.e(TAG, "registerUser: " + new Gson().toJson(linkedTreeMap));
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).sendResponse(headerMap, linkedTreeMap).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Response sent", true)));
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<List<UserListPojo>>> getUserListByLocation(LinkedTreeMap<String,
            String> headerMap, LinkedTreeMap<String, Object> map) {

        final MutableLiveData<Resource<List<UserListPojo>>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).getUserListByLocation(headerMap,
                map).enqueue(new Callback<List<UserListPojo>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserListPojo>> call,
                                   @NonNull Response<List<UserListPojo>> response) {
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
            public void onFailure(@NonNull Call<List<UserListPojo>> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> sendRequest(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).sendRequest(headerMap, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Request sent", true)));
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> seenResponse(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).seenResponse(headerMap, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Response seen", true)));
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> ignoreRequest(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).ignoreRequest(headerMap, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Request ignored", true)));
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> deleteRequest(String type, String requestId,
                                                                     LinkedTreeMap<String, String> headerMap) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).deleteRequest(type, requestId, headerMap).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel(response.message(), true)));
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> makePublic(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).makePublic(headerMap, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Response made public", true)));
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<Resource<ApiResponseModel>> makePrivate(LinkedTreeMap<String, String> headerMap, LinkedTreeMap<String, Object> map) {
        final MutableLiveData<Resource<ApiResponseModel>> mutableLiveData = new MutableLiveData<>();

        ApiClient.getClient().create(ApiInterface.class).makePrivate(headerMap, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mutableLiveData.postValue(Resource.success(new ApiResponseModel("Response made private", true)));
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Resource.error("Something went wrong!", null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

        return mutableLiveData;
    }

    public MutableLiveData<Resource<List<RequestResponseModel>>> getDashboard(String token, String id) {
        final MutableLiveData<Resource<List<RequestResponseModel>>> mutableLiveData = new MutableLiveData<>();
        LinkedTreeMap<String, String> headerMap = new LinkedTreeMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer " + token);
        ApiClient.getClient().create(ApiInterface.class).getDashboard(headerMap, id).enqueue(new Callback<List<RequestResponseModel>>() {
            @Override
            public void onResponse(Call<List<RequestResponseModel>> call, Response<List<RequestResponseModel>> response) {
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
            public void onFailure(Call<List<RequestResponseModel>> call, Throwable t) {
                mutableLiveData.setValue(Resource.error(t.getMessage(), null));
                LogCapture.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        return mutableLiveData;
    }
}
