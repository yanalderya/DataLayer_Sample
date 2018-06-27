package com.example.dell.datalayer_sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;

/****************************************
 * Created by ${DERYA_YANAL}            *
 * 25.06.2018.                          *
 ***************************************/
public class ListenerService extends WearableListenerService {

    //Data Layerda meydana gelen değişiklikleri algılayan metot
    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        //saatte veri olaylarını kontrol eden döngümüz
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                //verinin path bilgisi alınır.
                String path = event.getDataItem().getUri().getPath();
                //path wearable_data ise metin gönderilmiştir.
                if (path.equalsIgnoreCase("/wearable_data")) {

                    //DataItem ile gönderilen veriyi datamap in içerisine atarız.
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                    Intent i = new Intent(this, NameActivity.class);
                    i.putExtra("datamap", dataMap.toBundle());
                    //Her veri değiştiğinde yeniden başlatılır ve gelen yeni veri kullanıcıya gösterilir.
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else if (path.equalsIgnoreCase("/wearable_image")) {

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                    //key değeri ile datamapin içindeki resime erişiriz.
                    Asset asset = dataMapItem.getDataMap().getAsset("imagebitmap");
                    //asset bitmap e çevrilir.
                    Bitmap bitmap = loadBitmapFromAsset(asset);
                    Intent i = new Intent(this, ImageActivity.class);
                    i.putExtra("bitmap", bitmap);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
        }


    }

    private Bitmap loadBitmapFromAsset(Asset asset) {

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();

        //alınan asset verisi bir dosya olacak şekilde dönüştürülür.await() metodu ile bu işleme bitine kadar beklenir.
        InputStream ınputStream = Wearable.DataApi.getFdForAsset(googleApiClient, asset).await().getInputStream();
        //ihtiyaç kalmadığında sonlandırılır.
        googleApiClient.disconnect();

        return BitmapFactory.decodeStream(ınputStream);

    }
}
