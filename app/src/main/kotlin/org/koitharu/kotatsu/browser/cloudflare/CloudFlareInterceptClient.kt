package org.koitharu.kotatsu.browser.cloudflare

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koitharu.kotatsu.core.network.cookies.MutableCookieJar
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "CFInterceptClient"

/**
 * CloudFlare client with header interception to bypass blocking
 * Filters out sec-ch-ua, sec-ch-ua-full-version-list, and x-requested-with headers
 */
class CloudFlareInterceptClient(
	cookieJar: MutableCookieJar,
	callback: CloudFlareCallback,
	targetUrl: String,
) : CloudFlareClient(cookieJar, callback, targetUrl) {

	// Headers we want to block
	private val blockedHeaders = setOf(
		"sec-ch-ua",
		"sec-ch-ua-full-version-list",
		"x-requested-with"
	)

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		super.onPageStarted(view, url, favicon)
		Log.d(TAG, "Page started with interception enabled: $url")
	}

	override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
		if (request == null) return null

		try {
			// Skip POST requests
			if (request.method == "POST") {
				Log.d(TAG, "Skipping POST request: ${request.url}")
				return super.shouldInterceptRequest(view, request)
			}

			Log.d(TAG, "Intercepting request: ${request.url}")

			val client = OkHttpClient.Builder()
				.connectTimeout(15, TimeUnit.SECONDS)
				.readTimeout(15, TimeUnit.SECONDS)
				.build()

			val requestBuilder = Request.Builder()
				.url(request.url.toString())
				.method(request.method, null)

			// Filter headers using blocklist - keep everything except blocked headers
			val blockedCount = mutableListOf<String>()
			for ((key, value) in request.requestHeaders) {
				val lowerKey = key.lowercase(Locale.ROOT)
				if (!blockedHeaders.contains(lowerKey)) {
					requestBuilder.addHeader(key, value)
				} else {
					blockedCount.add(key)
				}
			}
			if (blockedCount.isNotEmpty()) {
				Log.d(TAG, "Blocked headers: ${blockedCount.joinToString(", ")}")
			}

			val response = client.newCall(requestBuilder.build()).execute()

			val contentType = response.header("Content-Type")
			val mimeType: String
			val charset: String?

			if (contentType != null) {
				val parts = contentType.split(";")
				mimeType = parts[0].trim()
				charset = parts.find { it.trim().startsWith("charset=", ignoreCase = true) }
					?.substringAfter("=")?.trim()
			} else {
				mimeType = "text/html"
				charset = "UTF-8"
			}

			return WebResourceResponse(mimeType, charset, response.body?.byteStream()).apply {
				val headers = mutableMapOf<String, String>()
				response.headers.forEach { headers[it.first] = it.second }
				responseHeaders = headers
			}
		} catch (e: Exception) {
			Log.e(TAG, "Error intercepting request: ${request.url}", e)
			return null
		}
	}
}
