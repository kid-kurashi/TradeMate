package ru.dwdm.trademate;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.view.PixelCopy;
import android.widget.Toast;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WritingFragment extends ArFragment {

    private WriteCallback callback;
    private Uri uri;

    public void setCallback(WriteCallback callback) {
        this.callback = callback;
    }

    @Override
    public String[] getAdditionalPermissions() {
        String[] additionalPermissions = super.getAdditionalPermissions();
        int permissionLength = additionalPermissions != null ? additionalPermissions.length : 0;
        String[] permissions = new String[permissionLength + 1];
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (permissionLength > 0) {
            System.arraycopy(additionalPermissions, 0, permissions, 1, additionalPermissions.length);
        }
        return permissions;
    }

    private Uri saveBitmapToDisk(Bitmap bitmap) {

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);
        String fileName = formatter.format(new Date()) + ".jpg";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, fileName, "");
        return Uri.parse(path);
    }

    public void takePhoto() {

        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.SHUTTER_CLICK);

        callback.onWriteStart();

        ArSceneView view = getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                uri = saveBitmapToDisk(bitmap);
                Snackbar snackbar = Snackbar.make(getView(),
                        "Photo saved", Snackbar.LENGTH_LONG);
                snackbar.setAction("Open in Photos", v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setDataAndType(uri, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                getActivity().runOnUiThread(() -> callback.onWriteEnd());
                snackbar.show();
            } else {
                Toast toast = Toast.makeText(getActivity(),
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    public interface WriteCallback {
        void onWriteStart();
        void onWriteEnd();
    }

}
