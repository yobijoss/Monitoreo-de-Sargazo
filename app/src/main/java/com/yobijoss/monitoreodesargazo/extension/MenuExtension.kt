package com.yobijoss.monitoreodesargazo.extension


import android.view.Menu
import android.view.MenuItem

fun Menu?.addItem(title: String, urlAction: (MenuItem) -> Boolean) {
    val menuItem = this?.add(title)

    menuItem?.setOnMenuItemClickListener (urlAction)
}
