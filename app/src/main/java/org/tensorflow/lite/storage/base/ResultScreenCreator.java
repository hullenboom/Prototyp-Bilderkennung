package org.tensorflow.lite.storage.base;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.main.DetectionType;
import org.product.activity.ProductInformation;
import org.tensorflow.lite.camera.base.tflite.Recognition;
import org.tensorflow.lite.storage.object_detection.Utils;

import java.util.ArrayList;
import java.util.List;

import masterarbeit.R;

public class ResultScreenCreator {

    private static float IMAGE_CLASSIFICATION_BUTTON_THRESHOLD = 0.75f;
    private static float OBJECT_DETECTION_THRESHOLD = Utils.MINIMUM_CONFIDENCE_TF_OD_API;


    public static ConstraintLayout createConstraintLayout(Activity activity, DetectionType detectionType, String fileName, Bitmap bitmap, List<Recognition> results, long lastProcessingTimeMs) {

        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(activity).inflate(R.layout.storage_result_screen_entry, null);

        setHeaderText(constraintLayout, fileName);

        if (detectionType == detectionType.OBJECT_DETECTION) {
            bitmap = Utils.drawRectsOnBitmap(bitmap, results);
        }

        setImageView(constraintLayout, bitmap, detectionType);
        setInferenceTime(constraintLayout, lastProcessingTimeMs);

        //if somehow the result list is null, show no result label, otherwise show results
        if (results == null) {
            showNoResultLabel(activity, constraintLayout);
        } else {
            showResultLabels(activity, constraintLayout, results, detectionType);
        }

        return constraintLayout;
    }

    private static void showResultLabels(Activity activity, ConstraintLayout constraintLayout, List<Recognition> results, DetectionType detectionType) {
        //used to check which products already shown
        List<String> alreadyShownProducts = new ArrayList<>();
        //counter for number of result, start with one
        List<LinearLayout> linearLayoutsToAdd = new ArrayList<>();

        //loop through result list
        for (Recognition result : results) {

            if (detectionType == DetectionType.OBJECT_DETECTION) {
                if (result.getConfidence() > Utils.MINIMUM_CONFIDENCE_TF_OD_API && !alreadyShownProducts.contains(result.getTitle())) {
                    LinearLayout resultLabelLayout = createObjectDetectionResultLabel(activity, result);
                    linearLayoutsToAdd.add(resultLabelLayout);
                    alreadyShownProducts.add(result.getTitle());

                }
            }
            if (detectionType == DetectionType.IMAGE_CLASSIFICATION) {
                LinearLayout resultLabelLayout = createImageClassificationResultLabel(activity, result);
                linearLayoutsToAdd.add(resultLabelLayout);

            }
        }

        //if list not empty, show result labels, else  return show no result view
        if (!linearLayoutsToAdd.isEmpty()) {
            addResultLabelsToView(constraintLayout, linearLayoutsToAdd);
        } else {
            showNoResultLabel(activity, constraintLayout);
        }
    }


    private static void addResultLabelsToView(ConstraintLayout constraintLayout, List<LinearLayout> layoutsToAdd) {
        FrameLayout resultLayout;
        //we need a counter to check which result the given layout is, to fill the correct framelayout
        int resultCounter = 1;
        for (LinearLayout layoutToadd : layoutsToAdd) {
            if (resultCounter == 1) {
                resultLayout = constraintLayout.findViewById(R.id.result1);
            } else if (resultCounter == 2) {
                resultLayout = constraintLayout.findViewById(R.id.result2);
            } else {
                resultLayout = constraintLayout.findViewById(R.id.result3);
            }
            resultLayout.addView(layoutToadd);
            resultCounter++;
        }
    }

    private static LinearLayout createObjectDetectionResultLabel(Activity activity, Recognition result) {
        LinearLayout resultLabelLayout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.result_entry, null);
        TextView name = resultLabelLayout.findViewById(R.id.detected_item_result);
        name.setText(Character.toUpperCase(result.getTitle().charAt(0)) + result.getTitle().substring(1));
        Button productInfoButton = resultLabelLayout.findViewById(R.id.detected_item_button_result);
        productInfoButton.setText("Mehr Informationen");
        productInfoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent productInfoIntent = new Intent(activity, ProductInformation.class);
                productInfoIntent.putExtra("recognizedProduct", result.getTitle());
                activity.startActivity(productInfoIntent);
            }
        });

        return resultLabelLayout;
    }

    private static LinearLayout createImageClassificationResultLabel(Activity activity, Recognition result) {
        LinearLayout resultLabelLayout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.result_entry, null);
        TextView name = resultLabelLayout.findViewById(R.id.detected_item_result);
        name.setText(result.getTitle());
        TextView confidence = resultLabelLayout.findViewById(R.id.detected_item_value_result);
        confidence.setText(String.format("%.2f", (100 * result.getConfidence())) + "%");
        Button productInfoButton = resultLabelLayout.findViewById(R.id.detected_item_button_result);
        if (result.getConfidence() > IMAGE_CLASSIFICATION_BUTTON_THRESHOLD) {
            productInfoButton.setText("Mehr Informationen");
            productInfoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent productInfoIntent = new Intent(activity, ProductInformation.class);
                    productInfoIntent.putExtra("recognizedProduct", result.getTitle());
                    activity.startActivity(productInfoIntent);
                }
            });
        } else {
            productInfoButton.setVisibility(View.INVISIBLE);
            productInfoButton.setEnabled(false);
        }

        return resultLabelLayout;
    }


    private static void showNoResultLabel(Activity activity, ConstraintLayout constraintLayout) {
        LinearLayout noResultLabelLayout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.result_entry, null);

        FrameLayout layout = constraintLayout.findViewById(R.id.result1);
        TextView textView = new TextView(activity);
        textView.setText("Keine Erkennung");
        textView.setGravity(Gravity.CENTER);
        textView.setTextAppearance(activity, R.style.TextAppearance_AppCompat_Display1);
        layout.addView(textView);
    }


    private static void setImageView(ConstraintLayout constraintLayout, Bitmap bitmap, DetectionType detectionType) {
        ImageView image = constraintLayout.findViewById(R.id.loadedImage);
        image.setImageBitmap(bitmap);
    }

    private static void setHeaderText(ConstraintLayout constraintLayout, String fileName) {
        TextView header = constraintLayout.findViewById(R.id.header);
        header.setText("Analyse für Datei: " + fileName);
    }

    private static void setInferenceTime(ConstraintLayout constraintLayout, long lastProcessingTimeMs) {
        TextView inferenceTime = constraintLayout.findViewById(R.id.inference_time);
        inferenceTime.setText(String.valueOf(lastProcessingTimeMs) + " ms");
    }

}
