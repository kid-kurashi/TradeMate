package ru.dwdm.trademate.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageWriter {

    public static void writeImage(Context inContext, Bitmap inImage) {

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
        String fileName = formatter.format(new Date()) + ".jpg";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, fileName, "");
        Log.e("@@@IMAGE", path);
    }
}
