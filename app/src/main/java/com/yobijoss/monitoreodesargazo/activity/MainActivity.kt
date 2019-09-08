package com.yobijoss.monitoreodesargazo.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.yobijoss.monitoreodesargazo.R
import com.yobijoss.monitoreodesargazo.extension.addItem
import com.yobijoss.monitoreodesargazo.util.AnalyticsUtil
import com.yobijoss.monitoreodesargazo.util.UrlUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var currentUrl: String

    private lateinit var btnShareUrl: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        btnShareUrl = findViewById(R.id.btnShareUrl)
        btnShareUrl.setOnClickListener { shareUrl() }

        webView.settings.javaScriptEnabled = true
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webView.webViewClient = SargassumWebClient(this)
        addSargassoItems(navView)

        goToMainUrl()

    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_start -> goToMainUrl()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun addSargassoItems(menuView: NavigationView) {
        resources.getStringArray(R.array.sargassum_links).let {
            it.forEachIndexed { index, item ->
                val title = UrlUtils().extractTitle(item)
                menuView.menu.addItem(title, index, R.id.mainGroup) {
                    goToUrl(item)
                }
            }
        }

        menuView.menu.setGroupCheckable(R.id.mainGroup, true, true)
    }

    private fun goToUrl(url: String): Boolean {
        currentUrl = url
        Log.d("monitoreo_sargazo", "The url is $url")
        webView.loadUrl(url)
        drawer_layout.closeDrawer(GravityCompat.START)
        AnalyticsUtil.logUrlClicked(getAnalytics(), url)
        return true
    }

    private fun goToMainUrl() {
        currentUrl = getString(R.string.main_url)
        goToUrl(currentUrl)
    }

    private fun shareUrl() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Sargassum Report")
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentUrl)
        AnalyticsUtil.logShareButtonClicked(getAnalytics(), currentUrl)
        startActivity(Intent.createChooser(shareIntent, "Share link!"))
    }

    private fun getAnalytics() = FirebaseAnalytics.getInstance(this)

    class SargassumWebClient(private val mainActivity: MainActivity) : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            request?.let {
                val url = it.url.toString()

                if (mainActivity.resources.getStringArray(R.array.sargassum_links).contains(url)) {
                    mainActivity.goToUrl(url)
                    return false
                }
            }

            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent(Intent.ACTION_VIEW, request?.url).apply {
                startActivity(this@SargassumWebClient.mainActivity, this, null)
            }

            return true
        }
    }

}
