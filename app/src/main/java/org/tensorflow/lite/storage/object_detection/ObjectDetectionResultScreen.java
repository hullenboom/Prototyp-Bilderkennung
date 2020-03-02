package org.tensorflow.lite.storage.object_detection;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.main.DetectionType;
import org.tensorflow.lite.camera.base.env.Logger;
import org.tensorflow.lite.camera.base.tflite.Recognition;
import org.tensorflow.lite.camera.object_detection.tflite.ClassifierObjectDetection;
import org.tensorflow.lite.camera.object_detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.storage.base.ImageProccessor;
import org.tensorflow.lite.storage.base.LocalBitmap;
import org.tensorflow.lite.storage.base.ResultScreenCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import masterarbeit.R;

public class ObjectDetectionResultScreen extends AppCompatActivity {
    private static final Logger LOGGER = new Logger();

    private static final int TF_OD_API_INPUT_SIZE = 300;

    private ViewFlipper simpleViewFlipper;
    private ClassifierObjectDetection detector;
    private static final String TF_OD_API_MODEL_FILE = "object_detection_198k_mobilenetv2.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final String HANDLE_THREAD_NAME = "Classifier";


    private float initialX;


    private HandlerThread backgroundThread;

    private Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        int cropSize = TF_OD_API_INPUT_SIZE;


        ImageProccessor imageProccessor = new ImageProccessor();
        ArrayList<String> inputUris = getIntent().getStringArrayListExtra("test");
        List<LocalBitmap> localBitmaps = new ArrayList<>();
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            false);
            localBitmaps = imageProccessor.getAdjustedBitmapsFromURIString(this, inputUris);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "ClassifierObjectDetection could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        setContentView(R.layout.storage_result_screen_viewflipper);
        simpleViewFlipper = (ViewFlipper) findViewById(R.id.simpleViewFlipper);

        if (!localBitmaps.isEmpty()) {

            // loop for creating ImageView'
            for (LocalBitmap localBitmap : localBitmaps) {
                Bitmap originalLoadedBitmap = Bitmap.createBitmap(localBitmap.getBitmap());
                Bitmap croppedBitmap = Utils.getResizedBitmap(localBitmap.getBitmap(), 300, 300);
                long startTime = SystemClock.uptimeMillis();
                List<Recognition> results = detector.recognizeImage(croppedBitmap);
                long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                ConstraintLayout resultViewForBitmap = ResultScreenCreator.createConstraintLayout(this, DetectionType.OBJECT_DETECTION, localBitmap.getFileName(), originalLoadedBitmap, results, lastProcessingTimeMs);
                simpleViewFlipper.addView(resultViewForBitmap);
                //simpleViewFlipper.addView(this.createConstraintLayout(localBitmap.getFileName(), bitmaptoshow, results, lastProcessingTimeMs));

            }
        }
    }

    public boolean onTouchEvent(MotionEvent touchevent)
    {
        switch (touchevent.getAction())
        {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN:
            {
                initialX = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                float currentX = touchevent.getX();

                // if left to right swipe on screen
                if (initialX < currentX)
                {
                    // If no more View/Child to flip
                    if (simpleViewFlipper.getDisplayedChild() == 0)
                        break;

                    simpleViewFlipper.showNext();
                }

                // if right to left swipe on screen
                if (initialX > currentX)
                {
                    if (simpleViewFlipper.getDisplayedChild() == 1)
                        break;

                    simpleViewFlipper.showPrevious();
                }
                break;
            }
        }
        return false;
    }

}
