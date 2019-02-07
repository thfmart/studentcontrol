package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by 40276655893 on 22/04/2016.
 */
public class DatabaseUtil {

    private static LruCache<String, Bitmap> photoMemoryCache;

    public static String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap stringToBitmap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static void initPhotoCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 10;

        photoMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount() / 1024;
                } else {
                    return ((bitmap.getRowBytes() * bitmap.getHeight())/1024);
                }
            }
        };
    }

    public static LruCache getPhotoCache(){
        return photoMemoryCache;
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            getPhotoCache().put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return (Bitmap)getPhotoCache().get(key);
    }

}
