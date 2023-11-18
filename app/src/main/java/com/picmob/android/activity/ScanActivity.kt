package com.picmob.android.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.picmob.android.R
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.picmob.android.ble.ChatServer

private const val TAG = "BluetoothLeChat"

class ScanActivity : AppCompatActivity() {

    private val REQUEST_BLUETOOTH_PERMISSION = 1
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        Log.d("BluetoothLeChat", "Requesting needed permissions")
//        requestPermissions.launch(arrayOf(
//            Manifest.permission.BLUETOOTH_ADVERTISE,
//            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.BLUETOOTH_SCAN
//        ))
    }
    // Run the chat server as long as the app is on screen
    override fun onStart() {
        super.onStart()
        checkBluetoothPermissions()

    }

    private fun checkBluetoothPermissions() {
        // Check if Bluetooth permissions are granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Bluetooth permissions are granted, check if Bluetooth is enabled
            ChatServer.startServer(application)
        } else {
            // Request Bluetooth permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }
    }
    private fun checkBluetoothEnabled() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            // Handle accordingly
        } else {
            if (!bluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled, you might want to prompt the user to enable it
                // You can use an Intent to open the Bluetooth settings page
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    return
                }

            } else {
                ChatServer.startServer(application)
                // Bluetooth is enabled, you can proceed with Bluetooth operations
            }
        }
    }
    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                ChatServer.startServer(application)
                // Bluetooth is enabled by the user
                // You can perform Bluetooth operations here
            } else {
                // The user declined to enable Bluetooth
                // Handle accordingly
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permissions granted, check if Bluetooth is enabled
                    ChatServer.startServer(application)
                } else {
                    // Bluetooth permissions denied, handle accordingly
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStop() {
        super.onStop()
        ChatServer.stopServer()
    }


//    private val requestPermissions =
//        (this as ComponentActivity).
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            permissions.entries.forEach {
//                Log.d(TAG, "${it.key} = ${it.value}")
//            }
//        }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}