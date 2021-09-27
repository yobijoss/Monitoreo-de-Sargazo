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
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.yobijoss.monitoreodesargazo.R
import com.yobijoss.monitoreodesargazo.databinding.ActivityMainBinding
import com.yobijoss.monitoreodesargazo.databinding.AppBarMainBinding
import com.yobijoss.monitoreodesargazo.extension.addItem
import com.yobijoss.monitoreodesargazo.util.AnalyticsUtil
import com.yobijoss.monitoreodesargazo.util.UrlUtils
import com.yobijoss.monitoreodesargazo.viewmodel.MainViewModel


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var appBarMainBinding: AppBarMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        appBarMainBinding = AppBarMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        setSupportActionBar(appBarMainBinding.toolbar)

        viewModel.urlLiveData.observe(this, { displayUrl(it) })

        val drawerLayout: DrawerLayout = findViewById(R.id.main_drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, appBarMainBinding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        appBarMainBinding.buttonShareUrl.setOnClickListener { shareUrl() }

        appBarMainBinding.webView.settings.javaScriptEnabled = true
        appBarMainBinding.webView.settings.layoutAlgorithm =
            WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        appBarMainBinding.webView.webViewClient = SargassumWebClient()
        addSargassoItems(navView)

        viewModel.urlLiveData.value = getString(R.string.main_url)
    }

    override fun onBackPressed() {
        if (mainBinding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_start -> viewModel.urlLiveData.value = getString(R.string.main_url)
        }
        mainBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
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
        appBarMainBinding.webView.loadUrl(url)
        mainBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
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

    inner class SargassumWebClient : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.i("Sargassum", "Page finished")

        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.let {
                Log.i("Sargassum", "Url overriding")

                val url = it.url.toString()

                if (this@MainActivity.resources.getStringArray(R.array.sargassum_links)
                        .contains(url)
                ) {
                    this@MainActivity.displayUrl(url)
                    return false
                }
            }

            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent(Intent.ACTION_VIEW, request?.url).apply {
                startActivity(this@MainActivity, this, null)
            }

            return true
        }
    }
}
