package com.at.agromap;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

//import com.esri.arcgisruntime.concurrent.Job;
//import com.esri.arcgisruntime.concurrent.ListenableFuture;
//import com.esri.arcgisruntime.geometry.Geometry;
//import com.esri.arcgisruntime.mapping.ArcGISMap;
//import com.esri.arcgisruntime.mapping.Basemap;
//import com.esri.arcgisruntime.mapping.view.MapView;
//import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob;
//import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
//import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;

import com.esri.android.map.MapView;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static MapView mMapView;
    private static SyncGeodatabase syncGdb = new SyncGeodatabase(mMapView);
//    private static CharSequence msgSyncResultText = "";
    private static final int duration = Toast.LENGTH_SHORT;
    private static TextView pathView;
    Boolean isSDPresent = android.os.Environment
            .getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.map);
        pathView = (TextView) findViewById(R.id.pathView);

        if (isSDPresent) {
            pathView.setText("SD card available");
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
    public void showToastSyncResult(CharSequence msgSyncResultText) {
        Toast.makeText(getApplicationContext(), msgSyncResultText, duration).show();
    }

}
