package com.yobijoss.monitoreodesargazo.activity

import android.content.Intent
import android.os.Bundle
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.yobijoss.monitoreodesargazo.R
import com.yobijoss.monitoreodesargazo.extension.addItem
import com.yobijoss.monitoreodesargazo.util.AnalyticsUtil
import com.yobijoss.monitoreodesargazo.util.UrlUtils
import com.yobijoss.monitoreodesargazo.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        viewModel.urlLiveData.observe(this, Observer { displayUrl(it) })

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

        btnShareUrl.setOnClickListener { shareUrl() }

        webView.settings.javaScriptEnabled = true
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webView.webViewClient = SargassumWebClient(this)
        addSargassoItems(navView)

        viewModel.urlLiveData.value = getString(R.string.main_url)
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
            R.id.nav_start -> viewModel.urlLiveData.value = getString(R.string.main_url)
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun addSargassoItems(menuView: NavigationView) {
        resources.getStringArray(R.array.sargassum_links).let {
            it.forEachIndexed { index, item ->
                val title = UrlUtils().extractTitle(item)
                menuView.menu.addItem(title, index, R.id.mainGroup) {
                    viewModel.urlLiveData.value = item
                    true
                }
            }
        }

        menuView.menu.setGroupCheckable(R.id.mainGroup, true, true)
    }

    private fun displayUrl(url: String): Boolean {
        webView.loadUrl(url)
        drawer_layout.closeDrawer(GravityCompat.START)
        AnalyticsUtil.logUrlClicked(getAnalytics(), url)
        return true
    }


    private fun shareUrl() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val url = viewModel.urlLiveData.value

        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.get_sargassum_report))
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_link)))

        AnalyticsUtil.logShareButtonClicked(getAnalytics(), url)
    }

    private fun getAnalytics() = FirebaseAnalytics.getInstance(this)

    class SargassumWebClient(private val mainActivity: MainActivity) : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.let {
                val url = it.url.toString()

                if (mainActivity.resources.getStringArray(R.array.sargassum_links).contains(url)) {
                    mainActivity.displayUrl(url)
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
