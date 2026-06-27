package org.koitharu.kotatsu.widget.common

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration

object WidgetSizes {

	fun currentSizeDp(
		context: Context,
		manager: AppWidgetManager,
		widgetId: Int,
		defaultWidth: Int,
		defaultHeight: Int,
	): Pair<Int, Int> {
		val options = manager.getAppWidgetOptions(widgetId)
		val isLandscape =
			context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
		val widthDp = (if (isLandscape) {
			options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
		} else {
			options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
		}).takeIf { it > 0 } ?: defaultWidth
		val heightDp = (if (isLandscape) {
			options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
		} else {
			options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
		}).takeIf { it > 0 } ?: defaultHeight
		return widthDp to heightDp
	}
}