package com.at.agromap;

import android.content.Intent;
import android.os.Build;
import java.io.File;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ArrayList<MapMeta> availableMaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        Button openOfflineMap = (Button) findViewById(R.id.offline_map_btn);

        final File basePath = Environment.getExternalStorageDirectory();
        final String appDataStorageDir = getString(R.string.offlinemap_files_dir);
        final String basemapFileName = getString(R.string.tpk_basemap_file_name);
        final ArrayList<String> geodatabaseDirs = getGeodatabaseDirsList(
                basePath.toString() + File.separator + appDataStorageDir);
        final File basemapFile = new File(basePath.toString() +
                File.separator + appDataStorageDir + File.separator + basemapFileName);
        final String basemapServiceUrl = getString(R.string.basemap_service_url);
        final String featureServiceUrl = getString(R.string.feature_service_url);
//        final ArrayList<File> geodatabaseFiles;

        for (String geodbDir : geodatabaseDirs) {
            String[] geodbFilesPath = getFilesByExtension(new File(geodbDir), ".geodatabase");
//            ArrayList<File> geodatabaseFiles = new ArrayList<>();
//
//            for (String geodbFile : geodbFilesPath) {
//                geodatabaseFiles.add(new File(geodbFile));
//            }
            File geodatabaseFile = new File(geodbFilesPath[0]);
            try{
                availableMaps.add(new MapMeta(basemapFile, geodatabaseFile));
            } catch (Exception e) {
                Log.e("MAP_META", String.format("Error - %s", e.getMessage()));
            }
        }

        // ФИЛЬТР СПИСКА КАРТ (ОНЛАЙН/ОФЛАЙН/ВСЕ)
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> mapsLocationList = new ArrayList<>();
        mapsLocationList.add(getString(R.string.spinner_maps_on_device));
        mapsLocationList.add(getString(R.string.spinner_maps_online));
        mapsLocationList.add(getString(R.string.spinner_maps_all));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mapsLocationList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // ОТОБРАЖЕНИЕ СПИСОК КАРТ (ПЕРЕЧИСЛЯЮТСЯ ВСЕ ДОСТУПНЫЕ КАРТЫ)
        final ListView mapsList = (ListView) findViewById(R.id.maps_list_view);
        String[] mapTitles = {"World", "USA", "Russa"};
        String[] mapNotes = {"All the world.", "The best country", ""};
        CustomMapsListAdapter customMapsListAdapter = new CustomMapsListAdapter(this, mapTitles, mapNotes);
        mapsList.setAdapter(customMapsListAdapter);

        TextView logo = (TextView) findViewById(R.id.agro_logo);
        // Проверка версии сборки для использования fromHtml()
        // в версии N и выше.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            logo.setText(Html.fromHtml(getString(R.string.logo_text), Html.FROM_HTML_MODE_LEGACY));
        } else {
            logo.setText(Html.fromHtml(getString(R.string.logo_text)));
        }

        final Intent intentOfflineMap = new Intent(MainActivity.this, MapDemo.class);

        openOfflineMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.startActivity(intentOfflineMap);
            }
        });
    }

    // МЕТОД ВОЗВРАЩАЕТ СПИСОК ПОДКАТАЛОГОВ В УКАЗАННОМ КАТАЛОГЕ
    private ArrayList<String> getGeodatabaseDirsList(String baseDir) {
        File[] allFiles = (new File(baseDir)).listFiles();
        ArrayList<String> dirs = new ArrayList<>();
        for (int i = 0; i < allFiles.length; i++) {
            if (allFiles[i].isDirectory() || allFiles[i].exists()) {
                dirs.add(allFiles[i].toString());
            }
        }
        return dirs;
    }

    // МЕТОД ВОЗВРАЩАЕТ СПИСОК СТРОК - АБСОЛЮТНЫХ ПУТЕЙ К ФАЙЛАМ ОПРЕДЕЛЕННОГО
    // ТИПА (РАСШИРЕНИЕ) В ОПРЕДЕЛЕННОМ КАТАЛОГЕ
    private String[] getFilesByExtension(File dirPath, final String ext) {
        String[] files = dirPath.list(
            new FilenameFilter() {
                File f;
                public boolean accept(File dir, String name) {
                    if (name.endsWith(ext.toLowerCase()) || name.endsWith(ext.toUpperCase())) {
                        return true;
                    }
                    f = new File(dir.getAbsolutePath() + File.separator + name);
                    return f.isDirectory();
                }
            });
        return files;
    }
}
