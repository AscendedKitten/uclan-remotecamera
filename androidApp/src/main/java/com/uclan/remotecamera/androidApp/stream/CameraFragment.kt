package com.uclan.remotecamera.androidApp.stream

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.common.util.concurrent.ListenableFuture
import com.uclan.remotecamera.androidApp.R
import com.uclan.remotecamera.androidApp.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), CameraXConfig.Provider {

    companion object {
        private fun nv21ToJPEG(nv21: ByteArray?): ByteArray? {
            val out = ByteArrayOutputStream()
            val yuv = YuvImage(nv21, ImageFormat.NV21, 1280, 720, null)
            yuv.compressToJpeg(Rect(0, 0, 1280, 720), 0, out)
            return out.toByteArray()
        }

        private fun yuv420880toNV21(image: Image): ByteArray {
            val crop: Rect = image.cropRect
            val format = image.format
            val width: Int = crop.width()
            val height: Int = crop.height()
            val planes = image.planes
            val data = ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
            val rowData = ByteArray(planes[0].rowStride)
            var channelOffset = 0
            var outputStride = 1
            for (i in planes.indices) {
                when (i) {
                    0 -> {
                        channelOffset = 0
                        outputStride = 1
                    }
                    1 -> {
                        channelOffset = width * height + 1
                        outputStride = 2
                    }
                    2 -> {
                        channelOffset = width * height
                        outputStride = 2
                    }
                }
                val buffer: ByteBuffer = planes[i].buffer
                val rowStride = planes[i].rowStride
                val pixelStride = planes[i].pixelStride
                val shift = if (i == 0) 0 else 1
                val w = width shr shift
                val h = height shr shift
                buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
                for (row in 0 until h) {
                    var length: Int
                    if (pixelStride == 1 && outputStride == 1) {
                        length = w
                        buffer.get(data, channelOffset, length)
                        channelOffset += length
                    } else {
                        length = (w - 1) * pixelStride + 1
                        buffer.get(rowData, 0, length)
                        for (col in 0 until w) {
                            data[channelOffset] = rowData[col * pixelStride]
                            channelOffset += outputStride
                        }
                    }
                    if (row < h - 1) {
                        buffer.position(buffer.position() + rowStride - length)
                    }
                }
            }
            return data
        }
    }

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private val imageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(1280, 720))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private val imageCapture = ImageCapture.Builder()
        .setTargetRotation(requireView().display.rotation)
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .build()

    private lateinit var cameraProvider: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProvider = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cameraExecutor = Executors.newSingleThreadExecutor()
        _binding = DataBindingUtil.setContentView(requireActivity(), R.layout.fragment_camera)
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding.floatingActionButton.setOnClickListener {
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File("")).build()
            imageCapture.takePicture(outputFileOptions, cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException) {
                        Log.e("CameraFragment", "Error capturing: ${error.message}")
                        Toast.makeText(
                            context, "Error capturing image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    }
                })
        }
        return binding.root
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProvider.addListener({
            val provider = cameraProvider.get()
            attachPreview(provider)
        }, ContextCompat.getMainExecutor(requireContext()))

        imageAnalysis.setAnalyzer(cameraExecutor, { image ->

            var compressed: ByteArray? = nv21ToJPEG(yuv420880toNV21(image.image!!))

            //YUV_420_888: original format
            //https://stackoverflow.com/questions/41775968/how-to-convert-android-media-image-to-bitmap-object

            Log.d("CameraFragment", "rotation ")
            image.close()
        })
    }

    // Control via intent from settings fragment? Look into demo project's setting fragment
    private fun attachPreview(provider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()

        val cameraChoice: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(binding.previewFrame.surfaceProvider)
        provider.bindToLifecycle(this, cameraChoice, imageAnalysis, preview)
    }

    // Control via intent from settings fragment?
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}