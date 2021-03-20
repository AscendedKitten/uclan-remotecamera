/*package com.uclan.remotecamera.androidApp.legacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.common.util.concurrent.ListenableFuture
import com.uclan.remotecamera.androidApp.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LegacyCameraFragment : Fragment(), CameraXConfig.Provider {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var provider: ProcessCameraProvider

    private lateinit var cameraProvider: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProvider = ProcessCameraProvider.getInstance(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        return binding.root
    }

    /*  private fun capture() {
          val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File("yeet.jpg")).build()
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
                      Log.d("CameraFramgent", "pls")
                  }
              })
      }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProvider.addListener({
            provider = cameraProvider.get()
            attachPreview(provider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Control via intent from settings fragment? Look into demo project's setting fragment
    private fun attachPreview(provider: ProcessCameraProvider) {
        //val preview: Preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewFrame.surfaceProvider) }

        val cameraChoice: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        //provider.bindToLifecycle(this as LifecycleOwner, cameraChoice, preview)
    }

    // Control via intent from settings fragment?
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}
*/
