package com.SR71.Copter;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;

public class MainActivity extends AppCompatActivity {

    FaceDetector detector;
    Bitmap imageBitmap;
    ImageView imageView;
    Button slct;
    Button shoot;
    private static final int PHOTO_REQUEST_GALLERY = 2;
    private static final int STORAGE_REQUEST = 0x0010;
    public static final int yourIntValue = 142;
    String URL = "http://10.5.5.9:8080/videos/DCIM/100GOPRO/GOPR0106.JPG";
    SharedPreferences sharedpreferences;



    Button button;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedpreferences = getSharedPreferences("mypref", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("photonum1", yourIntValue);
        //editor.putInt("photonum3", sharedpreferences.getInt("photonum2", 0));
        //editor.putInt("photonum1", sharedpreferences.getInt("photonum3", 0));

        editor.apply();

        imageView = (ImageView) findViewById(R.id.image);
        // Locate the Button in activity_main.xml
        button = (Button) findViewById(R.id.button);
     //   String URL2 = "http://10.5.5.9:8080/videos/DCIM/100GOPRO/GOPR0" + sharedpreferences.getInt("photonum1", 0) + ".JPG";
        // Capture button click
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                // Execute DownloadImage AsyncTask
                new DownloadImage().execute("http://10.5.5.9:8080/videos/DCIM/100GOPRO/GOPR0" + sharedpreferences.getInt("photonum1", 0) + ".JPG");
            }
        });

        detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        InputStream stream = getResources().openRawResource(R.raw.sample_image);
        imageBitmap = BitmapFactory.decodeStream(stream);
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(imageBitmap);
        slct= (Button) findViewById(R.id.slct_button);



        Button btnDetectFaces = (Button) findViewById(R.id.btn_detect_faces);
        btnDetectFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectFaces();
            }
        });








    }


    private void detectFaces() {
        Bitmap bmp = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(imageBitmap, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        Paint landmarkPaint = new Paint();
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setStyle(Paint.Style.STROKE);
        landmarkPaint.setStrokeWidth(5);


        Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
        SparseArray<Face> faces = detector.detect(frame);
        if(faces.size() > 0){
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);

                canvas.drawRect(
                        face.getPosition().x,
                        face.getPosition().y,
                        face.getPosition().x + face.getWidth(),
                        face.getPosition().y + face.getHeight(), paint);

                for (Landmark landmark : face.getLandmarks()) {
                    int cx = (int) (landmark.getPosition().x);
                    int cy = (int) (landmark.getPosition().y);
                    canvas.drawCircle(cx, cy, 5, landmarkPaint);
                }
            }

            imageView.setImageBitmap(bmp);
            Toast.makeText(this, faces.size() + " faces detected", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "No faces detected", Toast.LENGTH_LONG).show();
        }
    }
    public void onClickBtn(View v) {
        int rslt = requestPermissions();
        if ( 0 == rslt) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
        }
    }
    public void onClickBtn2(View v) {
      shootPhoto();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        //editor.putInt("photonum3",sharedpreferences.getInt("photonum2", 0) + 1);
        //editor.putInt("photonum2", sharedpreferences.getInt("photonum3", 0));
        editor.putInt("photonum1", sharedpreferences.getInt("photonum1", 0)+1);
        editor.apply();
        Toast.makeText(getApplicationContext(), "A photo is taken", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if (data == null){
                return;
            }

            Uri selectedImage = data.getData();
            getBitmap(selectedImage);
        }
    }

    private void getBitmap(Uri imageUri) {
        String[] pathColumn = {MediaStore.Images.Media.DATA};


        // Query the image corresponding to the URI from the system table.
        Cursor cursor = getContentResolver().query(imageUri,pathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(pathColumn[0]);


        // Get the image path
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        imageBitmap = BitmapFactory.decodeFile(picturePath);
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(imageBitmap);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( (requestCode == STORAGE_REQUEST) &&
                (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) ) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
        }
    }

    private int requestPermissions(){

        int rslt = -1;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},0x0010);
                }
                else {
                    rslt = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rslt;
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            imageBitmap = BitmapFactory.decodeStream(input);
            return imageBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void shootPhoto() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://10.5.5.9/camera/CM?t=1q2w3e4r1&p=%01",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        StringRequest stringRequest1 = new StringRequest(Request.Method.GET, "http://10.5.5.9/bacpac/SH?t=1q2w3e4r1&p=%01", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
        queue.add(stringRequest1);
    }
    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Downloading Image");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                imageBitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap imageBitmap) {
            // Set the bitmap into ImageView
            imageView.setImageBitmap(imageBitmap);
            // Close progressdialog
            mProgressDialog.dismiss();

        }
    }

}
