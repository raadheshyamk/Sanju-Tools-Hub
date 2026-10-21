package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sanju Tools Hub", appName)
  }

  @Test
  fun `test math expression evaluation`() {
    val res = com.example.sanjutools.utils.MathUtils.evaluateExpression("2+3*4")
    assertEquals("14", res)
  }

  @Test
  fun `test text utils statistics`() {
    val stats = com.example.sanjutools.utils.TextUtils.calculateStats("Sanju Tools Hub provides everything")
    assertEquals(5, stats.words)
  }
}
