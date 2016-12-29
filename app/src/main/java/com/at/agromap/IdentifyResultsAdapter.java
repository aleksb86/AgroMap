package com.at.agromap;

/**
 * Created by uaboev on 20.12.2016.
 */

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.core.tasks.identify.IdentifyResult;

import java.util.List;

/**
 * This class allows the user to customize the string shown in the callout.
 * By default its the display field name.
 *
 * A spinner adapter defines two different views; one that shows the data in
 * the spinner itself and one that shows the data in the drop down list when
 * spinner is pressed.
 *
 */
public class IdentifyResultsAdapter extends IdentifyResultSpinnerAdapter {
    String m_show = null;
    List<IdentifyResult> resultList;
    int currentDataViewed = -1;
    Context m_context;

    public IdentifyResultsAdapter(Context context, List<IdentifyResult> results) {
        super(context, results);
        this.resultList = results;
        this.m_context = context;
    }

    // Get a TextView that displays identify results in the callout.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String LSP = System.getProperty("line.separator");
        StringBuilder outputVal = new StringBuilder();

        // Resource Object to access the Resource fields
//        Resources res = getResources();
//
//        // Get Name attribute from identify results
//        IdentifyResult curResult = this.resultList.get(position);
//
//        if (curResult.getAttributes().containsKey(
//                res.getString(R.string.NAME))) {
//            outputVal.append("Place: "
//                    + curResult.getAttributes()
//                    .get(res.getString(R.string.NAME)).toString());
//            outputVal.append(LSP);
//        }
//
//        if (curResult.getAttributes().containsKey(
//                res.getString(R.string.ID))) {
//            outputVal.append("State ID: "
//                    + curResult.getAttributes()
//                    .get(res.getString(R.string.ID)).toString());
//            outputVal.append(LSP);
//        }
//
//        if (curResult.getAttributes().containsKey(
//                res.getString(R.string.ST_ABBREV))) {
//            outputVal.append("Abbreviation: "
//                    + curResult.getAttributes()
//                    .get(res.getString(R.string.ST_ABBREV))
//                    .toString());
//            outputVal.append(LSP);
//        }
//
//        if (curResult.getAttributes().containsKey(
//                res.getString(R.string.TOTPOP_CY))) {
//            outputVal.append("Population: "
//                    + curResult.getAttributes()
//                    .get(res.getString(R.string.TOTPOP_CY))
//                    .toString());
//            outputVal.append(LSP);
//
//        }
//
//        if (curResult.getAttributes().containsKey(
//                res.getString(R.string.LANDAREA))) {
//            outputVal.append("Area: "
//                    + curResult.getAttributes()
//                    .get(res.getString(R.string.LANDAREA))
//                    .toString());
//            outputVal.append(LSP);
//
//        }
//
//        // Create a TextView to write identify results
        TextView txtView;
        txtView = new TextView(this.m_context);
//        txtView.setText(outputVal);
//        txtView.setTextColor(Color.BLACK);
//        txtView.setLayoutParams(new ListView.LayoutParams(
//                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//        txtView.setGravity(Gravity.CENTER_VERTICAL);

        return txtView;
    }
}
