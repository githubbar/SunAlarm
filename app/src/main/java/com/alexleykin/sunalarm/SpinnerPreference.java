package com.alexleykin.sunalarm;

/**
 * Created by oleykin on 10/24/13.
 * Repurposed code by by Matt Falkoski <matt dot falkoski at gmail dot com>
 *     https://code.google.com/p/first-live-wallpaper/source/browse/src/com/falko/android/raven/settings/SpinnerPreference.java?r=959dedc7393a3dddceaea298faa03e993f2a8f05
 *     Apache 2.0 license
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class SpinnerPreference extends ListPreference {

    private final String TAG = getClass().getName();

    public SpinnerPreference(final Context context) {
        this(context, null);
    }

    public SpinnerPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        setSummary(getContext().getResources().getString(R.string.alarm_sunrise_summary)+" "+value+" min");
    }
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Toast.makeText(getContext(), "Sunrise will last "+getValue()+" minutes", Toast.LENGTH_SHORT).show();
            setSummary(getContext().getResources().getString(R.string.alarm_sunrise_summary)+" "+getValue()+" min");
        }
    }

/*    @Override
    public CharSequence getSummary() {
        final CharSequence entry = getValue();
        final CharSequence summary = super.getSummary();
        if (summary == null || entry == null) {
            return null;
        } else {
            return String.format(summary.toString(), entry);
        }
    }*/
}