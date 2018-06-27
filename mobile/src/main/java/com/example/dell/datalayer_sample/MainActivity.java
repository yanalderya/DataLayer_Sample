package com.example.dell.datalayer_sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    static final int SELECT_IMAGE_CAMERA = 15;
    static final int SELECT_IMAGE_GALLERY = 13;
    GoogleApiClient googleApiClient;
    EditText editTextName;
    String name;
    ImageView imageView;
    String wearable_data_path;
    Bitmap bitmap = null;
    int bitmapByteCount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = (EditText) findViewById(R.id.activity_main_edtName);


        imageView = (ImageView) findViewById(R.id.activity_main_imgView1);

        //DataLayer Api'sine ulaşmak için GoogleClient sınıfını kullandık.Amaç Wearable.Apı servisine ulaşarak
        //iki cihaz arasındaki veri senkronizasyonu işlemi yapmak
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                //onConnected,onConnectedSuspended,onConnectionFailed metotlarını client ile ilişkilendirmek için iki satırı yazdık.
                //aski halde metotlar çalışmayacaktı ve veri senkronizasyonu gerçekleşmiyecekti.
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void selectImageFromGallery(View view) {
        //doğrudan cihazın galerisine gider.
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //galeriden gelen sonuç alınır.
        startActivityForResult(intent, SELECT_IMAGE_GALLERY);
    }

    public void selectImageFromCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //kameradan gelen sonuç alınır.
        startActivityForResult(intent, SELECT_IMAGE_CAMERA);
    }

    // onActivityResult();Başlatılan bir etkinlik veya uygulamadan intent almak için kullanılır.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Başlatılan etkinlik olumlu şekilde dönmüş olup galeri mi kamera mı olduğu tespit edilir.
        if (resultCode == RESULT_OK) {

            if (requestCode == SELECT_IMAGE_GALLERY) {
                //galeriden seçilen resmin uri bilgisi alınır.
                Uri uri = data.getData();

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_IMAGE_CAMERA) {

                bitmap = data.getParcelableExtra("data");
            }
        }

        //kamera yada galeriden aldığımız bitmap resmi set ediyoruz.
        imageView.setImageBitmap(bitmap);

        //Resmin boyutunu düşürme işlemleri yapılır.
        bitmap = Bitmap.createScaledBitmap(bitmap, 350, 350, false);
        bitmapByteCount = BitmapCompat.getAllocationByteCount(bitmap);



    }



    //bu metot resim göndermek için kullanılır.
    //path bilgisi verilip googleapiclient başlatılır.
    public void sendImage(View view) {
        wearable_data_path = "/wearable_image";
        googleApiClient.connect();
    }

    //bu metot metin göndermek için kullanılır.
    //path bilgisi verilip googleapiclient başlatılır.
    public void sendName(View view) {
        name = editTextName.getText().toString();
        wearable_data_path = "/wearable_data";
        googleApiClient.connect();

    }

    // googleApiClient.connect(); bağlanma isteği başarılı ise asenkron şekilde çağrılır.
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (wearable_data_path.equalsIgnoreCase("/wearable_data")) {
            try {
                //Saate göndermek istediğimiz verileri ekliyoruz.
                DataMap dataMap = new DataMap();
                dataMap.putString("name", name);
                //Datamap oluştuktan sonra path ini belirtiyoruz.
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(wearable_data_path).setUrgent();
                putDataMapRequest.getDataMap().putAll(dataMap);

                PutDataRequest request = putDataMapRequest.asPutDataRequest();
                //istek ağa gönderilir.
                Wearable.DataApi.putDataItem(googleApiClient, request);
            } catch (Exception exp) {
            }
        } else if (wearable_data_path.equalsIgnoreCase("/wearable_image")) {
            //Resmi göndermek için Main Thread ile asenkron çalışan yeni bir thread oluşturduk.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //bitmap resim asset formatına dönüştürüldü.
                        Asset asset = createAssetFromBitmap(bitmap);

                        PutDataMapRequest dataMap = PutDataMapRequest.create(wearable_data_path).setUrgent();
                        //veriler key-value olarak tutulur.
                        dataMap.getDataMap().putAsset("imagebitmap", asset);

                        PutDataRequest request = dataMap.asPutDataRequest();
                        Wearable.DataApi.putDataItem(googleApiClient, request);
                    } catch (Exception exp) {

                    }


                }
            }).start();
        }
    }


    //bitmap resim Asset formatına dönüştürülür.
    public static Asset createAssetFromBitmap(Bitmap bitmap) {

        //verinin bir byte dizisine yazılmasını sağlar
        //ikinci satırda sıkıştırılmış görüntü formatını ve sıkıştırma kalitesi belirlenir.
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Asset.createFromBytes(byteArrayOutputStream.toByteArray());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    //client bağlantısı geçiçi olarak kesildiği zaman
    @Override
    public void onConnectionSuspended(int i) {

    }

    //Bağlantının başarısız olduğu zaman
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
