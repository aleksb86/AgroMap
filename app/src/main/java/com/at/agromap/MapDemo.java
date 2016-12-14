package com.at.agromap;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters.ExportBy;
import com.esri.core.tasks.tilecache.ExportTileCacheStatus;
import com.esri.core.tasks.tilecache.ExportTileCacheTask;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MapDemo extends AppCompatActivity {

    private MapView map;
    private File basePath;
//    private String geodbFileName;
    private String basemapFileName;
    private  String appDataStorageDir;
    private String tileUrl;
    private MenuItem selectLevels;
    private MenuItem download;
    private MenuItem switchMaps;
    private ProgressDialog mapLoadingProgress;
//    private HashMap<String, Double>mapResolution;
    private CharSequence[] detailsLevelNames;
    private double[] detailLevelValues;
    private ArrayList<Double> levelsArraylist = new ArrayList<>();
    // The generated tile cache will be a compact cache
    private boolean createAsTilePackage = false;
    private double[] levels;
    private ArcGISLocalTiledLayer localTiledLayer;
    private boolean[] itemsChecked;
    private Resources resources;
    private boolean isLocalLayerVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        basePath = Environment.getExternalStorageDirectory();
//        geodbFileName = getString(R.string.geodb_file_name);
        basemapFileName = getString(R.string.tpk_basemap_file_name);
        appDataStorageDir = getString(R.string.offlinemap_files_dir);
        tileUrl = getString(R.string.basemap_service_url);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_demo);

        map = (MapView) findViewById(R.id.map);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.map_demo_toolbar);
        setSupportActionBar(toolbar);
        final ArcGISTiledMapServiceLayer onlineBasemapLayer = new ArcGISTiledMapServiceLayer(tileUrl);
        final TextView testPath = (TextView) findViewById(R.id.geodb_file_path);
        resources = getResources();

//        mapLoadingProgress = new ProgressDialog(MapDemo.this);
//        mapLoadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        mapLoadingProgress.setIndeterminate(true);
//        mapLoadingProgress.setCancelable(true);
//        mapLoadingProgress.setMessage(getString(R.string.loading_text));
//        mapLoadingProgress.show();

        detailsLevelNames = resources.getStringArray(R.array.level_items_name);
        detailLevelValues = new double[detailsLevelNames.length];
        for (int i = 0; i < resources.getStringArray(R.array.level_items_value).length; i++) {
            detailLevelValues[i] = Double.parseDouble(resources.getStringArray(R.array.level_items_value)[i]);
        }

        itemsChecked = new boolean[detailsLevelNames.length];
        isLocalLayerVisible = false;

        /*
        Отслеживание изменений состояния карты при инициализации
         */
        map.setOnStatusChangedListener(new OnStatusChangedListener() {
            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (status.equals(OnStatusChangedListener.STATUS.INITIALIZED) && source == map) {
                    showToast("Map status:" + status.toString() + " source: " + source.toString());

                } else if (status.equals(OnStatusChangedListener.STATUS.LAYER_LOADED) && source == onlineBasemapLayer) {
                    showToast("Layer status: " + status.toString() + " source: " + source.toString());
//                    map.post(new Runnable() {
//                        @Override
//                        public void run() {
////                            mapLoadingProgress.dismiss();
//                            testPath.setText("222");
//                        }
//                    });

                } else if (status.equals(OnStatusChangedListener.STATUS.INITIALIZATION_FAILED)) {
                    map.post(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(map.getContext())
                                .setTitle(R.string.unable_map_load_dialog_title)
                                .setMessage(R.string.unable_map_load_dialog_msg)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        }
                    });
                }
            }
        });

        /*
        Проверка доступности внешнего носителя (SD)
         */
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // Проверка существования каталога, в котором хранятся
            // файл базы геоданных, файл TPK (базовая карта)
            if (!checkDirForAppFilesExists(basePath, appDataStorageDir)) {
                if (createDirForAppFiles(basePath, appDataStorageDir)) {
                    MediaScannerConnection.scanFile(this,
                            new String[] {
                                    Environment
                                            .getExternalStorageDirectory()
                                            .toString() + appDataStorageDir
                            },
                            null, null);
                    showToast("Каталог " + appDataStorageDir + " создан.");
                } else {
                    showToast("Невозможно создать каталог " + appDataStorageDir);
                    // TODO: здесь все должно остановиться, т.к. нет ресурсов - нет обработки
                }
            } else {
                // Каталог есть - ищем файл базы геоданных
                if (checkGdbFileExists(basePath, appDataStorageDir)) {// + basemapFileName)) {
                    // TODO: если файл присутствует, то нужно использовать его
                    ArcGISLocalTiledLayer localBaseMap = new ArcGISLocalTiledLayer(Environment.getExternalStorageDirectory().toString() + appDataStorageDir);// + basemapFileName);
                    map.addLayer(localBaseMap);
                } else {
                    // Если файла базовой карты нет, то пытаемся отобразить онлайновую
                    // базовую карту, полученную через Map сервис
                    map.addLayer(onlineBasemapLayer);
                    map.centerAt(51.756880, 36.134419, true);
                }
            }
        } else {
            showToast("Карта памяти не установлена/не подключена/не отвечает!");
            // TODO: здесь все должно остановиться, т.к. нет ресурсов - нет обработки
        }
    }

    /*
    Основное меню. Действия с картой и слоями
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.offline_map_menu, menu);
        // menu items
        selectLevels = menu.getItem(0);
//        selectLevels.setIcon(android.R.drawable.ic_menu_crop);
        download = menu.getItem(1);
        switchMaps = menu.getItem(2);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_levels:
                showDialog();
                return true;
            case R.id.download:
                downloadBasemap();
                return true;
            case R.id.switch_maps:
                switchMapToLayer();
//                if (isLocalLayerVisible) {
//                    switchMapToLayer();
//                } else {
//                    switchToLocalLayer();
//                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    Метод отображает диалог выбора уровня детализации (LOD),
    который нужен в качестве параметра для задания кэширования карты.
     */
    public void showDialog() {

        boolean[] uncheckedItems = new boolean[detailsLevelNames.length];
        Arrays.fill(uncheckedItems, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select the Levels of Detail");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // When ok button is pressed, we go through the array of
                // itemsChecked and add the selected
                // items to levelsArraylist
                for (int i = 0; i < detailLevelValues.length; i++) {
                    if (itemsChecked[i]) {

                        levelsArraylist.add((double) i);
                        itemsChecked[i] = false;
                    }
                }
            }
        });

        builder.setMultiChoiceItems(detailsLevelNames, uncheckedItems,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        itemsChecked[which] = isChecked;
                    }
                });
        builder.show();
    }

    // создать локальный кэш из данных Map сервиса
    private void downloadBasemap() {
        Envelope extentForTPK = new Envelope();
        final String tileCachePath = Environment.getExternalStorageDirectory().toString() + appDataStorageDir;// + basemapFileName;
        map.getExtent().queryEnvelope(extentForTPK);

        // If the user does not select the Level of details
        // then give out the status message in a toast
        if (levelsArraylist.size() == 0) {
            Toast.makeText(this, "Please Select Levels of Detail",
                    Toast.LENGTH_LONG).show();
            // Hide the progress bar
            setProgressBarIndeterminateVisibility(false);
            return;
        }

        levels = new double[levelsArraylist.size()];

        // Specify all the Levels of details in an integer array
        for (int i = 0; i < levelsArraylist.size(); i++) {
            levels[i] = levelsArraylist.get(i);
        }

        // Create an instance of ExportTileCacheTask for the mapService that
        // supports the exportTiles() operation
        final ExportTileCacheTask exportTileCacheTask = new ExportTileCacheTask(
                tileUrl, null);

        // Set up GenerateTileCacheParameters
        ExportTileCacheParameters params = new ExportTileCacheParameters(
                createAsTilePackage, levels, ExportBy.ID, extentForTPK,
                map.getSpatialReference());

        // create tile cache
        createTileCache(params, exportTileCacheTask, tileCachePath);
    }

    /*
    Запуск задания формирования тайлового кэша базовой карты
     */
    private void createTileCache(ExportTileCacheParameters params,
                                 final ExportTileCacheTask exportTileCacheTask,
                                 final String tileCachePath) {
        // estimate tile cache size
        exportTileCacheTask.estimateTileCacheSize(params,
                new CallbackListener<Long>() {

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Tile loading error", e.getMessage());
                    }

                    @Override
                    public void onCallback(Long objs) {
//                        Log.d("*** tilecachesize: ", "" + objs);
                        final long tilecachesize = objs / 1024;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Примерный размер кэша: " + tilecachesize + "Kb");
                            }
                        });
                    }
                });

        // create status listener for generateTileCache
        CallbackListener<ExportTileCacheStatus> statusListener = new CallbackListener<ExportTileCacheStatus>() {

            @Override
            public void onError(Throwable e) {
                Log.d("Cache status error", e.getMessage());
            }

            @Override
            public void onCallback(ExportTileCacheStatus objs) {
//                Log.d("*** tileCacheStatus : ", objs.getStatus().toString());
                final String status = objs.getStatus().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String desc;

//                        if (status.equals("SUCCEEDED")) {
//                            desc = "Успешно";
//                        } else if (status.equals("EXECUTING")) {
//                            desc = "В процессе";
//                        } else {
//                            desc = "Неизвестно";
//                        }
                        showToast("Статус загрузки кэша - " + status);//desc);
//                        Toast.makeText(getApplicationContext(), status,
//                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };

        // Submit tile cache job and download
        exportTileCacheTask.generateTileCache(params, statusListener,
                new CallbackListener<String>() {
                    private boolean errored = false;

                    @Override
                    public void onError(Throwable e) {
                        errored = true;
                        // print out the error message and disable the progress
                        // bar
                        Log.d("gen. TileCache error: ", e.getMessage());
                        final String error = e.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                setProgressBarIndeterminateVisibility(false);
                                showToast("generateTileCache error: " + error);
                            }
                        });
                    }

                    @Override
                    public void onCallback(final String path) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("path from callback " + path);
                                Log.e("Path", path);
                            }
                        });
                        if (!errored) {
                            Log.d("the Download Path = ", "" + path);


                            // switch to the successfully downloaded local layer
                            localTiledLayer = new ArcGISLocalTiledLayer(path);
                            map.addLayer(localTiledLayer);
                            // initially setting the visibility to false,
                            // turning it back on in the switchToLocalLayer()
                            // method
                            map.getLayers()[1].setVisible(false);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Hide the progress bar
//                                    setProgressBarIndeterminateVisibility(false);
                                    showToast("TileCache successfully downloaded, Switching to Local Tiled Layer");
                                    switchMapToLayer();
                                }
                            });
                        }
                    }
                }, tileCachePath);
    }

    private void switchMapToLayer() {
        map.setResolution(detailLevelValues[(int) levels[0]]);
        if (isLocalLayerVisible) {
            isLocalLayerVisible = false;
            map.getLayer(0).setVisible(false);
            map.getLayer(1).setVisible(true);
        } else {
            isLocalLayerVisible = true;
            map.getLayer(0).setVisible(true);
            map.getLayer(1).setVisible(false);
        }
    }

    private boolean checkDirForAppFilesExists(File base, String dirName) {
        File dir = new File(base, dirName);
        return dir.exists() && dir.isDirectory();
    }

    private boolean checkGdbFileExists(File base, String relPath) {
        File file = new File(base, relPath);
        return file.exists() && file.isFile();
    }

    private boolean createDirForAppFiles(File base, String dirName) {
        File dir = new File(base, dirName);
        return dir.mkdir();
    }

    private void showToast(String msgText) {
        Toast.makeText(
                getApplicationContext(), msgText, Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.unpause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
