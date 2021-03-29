package com.uclan.remotecamera.androidApp.stream

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.uclan.remotecamera.androidApp.databinding.FragmentCameraBinding
import com.uclan.remotecamera.androidApp.utility.GenericAlert
import com.uclan.remotecamera.androidApp.utility.SimpleAsyncClient
import io.ktor.http.cio.websocket.*
import io.ktor.server.engine.*
import io.ktor.util.*
import libs.pedroSG94.RtspServerCamera2
import java.io.ByteArrayOutputStream
import java.util.*

@KtorExperimentalAPI
class CameraFragment : Fragment(), ConnectCheckerRtsp, SurfaceHolder.Callback,
    View.OnTouchListener, ConnectErrorCallback {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var rtspServerCamera: RtspServerCamera2
    private lateinit var gestureDetector: GestureDetector
    private lateinit var args: CameraFragmentArgs

    private lateinit var client: SimpleAsyncClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        args = CameraFragmentArgs.fromBundle(requireArguments())
        client = SimpleAsyncClient(args.ownerAddress, args.ownerPort, this@CameraFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.surfaceView) {
            rtspServerCamera = RtspServerCamera2(this, this@CameraFragment, 1935)
            holder.addCallback(this@CameraFragment)
            setOnTouchListener(this@CameraFragment)
        }

        gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent?): Boolean {

                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    rtspServerCamera.tapToFocus(e)
                    return true
                }
            })
    }

    private fun init() {
        if (rtspServerCamera.prepareVideo()) {
            Log.d("CameraFragment", "Video prepared, attempting stream")
            rtspServerCamera.startStream()
            rtspServerCamera.startPreview()

            client.cameraFragmentBlock(this)
            client.queueMsg(Frame.Text("!unlock"))
        } else
            GenericAlert().create(
                requireContext(),
                "Error",
                "Device does not support RTSP streaming"
            ).show()
    }

    fun captureImageAsByteArray() {
        rtspServerCamera.glInterface.takePhoto { bitmap ->
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            bitmap.recycle()

            client.queueMsg(Frame.Binary(true, byteArray))
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (val action = event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount > 1)
                    if (action == MotionEvent.ACTION_MOVE)
                        rtspServerCamera.setZoom(event)
            }
        }
        return true
    }

    fun rtspUrl(): String = rtspServerCamera.getEndPointConnection()

    override fun onAuthErrorRtsp() {
        Log.e("CameraFragment", "RTSP auth failure")
        rtspServerCamera.stopStream()
        requireActivity().runOnUiThread {
            GenericAlert().create(requireContext(), "Error", "RTSP auth failed").show()
            findNavController().popBackStack()

        }
    }

    override fun onAuthSuccessRtsp() {
        Log.d("CameraFragment", "RTSP auth success")
    }

    override fun onConnectionFailedRtsp(reason: String) {
        Log.e("CameraFragment", "RTSP connection failure with error $reason")
        rtspServerCamera.stopStream()
        requireActivity().runOnUiThread {
            GenericAlert().create(requireContext(), "Error", "RTSP failed").show()
            findNavController().popBackStack()
        }
    }

    override fun onConnectionSuccessRtsp() {
        Log.d("CameraFragment", "RTSP connection success")
    }

    override fun onDisconnectRtsp() {
        Log.d("CameraFragment", "RTSP disconnected")
        requireActivity().runOnUiThread {
            GenericAlert().create(requireContext(), "Error", "RTSP disconnected").show()
            findNavController().popBackStack()
        }
    }

    override fun onNewBitrateRtsp(bitrate: Long) {
        Log.d("CameraFragment", "Bitrate updated to $bitrate")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        init()
        Log.d("CameraFragment", "Surface created")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d("CameraFragment", "Surface changed; start preview")
        rtspServerCamera.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d("CameraFragment", "Attempting to stop server")
        if (rtspServerCamera.isStreaming)
            rtspServerCamera.stopStream()
        rtspServerCamera.stopPreview()
        client.queueMsg(Frame.Text("!return_settings"))
    }

    override fun onPause() {
        super.onPause()
        surfaceDestroyed(binding.surfaceView.holder)
    }

    override fun onErrorCallback() {
        requireActivity().runOnUiThread {
            lifecycleScope.launchWhenResumed {
                Toast.makeText(requireContext(), "Server not yet ready", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }
    }
}