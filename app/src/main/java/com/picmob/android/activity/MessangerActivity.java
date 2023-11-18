package com.picmob.android.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.picmob.android.ApplicationCycle;
import com.picmob.android.R;
import com.picmob.android.adapters.MessageListAdapter;
import com.picmob.android.implementation.BlobStorageService;
import com.picmob.android.models.MessageModel;
import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.FileUtils;
import com.picmob.android.utils.LogCapture;
import com.picmob.android.utils.UtilsFunctions;
import com.picmob.android.viewmodels.MessageViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.picmob.android.utils.AppConstants.CAMERA;
import static com.picmob.android.utils.AppConstants.CAMERA_REQUEST;
import static com.picmob.android.utils.AppConstants.DOC_REQUEST;
import static com.picmob.android.utils.AppConstants.GALLERY_REQUEST;
import static com.picmob.android.utils.AppConstants.READ_STORE;
import static com.picmob.android.utils.AppConstants.STORE_PERMISSION;
import static com.picmob.android.utils.AppConstants.VIDEO_REQUEST;
import static com.picmob.android.utils.AppConstants.WRITE_STORE;

public class MessangerActivity extends BaseActivity implements com.picmob.android.listeners.BlobStorageService {

    @BindView(R.id.rvMesg)
    RecyclerView rvMsg;
    @BindView(R.id.etMesg)
    EditText etMsg;
    @BindView(R.id.btSend)
    Button btSend;
    @BindView(R.id.imgAttach)
    ImageView imgAttach;

    private String name, grp, role;
    private MessageListAdapter msgAdapter;
    private final ArrayList<MessageModel> msgList = new ArrayList<>();
    private static final String TAG = "MessangerActivity";

    private final int START = 0, SETUP = 1, CHECK = 2, SEND = 3, STOP = 4;
    private HubConnection hubConnection;
    private UtilsFunctions utilsFunctions;
    private MessageViewModel messageViewModel;

    private String sndMsg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatview);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        utilsFunctions = new UtilsFunctions();

        hubCommunicator(START, null);

        if (getIntent().getExtras() != null) {
            name = getIntent().getStringExtra(AppConstants.usr);
            grp = getIntent().getStringExtra(AppConstants.grp);
            role = getIntent().getStringExtra(AppConstants.role);
            utilsFunctions.showDialog(this);
            hubCommunicator(SETUP, null);
            hubCommunicator(CHECK, null);
        }

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvMsg.setLayoutManager(layoutManager);
        msgAdapter = new MessageListAdapter(msgList);
        rvMsg.setAdapter(msgAdapter);

        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        messageViewModel.getUserMutableLiveData().observe(this, msgListUpdateObserver);
    }


    Observer<ArrayList<MessageModel>> msgListUpdateObserver = new Observer<ArrayList<MessageModel>>() {
        @Override
        public void onChanged(ArrayList<MessageModel> msgList) {
            msgAdapter.addItem(msgList);
        }
    };

    @OnClick(R.id.btSend)
    void send() {
        if (name != null && grp != null && role != null) {
            if (etMsg.getText().toString() != null && etMsg.getText().toString().length() > 1) {
                hubCommunicator(SEND, etMsg.getText().toString());
                messageViewModel.addValue(new MessageModel(etMsg.getText().toString(), AppConstants.sendMsg));
                etMsg.setText("");
            } else {
                UtilsFunctions.showToast(MessangerActivity.this,
                        AppConstants.shortToast, "Enter your message!");
            }
        }
    }

    @OnClick(R.id.imgAttach)
    void attachFile() {
        checkPermissionAndOpenSheet();
    }

    private void checkPermissionAndOpenSheet() {
        if (UtilsFunctions.hasMultiplePermissions(this, READ_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, WRITE_STORE)
                && UtilsFunctions.hasMultiplePermissions(this, CAMERA)) {
            showFileuploadSheet();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_STORE, WRITE_STORE, CAMERA}, STORE_PERMISSION);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFileuploadSheet() {
        View dialogView =
                getLayoutInflater().inflate(R.layout.bottomsheet_file, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        BottomSheetBehavior bottomSheetBehavior =
                BottomSheetBehavior.from((View) (dialogView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
        bottomSheetBehavior.setFitToContents(true);

        LinearLayout vCam = dialogView.findViewById(R.id.vCam);
        LinearLayout vImg = dialogView.findViewById(R.id.vImg);
        LinearLayout vDoc = dialogView.findViewById(R.id.vDoc);
        LinearLayout vVideo = dialogView.findViewById(R.id.vVideo);

        vCam.setOnClickListener(v -> {
            fileAction(CAMERA_REQUEST);
            dialog.dismiss();
        });

        vImg.setOnClickListener(v -> {
            fileAction(GALLERY_REQUEST);
            dialog.dismiss();
        });

        vVideo.setOnClickListener(v -> {
            fileAction(VIDEO_REQUEST);
            dialog.dismiss();
        });
        vDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileAction(DOC_REQUEST);
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void cameraCapture() {
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = UtilsFunctions.createImageFile(this);
            } catch (IOException ex) {
                LogCapture.e(TAG, "takeCameraPicture:" + ex.getLocalizedMessage());
            }
            if (photoFile != null) {
                takePictureIntent.putExtra("output", FileProvider.getUriForFile(this,
                        AppConstants.FILE_PROVIDER, photoFile));
                /*takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);*/
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }


    private void getGalleryImage() {
       /* Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);*/
       /* Intent intent = new Intent();
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), GALLERY_REQUEST);*/
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), GALLERY_REQUEST);
    }

    private void getVideo() {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI), VIDEO_REQUEST);
    }


    private void getDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, DOC_REQUEST);
        intent.setType("*/*");
        startActivityForResult(intent, DOC_REQUEST);
    }


    private void fileAction(int action) {
        switch (action) {
            case CAMERA_REQUEST:
                cameraCapture();
                break;
            case GALLERY_REQUEST:
                getGalleryImage();
                break;
            case VIDEO_REQUEST:
                getVideo();
                break;
            case DOC_REQUEST:
                getDocument();
                break;
        }
    }


    private void hubCommunicator(int stage, String msg) {
        try {
            switch (stage) {
                case START:
                    startHub();
                    break;
                case SETUP:
                    setupHUb();
                    break;
                case CHECK:
                    checkForMsg();
                    break;
                case SEND:
                    sndMsg = msg;
                    sendMesg(msg);
                    break;
                case STOP:
//                    getState();

                    hubConnection.stop();
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "hubCommunicator: " + e.getLocalizedMessage());
        }
    }

    private void getState() {
        for (; ; ) {
            LogCapture.e(TAG, "hubCommunicator: " + hubConnection.getConnectionState());
            ApplicationCycle.isAppIsInBackground(getApplicationContext());
        }
    }

    private void sendMesg(String msg) {
        hubConnection.send("SendMessage", name, grp, role, msg);
    }


    private void checkForMsg() {

        hubConnection.on("ReceiveMessage", (msg) -> {
            if (!msg.equals(sndMsg))
                messageViewModel.addValue(new MessageModel(msg, AppConstants.receiveMsg));
            Log.e(TAG, "ReceiveMessage: " + msg);
        }, String.class);
    }


    private void setupHUb() {
        AsyncTask.execute(() -> hubConnection.invoke(AppConstants.setupSession, name, grp, role));
        if (hubConnection != null)
            hubConnection.on("UserConnected", (msg) -> {
                Log.e(TAG, "UserConnected: " + msg);
                if (msg != null)
                    utilsFunctions.hideDialog();
            }, String.class);
    }

    private void startHub() {
//        hubConnection = HubConnectionBuilder.create("https://picmobproto02.azurewebsites.net/messages").build();
        hubConnection = HubConnectionBuilder.create("https://picmobapp01.azurewebsites.net/messages").build();
        AsyncTask.execute(() -> hubConnection.start().blockingAwait());
    }

    @Override
    protected void onDestroy() {
        hubCommunicator(STOP, null);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == STORE_PERMISSION) {
            checkPermissionAndOpenSheet();
            LogCapture.e(TAG, "Permission Granted, Now you can use local drive .CONFIRMED");
        } else {
            LogCapture.e(TAG, "Permission Denied, You cannot use local drive .");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                Uri selectedImage = data != null ? Objects.requireNonNull(data).getData() : null;
                Log.e(TAG, "onActivityResult: " + selectedImage.toString());
                try {
                    utilsFunctions.showDialog(this);
                    new BlobStorageService(this).uploadFile(FileUtils.getFileFromUri(this, selectedImage));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == CAMERA_REQUEST) {
                utilsFunctions.showDialog(this);
                new BlobStorageService(this).uploadFile(new File(AppConstants.mPhotoPath));
            }
            if (requestCode == VIDEO_REQUEST)
                try {
                    utilsFunctions.showDialog(this);
                    Log.e(TAG, "onActivityResult: Video" + (data != null ? Objects.requireNonNull(data).getData() : null).toString());
                    new BlobStorageService(this).uploadFile(FileUtils.getFileFromUri(this, data != null ? Objects.requireNonNull(data).getData() : null));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            if (requestCode == DOC_REQUEST)
                try {
                    utilsFunctions.showDialog(this);
                    Log.e(TAG, "onActivityResult: DOC_REQUEST" + (data != null ? Objects.requireNonNull(data).getData() : null).toString());
                    new BlobStorageService(this).uploadFile(FileUtils.getFileFromUri(this, data != null ? Objects.requireNonNull(data).getData() : null));
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    }

    @Override
    public void getUrl(URI url) {
        utilsFunctions.hideDialog();
        messageViewModel.addValue(new MessageModel(url.toString(), AppConstants.sendMsg));
        hubCommunicator(SEND, url.toString());
    }

    @Override
    public void error(String error) {
        UtilsFunctions.showToast(MessangerActivity.this, AppConstants.shortToast, error);
        utilsFunctions.hideDialog();
    }
}
