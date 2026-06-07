package com.example.bookspace.quote;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QuoteCardExporter {
    private static final String QUOTE_DIR = "BookSpaceQuotes";
    private static final String MIME_PNG = "image/png";

    private QuoteCardExporter() {
    }

    public static Bitmap renderViewToBitmap(View view) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            throw new IllegalStateException("Quote card preview has not been laid out yet.");
        }

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static Uri saveBitmapToGallery(Context context, Bitmap bitmap) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveBitmapWithMediaStore(context, bitmap);
        }
        return saveBitmapLegacy(context, bitmap);
    }

    public static Uri saveBitmapToCache(Context context, Bitmap bitmap) throws IOException {
        File shareDir = new File(context.getCacheDir(), "shared_quotes");
        if (!shareDir.exists() && !shareDir.mkdirs()) {
            throw new IOException("Cannot create share cache directory.");
        }

        File imageFile = new File(shareDir, createFileName());
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            writePng(bitmap, outputStream);
        }

        String authority = context.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(context, authority, imageFile);
    }

    private static Uri saveBitmapWithMediaStore(Context context, Bitmap bitmap) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, createFileName());
        values.put(MediaStore.Images.Media.MIME_TYPE, MIME_PNG);
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + QUOTE_DIR);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Cannot create MediaStore record.");
        }

        try (OutputStream outputStream = resolver.openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IOException("Cannot open MediaStore output stream.");
            }
            writePng(bitmap, outputStream);
        } catch (IOException exception) {
            resolver.delete(uri, null, null);
            throw exception;
        }

        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
        return uri;
    }

    private static Uri saveBitmapLegacy(Context context, Bitmap bitmap) throws IOException {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File quoteDir = new File(picturesDir, QUOTE_DIR);
        if (!quoteDir.exists() && !quoteDir.mkdirs()) {
            throw new IOException("Cannot create quote directory.");
        }

        File imageFile = new File(quoteDir, createFileName());
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            writePng(bitmap, outputStream);
        }

        MediaScannerConnection.scanFile(
                context,
                new String[]{imageFile.getAbsolutePath()},
                new String[]{MIME_PNG},
                null
        );
        return Uri.fromFile(imageFile);
    }

    private static void writePng(Bitmap bitmap, OutputStream outputStream) throws IOException {
        boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        if (!success) {
            throw new IOException("Cannot encode quote card PNG.");
        }
    }

    private static String createFileName() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return "bookspace_quote_" + timestamp + ".png";
    }
}
