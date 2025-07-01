package com.application.storyapp.presentation.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.application.storyapp.R
import com.application.storyapp.model.Story
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = mutableListOf<Story>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        mWidgetItems.clear()

        val prefs = mContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val storyJson = prefs.getString("stories", null)

        if (storyJson == null || storyJson == "[]") {
            mWidgetItems.add(
                Story("0", "Belum ada cerita", "Deskripsi kosong","https://via.placeholder.com/500","")
            )
            return
        }

        val type = object : TypeToken<List<Story>>() {}.type
        val stories: List<Story> = Gson().fromJson(storyJson, type)
        mWidgetItems.addAll(stories)
    }

    override fun onDestroy() { mWidgetItems.clear() }
    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val story = mWidgetItems[position]
        val rv = RemoteViews(mContext.packageName, R.layout.item_widget)

        val bitmap = try {
            Glide.with(mContext.applicationContext)
                .asBitmap()
                .load(story.photoUrl)
                .submit(500, 500)
                .get()
        } catch (e: Exception) {
            BitmapFactory.decodeResource(mContext.resources, R.drawable.placeholder_image)
        }

        rv.setImageViewBitmap(R.id.imageView, bitmap)

        val fillInIntent = Intent().apply {
            putExtra(ImagesBannerWidget.EXTRA_ITEM, position)
            putExtra(ImagesBannerWidget.EXTRA_STORY_NAME, story.name)
            putExtra(ImagesBannerWidget.EXTRA_STORY_ID, story.id)
        }
        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
