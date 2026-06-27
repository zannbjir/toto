package org.koitharu.kotatsu.widget.common

import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

private const val TAG = "KotatsuWidget"

/**
 * Generous cap — goAsync() gives us ~10s before the system reclaims the process, but cover
 * downloads can blow past that on slow networks. We tolerate the work being killed mid-flight
 * (the widget keeps whatever was last drawn) and just put a sanity ceiling on the coroutine.
 */
private val WIDGET_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(20)

fun Context.widgetEntryPoint(): WidgetEntryPoint =
	EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)

fun AppWidgetProvider.runAsync(
	context: Context,
	tag: String,
	block: suspend CoroutineScope.(Context) -> Unit,
) {
	val pending: BroadcastReceiver.PendingResult = goAsync()
	val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	scope.launch {
		try {
			withTimeoutOrNull(WIDGET_TIMEOUT_MS) {
				block(context.applicationContext)
			} ?: Log.w(TAG, "$tag: update timed out")
		} catch (t: Throwable) {
			Log.e(TAG, "$tag: update failed", t)
		} finally {
			try {
				pending.finish()
			} catch (_: Throwable) {
				// host may have torn the receiver down already
			}
		}
	}
}