package eu.aboutall.beaconscanner

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import eu.aboutall.beaconscanner.BleActivity.Constants.REQUEST_ENABLE_BT
import eu.aboutall.beaconscanner.BleActivity.Constants.REQUEST_LOCATION_PERMISSION


//  Created by Denis Zelenevsky on 14/09/2017.


//    BleActivity takes care of checking if BLE is supported/enabled
//    and locations permissions are granted before executing BLE scan.
//
//    Start scan using safeStartScan() method, as soon as permissions
//    are granted, abstract method startScan() will be called.
//    In case of missing permissions or BLE not enebled/supported,
//    activity will be finished.


abstract class BleActivity : AppCompatActivity() {

    internal object Constants {

        const val REQUEST_ENABLE_BT = 1

        const val REQUEST_LOCATION_PERMISSION = 1
    }

    // permissions are granted. Start the actual scan logic.
    abstract fun startScan()

    protected lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if ( bleNotSupported() ){
            goodbyeAndQuit(R.string.ble_not_supported)
            return
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    fun safeStartScan() {

        if ( ! bleEnabled() ) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }

        when (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                startScan()
            }
            PackageManager.PERMISSION_DENIED -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BT -> when (resultCode) {
                Activity.RESULT_OK -> {
                    safeStartScan()
                }
                else -> {
                    goodbyeAndQuit(R.string.ble_not_enabled)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults contentEquals intArrayOf(PackageManager.PERMISSION_GRANTED)) {
                    safeStartScan()
                } else {
                    goodbyeAndQuit(R.string.permissions_not_granted)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun bleEnabled() : Boolean
    // Ensures Bluetooth is available on the device and it is enabled.
            = bluetoothAdapter.isEnabled


    private fun goodbyeAndQuit(msgResId: Int) {
        Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show()
        Log.d(MainActivity.Constants.TAG, getString(msgResId))
        finish()
    }

    private fun bleNotSupported() : Boolean
    // Use this check to determine whether BLE is supported on the device.
            = ! packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

}


