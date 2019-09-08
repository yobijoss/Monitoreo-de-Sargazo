package com.yobijoss.monitoreodesargazo.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsUtil {

    fun logUrlClicked(analytics: FirebaseAnalytics, url: String) {
        analytics.logEvent("URL_CLICKED", Bundle().apply { putString("url", url) })
    }

    fun logShareButtonClicked(analytics: FirebaseAnalytics, sharedUrl: String ) {
        analytics.logEvent("SHARE_BUTTON_CLICKED", Bundle().apply { putString("url", sharedUrl) })
    }
}