package com.example.navpe.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navpe.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PaymentWay extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView previewView;

    //    private String PaymentWay;
    String amount, receiverName;
    private static final String TAG = "MLKit Barcode";
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private Preview previewUseCase;
    FaceDetectorOptions realTimeOpts;
    private ImageAnalysis analysisUseCase;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_way);
        previewView = findViewById(R.id.activity_main_previewView1);
//        Intent in = getIntent();
//        amount = in.getStringExtra("Amount");
//        receiverName = in.getStringExtra("ReceiverName");
//
//        Log.e("",amount + receiverName);
//        amount = "₹" + amount;
//
//        TextView textView = findViewById(R.id.amount);
//        textView.setText(amount);
//        TextView textView2 = findViewById(R.id.receiver);
//        textView2.setText(receiverName);
        requestCamera();
    }

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(PaymentWay.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        int lensFacing = CameraSelector.LENS_FACING_FRONT;
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindAllCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "cameraProviderFuture.addListener Error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }
        Preview.Builder builder = new Preview.Builder();
        builder.setTargetRotation(getRotation());

        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase);
        } catch (Exception e) {
            Log.e(TAG, "Error when bind preview", e);
        }
    }

    protected int getRotation() throws NullPointerException {
        if (previewView.getDisplay() == null) return 0;
        return previewView.getDisplay().getRotation();
    }
    public static void restartActivity(Activity activity) {
        activity.recreate();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        restartActivity(this);
    }
    //
    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }


        Executor cameraExecutor = Executors.newSingleThreadExecutor();
        // High-accuracy landmark detection and face classification
//        highAccuracyOpts =
//            new FaceDetectorOptions.Builder()
//                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
//                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
//                    .build();

            // Real-time contour detection
        realTimeOpts =
        new FaceDetectorOptions.Builder()
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .build();

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        builder.setTargetRotation(getRotation());

        analysisUseCase = builder.build();
        analysisUseCase.setAnalyzer(cameraExecutor, this::analyze);

        try {
        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase);
        } catch (Exception e) {
        Log.e(TAG, "Error when bind analysis", e);
        }
    }
    @SuppressLint("UnsafeOptInUsageError")
    private void analyze(@NonNull ImageProxy image) {
        if (image.getImage() == null) return;

        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(),
                image.getImageInfo().getRotationDegrees()
        );
        FaceDetector detector = FaceDetection.getClient(realTimeOpts);
        detector.process(inputImage)
                        .addOnSuccessListener(this::onSuccessListener)
                        .addOnFailureListener(e -> Log.e(TAG, "Barcode process failure", e))
                        .addOnCompleteListener(task -> image.close());
    }
    private void onSuccessListener(List<Face> faces) {
        if (faces.size() > 0) {
            Log.e(TAG, String.valueOf(faces.get(0).getTrackingId()));
            Toast.makeText(this, "Face Detected.", Toast.LENGTH_SHORT).show();

            Intent in = new Intent(PaymentWay.this, PaymentSuccessful.class);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            in.putExtra("Amount", amount);
            in.putExtra("Time", currentDateAndTime);
            in.putExtra("ReceiverName", receiverName);
            int TIMEOUT = 3200;
            Toast.makeText(this, "Recognizing...........", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> startActivity(in), TIMEOUT);
            analysisUseCase.clearAnalyzer();
        }
    }
}
