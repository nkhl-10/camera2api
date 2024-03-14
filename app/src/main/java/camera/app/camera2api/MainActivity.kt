package camera.app.camera2api

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import camera.app.camera2api.databinding.ActivityMainBinding

class MainActivity :AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    private lateinit var cameraViewModel: MainVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraViewModel=MainVM(this)
        cameraViewModel.setPreviewSurface(binding.texture)
    }

    override fun onResume() {
        super.onResume()
        cameraViewModel.startBackgroundThread()
        cameraViewModel.openCamera()
        cameraViewModel.setPreviewSurface(binding.texture)
    }

    override fun onPause() {
        super.onPause()
        cameraViewModel.stopBackgroundThread()
    }
}




/*
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


        */
/*step 4 check permission and camera manager *//*

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


    */
/*step 7 open camera*//*

    @SuppressLint("MissingPermission")
    fun openCamera() {

        val cameraId = cameraManager.cameraIdList[0] // Assuming you're using the first camera
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val previewSize = getPreviewSize(characteristics)
        adjustTextureViewSize(previewSize)

        cameraManager.openCamera(
//            getCameraId()!!,
            cameraManager.cameraIdList[0],
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

    */
/*step 6 surfaceListener *//*

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

    */
/** step 5 start Thread*//*

    private fun startThread() {
        handlerThread = HandlerThread("name")
        handlerThread.start()
        handler = Handler((handlerThread).looper)
    }

    */
/** step 2 Get Permission *//*

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

    private fun adjustTextureViewSize(previewSize: Size) {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val viewWidth = if (isLandscape) binding.texture.width else binding.texture.height
        val viewHeight = if (isLandscape) binding.texture.height else binding.texture.width

        val viewAspectRatio = viewWidth.toFloat() / viewHeight
        val previewAspectRatio = previewSize.width.toFloat() / previewSize.height

        if (previewAspectRatio > viewAspectRatio) {
            // Preview is wider than the view, adjust height to match preview
            val newHeight = (viewWidth / previewAspectRatio).toInt()
            binding.texture.layoutParams.height = newHeight
        } else {
            // Preview is taller than the view, adjust width to match preview
            val newWidth = (viewHeight * previewAspectRatio).toInt()
            binding.texture.layoutParams.width = newWidth
        }

        binding.texture.requestLayout()
    }



    private fun getPreviewSize(characteristics: CameraCharacteristics): Size {
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        // Assuming you want to choose the largest preview size
        val previewSizes = map?.getOutputSizes(SurfaceTexture::class.java)
        // Sort the preview sizes based on their area
        previewSizes?.sortByDescending { it.width * it.height }
        // Return the largest preview size
        return previewSizes?.get(0) ?: Size(0, 0)
    }

}*/
