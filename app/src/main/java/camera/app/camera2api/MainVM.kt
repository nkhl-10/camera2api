package camera.app.camera2api

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MainVM(private val context: Context):ViewModel() {
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader

    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundThread: HandlerThread
    private lateinit var textureView: TextureView

    fun setPreviewSurface(textureView: TextureView) {
        this.textureView = textureView
    }

    fun openCamera() {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // Assuming you're using the first camera
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    cameraDevice.close()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreview() {
        val texture = textureView.surfaceTexture
        texture?.setDefaultBufferSize(1920, 1080) // Set the preview size

        val surface = Surface(texture)
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return

                    captureSession = session
                    try {
                        captureSession.setRepeatingRequest(
                            captureRequestBuilder.build(),
                            null,
                            backgroundHandler
                        )
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            null
        )
    }

    fun takePicture() {
        if (cameraDevice == null) return

        val captureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                // Handle capture completed
            }
        }

        try {
            val imageReader = ImageReader.newInstance(
                1920, 1080, android.graphics.ImageFormat.JPEG, 1
            )
            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(imageReader.surface)
            outputSurfaces.add(Surface(textureView.surfaceTexture))

            val captureBuilder =
                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation)

            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}.jpg"
            )

            val readerListener = ImageReader.OnImageAvailableListener { reader ->
                var image: android.media.Image? = null
                try {
                    image = reader.acquireLatestImage()
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)
                    // Save the image bytes to a file
                    FileOutputStream(file).use { output -> output.write(bytes) }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image?.close()
                }
            }

            imageReader.setOnImageAvailableListener(readerListener, backgroundHandler)

            val captureListener = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    // Handle capture completed
                }
            }

            cameraDevice.createCaptureSession(
                outputSurfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            session.capture(captureBuilder.build(), captureListener, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBackgroundThread()
    }
}
