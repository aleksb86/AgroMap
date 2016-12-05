package com.at.agromap;

//import android.app.Dialog;
//import android.app.ProgressDialog;
import android.content.Context;
//import android.content.res.Resources;
//import android.net.Uri;
//import android.support.annotation.IdRes;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.MapView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.util.List;
import java.util.Scanner;
//import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static MapView mMapView;
    private static SyncGeodatabase syncGdb = new SyncGeodatabase(mMapView);
//    private static CharSequence msgSyncResultText = "";
    private static final int duration = Toast.LENGTH_SHORT;
    private Boolean isSDPresent = android.os.Environment
            .getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.map);
        TextView pathView = (TextView) findViewById(R.id.pathView);
        Button buttonCheckGeodb = (Button) findViewById(R.id.check_geodb_file_btn);
//        final Button buttonWrite = (Button) findViewById(R.id.write_file_btn);
        Button buttonSwitchToTiled = (Button) findViewById(R.id.call_tiled_map_button);
        final Intent callTiledMapIntent = new Intent(MainActivity.this, TailedMapActivity.class);

        buttonCheckGeodb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        getApplicationContext(),
                        "Содержимое файла: " + readTestFile("jaba.txt"),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

//        buttonWrite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                writeTestFile();
//                buttonWrite.setText("File Del", TextView.BufferType.EDITABLE);
//
//                Toast.makeText(
//                        getApplicationContext(), "Файл записан.", Toast.LENGTH_SHORT
//                ).show();
//
////                if (checkIfFileExists("jaba.txt")) {
////                    deleteTestFile("jaba.txt");
////                    buttonWrite.setText("File W", TextView.BufferType.EDITABLE);
////                }
//            }
//        });

        buttonSwitchToTiled.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // call another activity
                MainActivity.this.startActivity(callTiledMapIntent);
            }
        });

        if (isSDPresent) {
//            pathView.setText("SD card available");
            pathView.setText(getApplicationContext().getFilesDir().toString());
        } else {
            pathView.setText("SD card not found!");
        }

//        syncGdb.downloadData(getString(R.string.basemap_service_url), mMapView.getContext());

//        syncGdb.createSyncTaskAndParameters();

    }

    @Override
    protected void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }

    // Toast for display result of sync operation
//    public void showToastSyncResult(CharSequence msgSyncResultText) {
//        Toast.makeText(getApplicationContext(), msgSyncResultText, duration).show();
//    }

    public void writeTestFile() {
        String txtContent = "Jaba !!11";
        String filename = "jaba.txt";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(txtContent.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e("FILE_WRITE", "Test file write error: " + e.getMessage());
        }
    }

    public String readTestFile(String fileName) {
        File filePath = getApplicationContext().getFilesDir();
        String fileContent = "";
        try {
            Scanner scanner = new Scanner(new File(filePath, fileName));
            while (scanner.hasNextLine()) {
                fileContent += scanner.nextLine() + "\n";
            }
            return fileContent;
        } catch (FileNotFoundException e) {
            Log.e("FILE_READ", "Test file read error: " + e.getMessage());
        }
        return fileContent;
    }

    public void deleteTestFile(String fileName) {
        File filePath = getApplicationContext().getFilesDir();
        File fullPath = new File(filePath, fileName);
        try {
            fullPath.delete();
        } catch (SecurityException e) {
            Log.e("FILE_DELETE", "Test file deletion error: " + e.getMessage());
        }
    }

    public boolean checkIfFileExists(String fileName) {
        File filePath = getApplicationContext().getFilesDir();
        File fullPath = new File(filePath, fileName);
        if (fullPath.exists()) { return true; }
        return false;
    }
}
