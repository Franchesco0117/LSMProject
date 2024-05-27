package com.francisco.lsmproyect;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    private Mat mRgba;
    private Mat mGrey;
    private CameraBridgeViewBase mOpenCvCameraView;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    private objectDetectorClass objectDetectorClass;
    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface
                        .SUCCESS: {
                    Log.i(TAG, "OpenCV esta cargado.");
                    mOpenCvCameraView.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instanciando nuevo " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        // If camera permission is not given it will ask for it
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.frameSurface);

        try {
            objectDetectorClass = new objectDetectorClass
                    (getAssets(), "hand_model.tflite", "custom_label.txt", 300, "Sign_language_model.tflite", 96);
            Log.d("MainActivity", "Modelo exitosamente cargado");

        } catch (IOException e) {
            Log.d("MainActivity", "Error");
            e.printStackTrace();
        }

        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permisos concedidos");
            initializeCamera(mOpenCvCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Solicitud de permiso");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_LONG).show();
                initializeCamera(mOpenCvCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeCamera(CameraBridgeViewBase mOpenCvCameraView, int activeCamera){
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.setCameraIndex(activeCamera);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //Si la carga es existosa
            Log.d(TAG, "Inicializacion de OpenCV terminada");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            //Si la carga NO es existosa
            Log.d(TAG, "OpenCV no esta cargado. Intenta otra vez");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallBack);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGrey = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGrey = inputFrame.gray();

        Mat out = new Mat();
        out = objectDetectorClass.recognizeImage(mRgba);

        return out;
    }
}