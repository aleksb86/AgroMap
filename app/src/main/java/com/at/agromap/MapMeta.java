package com.at.agromap;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geodatabase.Geodatabase;

import java.io.File;
import java.util.List;

/**
 * Класс-контейнер для хранения метаданных карты.
 */
class MapMeta {

    MapMeta(File pathBasemap, File pathGeodatabase) throws Exception {
        if (pathBasemap == null && pathGeodatabase == null) {
            throw new Exception("MapMeta object creation error: " +
                    "path of basemap or path of geodatabase must be provided!");
        }
        this.pathBasemap = pathBasemap;
        this.pathGeodatabase = pathGeodatabase;

        this.tiledBasemap = new ArcGISLocalTiledLayer(pathBasemap.toString());
        this.localGeodatabase = new Geodatabase(pathGeodatabase.toString());
    }

    MapMeta(String urlBasemap, String urlFeatureService) throws Exception {
        if (urlBasemap == null && urlFeatureService == null) {
            throw new Exception("MapMeta object creation error: " +
                    "URL of basemap service or URL of feature service must be provided!");
        }
        this.urlBasemap = urlBasemap;
        this.urlFeatureService = urlFeatureService;

        this.onlineBasemap = new ArcGISTiledMapServiceLayer(urlBasemap);
        this.onlineFeatureLayer = new ArcGISFeatureLayer(urlFeatureService,
                ArcGISFeatureLayer.MODE.ONDEMAND);
    }

    public Boolean isLocal() {
        return  (pathBasemap != null && pathGeodatabase != null);
    }

    public File getPathBasemap() {
        return pathBasemap;
    }

    public File getPathGeodatabase() {
        return pathGeodatabase;
    }

    public String getUrlBasemap() {
        return urlBasemap;
    }

    public String getUrlFeatureService() {
        return urlFeatureService;
    }

    /**
     *
     * @param basemap объект тайлового слоя с базовыми картами, полученный
     *                из сохраненного файла Tiled Package (tpk)
     * @return массив наименований слоев, используемых как базовые карты
     */
//    public String[] getBasemapLayerNames(ArcGISLocalTiledLayer basemap) {
//        List<ArcGISLayerInfo> layerInfos = basemap.getLayers();
//        String[] basemapLayerNames = new String[layerInfos.size()];
//
//        for (ArcGISLayerInfo info : layerInfos) {
//            basemapLayerNames[info.getId()] = info.getName();
//        }
//        return basemapLayerNames;
//    }


    private File pathBasemap;
    private File pathGeodatabase;
    private String urlBasemap;
    private String urlFeatureService;
    private ArcGISLocalTiledLayer tiledBasemap;
    private Geodatabase localGeodatabase;
    private ArcGISTiledMapServiceLayer onlineBasemap;
    private ArcGISFeatureLayer onlineFeatureLayer;
    private String mapName; // Общее внешнее имя карты
}
