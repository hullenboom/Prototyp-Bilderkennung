package org.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.storage.image_classification.ImageClassificationResultScreen;
import org.tensorflow.lite.storage.base.ImageProccessor;
import org.tensorflow.lite.storage.object_detection.ObjectDetectionResultScreen;
import org.tensorflow.lite.camera.object_detection.DetectorActivity;
import org.tensorflow.lite.camera.image_classification.ClassifierActivity;

import java.util.ArrayList;

import masterarbeit.R;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    int PICK_IMAGE_MULTIPLE = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final int PERMISSION_REQUEST_CODE_WRITE = 3;


    private TextView explanationText;
    Spinner detectionSpinner;
    DetectionType chosenDetectionType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissionsForThisViewWrite();
        requestPermissionsForThisViewRead();
        explanationText = findViewById(R.id.detectionTypeExplanation);

        detectionSpinner = findViewById(R.id.detectionTypeSpinner);
        detectionSpinner.setOnItemSelectedListener(this);
        chosenDetectionType = DetectionType.IMAGE_CLASSIFICATION;
        replaceExplanationText(chosenDetectionType);

        final Button buttonCamera = findViewById(R.id.cameraButton);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (chosenDetectionType == DetectionType.IMAGE_CLASSIFICATION) {
                    Intent myIntent = new Intent(MainActivity.this, ClassifierActivity.class);
                    MainActivity.this.startActivity(myIntent);
                } else if (chosenDetectionType == DetectionType.OBJECT_DETECTION) {
                    Intent myIntent = new Intent(MainActivity.this, DetectorActivity.class);
                    MainActivity.this.startActivity(myIntent);
                }
            }
        });

        final Button buttonStorage = findViewById(R.id.storageButton);
        buttonStorage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadImagesFromGallery();
            }
        });
    }

    public void loadImagesFromGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");


        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickIntent.setType("image/*");

  /*    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
  */
        startActivityForResult(pickIntent, PICK_IMAGE_MULTIPLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
                ImageProccessor imageProccessor = new ImageProccessor();
                ArrayList<String> inputUris = imageProccessor.getURIsAsStringListFromPickedData(this, data);
                for(String inputUri : inputUris){
                    Log.d("Test",inputUri );
                }
                if(chosenDetectionType  == DetectionType.IMAGE_CLASSIFICATION){
                    Intent myIntent = new Intent(MainActivity.this, ImageClassificationResultScreen.class);
                    myIntent.putStringArrayListExtra("test", inputUris);
                    MainActivity.this.startActivity(myIntent);
                }
                if(chosenDetectionType == DetectionType.OBJECT_DETECTION) {
                    Intent myIntent = new Intent(MainActivity.this, ObjectDetectionResultScreen.class);
                    myIntent.putStringArrayListExtra("test", inputUris);
                    MainActivity.this.startActivity(myIntent);
                }

            } else {
                Toast.makeText(this, "No Images selected",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("exception", e.getLocalizedMessage());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Implementiert Interface, um auf die Veränderung des Spinners zu hören
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        if (parent == (detectionSpinner)) {
            String detectionSpinnerValue = parent.getItemAtPosition(pos).toString().toUpperCase();
            Log.d("Test", detectionSpinnerValue);
            chosenDetectionType = DetectionType.getEnumFromUIName(detectionSpinnerValue);
            replaceExplanationText(chosenDetectionType);
            Log.d("Test", chosenDetectionType.toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> var1) {

    }

    public void replaceExplanationText(DetectionType detectionType) {
        if (detectionType == DetectionType.IMAGE_CLASSIFICATION) {
            explanationText.setText("Ein Image Classification Modell hat die Aufgabe, für ein Inputbild die Wahrscheinlichkeit zu bestimmen, dass zuvor antrainierte Objektklassen auf diesem Bild dargestellt sind. Die Summe der Wahrscheinlichkeiten aller Objektklassen ergibt immer 100%.");
        } else if (detectionType == DetectionType.OBJECT_DETECTION) {
            explanationText.setText("Ein Object Detection Modell hat die Aufgabe, auf einem Inputbild dargestellte, zuvor antrainierte Objekte zu erkennen und deren Position in Form von Bounding Boxes sowie die Konfidenz der Erkennung auszugeben.");
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
            case PERMISSION_REQUEST_CODE_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can write on local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot write one local drive .");
                }
                break;
        }
    }

    public void requestPermissionsForThisViewWrite(){
        if(!checkPermissionWrite()){
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_WRITE);

        }
    }

    public void requestPermissionsForThisViewRead(){
        if(!checkPermissionRead()){
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermissionRead() {
        int result = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPermissionWrite() {
        int result = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
