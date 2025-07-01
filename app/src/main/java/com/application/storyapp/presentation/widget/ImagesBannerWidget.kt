package com.application.storyapp.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.net.toUri
import com.application.storyapp.R


@Suppress("DEPRECATION")
class ImagesBannerWidget : AppWidgetProvider() {
    companion object {
        private const val TOAST_ACTION = "com.application.storyapp.TOAST_ACTION"
        const val EXTRA_ITEM = "com.application.storyapp.EXTRA_ITEM"
        const val EXTRA_STORY_ID = "com.application.storyapp.EXTRA_STORY_ID"
        const val EXTRA_STORY_NAME = "com.application.storyapp.EXTRA_STORY_NAME"
        private const val REFRESH_ACTION = "com.application.storyapp.REFRESH_ACTION"

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val intent = Intent(context, StackWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()
            }

            val views = RemoteViews(context.packageName, R.layout.image_banner_widget).apply {
                setRemoteAdapter(R.id.stack_view, intent)
                setEmptyView(R.id.stack_view, R.id.empty_view)

                val toastIntent = Intent(context, ImagesBannerWidget::class.java).apply {
                    action = TOAST_ACTION
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val toastPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    toastIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    else
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
                setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)

                val refreshIntent = Intent(context, ImagesBannerWidget::class.java).apply {
                    action = REFRESH_ACTION
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    1,
                    refreshIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    else
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
                setOnClickPendingIntent(R.id.banner_text, refreshPendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ImagesBannerWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            updateAppWidget(context, appWidgetManager, it)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            TOAST_ACTION -> {
                val index = intent.getIntExtra(EXTRA_ITEM, 0)
                val storyName = intent.getStringExtra(EXTRA_STORY_NAME) ?: "Unknown"
                Toast.makeText(context, "Story: $storyName (Position: ${index + 1})", Toast.LENGTH_SHORT).show()
            }
            REFRESH_ACTION -> {
                updateWidget(context)
                Toast.makeText(context, "Widget refreshed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
