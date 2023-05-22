package com.onicolian.meets.ui;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.onicolian.meets.CreateActivity;
import com.onicolian.meets.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Camera_fragment extends Fragment {

    String strName = "";
    String strDesk = "";
    String strPlace = "";
    String strDay = "";
    String strTime = "";

    int num = 1;

    Dialog myDialog;

    public Button mTextButton;
    private Bitmap mSelectedImage;
    private ImageView mImageView;
    private Button buttonLoadImage;
    private Button buttonMakeImage;

    private Integer mImageMaxWidth;
    private Integer mImageMaxHeight;

    String currentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    int SELECT_PICTURE = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera,  container, false);

        myDialog = new Dialog(getActivity());

        mImageView = view.findViewById(R.id.imgView);
        mTextButton = view.findViewById(R.id.button_text);
        mTextButton.setOnClickListener(v -> runTextRecognition());

        buttonLoadImage = view.findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(arg0 -> imageChooser());

        buttonMakeImage = view.findViewById(R.id.buttonMakePicture);
        buttonMakeImage.setOnClickListener(arg0 -> dispatchTakePictureIntent());
        return view;
    }

    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.onicolian.meets",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    mImageView.setImageURI(selectedImageUri);

                    try {
                        mSelectedImage = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImageUri);
                        mda();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == REQUEST_IMAGE_CAPTURE) {
//                data.getData();
//                Bundle extras = data.getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//                mImageView.setImageBitmap(imageBitmap);
//                mSelectedImage = imageBitmap;

                // Get the dimensions of the View
                int targetW = mImageView.getWidth();
                int targetH = mImageView.getHeight();

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
                mImageView.setImageBitmap(bitmap);
                mda();
            }
        }
    }

    private void runTextRecognition() {
        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

        InputImage image = InputImage.fromBitmap( drawable.getBitmap(), 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        mTextButton.setEnabled(false);
        recognizer.process(image)
                .addOnSuccessListener(
                        texts -> {
                            mTextButton.setEnabled(true);
                            processTextRecognitionResult(texts);
                        })
                .addOnFailureListener(
                        e -> {
                            mTextButton.setEnabled(true);
                            e.printStackTrace();
                        });
    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast();
            return;
        }

        ImageView img;
        Button button;
        myDialog.setContentView(R.layout.frag_popup);
        img = myDialog.findViewById(R.id.imgView);

        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
        img.setImageBitmap(drawable.getBitmap());
        button = (Button) myDialog.findViewById(R.id.button2);

//        for (int i = 0; i < blocks.size(); i++) {
//            List<Text.Line> lines = blocks.get(i).getLines();
//            strName = lines.get(0).getText();
//            strDesk = lines.get(1).getText();
//            strDay = lines.get(3).getText();
//            strTime = lines.get(2).getText();
//            strPlace = lines.get(4).getText();
//        }


        button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateActivity.class);
            intent.putExtra("name", (CharSequence) strName);
            intent.putExtra("deck", (CharSequence) strDesk);
            intent.putExtra("place", (CharSequence) strPlace);
            intent.putExtra("day", (CharSequence) strDay);
            intent.putExtra("time", (CharSequence) strTime);

            num = 1;

            startActivity(intent);
        });


        RelativeLayout layout;
        layout = myDialog.findViewById(R.id.linear);
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {

                Button myButton = new Button(getActivity());
                myButton.setText(lines.get(j).getText());

                RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                rel_btn.leftMargin = lines.get(j).getCornerPoints()[0].x;
                rel_btn.topMargin = lines.get(j).getCornerPoints()[0].y;

                myButton.setLayoutParams(rel_btn);
                myButton.getBackground().setAlpha(64);
                myButton.setOnClickListener(arg -> buttonOnClick(getView(), myButton));

                layout.addView(myButton);
            }
        }
        myDialog.show();
    }

    public void buttonOnClick(View view, Button button)
    {
        switch(num)
        {
            case 1:
                strName = (String) button.getText();
                button.getBackground().setAlpha(150);
                num++;
                break;
            case 2:
                strDesk = (String) button.getText();
                button.getBackground().setAlpha(150);
                num++;
                break;
            case 3:
                strTime = (String) button.getText();
                button.getBackground().setAlpha(150);
                num++;
                break;
            case 4:
                strDay = (String) button.getText();
                button.getBackground().setAlpha(150);
                num++;
                break;
            case 5:
                strPlace = (String) button.getText();
                button.getBackground().setAlpha(150);
                num++;
                break;
            default:
                break;
        }
    }

    private void showToast() {
        Toast.makeText(getActivity().getApplicationContext(), "No text found", Toast.LENGTH_SHORT).show();
    }

    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            mImageMaxWidth = mImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            mImageMaxHeight =
                    mImageView.getHeight();
        }

        return mImageMaxHeight;
    }

    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void mda() {
        if (mSelectedImage != null) {
            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
