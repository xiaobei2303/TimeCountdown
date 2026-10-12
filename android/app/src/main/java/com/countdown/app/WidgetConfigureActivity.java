package com.countdown.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class WidgetConfigureActivity extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText editTitle;
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_widget_configure);

        editTitle = findViewById(R.id.edit_title);
        datePicker = findViewById(R.id.date_picker);
        Button btnSave = findViewById(R.id.btn_save);

        // 获取widget ID
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // 加载已保存的设置（如果是修改模式）
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String savedTitle = prefs.getString("widget_" + mAppWidgetId + "_title", "");
        long savedDate = prefs.getLong("widget_" + mAppWidgetId + "_date", -1);

        if (savedDate > 0) {
            editTitle.setText(savedTitle);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(savedDate);
            datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
        } else {
            // 默认值
            editTitle.setText("生日");
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });
    }

    private void saveConfig() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) {
            title = "生日";
        }

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long birthDate = cal.getTimeInMillis();

        // 按widget ID独立保存
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("widget_" + mAppWidgetId + "_title", title);
        editor.putLong("widget_" + mAppWidgetId + "_date", birthDate);
        editor.apply();

        // 立即更新该widget
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        AnniversaryWidget widget = new AnniversaryWidget();
        widget.onUpdate(this, mgr, new int[]{mAppWidgetId});

        // 返回结果
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
