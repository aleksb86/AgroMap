package com.at.agromap;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private static final String TAG = "Map";
//    private Dialog dialog;
    private GeodatabaseSyncTask gdbSyncTask;
    private Geometry syncGdbExtent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.mapView);
        ArcGISMap map = new ArcGISMap(Basemap.Type.IMAGERY, 34.056295, -117.195800, 16);
        mMapView.setMap(map);
    }

    @Override
    protected void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    // begin sync task
//    void createSyncTaskAndParameters() {
//
//        // create a new GeodatabaseSyncTask to create a local version of feature service data, passing in the service url
//        String featureServiceUri = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer";
//        gdbSyncTask = new GeodatabaseSyncTask(featureServiceUri);
//
//        // get the default parameters for generating a geodatabase
//        // TODO: для syncGdbExtent ДОЛЖНО быть какое-то значение,
//        final ListenableFuture<GenerateGeodatabaseParameters> paramsFuture =
//                gdbSyncTask.createDefaultGenerateGeodatabaseParametersAsync(syncGdbExtent);
//
//        paramsFuture.addDoneListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // get default parameters
//                    GenerateGeodatabaseParameters generateGeodatabaseParameters = paramsFuture.get();
//
//                    // make any changes required to the parameters, for example do not return attachments
//                    generateGeodatabaseParameters.setReturnAttachments(false);
//
//                    // optionally, specify the spatial reference of the geodatabase to be generated
//                    generateGeodatabaseParameters.setOutSpatialReference(mMapView.getSpatialReference());
//
//                    // call a function to generate the geodatabase
//                    generateGeodatabase(generateGeodatabaseParameters);
//                }
//                catch (InterruptedException | ExecutionException e) {
//                    Log.v(TAG, e.getMessage());
//                }
//            }
//        });
//    }
//
//    void generateGeodatabase(final GenerateGeodatabaseParameters parameters) {
//
//        // create the generate geodatabase job, pass in the parameters and an output path for the local geodatabase
//        final GenerateGeodatabaseJob generateGeodatabaseJob =
//                gdbSyncTask.generateGeodatabaseAsync(parameters, "./path/to/gdb_file.gdb");
//
//        // add a job changed listener to check the status of the job
//        generateGeodatabaseJob.addJobChangedListener(new Runnable() {
//            @Override
//            public void run() {
//                // for example, if the job is still running, report the last message to the user
//                final List<Job.Message> messages = generateGeodatabaseJob.getMessages();
////                updateUiWithProgress(messages.get(messages.size() - 1).getMessage());
//            }
//        });
//
//        // add a job done listener to deal with job completion - success or failure
//        generateGeodatabaseJob.addJobDoneListener(new Runnable() {
//            @Override
//            public void run() {
//                if (generateGeodatabaseJob.getStatus() == Job.Status.FAILED) {
//                    // deal with job failure - check the error details on the job
////                    dealWithJobDoneFailed(generateGeodatabaseJob.getError());
//                    return;
//                }
//                else if (generateGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED)
//                {
//                    // if the job succeeded, the geodatabase is now available at the given local path.
//                    // add local data from the geodatabase to the map - see following section...
////                    addGeodatabaseLayerToMap();
//                }
//            }
//        });
//
//        // start the job to generate and download the geodatabase
//        generateGeodatabaseJob.start();
//    }
    // end sync task
}
