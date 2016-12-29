package com.at.agromap;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.media.MediaScannerConnection;
import android.net.Credentials;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters.ExportBy;
import com.esri.core.tasks.tilecache.ExportTileCacheStatus;
import com.esri.core.tasks.tilecache.ExportTileCacheTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

public class MapDemo extends AppCompatActivity {

    private MapView map;
    private File basePath;
    private String geodbFilesDir;
    private String geodbFileName;
    private String basemapFileName;
    private String appDataStorageDir;
    private String tileUrl;
    private MenuItem selectLevels;
    private MenuItem download;
    private MenuItem switchMaps;
    private ProgressDialog mapLoadingProgress;
    private CharSequence[] detailsLevelNames;
    private double[] detailLevelValues;
    private ArrayList<Double> levelsArraylist = new ArrayList<>();
    // Вариант с компактным кешем оставим до лучших времен - будем использовать
    // единый файл *.tpk
    private boolean createAsTilePackage = true;
    private double[] levels;
    private ArcGISLocalTiledLayer localTiledLayer;
    private boolean[] itemsChecked;
    private Resources resources;
    private boolean isLocalLayerVisible;
    private ArcGISTiledMapServiceLayer onlineBasemapLayer;
    private String username; // Учетная запись для доступа к feature сервису
    private String password; // это пароль
    private String featureServiceUrl;
    private ProgressDialog featureloadingDialog;
    private GeodatabaseSyncTask geodatabaseSyncTask;
    // Объекты для запросов к локальной базе геоданных
    private Geodatabase localGeodatabase;
    private GeodatabaseFeatureTable currentFeatureTable;
//    private FeatureResult featureResult;
    private Map<String, Object> featuresMapResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        basePath = Environment.getExternalStorageDirectory();
        geodbFilesDir = getString(R.string.geodb_files_dir);
        geodbFileName = getString(R.string.geodb_file_name);
        basemapFileName = getString(R.string.tpk_basemap_file_name);
        appDataStorageDir = getString(R.string.offlinemap_files_dir);
        tileUrl = getString(R.string.basemap_service_url);
        username = getString(R.string.username);
        password = getString(R.string.password);
        featureServiceUrl = getString(R.string.feature_service_url);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_demo);

        map = (MapView) findViewById(R.id.map);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.map_demo_toolbar);
        setSupportActionBar(toolbar);
        onlineBasemapLayer = new ArcGISTiledMapServiceLayer(tileUrl);
        final TextView testPath = (TextView) findViewById(R.id.geodb_file_path);
        resources = getResources();

        // ПРОВЕРКА ДОСТУПНОСТИ ВНЕШНЕГО НОСИТЕЛЯ
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // КАРТА SD НЕДОСТУПНА, ВСЕ ОСТАНАВЛИВАЕТСЯ
            new AlertDialog.Builder(map.getContext())
                    .setTitle(R.string.unable_to_write_sd_card_title)
                    .setMessage(R.string.unable_to_write_sd_card)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();
        } else {
            // КАРТА SD ДОСТУПНА - ПРОДОЛЖАЕМ
            // ПРОВЕРКА ВНЕШНЕГО КАТАЛОГА ПРИЛОЖЕНИЯ
            if (!checkDirForAppFilesExists(basePath, File.separator + appDataStorageDir)) {
                // ВНЕШНИЙ КАТАЛОГ НЕ СУЩЕСТВУЕТ - НАДО СОЗДАТЬ
                if (createDirForAppFiles(basePath, File.separator + appDataStorageDir)) {
                    // TODO: убрать при подготовке релиза!
                    showToast("Каталог " + appDataStorageDir + " создан.");

                } else {
                    // НЕВОЗМОЖНО СОЗДАТЬ ВНЕШНИЙ КАТАЛОГ, ВСЕ ОСТАНАВЛИВАЕТСЯ
                    new AlertDialog.Builder(map.getContext())
                            .setTitle(R.string.error_creating_dir_dialog_title)
                            .setMessage(R.string.unable_to_create_main_dir + appDataStorageDir)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
                }
            }
            // ПРОВЕРКА КАТАЛОГА ДЛЯ БАЗЫ ГЕОДАННЫХ
            if (!checkDirForAppFilesExists(basePath,
                    File.separator + appDataStorageDir + File.separator + geodbFilesDir)) {
                // КАТАЛОГ ДЛЯ БАЗЫ ГЕОДАННЫХ НЕ СУЩЕСТВУЕТ - НАДО СОЗДАТЬ
                if (createDirForAppFiles(basePath,
                        File.separator + appDataStorageDir +
                                File.separator + geodbFilesDir)) {
                    // TODO: убрать при подготовке релиза!
                    showToast("Каталог " + geodbFilesDir + " создан.");
                } else {
                    // НЕВОЗМОЖНО СОЗДАТЬ КАТАЛОГ ДЛЯ БАЗЫ ГЕОДАННЫХ, ВСЕ ОСТАНАВЛИВАЕТСЯ
                    new AlertDialog.Builder(map.getContext())
                            .setTitle(R.string.error_creating_dir_dialog_title)
                            .setMessage(R.string.unable_to_create_main_dir + geodbFilesDir)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
                }
            }
            // ПОИСК ФАЙЛА БАЗОВОЙ КАРТЫ
            File basemap = new File(basePath
                    + File.separator + appDataStorageDir + File.separator + basemapFileName);
            if (basemap.exists() && basemap.isFile()) {
                // Это он, создаем из него слой
                ArcGISLocalTiledLayer localBaseMap = new ArcGISLocalTiledLayer(basemap.toString());
                map.addLayer(localBaseMap);
                map.setScale(localBaseMap.getMinScale());
                showToast(String.valueOf(localBaseMap.getMaxScale()));
            } else {
                // Файла нет - используется базовая карта онлайн
                map.addLayer(onlineBasemapLayer);
            }

            // ОТОБРАЖЕНИЕ ЗАГРУЖЕННЫХ АТРИБУТИВНЫХ СЛОЕВ
            updateFeatureLayer(basePath
                    + File.separator + appDataStorageDir
                    + File.separator + geodbFilesDir
                    + File.separator + geodbFileName);
            currentFeatureTable = localGeodatabase.getGeodatabaseFeatureTableByLayerId(0);
        }






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
        ОБРАБОТЧИК ОДНОКРАТНОГО НАЖАТИЯ ДЛЯ КАРТЫ
         */
        map.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(final float x, final float y) {
                if (!map.isLoaded()) {
                    return;
                }
                showToast("X: " + x + " Y: " + y);
                Point point = map.toMapPoint(x, y);
                queryFeature(setQueryParams(point));
//                showToast("Features: " + featuresMapResult);
            }
        });
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

    /*
    ОСНОВНОЕ МЕНЮ. ДЕЙСТВИЯ С КАРТОЙ И СЛОЯМИ
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

    /*
    НАСТРОЙКА ДЕЙСТВИЙ ДЛЯ ПУНКТОВ МЕНЮ
     */
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
            case R.id.download_layers:
                downloadFeatureLayers();
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
        builder.setTitle("Выберите уровни детализации карты:");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // When ok button is pressed, we go through the array of
                // itemsChecked and add the selected
                // items to levelsArraylist
                for (int i = 0; i < detailsLevelNames.length; i++) {
                    if (itemsChecked[i]) {
                        levelsArraylist.add((double) i);
                        itemsChecked[i] = false;
                    }
                }

                showToast("selected " + levelsArraylist.size());
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

    /***********************************************************************************************
     * ЗАГРУЗКА КЭША БАЗОВОЙ КАРТЫ
     * *********************************************************************************************
     */

    // создать локальный кэш из данных Map сервиса
    private void downloadBasemap() {
        Envelope extentForTPK = new Envelope();
        final String tileCachePath = Environment.getExternalStorageDirectory().toString()
                + File.separator + appDataStorageDir + File.separator + basemapFileName;
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

    /***********************************************************************************************
     * ЗАГРУЗКА СЛОЕВ
     * *********************************************************************************************
     */

    // ПОДГОТОВКА К ЗАГРУЗКЕ СЛОЕВ (FEATURE SERVICE)
    void downloadFeatureLayers() {
        featureloadingDialog = ProgressDialog.show(map.getContext(),
                "Загрузка слоев", "Получение данных..");
        featureloadingDialog.show();
        UserCredentials credentials = new UserCredentials();
        credentials.setUserAccount(username, password);
        geodatabaseSyncTask = new GeodatabaseSyncTask(featureServiceUrl, credentials);
        geodatabaseSyncTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

            @Override
            public void onError(Throwable e) {
                Log.e("GDB", "Error fetching FeatureServiceInfo " + e.getMessage());
            }

            @Override
            public void onCallback(FeatureServiceInfo fsInfo) {
                if (fsInfo.isSyncEnabled()) {
                    createGeodatabase(fsInfo);
                }
            }
        });
    }

    // СОЗДАНИЕ БАЗЫ ГЕОДАННЫХ
    private void createGeodatabase(FeatureServiceInfo featureServerInfo) {
        // НАСТРОЙКА ПАРАМЕТРОВ ДЛЯ ГЕНЕРАЦИИ БАЗЫ
        GenerateGeodatabaseParameters params =
                new GenerateGeodatabaseParameters(featureServerInfo, map.getExtent(),
                map.getSpatialReference());

        // КОЛЛБЕК СРАБАТЫВАЕТ, КОГДА ЗАДАНИЕ ВЫПОЛНЯЕТСЯ ИЛИ ПАДАЕТ С ОШИБКОЙ
        CallbackListener<String> gdbResponseCallback = new CallbackListener<String>() {
            @Override
            public void onError(final Throwable e) {
                Log.e("GDB", "Error creating geodatabase" + e.getMessage());
                featureloadingDialog.dismiss();
            }

            @Override
            public void onCallback(String path) {
                Log.i("GDB", "Geodatabase is: " + path);
                featureloadingDialog.dismiss();
                // update map with local feature layer from geodatabase
                updateFeatureLayer(path);
            }
        };

        // КОЛЛБЕК СРАБАТЫВАЕТ ПРИ ИЗМЕНЕНИИ СТАТУСА ЗАДАНИЯ
        GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {
            @Override
            public void statusUpdated(GeodatabaseStatusInfo status) {
                Log.i("GDB", status.getStatus().toString());
            }
        };

        File oldGeodatabase = new File(basePath,
                        File.separator + appDataStorageDir +
                        File.separator + geodbFilesDir +
                        File.separator + geodbFileName);
        // *-shm и *-val - это доп. файлы, создаваемые движком SQLite
        File oldGeodatabaseShm = new File(basePath,
                        File.separator + appDataStorageDir +
                        File.separator + geodbFilesDir +
                        File.separator + geodbFileName + "-shm");
        File oldGeodatabaseVal = new File(basePath,
                        File.separator + appDataStorageDir +
                        File.separator + geodbFilesDir +
                        File.separator + geodbFileName + "-val");
        try {
            oldGeodatabase.delete();
            oldGeodatabaseShm.delete();
            oldGeodatabaseVal.delete();
        } catch (Exception e) {
            Log.e("GDB", "Delete old files error: " + e.getMessage());
        }


        // ЗАПУСК СОЗДАНИЯ БАЗЫ С НАСТРОЕННЫМИ ПАРАМЕТРАМИ
        submitTask(params,
                basePath.toString() +
                        File.separator + appDataStorageDir +
                        File.separator + geodbFilesDir +
                        File.separator + geodbFileName,
                statusCallback, gdbResponseCallback);
    }

    // МЕТОД ДЛЯ ЗАПУСКА ЗАДАЧИ СОЗДАНИЯ БАЗЫ ГЕОДАННЫХ
    private void submitTask(GenerateGeodatabaseParameters params, String file,
                                   GeodatabaseStatusCallback statusCallback,
                                   CallbackListener<String> gdbResponseCallback) {

        geodatabaseSyncTask.generateGeodatabase(params, file, false, statusCallback,
                gdbResponseCallback);
    }

    // ПРИМЕНЕНИЕ ЛОКАЛЬНЫХ АТРИБУТИВНЫХ СЛОЕВ
    // (ЕСЛИ ОНИ УЖЕ ЗАГРУЖЕНЫ)
    private void updateFeatureLayer(String featureLayerPath) {
// create a new geodatabase
//        Geodatabase localGdb = null;
        try {
            localGeodatabase = new Geodatabase(featureLayerPath);
        } catch (FileNotFoundException e) {
            Log.e("GDB", "Local geodatabase file not found!");
        }

// Geodatabase contains GdbFeatureTables representing attribute data
// and/or spatial data. If GdbFeatureTable has geometry add it to
// the MapView as a Feature Layer
        if (localGeodatabase != null) {
            for (GeodatabaseFeatureTable gdbFeatureTable : localGeodatabase.getGeodatabaseTables()) {
                if (gdbFeatureTable.hasGeometry())
                    map.addLayer(new FeatureLayer(gdbFeatureTable));
            }
        }
    }

    /***********************************************************************************************
     * ИДЕНТИФИКАЦИЯ ОБЪЕКТОВ
     * *********************************************************************************************
     */
    // ЗАПРОС АТРИБУТОВ И ВЫВОД ВСПЛЫВАЮЩИХ СООБЩЕНИЙ НА КАРТЕ
//    private class AttributeAsyncTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//        @Override
//        protected Void doInBackground(Void... args) {
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void arg) {
//
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... arg) {
//
//        }
//    }

    private ViewGroup createFeatureLayout(final Map<String, Object> results) {

        // create a new LinearLayout in application context
        LinearLayout layout = new LinearLayout(this);

        // view height and widthwrap content
        layout.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));

        // default orientation
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText("Наименование: " + results.get("name"));

        TextView culture = new TextView(this);
        culture.setText("Культура: " + results.get("usage_type"));

        TextView created = new TextView(this);
        created.setText("Дата создания" + results.get("created_date"));

        FloatingActionButton closeBtn = new FloatingActionButton(this);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                view.
            }
        });
//        closeBtn.setBackground(R.drawable.ic_menu_close_clear_cancel);

        layout.addView(name);
        layout.addView(culture);
        layout.addView(created);

        return layout;
    }

    // ПОДГОТОВКА ПАРАМЕТРОВ ДЛЯ ЗАПРОСА К АТРИБУТИВНОЙ ТАБЛИЦЕ СЛОЯ
    private QueryParameters setQueryParams(Point tappedPoint) {
        QueryParameters parameters = new QueryParameters();
        parameters.setGeometry(tappedPoint);
        parameters.setInSpatialReference(map.getSpatialReference());
        parameters.setReturnGeometry(false);
        return parameters;
    }

    // ЗАПРОС К АТРИБУТИВНОЙ ТАБЛИЦЕ СЛОЯ
    private void queryFeature(final QueryParameters params) {

        CallbackListener<FeatureResult> featureResultCallbackListener =
                new CallbackListener<FeatureResult>() {
            @Override
            public void onCallback(FeatureResult objects) {
                Log.d("IDN", "Success identify. Number of features: " + objects.featureCount());
                if (objects.featureCount() == 1) {
                    for (Object obj : objects) {
                        if (obj instanceof Feature) {
                            featuresMapResult = ((Feature) obj).getAttributes();

                            Callout callout = map.getCallout();
                            callout.setContent(createFeatureLayout(featuresMapResult));
                            callout.show((Point) params.getGeometry());

//                        Log.d("IDN", "Success identify: " + ((Feature) obj).getAttributes());
                            Log.d("IDN", "Created : "
                                    + getDate( (long) ((Feature) obj).getAttributes().get("created_date") ));
                            Log.d("IDN", "Culture : "
                                    + ((Feature) obj).getAttributes().get("usage_type"));
                            Log.d("IDN", "Name : "
                                    + ((Feature) obj).getAttributes().get("name"));
                        } else {
                            Log.e("IDN", "Объект в FeatureResult не относится к типу Feature!");
                        }
                    }
                } else if (objects.featureCount() > 1) {
                    Log.e("IDN", "FeatureResult содержит более одного объекта!");
                    featuresMapResult = null;
                } else {
                    Log.e("IDN", "FeatureResult не содержит ни одного объекта!");
                    featuresMapResult = null;
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("IDN", "Error with identify: " + e.getMessage());
            }
        };

        currentFeatureTable.queryFeatures(params, featureResultCallbackListener);
    }


    /***********************************************************************************************
     * MISCELLANEOUS
     * *********************************************************************************************
     */
    // МЕТОД ДЛЯ ПЕРЕКЛЮЧЕНИЯ ПОДЛОЖКИ (ОНЛАЙН/ОФЛАЙН)
    private void switchMapToLayer() {
        if (map.getLayers().length <= 1) {
            map.addLayer(onlineBasemapLayer);
        } else {
//            map.setResolution(detailLevelValues[(int) levels[0]]);
//            map.setMinScale(detailLevelValues[(int) levels[0]]);
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
    }

    private boolean checkDirForAppFilesExists(File base, String dirName) {
        File dir = new File(base, dirName);
        return dir.exists() && dir.isDirectory();
    }

    private boolean checkFileExists(File base, String relPath) {
        File file = new File(base, relPath);
        return file.exists() && file.isFile();
    }

    private boolean createDirForAppFiles(File base, String dirName) {
        File dir = new File(base, dirName);
        return dir.mkdir();
    }

    private boolean checkIsEmptyDirForAppFiles(File base, String dirName) {
        File dir = new File(base, dirName);
        File[] contents = dir.listFiles();
        return contents.length == 0;
    }

    private void showToast(String msgText) {
        Toast.makeText(
                getApplicationContext(), msgText, Toast.LENGTH_SHORT
        ).show();
    }

    // Преобразование даты Timestamp -> локализованная дата
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(new Locale("RU"));
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        return date;
    }


}
