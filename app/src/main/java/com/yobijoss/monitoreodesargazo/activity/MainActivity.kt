package com.yobijoss.monitoreodesargazo.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
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
import com.google.android.material.snackbar.Snackbar
import com.yobijoss.monitoreodesargazo.R
import com.yobijoss.monitoreodesargazo.extension.addItem
import com.yobijoss.monitoreodesargazo.util.UrlUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->

        }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
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
            it.forEach { item ->
                val title = UrlUtils().extractTitle(item)
                menuView.menu.addItem(title) {
                    goToUrl(item)
                }
            }
        }
    }

    private fun goToUrl(url: String): Boolean {
        webView.loadUrl(url)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun goToMainUrl() {
        goToUrl(getString(R.string.main_url))
    }

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
