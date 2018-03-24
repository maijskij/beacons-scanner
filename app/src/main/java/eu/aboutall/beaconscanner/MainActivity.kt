package eu.aboutall.beaconscanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import eu.aboutall.beaconscanner.MainActivity.Constants.TAG

//  Created by Denis Zelenevsky on 14/09/2017.


class MainActivity : BleScanActivity() {

    internal object Constants {
        const val TAG = "BeaconsScanner"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onScanData(result: ScanResult?) {
        Log.d(TAG, "${result?.device?.address} ")
        Log.d(TAG, "-> ${trace(result?.scanRecord?.bytes)}")
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


