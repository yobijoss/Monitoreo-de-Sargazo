package com.yobijoss.monitoreodesargazo.extension


import android.view.Menu
import android.view.MenuItem

fun Menu?.addItem(title: String, index: Int, groupId: Int, urlAction: (MenuItem) -> Boolean) {
    val menuItem = this?.add(groupId, index + title.hashCode(), index, title)
    menuItem?.setOnMenuItemClickListener(urlAction)
}
