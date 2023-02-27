package com.example.navpe.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Adapters.SuggestionAdapter;
import com.example.navpe.Models.GetSuggestion;
import com.example.navpe.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class QRCode extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    TextView backButton;
    RecyclerView suggestedRecyclerView;
    private PreviewView previewView;

    Executor cameraExecutor;
    //    private String qrCode;
    private static final String TAG = "MLKit Barcode";
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private Preview previewUseCase;
    private ImageAnalysis analysisUseCase;
    ArrayList<GetSuggestion> getSuggestions = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        getSuggestions.add(new GetSuggestion("Varun Wadhwa", ""));
        getSuggestions.add(new GetSuggestion("Austin Johnson", ""));
        getSuggestions.add(new GetSuggestion("Nevil P Biju", ""));
        suggestedRecyclerView = findViewById(R.id.name1);
        SuggestionAdapter SuggestionAdapter = new SuggestionAdapter(getSuggestions);
        suggestedRecyclerView.setAdapter(SuggestionAdapter);
        suggestedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        ImageView gallery = findViewById(R.id.galleryQR);
        gallery.setOnClickListener(v -> openGallery());

        backButton = findViewById(R.id.header2);
        backButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() <= backButton.getTotalPaddingLeft()) {
                    onBackPressed();
                    return true;
                }
            }
            return true;
        });
        previewView = findViewById(R.id.activity_main_previewView);
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }
    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(QRCode.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
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

        int lensFacing = CameraSelector.LENS_FACING_BACK;
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

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

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
    protected int getRotation() throws NullPointerException {
        if(previewView.getDisplay() == null) return 0;
        return previewView.getDisplay().getRotation();
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyze(@NonNull ImageProxy image) {
        if (image.getImage() == null) return;

        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(),
                image.getImageInfo().getRotationDegrees()
        );
        BarcodeScanner barcodeScanner = BarcodeScanning.getClient();

        barcodeScanner.process(inputImage)
                .addOnSuccessListener(this::onSuccessListener)
                .addOnFailureListener(e -> Log.e(TAG, "Barcode process failure", e))
                .addOnCompleteListener(task -> image.close());
    }
    public static void restartActivity(Activity activity){
            activity.recreate();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restartActivity(this);
    }

    private void onSuccessListener(List<Barcode> barcodes) {
        if (barcodes.size() > 0) {
//            Log.e(TAG, barcodes.get(0).getDisplayValue());
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(200);
            }
            if(Objects.requireNonNull(barcodes.get(0).getDisplayValue()).startsWith("upi")){
                Intent in = new Intent(QRCode.this, PayActivity.class);
                in.putExtra("Payment",barcodes.get(0).getDisplayValue());
                startActivity(in);
            }else{
                Toast.makeText(this,"Not a UPI QR code",Toast.LENGTH_SHORT).show();
            }
//            Toast.makeText(this, barcodes.get(0).getDisplayValue(), Toast.LENGTH_SHORT).show();
            analysisUseCase.clearAnalyzer();
        }
    }
    private void openGallery() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntentResultLauncher.launch(pickPhoto);
            }else {
                Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    ActivityResultLauncher<Intent> galleryIntentResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            Uri mediaUri = data != null ? data.getData() : null;
            InputImage image;
            try {
                if (mediaUri != null) {
                    image = InputImage.fromFilePath(getApplicationContext(), mediaUri);
                    BarcodeScanner scanner = BarcodeScanning.getClient();
                    scanner.process(image)
                            .addOnSuccessListener(this::onSuccessListener)
                            .addOnFailureListener(e -> Log.e(TAG, "Barcode process failure", e));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}