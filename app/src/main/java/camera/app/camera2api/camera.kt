package camera.app.camera2api

//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.ImageFormat
//import android.graphics.SurfaceTexture
//import android.hardware.camera2.*
//import android.media.ImageReader
//import android.os.Bundle
//import android.os.Environment
//import android.os.Handler
//import android.os.HandlerThread
//import android.view.Surface
//import android.view.TextureView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import java.io.File
//import java.io.FileOutputStream
//
//class MainActivity : AppCompatActivity() {
//
//    // Camera variables
//    private lateinit var cameraManager: CameraManager
//    private lateinit var cameraDevice: CameraDevice
//    private lateinit var cameraCaptureSession: CameraCaptureSession
//    private lateinit var captureRequestBuilder: CaptureRequest.Builder
//    private lateinit var imageReader: ImageReader
//
//    // Handler variables for background tasks
//    private lateinit var handler: Handler
//    private lateinit var handlerThread: HandlerThread
//
//    // TextureView for camera preview
//    private lateinit var textureView: TextureView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Initialize UI components
//        textureView = findViewById(R.id.texture_view)
//
//        // Check permissions and initialize camera
//        checkPermissionsAndInitializeCamera()
//    }
//
//    private fun checkPermissionsAndInitializeCamera() {
//        if (checkPermissions()) {
//            initializeCamera()
//        }
//    }
//
//    private fun checkPermissions(): Boolean {
//        val permissions = arrayOf(
//            android.Manifest.permission.CAMERA,
//            android.Manifest.permission.READ_EXTERNAL_STORAGE,
//            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//        )
//        var allPermissionsGranted = true
//        for (permission in permissions) {
//            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
//                allPermissionsGranted = false
//                requestPermissions(permissions, PERMISSION_REQUEST_CODE)
//                break
//            }
//        }
//        return allPermissionsGranted
//    }
//
//    private fun initializeCamera() {
//        // Initialize CameraManager
//        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
//
//        // Start background thread
//        startBackgroundThread()
//
//        // Set up TextureView
//        textureView.surfaceTextureListener = surfaceTextureListener
//
//        // Set up ImageReader
//        imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)
//        imageReader.setOnImageAvailableListener({ reader ->
//            // Handle image capture
//            handleImageCapture(reader)
//        }, handler)
//    }
//
//    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
//        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
//            // Open camera when TextureView is available
//            openCamera()
//        }
//        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}
//        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean = false
//        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
//    }
//
//    private fun startBackgroundThread() {
//        // Start background thread for camera operations
//        handlerThread = HandlerThread("CameraBackground").apply { start() }
//        handler = Handler(handlerThread.looper)
//    }
//
//    private fun openCamera() {
//        // Open the camera
//        val cameraId = getCameraId()
//        cameraManager.openCamera(cameraId!!, cameraStateCallback, handler)
//    }
//
//    private val cameraStateCallback = object : CameraDevice.StateCallback() {
//        override fun onOpened(camera: CameraDevice) {
//            // Camera opened successfully
//            cameraDevice = camera
//            createCameraPreviewSession()
//        }
//        override fun onDisconnected(camera: CameraDevice) {}
//        override fun onError(camera: CameraDevice, error: Int) {}
//    }
//
//    private fun createCameraPreviewSession() {
//        // Create a camera preview session
//        val surfaceTexture = textureView.surfaceTexture
//        surfaceTexture.setDefaultBufferSize(1080, 1920)
//        val surface = Surface(surfaceTexture)
//
//        // Configure capture request
//        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//        captureRequestBuilder.addTarget(surface)
//        captureRequestBuilder.addTarget(imageReader.surface)
//
//        // Create a session
//        cameraDevice.createCaptureSession(listOf(surface, imageReader.surface),
//            cameraCaptureSessionStateCallback, null)
//    }
//
//    private val cameraCaptureSessionStateCallback = object : CameraCaptureSession.StateCallback() {
//        override fun onConfigured(session: CameraCaptureSession) {
//            // Camera capture session configured successfully
//            cameraCaptureSession = session
//            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
//            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
//        }
//        override fun onConfigureFailed(session: CameraCaptureSession) {}
//    }
//
//    private fun handleImageCapture(reader: ImageReader) {
//        // Handle image capture here
//        val image = reader.acquireLatestImage()
//        // Save the image
//        saveImage(image)
//        image.close()
//        Toast.makeText(this@MainActivity, "Image saved", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun saveImage(image: Image) {
//        // Save the image to external storage
//        val buffer = image.planes[0].buffer
//        val bytes = ByteArray(buffer.remaining())
//        buffer.get(bytes)
//
//        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${System.currentTimeMillis()}image.jpeg")
//        val output = FileOutputStream(file)
//        output.write(bytes)
//        output.close()
//    }
//
//    private fun getCameraId(): String? {
//        // Get the ID of the first camera (assuming it's the rear-facing camera)
//        val cameraIds = cameraManager.cameraIdList
//        return if (cameraIds.isNotEmpty()) cameraIds[0] else null
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//}