package org.tensorflow.lite.storage.object_detection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import org.tensorflow.lite.camera.base.tflite.Recognition;

import java.io.FileDescriptor;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class Utils {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    public static Bitmap drawRectsOnBitmap(Bitmap bitmap, List<Recognition> results){
        Bitmap copybitmap = Bitmap.createBitmap(bitmap);
        Canvas canvasToShow = new Canvas(copybitmap);
        canvasToShow.drawBitmap(bitmap, 0, 0, null);
        final Paint paintBoundingBox = new Paint();
        paintBoundingBox.setColor(Color.RED);
        paintBoundingBox.setStyle(Paint.Style.STROKE);
        paintBoundingBox.setStrokeWidth(30.0f);

        final Paint paintLabel = new Paint();
        paintLabel.setColor(Color.RED);
        paintLabel.setStyle(Paint.Style.FILL);
        //paint2.setStrokeWidth(50.0f);
        paintLabel.setTextSize(130.0f);

        DecimalFormat df = new DecimalFormat("0.##");

        for (Recognition recognition: results){
            if (recognition.getConfidence()>MINIMUM_CONFIDENCE_TF_OD_API) {
                paintBoundingBox.setColor(getColorForProduct(recognition));
                paintLabel.setColor(getColorForProduct(recognition));
                //nachfolgende Zeilen mit originalImageRectF eingefügt zum Malen auf Ursprungsbild
                RectF originalImageRectF = recognition.getLocation();
                originalImageRectF.left = originalImageRectF.left * bitmap.getWidth()/300;
                originalImageRectF.right = originalImageRectF.right * bitmap.getWidth()/300;
                originalImageRectF.bottom = originalImageRectF.bottom * bitmap.getHeight()/300;
                originalImageRectF.top = originalImageRectF.top * bitmap.getHeight()/300;
                canvasToShow.drawRect(originalImageRectF, paintBoundingBox);
                String toShow = recognition.getTitle() +" "+ df.format( 100*recognition.getConfidence()) +"%";
                if(originalImageRectF.top < 60){
                    canvasToShow.drawText(toShow, originalImageRectF.left, originalImageRectF.top+120, paintLabel);
                } else {
                    canvasToShow.drawText(toShow, originalImageRectF.left, originalImageRectF.top-40, paintLabel);
                }
            }
        }
        return copybitmap;
    }

    private static int getColorForProduct(Recognition recognition){
        if(recognition.getTitle().equals("nutella")){
            return Color.RED;
        } else if (recognition.getTitle().equals("chipsfrisch")){
            return Color.BLUE;
        } else {
            return Color.YELLOW;
        }
    }

    /**
     * USed to resize bitmap to 300 x 300 px
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


}
