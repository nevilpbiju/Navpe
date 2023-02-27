//package com.example.navpe.Activities;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.PointF;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.media.FaceDetector;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.view.View;
//import android.widget.ImageView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.navpe.R;
//import com.squareup.picasso.Picasso;
//
//import java.io.ByteArrayOutputStream;
//
//public class FaceFromImage extends AppCompatActivity {
//    public View part1;
//    int viewHeight, viewWidth;
//    private FaceDetector myFaceDetect;
//    private FaceDetector.Face[] myFace;
//    float myEyesDistance;
//
//    @SuppressWarnings("deprecation")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.preview_image);
//        part1 = findViewById(R.id.faceImage);
////        part2 = findViewById(R.id.part2);
//        fromImage();
//    }
//    public void fromImage(){
//        viewHeight = part1.getMeasuredHeight();
//        viewWidth = part1.getMeasuredWidth();
//        try {
//            Paint paint = new Paint();
//            paint.setFilterBitmap(true);
//            Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
//            int targetWidth = bitmapOrg.getWidth();
//            int targetHeight = bitmapOrg.getHeight();
//            Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
//            RectF rectf = new RectF(0, 0, viewWidth, viewHeight);
//            Canvas canvas = new Canvas(targetBitmap);
//            Path path = new Path();
//            path.addRect(rectf, Path.Direction.CW);
//            canvas.clipPath(path);
//            canvas.drawBitmap(bitmapOrg, new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight()), new Rect(0, 0, targetWidth,
//                    targetHeight), paint);
//            Matrix matrix = new Matrix();
//            matrix.postScale(1f, 1f);
//            BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
//            bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
//
//            bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.logo, bitmapFatoryOptions);
//            myFace = new FaceDetector.Face[5];
//            myFaceDetect = new FaceDetector(targetWidth, targetHeight, 5);
//            int numberOfFaceDetected = myFaceDetect.findFaces(bitmapOrg, myFace);
//            Bitmap resizedBitmap = null;
//            if (numberOfFaceDetected > 0) {
//                PointF myMidPoint = null;
//                FaceDetector.Face face = myFace[0];
//                myMidPoint = new PointF();
//                face.getMidPoint(myMidPoint);
//                myEyesDistance = face.eyesDistance();
//
//                if (myMidPoint.x + viewWidth > targetWidth) {
//                    while (myMidPoint.x + viewWidth > targetWidth) {
//                        myMidPoint.x--;
//                    }
//                }
//                if (myMidPoint.y + viewHeight > targetHeight) {
//                    while (myMidPoint.y + viewHeight > targetHeight) {
//                        myMidPoint.y--;
//                    }
//                }
//                resizedBitmap = Bitmap.createBitmap(bitmapOrg,
//                        (int) (myMidPoint.x - myEyesDistance),
//                        (int) (myMidPoint.y - myEyesDistance),
//                        viewWidth, viewHeight, matrix, true);
//            } else {
//                resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
//                        viewWidth, viewHeight, matrix, true);
//            }
//
//        } catch (Exception e) {
//            System.out.println("Error1 : " + e.getMessage() + e);
//        }
//
//    }
//    public Bitmap getCroppedBitmap(Bitmap bitmap) {
//        int targetWidth = bitmap.getWidth();
//        int targetHeight = bitmap.getHeight();
//        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(targetBitmap);
//        Path path = new Path();
//        path.addCircle(((float) targetWidth - 1) / 2,
//                ((float) targetHeight - 1) / 2,
//                (Math.min(((float) targetWidth), ((float) targetHeight)) /    2),
//                Path.Direction.CCW);
//
//        canvas.clipPath(path);
//        Bitmap sourceBitmap = bitmap;
//        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
//                sourceBitmap.getHeight()), new Rect(0, 0, targetWidth,
//                targetHeight), null);
//        return targetBitmap;
//
//    }
//
//}
///*package com.example.navpe.Activities;
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.drawable.BitmapDrawable;
//import android.media.Image;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.example.navpe.MainActivity;
//import com.example.navpe.R;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//import com.google.mlkit.common.MlKitException;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.common.internal.ImageConvertUtils;
//import com.google.mlkit.vision.face.Face;
//import com.google.mlkit.vision.face.FaceDetection;
//import com.google.mlkit.vision.face.FaceDetector;
//import com.google.mlkit.vision.face.FaceDetectorOptions;
//import com.squareup.picasso.Picasso;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//import java.util.Objects;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//public class AddFaceActivity extends AppCompatActivity {
//
//    private static final int PERMISSION_REQUEST_CAMERA = 0;
//    String imageUrl ="",uid,url;
//    FirebaseUser user;
//    ImageView previous,part1;
//    PreviewView previewView;
//    Bitmap ScannedImage = null;
//    boolean storeFlag = false;
//    Button button;
//    int viewHeight, viewWidth;
//
//    private static final String TAG = "MLKit Barcode";
//    private CameraSelector cameraSelector;
//    private Preview previewUseCase;
//    private ProcessCameraProvider cameraProvider;
//    DatabaseReference databaseReference;
//    FaceDetectorOptions realTimeOpts, highAccuracyOpts;
//    private ImageAnalysis analysisUseCase;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_face);
//
//        part1 = findViewById(R.id.faceImage1);
//        previewView = findViewById(R.id.activity_main_previewView1);
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        databaseReference = FirebaseDatabase.getInstance().getReference();
//        uid = user.getUid();
//        requestCamera();
//        button = findViewById(R.id.click);
//        showPrevious();
//        button.setOnClickListener(v -> {
//            storeFlag = true;
//            part1.setBackgroundResource(0);
//            highAccuracyOpts = new FaceDetectorOptions.Builder()
//                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//                            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
//                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
//                            .build();
//
//            bindAnalysisUseCase();
//
//        });
//        previous = findViewById(R.id.imageView6);
//        previous.setOnClickListener(v -> showAddFaceDialog());
//    }
//    private void showPrevious(){
//        databaseReference.child("FaceData").child(uid).child("faceImage").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                url = Objects.requireNonNull(snapshot.getValue()).toString();
//                if(!url.equals("")){
//                    button.setText(R.string.update_face);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("", "Error in retrieving the image data " + error);
//            }
//        });
//    }
//    private void showAddFaceDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
////        LayoutInflater inflater = getLayoutInflater();
//        View dialogLayout = LayoutInflater.from(this).inflate(R.layout.preview_image,null,false);
//        ImageView ivFace = dialogLayout.findViewById(R.id.faceImage);
//        ImageView close = dialogLayout.findViewById(R.id.close_btn);
//        builder.setView(dialogLayout);
//        AlertDialog alertDialog = builder.create();
//        alertDialog.setCanceledOnTouchOutside(false);
//        close.setOnClickListener(v -> alertDialog.dismiss());
//        Picasso.get().load(url).into(ivFace);
//        alertDialog.show();
//    }
//    private void requestCamera() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            setupCamera();
//        } else {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//                ActivityCompat.requestPermissions(AddFaceActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
//            }
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_CAMERA) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setupCamera();
//            } else {
//                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//    private void setupCamera() {
//        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
//                ProcessCameraProvider.getInstance(this);
//
//        int lensFacing = CameraSelector.LENS_FACING_FRONT;
//        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
//
//        cameraProviderFuture.addListener(() -> {
//            try {
//                cameraProvider = cameraProviderFuture.get();
//                bindAllCameraUseCases();
//            } catch (ExecutionException | InterruptedException e) {
//                Log.e(TAG, "cameraProviderFuture.addListener Error", e);
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private void bindAllCameraUseCases() {
//        if (cameraProvider != null) {
//            cameraProvider.unbindAll();
//            bindPreviewUseCase();
////            bindAnalysisUseCase();
//        }
//    }
//    private void bindPreviewUseCase() {
//        if (cameraProvider == null) {
//            return;
//        }
//        if (previewUseCase != null) {
//            cameraProvider.unbind(previewUseCase);
//        }
//        Preview.Builder builder = new Preview.Builder();
//        builder.setTargetRotation(getRotation());
//
//        previewUseCase = builder.build();
//        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
//
//        try {
//            cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase);
//        } catch (Exception e) {
//            Log.e(TAG, "Error when bind preview", e);
//        }
//    }
//
//    protected int getRotation() throws NullPointerException {
//        if (previewView.getDisplay() == null) return 0;
//        return previewView.getDisplay().getRotation();
//    }
//    public static void restartActivity(Activity activity) {
//        activity.recreate();
//    }
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        restartActivity(this);
//    }
//    //
//    private void bindAnalysisUseCase() {
//        if (cameraProvider == null) {
//            return;
//        }
//        if (analysisUseCase != null) {
//            cameraProvider.unbind(analysisUseCase);
//        }
//        Executor cameraExecutor = Executors.newSingleThreadExecutor();
//        // Real-time contour detection
//        realTimeOpts = new FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL).build();
//
//        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
//        builder.setTargetRotation(getRotation());
//
//        analysisUseCase = builder.build();
//        analysisUseCase.setAnalyzer(cameraExecutor, this::analyze);
//
//        try {
//            cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase);
//        } catch (Exception e) {
//            Log.e(TAG, "Error when bind analysis", e);
//        }
//    }
//    @SuppressLint("UnsafeOptInUsageError")
//    private void analyze(@NonNull ImageProxy image) {
//        if (image.getImage() == null) return;
//
//        Image image1 = image.getImage();
//        InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());
//        try {
//            ScannedImage = ImageConvertUtils.getInstance().getUpRightBitmap(inputImage);
//        } catch (MlKitException e) {
//            e.printStackTrace();
//        }
//        InputImage inputImage1 = InputImage.fromBitmap(ScannedImage, 0);
//        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
//        detector.process(inputImage1).addOnSuccessListener(this::onSuccessListener)
//                .addOnFailureListener(e -> Log.e(TAG, "Can't be connected", e))
//                .addOnCompleteListener(task -> image.close());
////        Log.e("From Analyzer",ScannedImage.toString());
//    }
//
////    public void fromImage(){
////        viewHeight = part1.getHeight();
////        viewWidth = part1.getWidth();
////        Log.e("From Image","View Height = " + viewHeight + " Width = "+ viewWidth);
////        try {
////            Paint paint = new Paint();
////            paint.setFilterBitmap(true);
//////            Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
////            Bitmap bitmapOrg = ScannedImage;
////            Log.e("From Image",ScannedImage.toString());
////
////            int targetWidth = bitmapOrg.getWidth();
////            int targetHeight = bitmapOrg.getHeight();
////
////            Log.e("From Image","Target Height = " + targetHeight + " Width = "+targetWidth);
////            Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
////            RectF rectF = new RectF(0, 0, viewWidth, viewHeight);
////            Canvas canvas = new Canvas(targetBitmap);
////            Path path = new Path();
////            path.addRect(rectF, Path.Direction.CW);
////            canvas.clipPath(path);
////            canvas.drawBitmap(bitmapOrg, new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight()), new Rect(0, 0, targetWidth, targetHeight), paint);
////            Matrix matrix = new Matrix();
////            matrix.postScale(1f, 1f);
////            Bitmap changedBitmap = convert(bitmapOrg);
////
////            Log.e("From Image", changedBitmap.toString());
////
////            android.media.FaceDetector.Face[] myFace = new android.media.FaceDetector.Face[5];
////            android.media.FaceDetector myFaceDetect = new android.media.FaceDetector(targetWidth, targetHeight, 5);
////            int numberOfFaceDetected = myFaceDetect.findFaces(bitmapOrg, myFace);
////            Bitmap resizedBitmap = null;
////            if (numberOfFaceDetected > 0) {
////                Log.e("From Image","Face Detected from image");
////                PointF myMidPoint = null;
////                android.media.FaceDetector.Face face = myFace[0];
////                myMidPoint = new PointF();
////                face.getMidPoint(myMidPoint);
////                Log.e("From Image", myMidPoint.x + " " + myMidPoint.y);
////                myEyesDistance = face.eyesDistance();
////                if (myMidPoint.x + viewWidth > targetWidth) {
////                    while (myMidPoint.x + viewWidth > targetWidth) {
////                        myMidPoint.x--;
////                    }
////                }
////                if (myMidPoint.y + viewHeight > targetHeight) {
////                    while (myMidPoint.y + viewHeight > targetHeight) {
////                        myMidPoint.y--;
////                    }
////                }
////                Log.e("From Image","Eye = " +  myEyesDistance + "\nx-width: "+ (int)(myMidPoint.x -myEyesDistance) + viewWidth
////                        +"\nx-width: "+ (int)(myMidPoint.x -myEyesDistance) + viewWidth);
////
////                resizedBitmap = Bitmap.createBitmap( changedBitmap, (int) (myMidPoint.x -myEyesDistance), (int) (myMidPoint.y - myEyesDistance),
////                        viewWidth, viewHeight, matrix, true);
////            } else {
////                resizedBitmap = Bitmap.createBitmap( changedBitmap, 0, 0,
////                        viewWidth, viewHeight, matrix, true);
////            }
////            BitmapDrawable bd = new BitmapDrawable(resizedBitmap);
////            part1.setBackgroundDrawable(bd);
////
////        } catch (Exception e) {
////            System.out.println("Error1 : " + e.getMessage() + e);
////        }
//////        onDetectFace();
////    }
//    private Bitmap convert(Bitmap bitmap) {
//        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
//        Canvas canvas = new Canvas(convertedBitmap);
//        Paint paint = new Paint();
//        paint.setColor(Color.BLACK);
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//        return convertedBitmap;
//    }
//    public void onDetectFace(){
//        if(ScannedImage == null) return;
//        if(storeFlag){
//            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            ScannedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//            analysisUseCase.clearAnalyzer();
//            byte[] data1 = bytes.toByteArray();
//            storeInFirebase(data1);
//            Intent in = new Intent(getApplicationContext(), MainActivity.class);
//            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(in);
//            finish();
//        }
//    }
//    public void storeInFirebase(byte[] data) {
//        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
//        String filename = "IMG" + uid + ".jpeg";
//        StorageReference imageRef  = mStorageRef.child("faceData/" + filename);
//        UploadTask uploadTask = imageRef.putBytes(data);
//        uploadTask.addOnFailureListener(exception -> Toast.makeText(this, "Error in uploading this file", Toast.LENGTH_SHORT).show())
//                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                    imageUrl = uri.toString();
//                    databaseReference.child("FaceData").child(uid).child("faceImage").setValue(imageUrl);
//                    Log.e("Link",imageUrl);
//                }));
//    }
//    private void onSuccessListener(List<Face> faces) {
//        viewHeight = part1.getHeight();
//        viewWidth = part1.getWidth();
//        Log.e("From Ml","View Height = " + viewHeight + " Width = "+viewWidth);
//        try {
//            Paint paint = new Paint();
//            paint.setFilterBitmap(true);
////            Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
//            Bitmap bitmapOrg = ScannedImage;
//            Log.e("From <ML>",ScannedImage.toString());
//
//            int targetWidth = bitmapOrg.getWidth();
//            int targetHeight = bitmapOrg.getHeight();
//
//            Log.e("From <Ml>Image","Target Height = " + targetHeight + " Width = "+targetWidth);
//            Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
//            RectF rectf = new RectF(0, 0, viewWidth, viewHeight);
//            Canvas canvas = new Canvas(targetBitmap);
//            Path path = new Path();
//            path.addRect(rectf, Path.Direction.CW);
//            canvas.clipPath(path);
//            canvas.drawBitmap(bitmapOrg, new Rect(0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight()), new Rect(0, 0, targetWidth, targetHeight), paint);
//            Matrix matrix = new Matrix();
//            matrix.postScale(1f, 1f);
//            Bitmap changedBitmap = convert(bitmapOrg);
//            Log.e("From Image", changedBitmap.toString());
//
//            Bitmap resizedBitmap;
//            if (faces.size() > 0) {
//                Log.e("From <ML> Image","Face Detected from image");
//                Face face = faces.get(0);
//                Log.e("From Image","x-width = " + (face.getBoundingBox().centerX() + viewWidth) +
//                        "\n changedBitmap width = " + changedBitmap.getWidth());
//
//                Log.e("From Image","x-height = " + (face.getBoundingBox().centerX() + viewWidth) +
//                        "\n changedBitmap height = " + changedBitmap.getHeight());
//                int x = face.getBoundingBox().centerX() - viewWidth/2;
//                int y = face.getBoundingBox().centerY() - viewHeight/2;
//                resizedBitmap = Bitmap.createBitmap( changedBitmap, x, y,
//                        viewWidth, viewHeight, matrix, true);
//            } else {
//                resizedBitmap = Bitmap.createBitmap( changedBitmap, 0, 0, viewWidth, viewHeight, matrix, true);
//            }
//            BitmapDrawable bd = new BitmapDrawable(resizedBitmap);
//            part1.setBackgroundDrawable(bd);
//        } catch (Exception e) {
//            System.out.println("Error1 : " + e.getMessage() + e);
//        }
//    }
//}
//
//
///*
//    private Bitmap toBitmap(Image image) {
//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer yBuffer = planes[0].getBuffer();
//        ByteBuffer uBuffer = planes[1].getBuffer();
//        ByteBuffer vBuffer = planes[2].getBuffer();
//
//        int ySize = yBuffer.remaining();
//        int uSize = uBuffer.remaining();
//        int vSize = vBuffer.remaining();
//
//        byte[] nv21 = new byte[ySize + uSize + vSize];
//        //U and V are swapped
//        yBuffer.get(nv21, 0, ySize);
//        vBuffer.get(nv21, ySize, vSize);
//        uBuffer.get(nv21, ySize + vSize, uSize);
//
//        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
//
//        byte[] imageBytes = out.toByteArray();
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//        private static Bitmap rotateImage(Bitmap img, int degree) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(degree);
//        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
//        img.recycle();
//        return rotatedImg;
//    }
//    public Bitmap getCroppedBitmap(Bitmap bitmap) {
//        int targetWidth = bitmap.getWidth();
//        int targetHeight = bitmap.getHeight();
//        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
//                Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(targetBitmap);
//        Path path = new Path();
//        path.addCircle(((float) targetWidth - 1) / 2,
//                ((float) targetHeight - 1) / 2,
//                (Math.min(((float) targetWidth), ((float) targetHeight)) /    2),
//                Path.Direction.CCW);
//
//        canvas.clipPath(path);
//        Bitmap sourceBitmap = bitmap;
//        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
//                sourceBitmap.getHeight()), new Rect(0, 0, targetWidth,
//                targetHeight), null);
//        return targetBitmap;
//    }
//    }*/
////    Bitmap croppedBitmap = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888);*/
