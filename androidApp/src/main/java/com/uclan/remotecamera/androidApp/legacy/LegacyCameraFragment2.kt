package com.uclan.remotecamera.androidApp.legacy/*package com.uclan.remotecamera.androidApp.stream

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uclan.remotecamera.androidApp.databinding.FragmentCameraBinding
import net.majorkernelpanic.streaming.Session
import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.audio.AudioQuality
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.video.VideoQuality

class LegacyCameraFragment2 : Fragment(), Session.Callback, SurfaceHolder.Callback {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var surfaceView: SurfaceView
    private lateinit var session: Session

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        session.release()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            //surfaceView = binding.surfaceView
            with(surfaceView) {
                holder.addCallback(this)
            }

            session = SessionBuilder.getInstance()
                .setCallback(this)
                .setSurfaceView(surfaceView)
                .setPreviewOrientation(90)
                .setContext(context)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(VideoQuality(320, 240, 20, 500000))
                .build()
        }

    }

    override fun onBitrateUpdate(p0: Long) {
        Log.d("CameraFragment", "Bitrate changed to $p0")
    }

    override fun onSessionError(p0: Int, p1: Int, p2: Exception?) {
        Log.e("CameraFragment", "Session failed with message: ${p2?.message}")
    }

    override fun onPreviewStarted() {
        Log.d("CameraFragment", "Preview started")
    }

    override fun onSessionConfigured() {
        Log.d("CameraFragment", "Session configured")
        Log.d("CameraFragment", "SDP: ${session.sessionDescription}")
    }

    override fun onSessionStarted() {
        Log.d("CameraFragment", "Session started")
    }

    override fun onSessionStopped() {
        Log.d("CameraFragment", "Session stopped")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("CameraFragment", "have mercy")
        session.startPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        session.stop()
    }
}*/