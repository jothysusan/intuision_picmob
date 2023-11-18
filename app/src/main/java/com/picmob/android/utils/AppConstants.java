package com.picmob.android.utils;

public class AppConstants {

    public static final String sendMessage = "SendMessage"; //user,group,role,msg
    public static final String sendGrpMsg = "SendMessagToGroup"; // group,msg

    public static final String receiveMessage = "ReceiveMessage";

    public static final String userConnected = "UserConnected";
    public static final String setupSession = "SetupSession"; //username,group,role
    public static final String sendMsgToInitiator = "SendMessageToInitiator"; //group,message

    public static final String grp = "GROUP";
    public static final String usr = "USER";
    public static final String role = "ROLE";

    public static final int shortToast = 0;
    public static final int longToast = 1;

    public static final int sendMsg = 0;
    public static final int receiveMsg = 1;
    public static final String TYPE = "type";
    public static final String USER_ID = "user_id";
    public static final String USERNAME = "username";

    public static String mPhotoPath = null;
    public static final String FILE_PROVIDER = "com.picmob.android.fileprovider";

    public static final int CAMERA_REQUEST = 1001;
    public static final int GALLERY_REQUEST = 1002;
    public static final int DOC_REQUEST = 1003;
    public static final int VIDEO_REQUEST = 1004;
    public static final int CONTACT_REQUEST = 1005;
    public static final int GPS_REQUEST = 10006;
    public static final int GALLERY_VIDEO_REQUEST = 1007;

    public static final String CAMERA = "android.permission.CAMERA";
    public static final String READ_STORE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_STORE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String RECORD = "android.permission.RECORD_AUDIO";
    public static final String CONTACT = android.Manifest.permission.READ_CONTACTS;

    public static final int STORE_PERMISSION = 101;

    // Azure storage account details for blob
    public static final String CONTAINER_NAME = "container01";
    public static final String ACC_NAME = "picmobstore";
    public static final String ACC_KEY = "DnJoNZoqZ4Yp8GxWt9ftxjEAN9XLz7AkCfyEWqoFmYuOLyeJnPEyCkb3oHP3XbpOwXazzmLqTU7cCibaquPqzg==";
    public static final String CONNECTION_STRING = "DefaultEndpointsProtocol=https;" +
            "AccountName=" + ACC_NAME + ";" +
            "AccountKey=" + ACC_KEY + ";" +
            "EndpointSuffix=core.windows.net;";

    public static final String USR_DETAIL = "user_Details";
    public static final String MESSAGE_DETAILS = "msg_details";
    public static final String FRIEND_DETAILS = "frnd_details";
    public static final String EVENT_DETAILS = "event_details";
    public static final String TRANSACTION = "transaction";
    public static final String ACTION = "action";
    public static final String SCREEN = "screen";

    public static final String BTN_RESET = "button_reset";
    public static final String BTN_SEND = "button_send";
    public static final String BTN_VERIFY = "button_verify";

    public static final int REQUEST_SCREEN = 0;
    public static final int GALLERY_SCREEN = 1;
    public static final int HOME_SCREEN = 2;
    public static final int EVENT_DETAILS_SCREEN = 3;

    public static final String LOGIN = "login";

    public static final int REQUEST = 0;
    public static final int RESPOND = 1;
    public static final int NEW_MESSAGE = 2;
    public static final int EVENT = 3;

    public static final int PICTURE = 0;
    public static final int VIDEO = 1;

    public static final String PICTURES = "picture";
    public static final String VIDEOS = "video";

    public static final String MSG = "message";
    public static final String MEDIA = "media";

    public static boolean APP_FOREGROUND = false;

    public static boolean NEW_DATA = false;

    public static final int GOOGLE_SIGN_IN = 9001;

    public static final String LOGOUT = "logout";

    public static int TRANS = 0;

    public static final String REGISTRATION = "registration";
    public static final String FORGET_PASSWORD = "forget_password";

    public static boolean GALLERY_UPDATE = false;
    public static boolean FEED_UPDATE = false;
    public static boolean REQUEST_RESPONSE_UPDATE = false;
    public static boolean DM_UPDATE = false;
    public static boolean EVENT_LIST_UPDATE = false;

    public static final String TAB = "tab";
    public static final int DEFAULT_RADIUS = 50;
    public static final int START_RADIUS = 10;
    public static final int END_RADIUS = 100;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;


    public static final String VIDEO_URL = "video_url";
    public static final float ZOOM_THRESHOLD = 15f;

    public static final int REQUEST_TYPE = 0;
    public static final int RESPONSE_TYPE = 1;
    public static final int INITIATOR_TYPE = 2;

    public static final int RECEIVED_PRIVATE_TYPE = 0;
    public static final int RECEIVED_PUBLIC_TYPE = 1;
    public static final int CREATED_TYPE = 2;
    public static final int PRIVATE_TYPE = 1;
    public static final int PUBLIC_TYPE = 2;
    public static String HIDE_DELETE_BUTTON = "hide_delete_button";
    public static final int DEFAULT_USER_VICINITY_RADIUS = 100;
    public static final String PRIVATE_TYPE_LABEL = "Private";
    public static final String PUBLIC_TYPE_LABEL = "Public";
}
