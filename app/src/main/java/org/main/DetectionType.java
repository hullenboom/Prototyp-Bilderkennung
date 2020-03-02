package org.main;

public enum DetectionType {
    IMAGE_CLASSIFICATION("IMAGE-CLASSIFICATION"),
    OBJECT_DETECTION("OBJECT-DETECTION");

    DetectionType(String uiName){
        this.uiName = uiName;
    }

    private String uiName;

    public String getUiName(){
        return uiName;
    }

    public static DetectionType getEnumFromUIName(String uiName){
        if (uiName.equals(IMAGE_CLASSIFICATION.getUiName())){
            return IMAGE_CLASSIFICATION;
        } else if (uiName.equals(OBJECT_DETECTION.getUiName())){
            return OBJECT_DETECTION;
        }
        return IMAGE_CLASSIFICATION;
    }


}
