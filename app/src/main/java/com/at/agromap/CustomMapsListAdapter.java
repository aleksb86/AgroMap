package com.at.agromap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by uaboev on 09.01.2017.
 * Класс - адаптер для визуализации списка доступных карт
 * в контейнере ListView.
 */

public class CustomMapsListAdapter extends ArrayAdapter<String> {

    private final String[] mapTitles;
    private final String[] mapItemNotes;
    private final Activity context;

    public CustomMapsListAdapter(Activity context, String[] mapTitles, String[] mapItemNotes) {
        super(context, R.layout.item_map_layout, mapTitles);
        this.mapTitles = mapTitles;
        this.mapItemNotes = mapItemNotes;
        this.context = context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.item_map_layout, null,true);
        TextView textMapTitles = (TextView) rowView.findViewById(R.id.map_item_title);
        TextView textMapsNotes = (TextView) rowView.findViewById(R.id.map_item_note);

        textMapTitles.setText(mapTitles[position]);
        // TODO put hardcoded string to res. file
        textMapsNotes.setText(String.format("Описание: %s", mapItemNotes[position]));

        return rowView;
    }
}