package com.uclan.remotecamera.androidApp.stream

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.pedro.vlc.VlcListener
import com.pedro.vlc.VlcVideoLibrary
import com.uclan.remotecamera.androidApp.databinding.FragmentDisplayBinding
import com.uclan.remotecamera.androidApp.utility.SimpleAsyncClient
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Callable

class DisplayFragment : Fragment(), VlcListener {

    private var _binding: FragmentDisplayBinding? = null
    private val binding get() = _binding!!
    private lateinit var vlcLib: VlcVideoLibrary

    companion object {
        private const val TAG = "DisplayFragment"
    }

    class FetchRtspURLTask(private val serverAddress: String, private val serverPort: Int) :
        Callable<String> {
        override fun call(): String {
            val socket = Socket()
            Log.d(TAG, "Attempting connection on $serverAddress:$serverPort")
            socket.connect(InetSocketAddress(serverAddress, serverPort), 5000)
            Log.d(TAG, "Established connection, blocking for rtsp url")

            val rtspUrl = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()

            socket.close()
            return rtspUrl
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplayBinding.inflate(inflater, container, false)

        return binding.root
    }

    @KtorExperimentalAPI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val port = requireArguments().getInt("OWNER_PORT")
        val address = requireArguments().getString("OWNER_ADDRESS")!!

        Handler().postDelayed({
            CoroutineScope(Dispatchers.IO).launch {
                SimpleAsyncClient(address, port).send("!request_url");
            }
        }, 2000)


        /* val futureRtspAddress =
             Executors.newSingleThreadExecutor().submit(FetchRtspURLTask(address, port))
         val rtspURL = futureRtspAddress.get()

         binding.floatingActionButton.setOnClickListener {
             with(binding.surfaceView) {
                 vlcLib = VlcVideoLibrary(activity, this@DisplayFragment, this)
             }
             vlcLib.play(rtspURL)
         }*/
    }

    override fun onComplete() {
        Log.d(TAG, "Playing rtsp stream")
    }

    override fun onError() {
        Log.d(TAG, "Could not play rtsp stream")
    }
}



