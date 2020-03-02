package org.product.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.product.restservice.Product;
import org.product.restservice.ProductEndpoint;
import org.product.restservice.RetrofitClientInstance;

import masterarbeit.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductInformation extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_information);
        String product = getIntent().getStringExtra("recognizedProduct");
        int id = getRestIdForProductName(product);
        displayProduct(id);
    }


    public void displayProduct(int id){
        ProductEndpoint service = RetrofitClientInstance.getRetrofitInstance().create(ProductEndpoint.class);
        Call<Product> call = service.getProduct(String.valueOf(id));

        int pictureDrawableId = 0;
        if(id == 1){
            pictureDrawableId =  R.drawable.nutellaproduct;
        } else if (id == 2){
            pictureDrawableId = R.drawable.chipsfrischproduct;
        } else if (id == 3){
            pictureDrawableId = R.drawable.laettaproduct;
        }
        processCall(call, pictureDrawableId);
    }

    public void processCall(Call<Product> call, int bildId){
        System.out.println(call.request());
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                System.out.println(response);
                Product info = response.body();
                Toast.makeText(ProductInformation.this, "Got "+ info.getProduktName()+ " Information", Toast.LENGTH_SHORT).show();
                ImageView nutella = findViewById(R.id.display_product);
                nutella.setImageResource(bildId);
                TextView produktName = findViewById(R.id.ProduktName);
                produktName.setText(info.getProduktName());
                TextView produktBeschreibung = findViewById(R.id.ProduktBeschreibung);
                produktBeschreibung.setText(info.getProduktBeschreibung());
                TextView produktPreis = findViewById(R.id.ProduktPreis);
                produktPreis.setText(info.getProduktPreis());
                TextView produktMarke = findViewById(R.id.ProduktMarke);
                produktMarke.setText(info.getProduktMarke());
                TextView produktMenge = findViewById(R.id.ProduktMenge);
                produktMenge.setText(info.getProduktMenge());

            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {


                Toast.makeText(ProductInformation.this, "Failure", Toast.LENGTH_SHORT).show();
                // FAILURE
            }
        });
    }

    private int getRestIdForProductName(String productName){
        String name = productName.toLowerCase();
        Log.d("test" ,name);
        if(name.equals("nutella")){
            return 1;
        } else if (name.equals("chipsfrisch")){
            return 2;
        } else{
            return 3;
        }
    }
}
