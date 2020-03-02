package org.tensorflow.lite.storage.image_classification;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.main.DetectionType;
import org.tensorflow.lite.camera.base.tflite.Recognition;
import org.tensorflow.lite.camera.image_classification.tflite.Classifier;
import org.tensorflow.lite.storage.base.ImageProccessor;
import org.tensorflow.lite.storage.base.LocalBitmap;
import org.tensorflow.lite.storage.base.ResultScreenCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import masterarbeit.R;

public class ImageClassificationResultScreen extends AppCompatActivity {

    private ViewFlipper simpleViewFlipper;
    Classifier classifier;
    private float initialX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageProccessor imageProccessor = new ImageProccessor();
        ArrayList<String> inputUris = getIntent().getStringArrayListExtra("test");
        List<LocalBitmap> localBitmaps = new ArrayList<>();
        try{
            classifier = Classifier.create(this, Classifier.Model.FLOAT, Classifier.Device.CPU, 1);
            localBitmaps = imageProccessor.getAdjustedBitmapsFromURIString(this, inputUris);
        }  catch (IOException e) {
            Log.e("exception", e.getLocalizedMessage());
        }
        for (String uri : inputUris){
            Log.d("Test", uri);
        }

        setContentView(R.layout.storage_result_screen_viewflipper);
        simpleViewFlipper = (ViewFlipper) findViewById(R.id.simpleViewFlipper); // get the reference of ViewFlipper

        if(!localBitmaps.isEmpty()) {
            // loop for creating ImageView's
            int pictureCounter = 1;
            for (LocalBitmap localBitmap : localBitmaps) {
                long startTime = SystemClock.uptimeMillis();
                List<Recognition> results = classifier.recognizeImage(localBitmap.getBitmap(), 0);
                long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                ConstraintLayout resultViewForBitmap = ResultScreenCreator.createConstraintLayout(this, DetectionType.IMAGE_CLASSIFICATION, localBitmap.getFileName(), localBitmap.getBitmap(), results, lastProcessingTimeMs);
                simpleViewFlipper.addView(resultViewForBitmap);

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

                    // Show the next Screen
                    simpleViewFlipper.showNext();
                }

                // if right to left swipe on screen
                if (initialX > currentX)
                {
                    if (simpleViewFlipper.getDisplayedChild() == 1)
                        break;

                    // Show The Previous Screen
                    simpleViewFlipper.showPrevious();
                }
                break;
            }
        }
        return false;
    }

}
