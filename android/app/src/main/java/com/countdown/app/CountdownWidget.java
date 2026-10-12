package com.countdown.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CountdownWidget extends AppWidgetProvider {

    private static final String ACTION_UPDATE = "com.countdown.app.UPDATE_WIDGET";
    private static final int REQUEST_CODE = 1001;
    private static final long INTERVAL = 60 * 1000L; // 每分钟

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
        // 每次更新时确保闹钟在运行
        ensureAlarm(context);

        for (int appWidgetId : appWidgetIds) {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            } catch (Exception e) {
                // 单个 widget 更新失败不影响其他
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            try {
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName cn = new ComponentName(context, CountdownWidget.class);
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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_countdown);

        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);
        int h = now.get(Calendar.HOUR_OF_DAY);

        long nowMs = now.getTimeInMillis();

        // 本年剩余天数（向下取整：如80天13小时记为80天）
        Calendar yearEnd = Calendar.getInstance();
        yearEnd.set(y + 1, 0, 1, 0, 0, 0);
        long yearDiff = yearEnd.getTimeInMillis() - nowMs;
        long yearLeft = yearDiff / 86400000;
        views.setTextViewText(R.id.year_number, String.valueOf(yearLeft));

        // 本月剩余天数（向下取整）
        Calendar monthEnd = Calendar.getInstance();
        monthEnd.set(y, m + 1, 1, 0, 0, 0);
        long monthDiff = monthEnd.getTimeInMillis() - nowMs;
        long monthLeft = monthDiff / 86400000;
        views.setTextViewText(R.id.month_number, String.valueOf(monthLeft));

        // 本周剩余天数（周一为一周开始，向下取整）
        int dow = now.get(Calendar.DAY_OF_WEEK);
        int offset = (dow == Calendar.SUNDAY) ? 6 : (dow - Calendar.MONDAY);
        Calendar weekStart = Calendar.getInstance();
        weekStart.set(y, m, d - offset, 0, 0, 0);
        long weekEndMs = weekStart.getTimeInMillis() + 7L * 86400000;
        long weekDiff = weekEndMs - nowMs;
        long weekLeft = weekDiff / 86400000;
        views.setTextViewText(R.id.week_number, String.valueOf(weekLeft));

        // 今日剩余：不足100分钟显示分钟，否则显示小时
        long todayMsLeft = 24L * 3600000 - (h * 3600000L + now.get(Calendar.MINUTE) * 60000L + now.get(Calendar.SECOND) * 1000L);
        long todayMinLeft = todayMsLeft / 60000L;
        if (todayMinLeft <= 100) {
            views.setTextViewText(R.id.today_number, String.valueOf(todayMinLeft));
            views.setTextViewText(R.id.today_unit, "分钟");
        } else {
            long todayHoursLeft = todayMsLeft / 3600000L;
            views.setTextViewText(R.id.today_number, String.valueOf(todayHoursLeft));
            views.setTextViewText(R.id.today_unit, "小时");
        }

        // 点击widget打开APP
        try {
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.cell_year, pendingIntent);
            views.setOnClickPendingIntent(R.id.cell_month, pendingIntent);
            views.setOnClickPendingIntent(R.id.cell_week, pendingIntent);
            views.setOnClickPendingIntent(R.id.cell_today, pendingIntent);
        } catch (Exception e) {
            // 点击事件设置失败不影响数字显示
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void ensureAlarm(Context context) {
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, CountdownWidget.class);
            intent.setAction(ACTION_UPDATE);
            PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // 先取消已有的，避免重复
            am.cancel(pi);

            // 使用 setRepeating 更稳定，每分钟触发
            long triggerAt = SystemClock.elapsedRealtime() + INTERVAL;
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, INTERVAL, pi);
        } catch (Exception e) {
            // 闹钟设置失败忽略，widget 仍会在系统触发时更新
        }
    }

    private void cancelAlarm(Context context) {
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, CountdownWidget.class);
            intent.setAction(ACTION_UPDATE);
            PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi);
        } catch (Exception e) {
            // 忽略
        }
    }
}
