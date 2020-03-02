/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.camera.object_detection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.product.activity.ProductInformation;
import org.tensorflow.lite.camera.base.customview.OverlayView;
import org.tensorflow.lite.camera.base.env.ImageUtils;
import org.tensorflow.lite.camera.base.env.Logger;
import org.tensorflow.lite.camera.base.tflite.Recognition;
import org.tensorflow.lite.camera.base.tflite.BorderedText;
import org.tensorflow.lite.camera.object_detection.tflite.ClassifierObjectDetection;
import org.tensorflow.lite.camera.object_detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.camera.object_detection.tracking.MultiBoxTracker;

import masterarbeit.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = false;
  private static final String TF_OD_API_MODEL_FILE = "object_detection_198k_mobilenetv2.tflite";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private ClassifierObjectDetection detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
          TFLiteObjectDetectionAPIModel.create(
              getAssets(),
              TF_OD_API_MODEL_FILE,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "ClassifierObjectDetection could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new OverlayView.DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
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
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            //writeToFile(String.valueOf(lastProcessingTimeMs));

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
            }

            final List<Recognition> mappedRecognitions =
                new LinkedList<Recognition>();

            for (final Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {
                canvas.drawRect(location, paint);

                cropToFrameTransform.mapRect(location);

                result.setLocation(location);
                mappedRecognitions.add(result);
              }
            }

            runOnUiThread(new Runnable() {

              @Override
              public void run() {

                addFrameLayouts(results);
              }
            });


            tracker.trackResults(mappedRecognitions, currTimestamp);
            trackingOverlay.postInvalidate();

            computingDetection = false;

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    showFrameInfo(previewWidth + "x" + previewHeight);
                    showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                    showInference(lastProcessingTimeMs + "ms");
                  }
                });
          }
        });
  }

  private void resetFrameLayouts(LinearLayout bottomSheetLayout){
    FrameLayout resultLayout1 = bottomSheetLayout.findViewById(R.id.resultObject1);
    FrameLayout resultLayout2 = bottomSheetLayout.findViewById(R.id.resultObject2);
    FrameLayout resultLayout3 = bottomSheetLayout.findViewById(R.id.resultObject3);

    resultLayout1.removeAllViews();
    resultLayout2.removeAllViews();
    resultLayout3.removeAllViews();
  }

  public void addFrameLayouts(List<Recognition> results) {

    LinearLayout bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
    //first reset all views
    resetFrameLayouts(bottomSheetLayout);
    //create list to save which products are already shown, because there could be multiple nutella locations
    List<String> alreadyShownProducts = new ArrayList<>();
    //variable needed to determine which resultview should be filled
    int i = 0;
    if(results != null) {
      for (Recognition r : results) {
        if (!alreadyShownProducts.contains(r.getTitle()) && r.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
          Log.d("Result", i + " " + r.getTitle() + " " + r.getConfidence());
          LinearLayout testLayout = (LinearLayout) LayoutInflater.from(DetectorActivity.this).inflate(R.layout.result_entry, null);
          TextView name = testLayout.findViewById(R.id.detected_item_result);
          name.setText(Character.toUpperCase(r.getTitle().charAt(0)) + r.getTitle().substring(1));
          Button productInfoButton = testLayout.findViewById(R.id.detected_item_button_result);
          productInfoButton.setText("Mehr Informationen");
          productInfoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              Intent productInfoIntent = new Intent(DetectorActivity.this, ProductInformation.class);
              productInfoIntent.putExtra("recognizedProduct", r.getTitle());
              DetectorActivity.this.startActivity(productInfoIntent);
            }
          });
          FrameLayout resultLayout;
          if (i == 0) {
            resultLayout = bottomSheetLayout.findViewById(R.id.resultObject1);
          } else if (i == 1) {
            resultLayout = bottomSheetLayout.findViewById(R.id.resultObject2);
          } else {
            resultLayout = bottomSheetLayout.findViewById(R.id.resultObject3);
          }
          resultLayout.addView(testLayout);
          i++;
          alreadyShownProducts.add(r.getTitle());
        }
      }
    }
  }



  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
  public void writeToFile(String data) {
    try {
      File file = new File(Environment.getExternalStorageDirectory() + "/object-detection-camera-inference-time.txt");
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file, true));
      outputStreamWriter.append(data + "\n");
      outputStreamWriter.close();
    }
    catch (IOException e) {
      Log.e("Exception", "File write failed: " + e.toString());
    }
  }



}
