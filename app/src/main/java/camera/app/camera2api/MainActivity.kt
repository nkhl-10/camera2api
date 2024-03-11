package camera.app.camera2api

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import camera.app.camera2api.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread

    private lateinit var cameraManager: CameraManager
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest.Builder

    lateinit var imageReader: ImageReader

    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var videoFile: File? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /*step 4 check permission and camera manager */
        getPermission()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        startThread()
        binding.texture.surfaceTextureListener = surfaceListener

        imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener({ it ->
            val image = it.acquireLatestImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)


            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "${System.currentTimeMillis()}image.jpeg"
            )
            val output = FileOutputStream(file)
            output.write(bytes)
            output.close()

            image.close()
            Toast.makeText(this@MainActivity, "Clicked", Toast.LENGTH_SHORT).show()
        }, handler)

        binding.imageClick.setOnClickListener {
            captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequest.addTarget(imageReader.surface)
            cameraCaptureSession.capture(captureRequest.build(), null, null)
        }

        binding.videoClick.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }


    }


    /*step 7 open camera*/
    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(
//            getCameraId()!!,
            cameraManager.cameraIdList[1],
            object : CameraDevice.StateCallback() {
                override fun onOpened(p0: CameraDevice) {
                    cameraDevice = p0
                    captureRequest =
                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)


                    // Set the ISO sensitivity range for automatic ISO control
                    captureRequest.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON
                    )
                    captureRequest.set(
                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
                    )

                    val surface = Surface(binding.texture.surfaceTexture)
                    captureRequest.addTarget(surface)
                    cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object :
                        CameraCaptureSession.StateCallback() {
                        override fun onConfigured(p0: CameraCaptureSession) {
                            cameraCaptureSession = p0
                            cameraCaptureSession.setRepeatingRequest(
                                captureRequest.build(), null, null
                            )
                        }

                        override fun onConfigureFailed(p0: CameraCaptureSession) {}

                    }, handler)
                }

                override fun onDisconnected(p0: CameraDevice) {

                }

                override fun onError(p0: CameraDevice, p1: Int) {

                }

            },
            handler
        )
    }

    /*step 6 surfaceListener */
    private var surfaceListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
        }

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean = false

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
        }

    }

    /** step 5 start Thread*/
    private fun startThread() {
        handlerThread = HandlerThread("name")
        handlerThread.start()
        handler = Handler((handlerThread).looper)
    }

    /** step 2 Get Permission */
    private fun getPermission() {
        val permission = mutableListOf<String>()
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) permission.add(
            android.Manifest.permission.CAMERA
        )
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) permission.add(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) permission.add(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) permission.add(
            android.Manifest.permission.RECORD_AUDIO
        )

        if (permission.size > 0) {
            requestPermissions(permission.toTypedArray(), 100)
        }

    }

    // Function to get the camera ID
    private fun getCameraId(): String? {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        // Check if cameraIds array is not empty
        if (cameraIds.isNotEmpty()) {
            // Return the first camera ID (assuming it's the rear-facing camera)
            return cameraIds[0]
        }
        // Return null if no camera IDs are available
        return null
    }

    private fun startRecording() {
        try {
            // Initialize MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(getOutputMediaFile().toString())
                setVideoEncodingBitRate(10000000)
                setVideoFrameRate(30)
                setVideoSize(1280, 720)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                prepare()
                start()
            }

            isRecording = true
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting recording: ${e.message}")
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                reset()
                release()
                isRecording = false
                Toast.makeText(this@MainActivity, "Recording stopped", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error stopping recording: ${e.message}")
                Toast.makeText(this@MainActivity, "Failed to stop recording", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        mediaRecorder = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) getPermission()
        }
    }

    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "MyCameraApp"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val mediaFile: File
        val outputFilePath = "${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4"
        mediaFile = File(outputFilePath)
        return mediaFile
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
    }
}