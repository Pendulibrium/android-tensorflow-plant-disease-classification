package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.List;

public class CameraRollActivity extends Activity{

  private static final int SELECT_IMAGE = 505;
  private RecognitionScoreView resultView;
  private Bitmap bitmap;

  // Classifier
  private Classifier classifier;
  private static final int INPUT_SIZE = 224;
  private static final int IMAGE_MEAN = 128;
  private static final float IMAGE_STD = 128.0f;
  private static final String INPUT_NAME = "input";
  private static final String OUTPUT_NAME = "final_result";
  private static final String MODEL_FILE = "file:///android_asset/optimized_mobilenet_plant_graph.pb";
  private static final String LABEL_FILE = "file:///android_asset/plant_labels.txt";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera_roll);
    Button chooseImage = (Button) findViewById(R.id.choose_image);
    resultView = (RecognitionScoreView) findViewById(R.id.results);

    resultView.setVisibility(View.INVISIBLE);

    classifier =
        TensorFlowImageClassifier.create(
            getAssets(),
            MODEL_FILE,
            LABEL_FILE,
            INPUT_SIZE,
            IMAGE_MEAN,
            IMAGE_STD,
            INPUT_NAME,
            OUTPUT_NAME);

    chooseImage.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == SELECT_IMAGE) {
        if (resultCode == Activity.RESULT_OK) {
          if (data != null) {

            Uri selectedImageURI = data.getData();
            Picasso.with(this).load(selectedImageURI).noPlaceholder().centerCrop().fit()
                .into((ImageView) this.findViewById(R.id.image));

            try
            {
              bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
              bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);

            } catch (IOException e)
            {
              e.printStackTrace();
            }
            classifyImage();
          }
        } else if (resultCode == Activity.RESULT_CANCELED) {
          Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
      }
  }

  private void classifyImage() {
    resultView.setVisibility(View.VISIBLE);
    final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
    resultView.setResults(results);
  }
}
