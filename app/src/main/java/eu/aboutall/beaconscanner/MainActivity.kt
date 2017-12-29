package eu.aboutall.beaconscanner

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import eu.aboutall.beaconscanner.MainActivity.Constants.REQUEST_ENABLE_BT
import eu.aboutall.beaconscanner.MainActivity.Constants.REQUEST_LOCATION_PERMISSION
import eu.aboutall.beaconscanner.MainActivity.Constants.SCAN_TIMEOUT
import eu.aboutall.beaconscanner.MainActivity.Constants.TAG

//  Created by Denis Zelenevsky on 14/09/2017.


class MainActivity : AppCompatActivity() {

    internal object Constants {
        val TAG = "BeaconsScanner"

        val REQUEST_ENABLE_BT = 1
        val REQUEST_LOCATION_PERMISSION = 1

        val SCAN_TIMEOUT: Long = 10000
        val IDLE_TIMEOUT: Long = 6000
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val handler = Handler()
    private var scanLoopEnabled = false

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(TAG, "${result?.device?.address} - ${trace(result?.scanRecord?.bytes)}")
        }
    }

    private val stopScanRunnable: Runnable = Runnable {
        if (!scanLoopEnabled) return@Runnable

        stopBleScanner()
        handler.postDelayed(startScanRunnable, Constants.IDLE_TIMEOUT)
    }

    private val startScanRunnable: Runnable = Runnable {
        if (!scanLoopEnabled) return@Runnable

        startScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    override fun onStart() {
        super.onStart()

        checkBleIsSupported()

        startScan()
    }

    override fun onStop() {
        super.onStop()

        stopScan()
    }

    private fun startScan() {

        if ( !bleIsEnabled() ) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }


        when (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {

                bluetoothAdapter.bluetoothLeScanner.startScan(leScanCallback)
                scanLoopEnabled = true
                handler.postDelayed(stopScanRunnable, SCAN_TIMEOUT)
            }
            PackageManager.PERMISSION_DENIED -> {

                stopScan()
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults contentEquals intArrayOf(PackageManager.PERMISSION_GRANTED)) {
                    startScan()
                } else {
                    goodbyeAndQuit(R.string.permissions_not_granted)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun stopScan() {

        scanLoopEnabled = false
        handler.removeCallbacks(stopScanRunnable)
        handler.removeCallbacks(startScanRunnable)
        stopBleScanner()
    }


    private fun stopBleScanner() {
        if ( bleIsEnabled() )
            bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
    }

    private fun checkBleIsSupported() {

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            goodbyeAndQuit(R.string.ble_not_supported)
        }
    }

    private fun bleIsEnabled() : Boolean{
        // Ensures Bluetooth is available on the device and it is enabled.
        return bluetoothAdapter.isEnabled
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BT -> when (resultCode) {
                Activity.RESULT_OK -> {
                    startScan()
                }
                else -> {
                    goodbyeAndQuit(R.string.ble_not_enabled)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun goodbyeAndQuit(msgResId: Int) {
        Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show()
        Log.d(TAG, getString(msgResId))
        finish()
    }

    private fun trace(data: ByteArray?): String {

        val bytes: ByteArray = data ?: return ""

        val builder = StringBuilder()
        for (b in bytes) {
            builder.append(String.format("%02x", b))
        }

        return builder.toString()
    }
}


