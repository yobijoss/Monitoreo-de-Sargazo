package com.yobijoss.monitoreodesargazo.util

import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class UrlUtilsTest {
  
  private val urlUtils = UrlUtils()
  
  @Test
  fun extractTitle() {
    var url = "https://www.viajefest.com/sargazo-en-quintana-roo/hay-sargazo-en-holbox/"
    var expectedTitle = urlUtils.extractTitle(url)
    
    
    assertThat(expectedTitle, Is("Sargazo en Holbox"))
    
    url = "https://www.viajefest.com/sargazo-en-quintana-roo/hay-sargazo-en-playa-del-carmen/"
    expectedTitle = urlUtils.extractTitle(url)
    
    
    assertThat(expectedTitle, Is("Sargazo en Playa Del Carmen"))
  }
}