package org.tensorflow.lite.storage.base;

import android.graphics.Bitmap;

public class LocalBitmap {

    private String fileName;
    private Bitmap bitmap;

    LocalBitmap(String fileName, Bitmap bitmap){
        this.fileName = fileName;
        this.bitmap = bitmap;
    }

    public String getFileName() {
        return fileName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
