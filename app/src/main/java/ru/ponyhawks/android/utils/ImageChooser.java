package ru.ponyhawks.android.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.statics.Providers;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 22:40 on 29/04/15
 *
 * @author cab404
 */
public class ImageChooser {

    ImageUrlHandler handler;

    private String[] ITEMS = new String[]{
            "Сфотографировать",
            "Выбрать из галереи",
            "Ввести ссылку",
    };

    private static final int
            TAKE_PHOTO = 0,
            CHOOSE_PHOTO = 1,
            INPUT_URI = 2,
            REQUEST_CODE_TAKE_PHOTO = 9812,
            REQUEST_CODE_CHOOSE_PHOTO = 9814;
    private StartForResultMethod act;
    private Activity ctx;

    public ImageChooser(StartForResultMethod sfrm, Activity ctx, ImageUrlHandler handler) {
        this.act = sfrm;
        this.ctx = ctx;
        this.handler = handler;
        ITEMS = ctx.getResources().getStringArray(R.array.image_chooser_entries);
    }

    File subtarget;

    /**
     * Will finish given activity on cancel, if urgent.
     */
    @SuppressLint("NewApi")
    public void requestImageSelection(final boolean urgent) {

        new AlertDialog.Builder(ctx)
                .setItems(ITEMS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case TAKE_PHOTO:


                                File store = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                store = new File(store, ctx.getPackageName());
                                try {
                                    if (!store.exists() && !(store.mkdirs() && new File(store, ".nomedia").createNewFile()))
                                        throw new RuntimeException("Cannot create pictures dir");
                                } catch (IOException e) {
                                    throw new RuntimeException("Cannot create .nomedia", e);
                                }

                                subtarget = new File(store, "IMAGE_" + System.currentTimeMillis() + ".png");

                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(subtarget));

                                act.startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO);

                                break;

                            case CHOOSE_PHOTO:

                                Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                choosePhotoIntent.setType("image/*");
                                act.startActivityForResult(choosePhotoIntent, REQUEST_CODE_CHOOSE_PHOTO);

                                break;

                            case INPUT_URI:
                                final EditText text = new EditText(ctx);
                                text.setHint(R.string.enter_image_url);
                                text.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

                                final ClipboardManager cbm =
                                        (ClipboardManager)
                                                ctx.getSystemService(Context.CLIPBOARD_SERVICE);

                                @SuppressWarnings("deprecation")
                                CharSequence clipboard = cbm.getText();

                                if (clipboard != null) {
                                    try {
                                        new URL(clipboard.toString());
                                    } catch (MalformedURLException e) {
                                        clipboard = "";
                                    }
                                }

                                text.setText(clipboard);

                                new AlertDialog.Builder(ctx)
                                        .setView(text)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                handler.handleImage(text.getText().toString());
                                            }
                                        }).show();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (urgent)
                            act.finish();
                    }
                })
                .show();


    }

    void uploadData(Imgur.Upload upload) {
        final ProgressDialog dialog = new ProgressDialog(ctx);
        dialog.setMessage(ctx.getString(R.string.uploading_image));
        dialog.setCancelable(false);
        dialog.show();
        RequestManager
                .fromActivity(ctx)
                .manage(upload)
                .setProfile(Providers.ImgurGateway.get())
                .setCallback(new RequestManager.SimpleRequestCallback<Imgur.Upload>() {
                    @Override
                    public void onSuccess(Imgur.Upload what) {
                        super.onSuccess(what);
                        final JSONObject response = what.getResponse();
                        if (response.optBoolean("success")) {
                            handler.handleImage(response.optJSONObject("data").optString("link"));
                        } else {
                            msg(response.optString("error"));
                        }
                    }

                    @Override
                    public void onError(Imgur.Upload what, Exception e) {
                        super.onError(what, e);
                        e.printStackTrace();
                        msg(e.getLocalizedMessage());
                    }

                    @Override
                    public void onFinish(Imgur.Upload what) {
                        super.onFinish(what);
                        dialog.dismiss();
                    }
                })
                .start();

    }

    void msg(final String msg) {
        //noinspection ConstantConditions
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void handleActivityResult(int requestCode, int responseCode, Intent data) {
        if (responseCode == Activity.RESULT_OK)
            switch (requestCode) {
                case REQUEST_CODE_TAKE_PHOTO:
                    try {
                        uploadData(new Imgur.Upload(subtarget));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("", e);
                    }
                    break;
                case REQUEST_CODE_CHOOSE_PHOTO:
                    try {
                        uploadData(new Imgur.Upload(ctx.getContentResolver().openInputStream(data.getData())));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("", e);
                    }
                    break;
            }
    }

    public interface ImageUrlHandler {
        void handleImage(String image);
    }

    public interface StartForResultMethod {
        void startActivityForResult(Intent intent, int code);

        void finish();
    }

}
