package com.at.agromap;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.util.Log;

//import com.esri.arcgisruntime.concurrent.Job;
//import com.esri.arcgisruntime.concurrent.ListenableFuture;
//import com.esri.arcgisruntime.geometry.Geometry;
//import com.esri.arcgisruntime.geometry.SpatialReference;
//import com.esri.arcgisruntime.mapping.view.MapView;
//import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob;
//import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
//import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.R.attr.duration;
import static android.content.Context.ACTIVITY_SERVICE;
import static com.at.agromap.R.id.text;

/**
 * Created by uaboev on 30.11.2016.
 */

public class SyncGeodatabase {

//    private static final String TAG = "Map";
//    private GeodatabaseSyncTask gdbSyncTask;
//    private Geometry syncGdbExtent = new Geometry();
//    private static SpatialReference spatialRef = SpatialReference.create(102100);
//
//    void createSyncTaskAndParameters() {
//
//        // create a new GeodatabaseSyncTask to create a local version of feature service data, passing in the service url
//        String featureServiceUri = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer";
//        gdbSyncTask = new GeodatabaseSyncTask(featureServiceUri);
//
//        // get the default parameters for generating a geodatabase
//        // TODO: для syncGdbExtent ДОЛЖНО быть какое-то значение типа Geometry
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
//                    generateGeodatabaseParameters.setOutSpatialReference(spatialRef);
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
//                gdbSyncTask.generateGeodatabaseAsync(parameters, "gdb_file.gdb");
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
//
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

    /*
    Misc methods
     */

}
