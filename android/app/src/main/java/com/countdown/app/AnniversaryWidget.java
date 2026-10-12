package com.countdown.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.util.Calendar;

public class AnniversaryWidget extends AppWidgetProvider {

    private static final String ACTION_UPDATE = "com.countdown.app.UPDATE_ANNIVERSARY";
    private static final int REQUEST_CODE = 2001;
    private static final long INTERVAL = 60 * 1000L;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        ensureAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelAlarm(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ensureAlarm(context);
        for (int appWidgetId : appWidgetIds) {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            } catch (Exception e) {
                // 忽略单个更新失败
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            try {
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName cn = new ComponentName(context, AnniversaryWidget.class);
                int[] ids = mgr.getAppWidgetIds(cn);
                if (ids != null && ids.length > 0) {
                    onUpdate(context, mgr, ids);
                }
            } catch (Exception e) {
                // 忽略
            }
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_anniversary);

        // 按widget ID读取独立设置
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String title = prefs.getString("widget_" + appWidgetId + "_title", "");
        long birthDate = prefs.getLong("widget_" + appWidgetId + "_date", -1);

        // 如果该widget没有独立设置，用全局默认设置
        if (birthDate <= 0) {
            title = prefs.getString(SettingsActivity.KEY_TITLE, "生日倒计时");
            birthDate = prefs.getLong(SettingsActivity.KEY_DATE, System.currentTimeMillis());
        }
        if (title == null || title.isEmpty()) {
            title = "生日倒计时";
        }

        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis();

        // 解析出生日期的月日
        Calendar birth = Calendar.getInstance();
        birth.setTimeInMillis(birthDate);
        int birthMonth = birth.get(Calendar.MONTH);
        int birthDay = birth.get(Calendar.DAY_OF_MONTH);
        int birthYear = birth.get(Calendar.YEAR);

        // 计算下一个生日（今年或明年）
        Calendar nextBirthday = Calendar.getInstance();
        nextBirthday.set(now.get(Calendar.YEAR), birthMonth, birthDay, 0, 0, 0);
        nextBirthday.set(Calendar.MILLISECOND, 0);
        if (nextBirthday.getTimeInMillis() <= nowMs) {
            // 今年生日已过，算明年
            nextBirthday.set(now.get(Calendar.YEAR) + 1, birthMonth, birthDay, 0, 0, 0);
        }
        long daysToNextBirthday = (nextBirthday.getTimeInMillis() - nowMs) / 86400000L;

        // 计算从出生到现在已过去多少天
        long daysSinceBirth = (nowMs - birthDate) / 86400000L;

        // 设置标题
        views.setTextViewText(R.id.anni_title, title);

        // 第三行：距离下一个生日还有多少天
        views.setTextViewText(R.id.anni_prefix, "还有");
        views.setTextViewText(R.id.anni_number, String.valueOf(daysToNextBirthday));
        views.setTextViewText(R.id.anni_unit, "天");

        // 第四行：已过去多少天（精简）
        views.setTextViewText(R.id.anni_desc, "已出生" + daysSinceBirth + "天");

        // 点击widget打开该widget的配置界面
        try {
            Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.anni_title, pendingIntent);
            views.setOnClickPendingIntent(R.id.anni_number, pendingIntent);
            views.setOnClickPendingIntent(R.id.anni_desc, pendingIntent);
            views.setOnClickPendingIntent(R.id.anni_prefix, pendingIntent);
            views.setOnClickPendingIntent(R.id.anni_unit, pendingIntent);
        } catch (Exception e) {
            // 忽略
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void ensureAlarm(Context context) {
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AnniversaryWidget.class);
            intent.setAction(ACTION_UPDATE);
            PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi);
            long triggerAt = SystemClock.elapsedRealtime() + INTERVAL;
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, INTERVAL, pi);
        } catch (Exception e) {
            // 忽略
        }
    }

    private void cancelAlarm(Context context) {
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AnniversaryWidget.class);
            intent.setAction(ACTION_UPDATE);
            PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi);
        } catch (Exception e) {
            // 忽略
        }
    }
}
