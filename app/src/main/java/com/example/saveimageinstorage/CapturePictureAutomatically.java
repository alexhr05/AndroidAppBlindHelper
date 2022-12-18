package com.example.saveimageinstorage;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
//AppCompatActivity
public class CapturePictureAutomatically extends MainActivity{
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Button bTakePicture, btnStopTakePicture, btnStartTakingPhotos,btnShowImage;
    PreviewView previewView;
    private ImageCapture imageCapture;
    Handler handler;
    Runnable runnable;
    Uri imageUri;
    OutputStream outputStream;
    ArrayList<Image> arrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_picture_automatically);
        bTakePicture = findViewById(R.id.bTakePicture);
        btnStopTakePicture = findViewById(R.id.btnStopTakePicture);
        btnStartTakingPhotos = findViewById(R.id.btnStartTakingPhotos);
        btnShowImage = findViewById(R.id.btnShowImage);


        previewView = findViewById(R.id.previewView);


        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());
        MainActivity obj = new MainActivity();
        btnStartTakingPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Param is optional, to run task on UI thread.
                handler = new Handler(Looper.getMainLooper());
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // Do the task...
                        capturePhoto();

                        try {
                            obj.getImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(CapturePictureAutomatically.this, "Започна да прави снимки", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this, 5000);
                        // Optional, to repeat the task
                    }
                };
                handler.postDelayed(runnable, 5000);

            }
        });


        btnStopTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop a repeating task like this.
                handler.removeCallbacks(runnable);
                Toast.makeText(CapturePictureAutomatically.this, "Успешно спряхте снимките", Toast.LENGTH_SHORT).show();
            }
        });
        bTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto();
                try {

                    obj.getImages();
                } catch (IOException e) {
                    Toast.makeText(obj, "e="+e.getMessage(), Toast.LENGTH_SHORT).show();
                    //e.printStackTrace();

                }
                //Uri imagePath = createImage();
                //Intent intent = new Intent();
                //intent.putExtra(MediaStore.EXTRA_OUTPUT,imagePath);
            }
        });
        btnShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //obj.getImages();
                startActivity(new Intent(CapturePictureAutomatically.this, MainActivity.class));
                //Uri imagePath = createImage();
                //Intent intent = new Intent();
                //intent.putExtra(MediaStore.EXTRA_OUTPUT,imagePath);
            }
        });

    }


    private void capturePhoto() {
        /*File photoDir = new File("/storage/emulated/0/111");

        if(!photoDir.exists()){
            photoDir.mkdir();
        }
        Date date = new Date();*/
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        Toast.makeText(this, " " + str, Toast.LENGTH_SHORT).show();
        String timestamp = "blindHelper-" + str;// +System.currentTimeMillis();//+ Integer.parseInt(String.valueOf(now));

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        /*String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";

        File photoFile = new File(photoFilePath);*/

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CapturePictureAutomatically.this, "Photo has been saved successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        //Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(CapturePictureAutomatically.this, "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }


        );
        Toast.makeText(this, " " + imageCapture, Toast.LENGTH_LONG).show();


    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        //Camera Selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Preview use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);


    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    public void getImages() throws IOException {
        arrayList.clear();
//        Toast.makeText(this, "Vikat me", Toast.LENGTH_SHORT).show();
        String filepath = "/storage/emulated/0/Pictures";
        //Environment.getExternalStorageDirectory() + "/Pictures";
        File file = new File(filepath);
        Toast.makeText(this, "file="+file, Toast.LENGTH_SHORT).show();
        File[] files = file.listFiles();
        Toast.makeText(this, "file.listFiles()="+file.listFiles(), Toast.LENGTH_SHORT).show();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        //Bitmap bitmap = null;
        if(files != null){
            for(File file1: files){
                if((file1.getPath().endsWith(".png") || file1.getPath().endsWith(".jpg")) && file1.getPath().contains("blindHelper")){
                    FileInputStream fis = null;
                    String filePath = file1.getPath();
                    Toast.makeText(CapturePictureAutomatically.this, "filePath="+filePath, Toast.LENGTH_SHORT).show();
                    try {
                        fis = new FileInputStream(filePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);

                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    arrayList.add(new Image(file1.getName(),file1.getPath(),file1.length()));
                    Toast.makeText(this, " file1.getPath()="+file1.getPath()+"; file1.getName()="+file1.getName(), Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse(file1.getPath());

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if(bitmap != null){
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        final String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

                        // Make Post method
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url ="http://bgroutingmap.com/_testGetImage/test55.php";

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        Toast.makeText(CapturePictureAutomatically.this, "response="+response, Toast.LENGTH_LONG).show();
                                        if(response.contains("success")){
                                            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }){
                            protected Map<String, String> getParams(){
                                Map<String, String> paramV = new HashMap<>();
                                paramV.put("image", base64Image);
                                return paramV;
                            }
                        };
                        queue.add(stringRequest);
                    }else{
                        Toast.makeText(CapturePictureAutomatically.this, "Select the image first", Toast.LENGTH_SHORT).show();
                    }

                    break;

                }
            }

        }

        ImageAdapter adapter = new ImageAdapter(CapturePictureAutomatically.this,arrayList);

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListner((view, path) -> startActivity(new Intent(CapturePictureAutomatically.this, ImageViewTarget.class).putExtra("image",path)));
        Bitmap bitmap;
        //bitmap = new Bitmap(file1.getName(),file1.getPath(),file1.length());
        Uri uri = Uri.parse(arrayList.get(0).getPath());
        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
        Toast.makeText(this, ""+uri, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, ""+arrayList.get(0).getPath(), Toast.LENGTH_SHORT).show();
        //bitmap = arrayList.get(0);



    }

}