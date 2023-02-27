//package com.example.navpe.Activities;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.DialogFragment;
//
//import com.example.navpe.R;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.face.FirebaseVisionFace;
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
//
//import java.util.Objects;
//
//public class FacePay extends AppCompatActivity {
//    Button cameraButton;
//
//    private final static int REQUEST_IMAGE_CAPTURE = 124;
//    FirebaseVisionImage image;
//    FirebaseVisionFaceDetector detector;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_face_pay);
//
//        cameraButton = findViewById(R.id.camera_button);
//        cameraButton.setOnClickListener(
//                v -> {
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    if (intent.resolveActivity(getPackageManager()) != null) {
//                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//                    }else {
//                        Toast.makeText(FacePay.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extra = Objects.requireNonNull(data).getExtras();
//            Bitmap bitmap = (Bitmap)extra.get("data");
//            detectFace(bitmap);
//        }
//    }
//
//    private void detectFace(Bitmap bitmap)
//    {
//        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions
//                .Builder().setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE).
//                setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
//                .setClassificationMode( FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//                .build();
//        try {
//            image = FirebaseVisionImage.fromBitmap(bitmap);
//            detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        detector.detectInImage(image).addOnSuccessListener(firebaseVisionFaces -> {
//                    String resultText = "";
//                    int i = 1;
//                    for (FirebaseVisionFace face : firebaseVisionFaces) {
//                        resultText = resultText.concat("\nFACE NUMBER. " + i + ": ")
//                                .concat("\nSmile: "+ face.getSmilingProbability() * 100 + "%")
//                                .concat("\nleft eye open: "+ face.getLeftEyeOpenProbability() * 100 + "%")
//                                .concat("\nright eye open " + face.getRightEyeOpenProbability() * 100 + "%");
//                        i++;
//                    }
//                    if (firebaseVisionFaces.size() == 0) {
//                        Toast.makeText(FacePay.this,"NO FACE DETECT",Toast.LENGTH_SHORT).show();
//                    } else {
//                        Bundle bundle = new Bundle();
//                        bundle.putString(
//                                LCOFaceDetection.RESULT_TEXT, resultText);
//                        DialogFragment resultDialog = new ResultDialog();
//                        resultDialog.setArguments(bundle);
//                        resultDialog.setCancelable(true);
//                        resultDialog.show(getSupportFragmentManager(), LCOFaceDetection.RESULT_DIALOG);
//                    }
//                })
//                .addOnFailureListener(e -> Toast.makeText(FacePay.this, "Oops, Something went wrong", Toast.LENGTH_SHORT).show());
//    }
//}
