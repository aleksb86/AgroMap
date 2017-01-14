package com.at.agromap;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheStatus;
import com.esri.core.tasks.tilecache.ExportTileCacheTask;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by uaboev on 06.01.2017.
 * Basemaps processing demo:
 * load basemaps from given service
 * URL or saved *.tpk files.
 */

public class Basemaps {
    private String[] basemapPaths;
    private String[] basemapURLs;

    private void fetchBasemaps(String basemapAppFolder, String[] urlsList) {

    }

    /**
     * Подготовка параметров (в т.ч. уровня детализации)
     * для задания загрузки тайлового кеша базовой карты.
     * @param map объект карты (MapView), созданный во внешнем контексте.
     * @param tileCachePath абсолютный путь к файлу TPK.
     */
    // TODO утрясти параметры метода!
    private void downloadBasemap(MapView map, final String tileCachePath,
                                 final UserCredentials credentials,
                                 ArrayList<Double> levelsArraylist,
                                 String tileUrl,
                                 boolean createAsTilePackage) {
        double[] levels = new double[levelsArraylist.size()];
        // Настройка экстента карты
        Envelope extentForTPK = new Envelope();
        map.getExtent().queryEnvelope(extentForTPK);

        // If the user does not select the Level of details
        // then give out the status message in a toast
//        if (levelsArraylist.size() == 0) {
//            Toast.makeText(this, "Please Select Levels of Detail",
//                    Toast.LENGTH_LONG).show();
//            // Hide the progress bar
//            setProgressBarIndeterminateVisibility(false);
//            return;
//        }
//
//        // Specify all the Levels of details in an integer array
//        for (int i = 0; i < levelsArraylist.size(); i++) {
//            levels[i] = levelsArraylist.get(i);
//        }
//
//        // Create an instance of ExportTileCacheTask for the mapService that
//        // supports the exportTiles() operation
//        final ExportTileCacheTask exportTileCacheTask = new ExportTileCacheTask(
//                tileUrl, null);
//
//        // Set up GenerateTileCacheParameters
//        ExportTileCacheParameters params = new ExportTileCacheParameters(
//                createAsTilePackage, levels, ExportTileCacheParameters.ExportBy.ID, extentForTPK,
//                map.getSpatialReference());
//
//        // create tile cache
//        createTileCache(params, exportTileCacheTask, tileCachePath);
//    }
//
//    /*
//    Запуск задания формирования тайлового кэша базовой карты
//     */
//    private void createTileCache(ExportTileCacheParameters params,
//                                 final ExportTileCacheTask exportTileCacheTask,
//                                 final String tileCachePath,
//                                 CallbackListener<Long> callbackListener) {
//        // estimate tile cache size
//        exportTileCacheTask.estimateTileCacheSize(params,
//                callbackListener);
//
//        // create status listener for generateTileCache
//        CallbackListener<ExportTileCacheStatus> statusListener = new CallbackListener<ExportTileCacheStatus>() {
//
//            @Override
//            public void onError(Throwable e) {
//                Log.d("Cache status error", e.getMessage());
//            }
//
//            @Override
//            public void onCallback(ExportTileCacheStatus objs) {
////                Log.d("*** tileCacheStatus : ", objs.getStatus().toString());
//                final String status = objs.getStatus().toString();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String desc;
//
////                        if (status.equals("SUCCEEDED")) {
////                            desc = "Успешно";
////                        } else if (status.equals("EXECUTING")) {
////                            desc = "В процессе";
////                        } else {
////                            desc = "Неизвестно";
////                        }
//                        showToast("Статус загрузки кэша - " + status);//desc);
////                        Toast.makeText(getApplicationContext(), status,
////                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        };
//
//        // Submit tile cache job and download
//        exportTileCacheTask.generateTileCache(params, statusListener,
//                new CallbackListener<String>() {
//                    private boolean errored = false;
//
//                    @Override
//                    public void onError(Throwable e) {
//                        errored = true;
//                        // print out the error message and disable the progress
//                        // bar
//                        Log.d("gen. TileCache error: ", e.getMessage());
//                        final String error = e.toString();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
////                                setProgressBarIndeterminateVisibility(false);
//                                showToast("generateTileCache error: " + error);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCallback(final String path) {
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast("path from callback " + path);
//                                Log.e("Path", path);
//                            }
//                        });
//                        if (!errored) {
//                            Log.d("the Download Path = ", "" + path);
//
//                            // switch to the successfully downloaded local layer
//                            localTiledLayer = new ArcGISLocalTiledLayer(path);
//                            map.addLayer(localTiledLayer);
//                            // initially setting the visibility to false,
//                            // turning it back on in the switchToLocalLayer()
//                            // method
//                            map.getLayers()[1].setVisible(false);
//
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    // Hide the progress bar
////                                    setProgressBarIndeterminateVisibility(false);
//                                    showToast("TileCache successfully downloaded, Switching to Local Tiled Layer");
//                                    switchMapToLayer();
//                                }
//                            });
//                        }
//                    }
//                }, tileCachePath);
    }
}
