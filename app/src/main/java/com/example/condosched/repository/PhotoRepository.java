package com.example.condosched.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import com.example.condosched.domain.DataError;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Grava a imagem fora da thread da interface, usando apenas o Context da aplicação.
 */
public final class PhotoRepository {
    private final ContentResolver resolver;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean closed;

    public PhotoRepository(Context context) {
        resolver = context.getApplicationContext().getContentResolver();
    }

    public void save(Bitmap bitmap, RepositoryCallback<Uri> callback) {
        worker.execute(() -> {
            Uri uri = null;
            try {
                String filename = "capture_" + UUID.randomUUID() + ".png";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                if (Build.VERSION.SDK_INT >= 29) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CondoSched");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                } else {
                    File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CondoSched");
                    if (!folder.exists() && !folder.mkdirs()) throw new IOException("Cannot create photo directory");
                    values.put(MediaStore.Images.Media.DATA, new File(folder, filename).getAbsolutePath());
                }
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("Cannot create image");
                try (OutputStream output = resolver.openOutputStream(uri)) {
                    if (output == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        throw new IOException("Cannot write image");
                    }
                }
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues published = new ContentValues();
                    published.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(uri, published, null, null);
                }
                Uri saved = uri;
                main.post(() -> {
                    if (!closed) callback.onSuccess(saved);
                });
            } catch (Exception error) {
                if (uri != null) {
                    try {
                        resolver.delete(uri, null, null);
                    } catch (Exception ignored) {
                    }
                }
                main.post(() -> {
                    if (!closed) callback.onError(DataError.SAVE_FAILED);
                });
            }
        });
    }

    public void close() {
        closed = true;
        worker.shutdown(); // Termina uma gravação já iniciada, sem reter a Activity.
    }
}