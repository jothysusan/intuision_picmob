package com.picmob.android.listeners;

import java.net.URI;

public interface BlobStorageService {
    void getUrl(URI url);
    void error (String error);
}
