package com.at.agromap;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.R.attr.duration;
import static android.content.Context.ACTIVITY_SERVICE;
import static com.at.agromap.R.id.text;

/**
 * Created by uaboev on 30.11.2016.
 */

public class SyncGeodatabase {

    private static final String TAG = "Map";
    private static Dialog dialog = null;
//    private GeodatabaseSyncTask gdbSyncTask;
//    private Geometry syncGdbExtent = new Geometry();
//    private static SpatialReference spatialRef = SpatialReference.create(102100);
    private static MapView mapView = null;
    static GeodatabaseSyncTask gdbSyncTask;
    static String localGdbFilePath = "offline_geodb_file.gdb";
    static String returnGdbPath = "";

    SyncGeodatabase(MapView outerMapView) {
        this.mapView = outerMapView;
    }

    public String getGdbPath() {
        return returnGdbPath;
    }

    /**
     * Create the GeodatabaseTask from the feature service URL w/o credentials.
     */
    public void downloadData(String url, Context context) {
        Log.i(TAG, "Create GeoDatabase");
        // create a dialog to update user on progress
        dialog = ProgressDialog.show(context, "Download Data", "Create local runtime geodatabase");
        dialog.show();

        // create the GeodatabaseTask
        gdbSyncTask = new GeodatabaseSyncTask(url, null);
        gdbSyncTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

            @Override
            public void onError(Throwable arg0) {
                Log.e(TAG, "Error fetching FeatureServiceInfo");
            }

            @Override
            public void onCallback(FeatureServiceInfo fsInfo) {
                if (fsInfo.isSyncEnabled()) {
                    createGeodatabase(fsInfo, mapView);
                }
            }
        });

    }

    /**
     * Set up parameters to pass the the submitTask() method. A
     * CallbackListener is used for the response.
     */
    private static void createGeodatabase(FeatureServiceInfo featureServerInfo, MapView mMapView) {
        // set up the parameters to generate a geodatabase
        GenerateGeodatabaseParameters params = new GenerateGeodatabaseParameters(featureServerInfo, mMapView.getExtent(),
                mMapView.getSpatialReference());

        // a callback which fires when the task has completed or failed.
        CallbackListener<String> gdbResponseCallback = new CallbackListener<String>() {
            @Override
            public void onError(final Throwable e) {
                Log.e(TAG, "Error creating geodatabase");
                dialog.dismiss();
            }

            @Override
            public void onCallback(String path) {
                Log.i(TAG, "Geodatabase is: " + path);
                dialog.dismiss();
                // update map with local feature layer from geodatabase
                updateFeatureLayer(path, mapView);

            }
        };

        // a callback which updates when the status of the task changes
        GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {
            @Override
            public void statusUpdated(GeodatabaseStatusInfo status) {
                Log.i(TAG, status.getStatus().toString());

            }
        };

        // create the fully qualified path for geodatabase file
//        localGdbFilePath = createGeodatabaseFilePath();

        // get geodatabase based on params
        submitTask(params, localGdbFilePath, statusCallback, gdbResponseCallback);
        dialog.hide();
    }


    /**
     * Request database, poll server to get status, and download the file
     */
    private static void submitTask(GenerateGeodatabaseParameters params, String file,
                                   GeodatabaseStatusCallback statusCallback, CallbackListener<String> gdbResponseCallback) {
        // submit task
        gdbSyncTask.generateGeodatabase(params, file, false, statusCallback, gdbResponseCallback);
    }

    /**
     * Add feature layer from local geodatabase to map
     */
    private static void updateFeatureLayer(String featureLayerPath, MapView mMapView) {
        // create a new geodatabase
        Geodatabase localGdb = null;
        try {
            localGdb = new Geodatabase(featureLayerPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Geodatabase contains GdbFeatureTables representing attribute data
        // and/or spatial data. If GdbFeatureTable has geometry add it to
        // the MapView as a Feature Layer
        if (localGdb != null) {
            for (GeodatabaseFeatureTable gdbFeatureTable : localGdb.getGeodatabaseTables()) {
                if (gdbFeatureTable.hasGeometry())
                    mMapView.addLayer(new FeatureLayer(gdbFeatureTable));
            }
        }
        // display the path to local geodatabase
        returnGdbPath = featureLayerPath;
//        pathView.setText(featureLayerPath);

    }

    /*
    Misc methods
     */
}
