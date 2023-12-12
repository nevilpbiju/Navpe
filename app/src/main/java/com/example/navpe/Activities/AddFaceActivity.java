package com.example.navpe.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import androidx.core.content.res.ResourcesCompat;

import com.example.navpe.MainActivity;
import com.example.navpe.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.internal.ImageConvertUtils;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.NoSuchPaddingException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddFaceActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 0;
    String imageUrl ="",uid,url;
    FirebaseUser user;
    ImageView previous,part1;
    PreviewView previewView;
    Bitmap ScannedImage = null;
    BitmapDrawable bd;
//    boolean storeFlag = false;
    Bitmap resizedBitmap;
    Button button;
    int viewHeight, viewWidth;

    private static final String TAG = "MLKit Barcode";
    private CameraSelector cameraSelector;
    private Preview previewUseCase;
    private ProcessCameraProvider cameraProvider;
    DatabaseReference databaseReference;
    FaceDetectorOptions realTimeOpts, highAccuracyOpts;
    private ImageAnalysis analysisUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face);

        part1 = findViewById(R.id.faceImage1);
        previewView = findViewById(R.id.activity_main_previewView1);
        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        uid = user.getUid();
        requestCamera();
        button = findViewById(R.id.click);
        showPrevious();
        part1.setOnClickListener(v -> showImageDialog());
        button.setOnClickListener(v -> {
            part1.setBackgroundResource(0);
            highAccuracyOpts = new FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                            .build();
            bindAnalysisUseCase();
        });
        previous = findViewById(R.id.imageView6);
        previous.setOnClickListener(v -> showFaceDialog());
    }

    private void showImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogLayout = LayoutInflater.from(this).inflate(R.layout.preview_image_dialog,null,false);
        ImageView ivFace = dialogLayout.findViewById(R.id.faceImage1);
        Button cancel = dialogLayout.findViewById(R.id.cancel1);
        Button update = dialogLayout.findViewById(R.id.update_face);
        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        ivFace.setBackgroundResource(0);
        cancel.setOnClickListener(v -> alertDialog.dismiss());
        update.setOnClickListener(v -> onDetectFace());
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ivFace.setBackground(bd);
//        Picasso.get().load().into(ivFace);
        alertDialog.show();
    }
    private void showPrevious(){
        databaseReference.child("FaceData").child(uid).child("faceImage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    url = snapshot.getValue().toString();
                    previous.setClickable(true);
                }else{
                    url = "";
                }
                if(url.equals("")){
                    previous.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(),R.drawable.show_image_dark ,getApplicationContext().getTheme()));
                    previous.setClickable(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("", "Error in retrieving the image data " + error);
            }
        });
    }
    private void showFaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogLayout = LayoutInflater.from(this).inflate(R.layout.preview_image,null,false);
        ImageView ivFace = dialogLayout.findViewById(R.id.faceImage);
        ImageView close = dialogLayout.findViewById(R.id.close_btn);
        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        close.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Picasso.get().load(url).into(ivFace);
        alertDialog.show();
    }
    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(AddFaceActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
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
//            bindAnalysisUseCase();
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
        // Real-time contour detection
        realTimeOpts = new FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL).build();
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

        Image image1 = image.getImage();
        InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());
        try {
            ScannedImage = ImageConvertUtils.getInstance().getUpRightBitmap(inputImage);
        } catch (MlKitException e) {
            e.printStackTrace();
        }
        InputImage inputImage1 = InputImage.fromBitmap(ScannedImage, 0);
        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
        detector.process(inputImage1).addOnSuccessListener(this::onSuccessListener)
                .addOnFailureListener(e -> Log.e(TAG, "Can't be connected", e))
                .addOnCompleteListener(task -> image.close());
//        Log.e("From Analyzer",ScannedImage.toString());
    }
    private Bitmap convert(Bitmap bitmap) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }
    public void onDetectFace(){
        if(ScannedImage == null) return;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ScannedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        analysisUseCase.clearAnalyzer();
        byte[] data1 = bytes.toByteArray();

        ByteArrayOutputStream bytes1 = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes1);
        byte[] data2 = bytes1.toByteArray();

        storeInFirebase(data1, "IMG" + uid + "- Original" + ".jpeg");
        storeInFirebase(data2, "IMG" + uid + ".jpeg");

        encrypt(data1);
        startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
    private void encrypt(byte[] data) {
        try {
            AESHelper.encrypt(this, data);
            File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + "encryptedFaceData.jpeg");
            if(file.exists()){
                data = fileToBytes(file);
                Log.e("Byte Array: ", Base64.encodeToString(data, Base64.DEFAULT));
                storeInFirebase(data, "IMG" + uid + "-Encrypted" + ".jpeg");
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IOException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
    private byte[] fileToBytes(File file){
        byte[] bytes = new byte[0];
        try(FileInputStream inputStream = new FileInputStream(file)) {
            bytes = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
    public void storeInFirebase(byte[] data, String filename) {
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
//        String filename = ;
        StorageReference imageRef  = mStorageRef.child("faceData/" + filename);
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> Toast.makeText(this, "Error in uploading this file", Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    String link = filename.contains("Original") ? "Original" : "faceImage";
                    databaseReference.child("FaceData").child(uid).child(link).setValue(imageUrl);
                    Log.e("Link",imageUrl);
                }));
    }
    private void onSuccessListener(List<Face> faces) {

        viewHeight = part1.getHeight();
        viewWidth = part1.getWidth();
        Log.e("From Ml","View Height = " + viewHeight + " Width = "+viewWidth);
        try {
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
//            Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            Bitmap bitmapOrg = ScannedImage;
            Log.e("From <ML>",ScannedImage.toString());

            int targetWidth = bitmapOrg.getWidth();
            int targetHeight = bitmapOrg.getHeight();

            Log.e("From <Ml>Image","Target Height = " + targetHeight + " Width = "+targetWidth);
            Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            RectF rectf = new RectF(0, 0, viewWidth, viewHeight);
            Canvas canvas = new Canvas(targetBitmap);
            Path path = new Path();
            path.addRect(rectf, Path.Direction.CW);
            canvas.clipPath(path);
            canvas.drawBitmap(bitmapOrg, new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight()), new Rect(0, 0, targetWidth, targetHeight), paint);
            Matrix matrix = new Matrix();
            matrix.postScale(1f, 1f);
            Bitmap changedBitmap = convert(bitmapOrg);
            Log.e("From Image", changedBitmap.toString());

            if (faces.size() > 0) {
                Log.e("From <ML> Image","Face Detected from image");
                Face face = faces.get(0);
                Log.e("From Image","x-width = " + (face.getBoundingBox().centerX() + viewWidth) +
                        "\n changedBitmap width = " + changedBitmap.getWidth());

                Log.e("From Image","x-height = " + (face.getBoundingBox().centerX() + viewWidth) +
                        "\n changedBitmap height = " + changedBitmap.getHeight());
                int x = face.getBoundingBox().centerX() - viewWidth/2;
                int y = face.getBoundingBox().centerY() - viewHeight/2;
                resizedBitmap = Bitmap.createBitmap( changedBitmap, x, y,
                        viewWidth, viewHeight, matrix, true);
            } else {
                resizedBitmap = Bitmap.createBitmap( changedBitmap, 0, 0, viewWidth, viewHeight, matrix, true);
            }
            bd = new BitmapDrawable(this.getResources(),resizedBitmap);
            part1.setBackground(bd);
        } catch (Exception e) {
            System.out.println("Error1 : " + e.getMessage() + e);
        }
        analysisUseCase.clearAnalyzer();
    }
}