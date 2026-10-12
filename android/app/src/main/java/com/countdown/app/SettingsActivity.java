package com.countdown.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class SettingsActivity extends Activity {

    public static final String PREFS_NAME = "anniversary_prefs";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DATE = "target_date";

    private EditText editTitle;
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editTitle = findViewById(R.id.edit_title);
        datePicker = findViewById(R.id.date_picker);
        Button btnSave = findViewById(R.id.btn_save);

        // 加载已保存的设置
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedTitle = prefs.getString(KEY_TITLE, "茉莉生日");
        long savedDate = prefs.getLong(KEY_DATE, System.currentTimeMillis());
        editTitle.setText(savedTitle);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(savedDate);
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) {
            title = "纪念日";
        }

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long targetDate = cal.getTimeInMillis();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TITLE, title);
        editor.putLong(KEY_DATE, targetDate);
        editor.apply();

        // 立即更新所有widget
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName cn = new ComponentName(this, AnniversaryWidget.class);
        int[] ids = mgr.getAppWidgetIds(cn);
        if (ids != null && ids.length > 0) {
            AnniversaryWidget widget = new AnniversaryWidget();
            widget.onUpdate(this, mgr, ids);
        }

        Toast.makeText(this, "已保存，桌面组件已更新", Toast.LENGTH_SHORT).show();
        finish();
    }
}
