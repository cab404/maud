package ru.ponyhawks.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

    private final static String[] ITEMS = new String[]{
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
    private Activity act;
    private Fragment fr;

    public ImageChooser(Fragment fr, ImageUrlHandler handler) {
        this.fr = fr;
        this.act = fr.getActivity();
        this.handler = handler;
    }

    File subtarget;

    /**
     * Will finish given activity on cancel, if urgent.
     */
    public void requestImageSelection(final boolean urgent) {

        new AlertDialog.Builder(act)
                .setItems(ITEMS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case TAKE_PHOTO:


                                File store = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                store = new File(store, act.getPackageName());
                                try {
                                    if (!store.exists() && !(store.mkdirs() && new File(store, ".nomedia").createNewFile()))
                                        throw new RuntimeException("Cannot create pictures dir");
                                } catch (IOException e) {
                                    throw new RuntimeException("Cannot create .nomedia", e);
                                }

                                subtarget = new File(store, "IMAGE_" + System.currentTimeMillis() + ".png");

                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(subtarget));

                                fr.startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO);

                                break;

                            case CHOOSE_PHOTO:

                                Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                choosePhotoIntent.setType("image/*");
                                fr.startActivityForResult(choosePhotoIntent, REQUEST_CODE_CHOOSE_PHOTO);

                                break;

                            case INPUT_URI:
                                final EditText text = new EditText(act);
                                new AlertDialog.Builder(act).setView(text).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
        final ProgressDialog dialog = new ProgressDialog(act);
        dialog.setMessage(act.getString(R.string.uploading_image));
        dialog.show();
        RequestManager
                .fromActivity(act)
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

    void msg(final String msg){
        //noinspection ConstantConditions
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
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
                        uploadData(new Imgur.Upload(act.getContentResolver().openInputStream(data.getData())));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("", e);
                    }
                    break;
            }
    }

    public interface ImageUrlHandler {
        void handleImage(String image);
    }

}
