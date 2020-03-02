package org.product.restservice;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("id")
    private Integer ProduktId;

    @SerializedName("ProduktName")
    private String ProduktName;

    @SerializedName("ProduktMarke")
    private String ProduktMarke;

    @SerializedName("ProduktPreis")
    private String ProduktPreis;

    @SerializedName("ProduktBeschreibung")
    private String ProduktBeschreibung;

    @SerializedName("ProduktMenge")
    private String ProduktMenge;

    public Product(Integer ProduktId, String ProduktName, String ProduktMarke, String ProduktPreis, String ProduktBeschreibung, String ProduktMenge){
        this.ProduktId = ProduktId;
        this.ProduktName = ProduktName;
        this.ProduktMarke = ProduktMarke;
        this.ProduktPreis = ProduktPreis;
        this.ProduktBeschreibung = ProduktBeschreibung;
        this.ProduktMenge = ProduktMenge;
    }

    public Integer getProduktId() {
        return ProduktId;
    }

    public String getProduktName() {
        return ProduktName;
    }

    public String getProduktMarke() {
        return ProduktMarke;
    }

    public String getProduktPreis() {
        return ProduktPreis;
    }

    public String getProduktBeschreibung() {
        return ProduktBeschreibung;
    }

    public String getProduktMenge() {
        return ProduktMenge;
    }

    public void setProduktId(Integer produktId) {
        ProduktId = produktId;
    }

    public void setProduktName(String produktName) {
        ProduktName = produktName;
    }

    public void setProduktMarke(String produktMarke) {
        ProduktMarke = produktMarke;
    }

    public void setProduktPreis(String produktPreis) {
        ProduktPreis = produktPreis;
    }

    public void setProduktBeschreibung(String produktBeschreibung) {
        ProduktBeschreibung = produktBeschreibung;
    }

    public void setProduktMenge(String produktMenge) {
        ProduktMenge = produktMenge;
    }
}
