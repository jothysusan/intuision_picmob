package com.picmob.android.implementation;

import android.os.AsyncTask;
import android.util.Log;

import com.picmob.android.utils.AppConstants;
import com.picmob.android.utils.LogCapture;
import com.google.gson.Gson;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

public class BlobStorageService {
    private static final String TAG = "BlobStorageService";

    private com.picmob.android.listeners.BlobStorageService blobStorageService;


    public BlobStorageService(com.picmob.android.listeners.BlobStorageService blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

  /*  public void uploadFile(File file){
        try {
            AsyncTask.execute(new Runnable() {
                @SuppressWarnings("MalformedFormatString")
                @Override
                public void run() {
                    StorageSharedKeyCredential credential = new StorageSharedKeyCredential( AppConstants.ACC_NAME, AppConstants.ACC_KEY);
                    String endPoint = String.format(Locale.ROOT, "https://pmstore01.blob.core.windows.net/", AppConstants.ACC_NAME);
                    BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endPoint).credential(credential).buildClient();
                    BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("container-02");
                    blobContainerClient.create();
                    String path = file.getAbsolutePath();
                    String fileName = file.getName();
                    BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
                    String url = blobClient.getBlobUrl();
                    System.out.println("BlobUrl=>" + url);
                    System.out.println("Path=>" + path);
                    blobClient.uploadFromFile(path);
                    LogCapture.e(TAG, "uploadFile: "+url);
                    System.out.print("Next Blob URL " + url);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "uploadFile: Error "+e.getLocalizedMessage() );
        }
    }
*/

    public void uploadFile(File file) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    CloudStorageAccount storageAccount = CloudStorageAccount
                            .parse(AppConstants.CONNECTION_STRING);

                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                    CloudBlobContainer container = blobClient.getContainerReference(AppConstants.CONTAINER_NAME);

                    BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

                    // Include public access in the permissions object
                    containerPermissions
                            .setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

                    // Set the permissions on the containe
                    container.uploadPermissions(containerPermissions);

                    String extension = file.getName().substring(file.getName().lastIndexOf("."));
                    String prefX = "picmob-" + UUID.randomUUID().toString();
                    String fileName = prefX  + extension;

                    LogCapture.e(TAG, "run: prefX=>" + prefX + " ext=>" + extension);

                    CloudBlockBlob blob = container.getBlockBlobReference(fileName);

                    blob.upload(new FileInputStream(file), file.length());

//                    blob.uploadFromFile(file.getAbsolutePath());

                    LogCapture.e(TAG, "run: " + blob.getUri());


                    blobStorageService.getUrl(blob.getUri());


                  /*  for (ListBlobItem blobItem : container.listBlobs()) {
                        // If the item is a blob, not a virtual directory
                        if (blobItem instanceof CloudBlockBlob) {
                            // Download the text
                            CloudBlockBlob retrievedBlob = (CloudBlockBlob) blobItem;
                            Log.e(TAG, "run: ListBlobItem=> "+retrievedBlob.downloadText());

                        }
                    }

                    // List the blobs in a container, loop over them and
                    // output the URI of each of them
                    for (ListBlobItem blobItem : container.listBlobs()) {
                        Log.e(TAG, "run: ListBlobItem2=> "+blobItem.getUri().toString());
                    }*/

                    Log.e(TAG, "uploadFile: " + new Gson().toJson(blob.getProperties()));


                } catch (URISyntaxException | InvalidKeyException | StorageException e) {
                    blobStorageService.error("File not found!");
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    blobStorageService.error("File not found!");
                    e.printStackTrace();
                } catch (IOException e) {
                    blobStorageService.error("File not found!");
                    e.printStackTrace();
                }
            }
        });
    }

}
