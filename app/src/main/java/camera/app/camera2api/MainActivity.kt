package camera.app.camera2api

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import camera.app.camera2api.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var cameraManager: CameraManager
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest.Builder
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

    }


    /*step 7 open camera*/
    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(
            cameraManager.cameraIdList[0],
            object : CameraDevice.StateCallback() {
                override fun onOpened(p0: CameraDevice) {
                    cameraDevice = p0
                    captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
                    val surfaceTexture=SurfaceTexture(1)
                    val surface = Surface(binding.texture.surfaceTexture)
                    captureRequest.addTarget(surface)
                    cameraDevice.createCaptureSession(listOf(surface), object :
                        CameraCaptureSession.StateCallback() {
                        override fun onConfigured(p0: CameraCaptureSession) {
                            cameraCaptureSession = p0
                            cameraCaptureSession.setRepeatingRequest(captureRequest.build(), null, null
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

        if (permission.size > 0) {
            requestPermissions(permission.toTypedArray(), 100)
        }

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
}