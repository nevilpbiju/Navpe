package com.example.navpe.Recognitions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.navpe.Activities.PaymentSuccessful;
import com.example.navpe.Activities.UPI_Pin;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    // MobileFaceNet
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    //Model files used from tenserFlow
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final float TEXT_SIZE_DIP = 10;
    //Tracking activity
    private MultiBoxTracker tracker;
    OverlayView trackingOverlay;
    //Get the interface for Recognizing the image
    private Integer sensorOrientation;
    private SimilarityClassifier detector;
    //Bitmaps to store the color and cropped image
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap portraitBmp = null;
    private Bitmap urlBmp = null;
    // here the face is cropped and drawn
    private Bitmap faceBmp = null;
    //Flag to set if the face is already known or not
    private boolean addFlag = false;
    private boolean computingDetection = false;
    //Get the url of the base image [already known face]
    private String url;
    private long timestamp = 0;
    //To copy the frames
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    //private Matrix cropToPortraitTransform;
    SimilarityClassifier.Recognition result;
    String amount, receiverName;
    // Face detector
    private FaceDetector faceDetector;
    FirebaseUser user;
    FirebaseAuth auth;
//    private final HashMap<String, SimilarityClassifier.Recognition> knownFaces = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        //Get the details from the previous activity
        Intent in = getIntent();
        amount = in.getStringExtra("Amount");
        String upi = in.getStringExtra("UPI");
        receiverName = in.getStringExtra("ReceiverName");
        Log.e("",amount + receiverName);
        amount = "₹" + amount;
        TextView textView = findViewById(R.id.amount);
        textView.setText(amount);
        TextView textView2 = findViewById(R.id.receiver);
        textView2.setText(receiverName);

        //On UPI pin click
        findViewById(R.id.pin_use).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UPI_Pin.class);
            intent.putExtra("Function", "Pay");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            intent.putExtra("Amount", amount);
            intent.putExtra("Time", currentDateAndTime);
            intent.putExtra("UPI", upi);
            intent.putExtra("ReceiverName", receiverName);
            startActivity(intent);
            finish();
        });
        //On confirm click
        findViewById(R.id.confirm_pay).setOnClickListener(v -> {
            if(result.getTitle().equals(Objects.requireNonNull(user.getPhoneNumber()).substring(3)) && result.getDistance() >= 0.63){
                Intent ind = new Intent(this, PaymentSuccessful.class);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                String currentDateAndTime = sdf.format(new Date());
                ind.putExtra("Amount", amount);
                ind.putExtra("Time", currentDateAndTime);
                ind.putExtra("UPI", upi);
                ind.putExtra("ReceiverName", receiverName);
                int TIMEOUT = 2200;
                Toast.makeText(this, "Valid Match Found.....", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> {
                    ind.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(ind);
                    finish();
                }, TIMEOUT);
            }else{
                Toast.makeText(this, "Please try again....", Toast.LENGTH_SHORT).show();
            }
        });

        //Load the image from the firebase
        LoadImage();
        // Real-time contour detection of multiple faces
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();
        faceDetector = FaceDetection.getClient(options);
    }
    //Function to load image from the firebase
    public void LoadImage(){
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().child("FaceData").child(uid).child("Original").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                url = Objects.requireNonNull(snapshot.getValue()).toString();
                if(url.length() > 0) addFlag = true;
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> getBitmapFromURL(url));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("", "Error in retrieving the image data " + error);
            }
        });
    }
    //Get the Bitmap from the given URL
    private void getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            urlBmp = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        //For Camera2 activity
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        BorderedText borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        //Create a tracker object which keeps track of the user
        tracker = new MultiBoxTracker(this);
        try {
//              Get the detector object
                detector = TFLiteObjectDetectionAPIModel.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE, TF_OD_API_IS_QUANTIZED);
                //cropSize = TF_OD_API_INPUT_SIZE;
        }catch (final IOException e) {
              e.printStackTrace();
//              LOGGER.e(e, "Exception initializing classifier!");
              Toast.makeText(getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT).show();
              finish();
        }
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();
        sensorOrientation = rotation - getScreenOrientation();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        int targetW, targetH;
        if (sensorOrientation == 90 || sensorOrientation == 270) {
            targetH = previewWidth;
            targetW = previewHeight;
        }else {
            targetW = previewWidth;
            targetH = previewHeight;
        }
        int cropW = (int) (targetW / 2.0), cropH = (int) (targetH / 2.0);
        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);
        portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight, cropW, cropH, sensorOrientation, MAINTAIN_ASPECT);
        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
        //    Matrix frameToPortraitTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight, targetW, targetH, sensorOrientation, MAINTAIN_ASPECT);
        //Now here the overlay is called which draw above the image
        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(canvas -> {
                  tracker.draw(canvas);
                  if (isDebug()) {tracker.drawDebug(canvas);}
        });
        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }
    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        readyForNextImage();
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        //Setting the input image to a cropped image
        InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
        //Detect the face from the image using Google Face Detection
        faceDetector.process(image).addOnSuccessListener(faces -> {
            if (faces.size() == 0) {
                    updateResults(currTimestamp, new LinkedList<>());
                    return;
            }
            runInBackground(() -> {
                onFacesDetected(currTimestamp, faces,addFlag);
                addFlag = false;
            });
        });
    }
    @Override
    protected int getLayoutId() {return R.layout.tfe_od_camera_connection_fragment_tracking;}
    @Override
    protected Size getDesiredPreviewFrameSize() {return DESIRED_PREVIEW_SIZE;}
    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }
    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
    // Face Processing
    private Matrix createTransform(final int srcWidth, final int srcHeight, final int dstWidth, final int dstHeight, final int applyRotation) {
        Matrix matrix = new Matrix();
        //If the face rotation is not 0 then make it to the center
        if (applyRotation != 0) {
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);
            matrix.postRotate(applyRotation);
        }
        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }
        return matrix;
    }
    private void updateResults(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions) {
        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();
        computingDetection = false;
        //adding = false;
        if (mappedRecognitions.size() > 0) {
//            LOGGER.i("Adding results");
            SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
            if (rec.getExtra() != null) {
//                Log.e("","Called DiaLOG");
                detector.register(Objects.requireNonNull(user.getPhoneNumber()).substring(3), rec);
            }
        }
    }
    private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {

        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2.0f);
        final List<SimilarityClassifier.Recognition> mappedRecognitions = new LinkedList<>();
        //final List<Classifier.Recognition> results = new ArrayList<>();
        //Get the function to crop the image
        int sourceW = rgbFrameBitmap.getWidth();
        int sourceH = rgbFrameBitmap.getHeight();
        int targetW = portraitBmp.getWidth();
        int targetH = portraitBmp.getHeight();
        Matrix transform = createTransform(sourceW, sourceH, targetW, targetH, sensorOrientation);
        final Canvas cv = new Canvas(portraitBmp);

        // draws the original image in portrait mode.
        cv.drawBitmap(rgbFrameBitmap, transform, null);
        final Canvas cvFace = new Canvas(faceBmp);
        //For each face
        for (Face face : faces) {
            //Get the bounding rectangle around the face
            final RectF boundingBox = new RectF(face.getBoundingBox());
            // maps crop coordinates to original
            cropToFrameTransform.mapRect(boundingBox);
            // maps original coordinates to portrait coordinates
            RectF faceBB = new RectF(boundingBox);
            transform.mapRect(faceBB);
            // translates portrait to origin and scales to fit input inference size
            //cv.drawRect(faceBB, paint);
            float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
            float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
            Matrix matrix = new Matrix();
            matrix.postTranslate(-faceBB.left, -faceBB.top);
            matrix.postScale(sx, sy);

            cvFace.drawBitmap(portraitBmp, matrix, null);
            //canvas.drawRect(faceBB, paint);
            String label = "";
            float confidence = -1f;
            int color = Color.BLUE;
            Object extra = null;
            Bitmap crop = null;
            //If the input data is already define do this function[only once]
            if(add){
                crop =  (url != null && urlBmp != null) ? Bitmap.createBitmap(urlBmp,
                        (int) faceBB.left,
                        (int) faceBB.top,
                        (int) faceBB.width(),
                        (int) faceBB.height()) : null;
                url = "";
            }
            //Get the Image from the tenser flow recognition activity
            final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp,addFlag);
            //If the result is greater than 0
            if (resultsAux.size() > 0) {
                SimilarityClassifier.Recognition result = resultsAux.get(0);
                extra = result.getExtra();
                float conf = result.getDistance();
                if (conf < 1.0f) {
                      confidence = conf;
                      label = result.getTitle();
                      color = result.getId().equals("0") ? Color.GREEN : Color.RED;
                }
            }
            //If the camera is front then flip the image
            if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {
                Matrix flip = new Matrix();
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
                }
                else {
                    flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
                }
                flip.mapRect(boundingBox);
            }
            //Set the result as the first image and set all its parameter
            result = new SimilarityClassifier.Recognition("0", label, confidence, boundingBox);
            result.setColor(color);
            result.setLocation(boundingBox);
            result.setExtra(extra);
            result.setCrop(crop);
            mappedRecognitions.add(result);
        }
        updateResults(currTimestamp, mappedRecognitions);
    }
}
//        For examining the actual TF input.
//        if (SAVE_PREVIEW_BITMAP) {
//            ImageUtils.saveBitmap(croppedBitmap);
//        }
//        Bitmap cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
//        final Canvas canvas = new Canvas(cropCopyBitmap);
//        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);
//        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
//        flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
//        detector.register("Varun", mappedRecognitions.get(0));
