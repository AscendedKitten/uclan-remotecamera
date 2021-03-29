package com.uclan.remotecamera.androidApp.stream

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pedro.vlc.VlcListener
import com.pedro.vlc.VlcVideoLibrary
import com.uclan.remotecamera.androidApp.databinding.FragmentDisplayBinding
import com.uclan.remotecamera.androidApp.utility.GenericAlert
import com.uclan.remotecamera.androidApp.utility.SimpleAsyncClient
import io.ktor.client.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@KtorExperimentalAPI
class DisplayFragment : Fragment(), VlcListener, ConnectErrorCallback {

    private var _binding: FragmentDisplayBinding? = null
    private val binding get() = _binding!!

    private lateinit var vlcLib: VlcVideoLibrary
    private lateinit var args: DisplayFragmentArgs

    private lateinit var client: SimpleAsyncClient

    companion object {
        private const val TAG = "DisplayFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        args = DisplayFragmentArgs.fromBundle(requireArguments())
        client = SimpleAsyncClient(args.ownerAddress, args.ownerPort, this@DisplayFragment)
        client.displayFragmentBlock(this@DisplayFragment)
        client.queueMsg(Frame.Text("!request_unlock"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplayBinding.inflate(inflater, container, false)

        return binding.root
    }

    fun init() {
        if (!this::vlcLib.isInitialized) {
            with(binding) {
                waitingLabel.visibility = View.GONE
                waitingBar.visibility = View.GONE
                surfaceView.visibility = View.VISIBLE
            }

            Toast.makeText(requireContext(), "Connection established!", Toast.LENGTH_SHORT)
                .show()
            client.queueMsg(Frame.Text("!request_url"))

            binding.floatingActionButton.setOnClickListener {
                client.queueMsg(Frame.Text("!request_img"))
            }
        } else {
            Log.d(TAG, "Received init request but already streaming; ignoring")
        }
    }

    fun startStream(rtspUrl: String) {
        Log.d(TAG, "Attempting to start stream")
        with(binding.surfaceView) {
            vlcLib = VlcVideoLibrary(activity, this@DisplayFragment, this)
        }
        vlcLib.play(rtspUrl)
    }

    override fun onDestroy() {
        if (this::vlcLib.isInitialized)
            if (vlcLib.isPlaying) {
                vlcLib.stop()
                client.queueMsg(Frame.Text("!return_settings"))
            }
        super.onDestroy()
    }

    fun saveImage(bitmap: Bitmap) {
        Log.d(TAG, "Attempting to save image")
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
                    "${Environment.DIRECTORY_DCIM}/remoteCamera"
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

    override fun onComplete() {
        Log.d(TAG, "Playing rtsp stream")
    }

    override fun onError() {
        GenericAlert().create(requireContext(), "Error", "Error with RTSP transmission").show()
        findNavController().popBackStack()
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



