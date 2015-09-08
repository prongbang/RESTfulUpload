package com.prongbang.restfulupload;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.prongbang.dto.Properties;
import com.prongbang.service.FileUploadService;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String HOST_SERVICE = "http://172.16.1.39:8080/rest-upload/rest/service";
    private static final int FILE_CODE = 0;
    private EditText pathFile;
    private TextView statusUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathFile = (EditText) findViewById(R.id.path_file);
        pathFile.setEnabled(false);
        statusUpload = (TextView) findViewById(R.id.status);
        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(this);

        /**
         * Choose File Dialog
         */
        Button choseFile = (Button) findViewById(R.id.choose_file);
        choseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();

                // This always works
                Intent i = new Intent(context, FilePickerActivity.class);
                // This works if you defined the intent filter
                // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                // Configure initial directory by specifying a String.
                // You could specify a String like "/storage/emulated/0/", but that can
                // dangerous. Always use Android's API calls to get paths to the SD-card or
                // internal memory.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, FILE_CODE);
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            // Do something with the URI
                            pathFile.setText(uri.getPath());
                            Log.d("uri", uri.getPath());
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            // Do something with the URI
                            pathFile.setText(uri.getPath());
                            Log.d("uri", uri.getPath());
                        }
                    }
                }
            } else {
                Uri uri = data.getData();
                // Do something with the URI
                pathFile.setText(uri.getPath());
                Log.d("uri", uri.getPath());
            }
        }
    }


    /**
     * @param view
     * @button event
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUpload:
                if (!pathFile.getText().equals("") && pathFile.getText() != null) {
                    File file = new File(pathFile.getText().toString());
                    String filename = file.getName();

                    try {

                        /**
                         * Convert File to Array of Byte
                         */
                        byte[] fileBytes = org.apache.commons.io.FileUtils.readFileToByteArray(file);
                        Properties properties = new Properties();
                        properties.setFilename(filename);
                        properties.setFileBytes(fileBytes);

                        /**
                         * Retrofit 1.9.0
                         */
//                        JacksonConverter converter = new JacksonConverter(new ObjectMapper());
                        RestAdapter adapter = new RestAdapter.Builder()
                                .setEndpoint(HOST_SERVICE)
                                .build();
                        FileUploadService fileUploadService = adapter.create(FileUploadService.class);
                        fileUploadService.upload(properties, new Callback<Boolean>() {
                            @Override
                            public void success(Boolean status, Response response) {
                                if (status) statusUpload.setText("Upload Success :)");
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                statusUpload.setText("Upload Fail :(");
                                Log.e("error",error.toString());
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "โปรดเลือกไฟล์ที่จะอัพโหลด", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
