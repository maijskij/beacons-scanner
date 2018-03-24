package eu.aboutall.beaconscanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Handler
import eu.aboutall.beaconscanner.BleScanActivity.Constants.SCAN_TIMEOUT


//  Created by Denis Zelenevsky on 14/09/2017.


//  BleScanActivity when in a foregrouns, scans in a loop
//  for any BLE advertisments. As soon as data is received,
//  abstract onScanData(result: ScanResult?) is called

abstract class BleScanActivity : BleActivity() {

    internal object Constants {
        const val SCAN_TIMEOUT: Long = 10000
        const val IDLE_TIMEOUT: Long = 6000
    }

    abstract fun onScanData(result: ScanResult?)

    private val handler = Handler()
    private var scanLoopEnabled = false

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            onScanData(result)
        }
    }

    private val stopScanRunnable: Runnable = Runnable {
        if (!scanLoopEnabled) return@Runnable

        stopBleScanner()
        handler.postDelayed(startScanRunnable, Constants.IDLE_TIMEOUT)
    }

    private val startScanRunnable: Runnable = Runnable {
        if (!scanLoopEnabled) return@Runnable

        safeStartScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        safeStartScan()
    }

    override fun onPause() {
        super.onPause()

        stopScan()
    }

    override fun startScan() {

        stopScan()

        bluetoothAdapter.bluetoothLeScanner.startScan(leScanCallback)
        scanLoopEnabled = true
        handler.postDelayed(stopScanRunnable, Constants.SCAN_TIMEOUT)
    }

    private fun stopScan() {

        scanLoopEnabled = false
        handler.removeCallbacks(stopScanRunnable)
        handler.removeCallbacks(startScanRunnable)
        stopBleScanner()
    }

    private fun stopBleScanner() {
        if ( bleEnabled() )
            bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
    }
}


