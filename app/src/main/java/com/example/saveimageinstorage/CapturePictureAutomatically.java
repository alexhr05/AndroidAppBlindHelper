package com.example.saveimageinstorage;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
// a
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

//AppCompatActivity
public class CapturePictureAutomatically extends MainActivity{
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Button bTakePicture, btnStopTakePicture, btnStartTakingPhotos,btnShowImage;
    PreviewView previewView;
    private ImageCapture imageCapture;
    Handler handler;
    Runnable runnable;
    Uri imageUri;
    OutputStream outputStream;
    ArrayList<Image> arrayList = new ArrayList<>();
    String timestamp, serverHostName, volume, language;
    TextToSpeech t1;
    ArrayList<String> result;
    Thread soundForListen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_picture_automatically);
        bTakePicture = findViewById(R.id.bTakePicture);
        btnStopTakePicture = findViewById(R.id.btnStopTakePicture);
        btnStartTakingPhotos = findViewById(R.id.btnStartTakingPhotos);
        btnShowImage = findViewById(R.id.btnShowImage);


        previewView = findViewById(R.id.previewView);

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    t1.setLanguage(Locale.ENGLISH);
                    //forLanguageTag("bg-BG")
                }
            }
        });


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

                    /*    try {
                            obj.getImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
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
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                int minVolume = 0;//audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, minVolume, 0);

                //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                //int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

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

        speakText();
       /* soundForListen = new Thread(new Runnable(){
            public void run(){
                while(true){
                    try {
                        //mediaPlayer.start();
                        Thread.sleep(2000);

                        //mediaPlayer.setLooping(true);
                        //checkForWords();
                        //  Thread.sleep(5000);
                       // mediaPlayer.setLooping(false);
                        //speakText();


                    } catch (InterruptedException ex) {
                        //Logger.getLogger(AwesomeCarGame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        soundForListen.start();*/


    }
    private void speakText() {
        // intent to show speech to text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi speak something");
        // start intent

        try{
            // show dialog
            startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
        }catch (Exception e){

            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }



    // recieve voice input and handle it
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT:{
                if(resultCode == RESULT_OK && data != null){
                    // get text array from voice intent
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.contains("snimai") || result.contains("снимай")) {
                        capturePhoto();
                        soundForListen.interrupt();

                    }

                    // set a text view
                    //editTextShowText.setText(result.get(0));



                }
                break;
            }

        }
    }



    /*public void readSettingsFile(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("myfile.txt"));
            String line;
            while ((line = br.readLine()) != null) {

                System.out.println(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    private class NetworkRequestTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... params) {
            byte[] imageData = params[0];

            URL url = null;
            HttpURLConnection client = null;
            OutputStream outputPost = null;
            try {
                url = new URL("http://46.10.208.174:8033");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setDoInput(true);
                client.setDoOutput(true);
                outputPost = new BufferedOutputStream(client.getOutputStream());

                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                outputPost.write(imageBytes);
                outputPost.flush();
                String result = "";
                if (client.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line + "\r\n";
                        }
                    }
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "ERROR";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(CapturePictureAutomatically.this, result, Toast.LENGTH_SHORT).show();
            t1.speak(result, TextToSpeech.QUEUE_FLUSH,null);

        }
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

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(result).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CapturePictureAutomatically.this, "Photo has been saved successfully", Toast.LENGTH_SHORT).show();

                        new NetworkRequestTask().execute(result.toByteArray());
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

    /*private void capturePhoto() {
        /*File photoDir = new File("/storage/emulated/0/111");

        if(!photoDir.exists()){
            photoDir.mkdir();
        }
        Date date = new Date();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        Toast.makeText(this, " " + str, Toast.LENGTH_SHORT).show();
        timestamp = "blindHelper-" + str + "-" + System.currentTimeMillis();// +System.currentTimeMillis();//+ Integer.parseInt(String.valueOf(now));

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        /*String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";

        File photoFile = new File(photoFilePath);


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
                        try {
                            getImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        //Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(CapturePictureAutomatically.this, "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }


        );
        Toast.makeText(this, " " + imageCapture, Toast.LENGTH_LONG).show();


    }*/

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
        String filepaths = "/storage/emulated/0/Pictures";
        //Environment.getExternalStorageDirectory() + "/Pictures";
        File file = new File(filepaths);
        Toast.makeText(this, "file="+file, Toast.LENGTH_SHORT).show();
        File[] files = file.listFiles();
        Toast.makeText(this, "file.listFiles()="+file.listFiles(), Toast.LENGTH_SHORT).show();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });
        //Bitmap bitmap = null;
       /* if(files != null){
            for(File file1: files){
                if((file1.getPath().endsWith(".png") || file1.getPath().endsWith(".jpg")) && file1.getPath().contains("blindHelper")){
                    FileInputStream fis = null;
                    String filePath = file1.getPath();
                    //String filePath = filepaths + "/"+timestamp+".jpg";
                    Toast.makeText(CapturePictureAutomatically.this, "------------------filePath="+filepaths+"/"+timestamp, Toast.LENGTH_SHORT).show();
                    Toast.makeText(CapturePictureAutomatically.this, "filePath-file11111="+file1.getPath(), Toast.LENGTH_SHORT).show();
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
                    //Toast.makeText(this, " file1.getPath()="+file1.getPath()+"; file1.getName()="+file1.getName(), Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse(file1.getPath());

                    /*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
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

        }*/

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