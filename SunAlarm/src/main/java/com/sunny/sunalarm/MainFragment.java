package com.sunny.sunalarm;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;

/**
 Copyright 2013 Alex Leykin

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.

 You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
public class MainFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private Toast mToast;
    private int INTENT_CODE = 357465;
    public int DAYS_OF_WEEK = 7;
    public static final int COLOR_STAGES = 5;
    private String TAG = "MainFragment";
    public static final String ARG_SECTION_NUMBER = "section_number";
    private CheckBox[] daysOfWeek = new CheckBox[DAYS_OF_WEEK];
    private Button[] colors = new Button[COLOR_STAGES];
    private String[] strDaysOfWeek = {"Sun","Mon","Tue","Wed","Th","Fri","Sat"};
    public MainFragment() {
    }
    public static int[] colorChoice(Context context){

        int[] mColorChoices=null;
        String[] color_array = context.getResources().
                getStringArray(R.array.default_color_choice_values);

        if (color_array!=null && color_array.length>0) {
            mColorChoices = new int[color_array.length];
            for (int i = 0; i < color_array.length; i++) {
                mColorChoices[i] = Color.parseColor(color_array[i]);
            }
        }
        return mColorChoices;
    }

    public ImageButton.OnClickListener pickColorClicked = new ImageButton.OnClickListener() {

        public void onClick(View v) {
            Button btn = (Button)v;
            ColorPickerDialog colorcalendar = ColorPickerDialog.newInstance(
                    R.string.color_picker_default_title,
                    colorChoice(v.getContext()), 0, 5, ColorPickerDialog.SIZE_SMALL);

            colorcalendar.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener(){
                private View v;

                @Override
                public void onColorSelected(int color) {
                    v.setBackgroundColor(color);
                }

                public ColorPickerSwatch.OnColorSelectedListener init(View v) {
                    this.v = v;
                    return this;
                }
            }.init(btn));
            colorcalendar.show(getActivity().getFragmentManager(),"cal");
        }
    };

    public CompoundButton.OnCheckedChangeListener updateAlarms = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            // TODO: add color pickers
            // TODO: save state
            View rootView = v.getRootView();
            final Button button = (Button) rootView.findViewById(R.id.buttonTestAlarm);
            final Switch switchAlarm = (Switch) rootView.findViewById(R.id.switchAlarm);
            final TimePicker timePicker = (TimePicker) rootView.findViewById(R.id.timePicker);
            final LinearLayout weekLayout = (LinearLayout) rootView.findViewById(R.id.weekLayout);
            final EditText delayText = (EditText) rootView.findViewById(R.id.delayText);
            AlarmManager alarmManager = (AlarmManager) v.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(v.getContext(), OnAlarmReceive.class);
            int delay = Integer.parseInt(delayText.getText().toString());
            intent.putExtra("DELAY_SEC", delay);
            for (int i = 0; i < daysOfWeek.length; i++) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(v.getContext(), INTENT_CODE+i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (isChecked && daysOfWeek[i].isChecked()) {
                    Calendar cal = Calendar.getInstance();
                    Log.d(TAG, String.valueOf(cal.getTimeInMillis()));
                    cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.DAY_OF_WEEK, i+1);
                    cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    // If in the past or within one minute, schedule starting from the next week
                    if(cal.getTimeInMillis() < (System.currentTimeMillis()+1000*60)) {
                        cal.add(Calendar.DATE, 7);
                    }
                    Log.d(TAG, String.valueOf(cal.getTimeInMillis()));
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000*60*60*24*7, pendingIntent); // repeat every week
                }
                else
                    alarmManager.cancel(pendingIntent);
            }
            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(rootView.getContext(), R.string.one_shot_scheduled, Toast.LENGTH_LONG);
            mToast.show();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
//        dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

        final Button button = (Button) rootView.findViewById(R.id.buttonTestAlarm);
        final Switch switchAlarm = (Switch) rootView.findViewById(R.id.switchAlarm);
        final TimePicker timePicker = (TimePicker) rootView.findViewById(R.id.timePicker);
        final LinearLayout weekLayout = (LinearLayout) rootView.findViewById(R.id.weekLayout);
        final EditText delayText = (EditText) rootView.findViewById(R.id.delayText);
        for (int i=0; i < daysOfWeek.length; i++){
            daysOfWeek[i] = new CheckBox(rootView.getContext());
            daysOfWeek[i].setOnCheckedChangeListener(updateAlarms);
            LinearLayout l = new LinearLayout(rootView.getContext());
            l.setOrientation(LinearLayout.VERTICAL);
            l.setGravity(Gravity.CENTER_HORIZONTAL);
//            l.setPadding(9,0,9,0);
            TextView text = new TextView(rootView.getContext());
            text.setText(strDaysOfWeek[i]);
            text.setTextSize(15);
            l.addView(daysOfWeek[i]);
            l.addView(text);
            weekLayout.addView(l);
        }

        switchAlarm.setOnCheckedChangeListener(updateAlarms);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlarmManager alarmManager = (AlarmManager) v.getContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(v.getContext(), OnAlarmReceive.class);
                intent.setAction(Intent.ACTION_MAIN);
                int delay = Integer.parseInt(delayText.getText().toString());
                intent.putExtra("DELAY_SEC", delay);
                int btnColors[] = colorChoice(v.getContext());
                for (int i=0; i < COLOR_STAGES; i++){
                    int c = ((ColorDrawable)colors[i].getBackground()).getColor();
                    intent.putExtra("Color"+String.valueOf(i), c);
                }
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        v.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Log.d(TAG, "Setup the alarm");

                // Getting current time and add the seconds in it
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 1);
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }
        });

        // Add color buttons
        final LinearLayout colorLayout = (LinearLayout) rootView.findViewById(R.id.colorLayout);
        int btnColors[] = colorChoice(rootView.getContext());
        for (int i=0; i < COLOR_STAGES; i++){
            colors[i] = new Button(rootView.getContext());
            colors[i].setBackgroundColor(btnColors[i]);
            colors[i].setOnClickListener(pickColorClicked);
            colorLayout.addView(colors[i]);
        }

        return rootView;
    }
}
