package com.at.agromap;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.TextView;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;

import java.io.FileNotFoundException;

class GeodatabaseProc {

    private ProgressDialog dialog;
    private MapView map;
    private static GeodatabaseSyncTask gdbSyncTask;
    private String localGdbFilePath;
    private TextView pathView;
    private String username;
    private String password;

    GeodatabaseProc(MapView map, String localGdbFile, TextView pathView, String username, String password) {
        this.map = map;
        this.localGdbFilePath = localGdbFile;
        this.pathView = pathView;
        this.username = username;
        this.password = password;
    }

    void downloadData(String url) {
        Log.i("GDB", "Create GeoDatabase");
// create a dialog to update user on progress
        dialog = ProgressDialog.show(map.getContext(), "Загрузка слоя данных", "Сздание локальной базы геоданных");
        dialog.show();

// create the GeodatabaseTask
        UserCredentials credentials = new UserCredentials();
        credentials.setUserAccount(username, password);
        gdbSyncTask = new GeodatabaseSyncTask(url, credentials);
        gdbSyncTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

            @Override
            public void onError(Throwable arg0) {
                Log.e("GDB", "Error fetching FeatureServiceInfo");
            }

            @Override
            public void onCallback(FeatureServiceInfo fsInfo) {
                if (fsInfo.isSyncEnabled()) {
                    createGeodatabase(fsInfo);
                }
            }
        });
    }

    /**
     * Set up parameters to pass the the submitTask() method. A
     * CallbackListener is used for the response.
     */
    private void createGeodatabase(FeatureServiceInfo featureServerInfo) {
// set up the parameters to generate a geodatabase
        GenerateGeodatabaseParameters params = new GenerateGeodatabaseParameters(featureServerInfo, map.getExtent(),
                map.getSpatialReference());

// a callback which fires when the task has completed or failed.
        CallbackListener<String> gdbResponseCallback = new CallbackListener<String>() {
            @Override
            public void onError(final Throwable e) {
                Log.e("GDB", "Error creating geodatabase" + e.getMessage());
                dialog.dismiss();
            }

            @Override
            public void onCallback(String path) {
                Log.i("GDB", "Geodatabase is: " + path);
                dialog.dismiss();
                // update map with local feature layer from geodatabase
                updateFeatureLayer(path);

            }
        };

// a callback which updates when the status of the task changes
        GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {
            @Override
            public void statusUpdated(GeodatabaseStatusInfo status) {
                Log.i("GDB", status.getStatus().toString());

            }
        };

// create the fully qualified path for geodatabase file
//        localGdbFilePath = createGeodatabaseFilePath();

// get geodatabase based on params
        submitTask(params, localGdbFilePath, statusCallback, gdbResponseCallback);
    }

    /**
     * Request database, poll server to get status, and download the file
     */
    private static void submitTask(GenerateGeodatabaseParameters params, String file,
                                   GeodatabaseStatusCallback statusCallback,
                                   CallbackListener<String> gdbResponseCallback) {
// submit task
        gdbSyncTask.generateGeodatabase(params, file, false, statusCallback, gdbResponseCallback);
    }

    /**
     * Add feature layer from local geodatabase to map
     */
    private void updateFeatureLayer(String featureLayerPath) {
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
                    map.addLayer(new FeatureLayer(gdbFeatureTable));
            }
        }
// display the path to local geodatabase
        pathView.setText(featureLayerPath);

    }
}
