package com.shield.barcodescanner

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.shield.barcodescanner.databinding.ActivityBarcodeScanBinding
import java.io.IOException

class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private var intentData = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun iniBarcode(){
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this,barcodeDetector)
            .setRequestedPreviewSize(1920,1080)
            .setAutoFocusEnabled(true)
            .build()
        binding.surfaceView!!.holder.addCallback(object :SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(binding.surfaceView.holder)
                }
                catch (ex: IOException){
                    ex.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }

        })
        barcodeDetector.setProcessor(object :Detector.Processor<Barcode>{
            override fun release() {
                Toast.makeText(applicationContext, "Barcode Scanner Stopped!!", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                var barcodes = detections.detectedItems
                if(barcodes.size()!=0){
                    binding.txtBarcode.post{
                        binding.btnAction.text = "COPY TEXT"
                        intentData = barcodes.valueAt(0).displayValue
                        binding.txtBarcode.text = intentData

                        binding.btnAction.setOnClickListener {
                            try {
                                val textToCopy = intentData
                                copyToClipboard(textToCopy)
                            }
                            catch (ex:IOException){
                                Toast.makeText(applicationContext,"Some Error Occurred!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

        })
    }

    private fun copyToClipboard(textToCopy: String) {
        if (textToCopy.isNotEmpty()) {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = android.content.ClipData.newPlainText("Copied Text", textToCopy)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(applicationContext, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        cameraSource!!.release()
    }

    override fun onResume() {
        super.onResume()
        iniBarcode()
    }
}