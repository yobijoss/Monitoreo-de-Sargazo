package com.yobijoss.monitoreodesargazo.util

class

UrlUtils {
  fun extractTitle(url: String) = url
      .substringAfter("/sargazo-en-quintana-roo/hay-")
      .substringBeforeLast("/")
      .split("-")
      .joinToString(" ") { if (it == "en") it else it.capitalize() }
}

