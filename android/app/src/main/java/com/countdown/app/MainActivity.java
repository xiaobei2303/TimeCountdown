package com.countdown.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends Activity {

    private EditText editTitle;
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);

        editTitle = findViewById(R.id.edit_title);
        datePicker = findViewById(R.id.date_picker);
        Button btnSave = findViewById(R.id.btn_save);

        // 加载已保存的设置
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String savedTitle = prefs.getString(SettingsActivity.KEY_TITLE, "生日倒计时");
        long savedDate = prefs.getLong(SettingsActivity.KEY_DATE, System.currentTimeMillis());
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
            title = "生日倒计时";
        }

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long targetDate = cal.getTimeInMillis();

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsActivity.KEY_TITLE, title);
        editor.putLong(SettingsActivity.KEY_DATE, targetDate);
        editor.apply();

        // 立即更新所有纪念日widget
        try {
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            ComponentName cn = new ComponentName(this, AnniversaryWidget.class);
            int[] ids = mgr.getAppWidgetIds(cn);
            if (ids != null && ids.length > 0) {
                AnniversaryWidget widget = new AnniversaryWidget();
                widget.onUpdate(this, mgr, ids);
            }
        } catch (Exception e) {
            // 忽略
        }

        // 计算显示结果（生日逻辑）
        long nowMs = System.currentTimeMillis();
        Calendar nowCal = Calendar.getInstance();
        Calendar birthCal = Calendar.getInstance();
        birthCal.setTimeInMillis(targetDate);
        int bMonth = birthCal.get(Calendar.MONTH);
        int bDay = birthCal.get(Calendar.DAY_OF_MONTH);
        int bYear = birthCal.get(Calendar.YEAR);

        Calendar nextBd = Calendar.getInstance();
        nextBd.set(nowCal.get(Calendar.YEAR), bMonth, bDay, 0, 0, 0);
        if (nextBd.getTimeInMillis() <= nowMs) {
            nextBd.set(nowCal.get(Calendar.YEAR) + 1, bMonth, bDay, 0, 0, 0);
        }
        long daysToNext = (nextBd.getTimeInMillis() - nowMs) / 86400000L;
        long daysSinceBirth = (nowMs - targetDate) / 86400000L;
        String msg = "已保存！距离下一个生日还有 " + daysToNext + " 天，已出生 " + daysSinceBirth + " 天";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
