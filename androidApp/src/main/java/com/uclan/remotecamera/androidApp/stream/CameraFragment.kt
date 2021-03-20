package com.uclan.remotecamera.androidApp.stream

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.uclan.remotecamera.androidApp.databinding.FragmentCameraBinding
import com.uclan.remotecamera.androidApp.utility.GenericAlert
import com.uclan.remotecamera.androidApp.utility.SimpleAsyncClient
import com.uclan.remotecamera.androidApp.utility.SimpleBroadcastServer
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import libs.pedroSG94.RtspServerCamera2
import java.io.ByteArrayOutputStream
import java.util.*


class CameraFragment : Fragment(), ConnectCheckerRtsp, SurfaceHolder.Callback,
    View.OnTouchListener {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var rtspServerCamera: RtspServerCamera2
    private lateinit var httpServer: SimpleBroadcastServer
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        SimpleBroadcastServer(CameraSettingsFragment.PORT).start()
    }

    override fun onDestroy() {
        super.onDestroy()
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

            gestureDetector = GestureDetector(
                requireContext(),
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent?): Boolean {

                        return true
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                        Log.e("maybe", "we should call this")
                        rtspServerCamera.tapToFocus(e)
                        return true
                    }
                })
        }
    }

    @KtorExperimentalAPI
    private fun init() {
        if (rtspServerCamera.prepareVideo()) {
            Log.d("CameraFragment", "Video prepared, attempting stream")
            rtspServerCamera.startStream()
            rtspServerCamera.startPreview()

            CoroutineScope(Dispatchers.IO).launch {
                SimpleAsyncClient(httpUrl()!!, httpPort()).advertise(this@CameraFragment)
            }

            binding.button.setOnClickListener {
                //broadcastRtspUrl()
            }
        } else
            GenericAlert().create(
                requireContext(),
                "Error",
                "Device does not support RTSP streaming"
            ).show()
    }

    /*
                run {
                val filename = "${System.currentTimeMillis()}.png"
                val write: (OutputStream) -> Boolean = {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DCIM}/yeet"
                        )
                    }

                    requireContext().contentResolver.let {
                        it.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                            ?.let { uri ->
                                it.openOutputStream(uri)?.let(write)
                            }
                    }
                } else {
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .toString() + File.separator + "yeet"
                    val file = File(imagesDir)
                    if (!file.exists()) {
                        file.mkdir()
                    }
                    val image = File(imagesDir, filename)
                    write(FileOutputStream(image))
                }
            }
     */

    fun captureImageAsByteArray(): ByteArray? {
        var byteArray: ByteArray? = null
        rtspServerCamera.glInterface.takePhoto { bitmap ->
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            byteArray = outputStream.toByteArray()
        }
        return byteArray
    }

    private fun broadcastRtspUrl() {
        val service = Intent(activity, LocalFileTransferService::class.java)
        service.action = LocalFileTransferService.ACTION_SEND_URL
        service.putExtra(
            LocalFileTransferService.EXTRAS_RTSP_LINK,
            rtspServerCamera.getEndPointConnection()
        )
        service.putExtra(
            LocalFileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
            arguments?.getString("OWNER_ADDRESS")
        )
        service.putExtra(
            LocalFileTransferService.EXTRAS_GROUP_OWNER_PORT,
            arguments?.getInt("OWNER_PORT")
        )
        LocalFileTransferService.enqueueWork(requireContext(), service)
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

    fun httpPort(): Int = requireArguments().getInt("OWNER_PORT")

    fun httpUrl(): String? = requireArguments().getString("OWNER_ADDRESS")


    override fun onAuthErrorRtsp() {
        Log.e("CameraFragment", "RTSP auth failure")
        rtspServerCamera.stopStream()
    }

    override fun onAuthSuccessRtsp() {
        Log.d("CameraFragment", "RTSP auth success")
    }

    override fun onConnectionFailedRtsp(reason: String) {
        Log.e("CameraFragment", "RTSP connection failure with error $reason")
        rtspServerCamera.stopStream()
    }

    override fun onConnectionSuccessRtsp() {
        Log.d("CameraFragment", "RTSP connection success")
    }

    override fun onDisconnectRtsp() {
        Log.d("CameraFragment", "RTSP disconnected")
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
        if (rtspServerCamera.isStreaming)
            rtspServerCamera.stopStream()
        rtspServerCamera.stopPreview()
    }
}