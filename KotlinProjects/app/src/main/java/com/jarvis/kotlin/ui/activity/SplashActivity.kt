package com.jarvis.kotlin.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.jarvis.kotlin.R


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {
    private val mHandler = Handler()

    private val mRunnable = Runnable {
        val togglIntent = Intent(this, TogglActivity::class.java)
        startActivity(togglIntent)
        finish()
    }
    private var mVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mVisible = true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mHandler.postDelayed(mRunnable, UI_DELAY.toLong())
    }

    private fun createSpringAnimation() {
        /*
        val springAnim = SpringAnimation(fullscreen_content, DynamicAnimation.TRANSLATION_Y, 600f)
        springAnim.setStartValue(-100f)
                .setMaxValue(2000f)
                .setMinValue(-100f)
                .setStartVelocity(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.2f, resources.displayMetrics))
        springAnim.addEndListener({ animation: DynamicAnimation<out DynamicAnimation<*>>?, canceled: Boolean, value: Float, velocity: Float ->
            Handler().postDelayed({
                val togglIntent = Intent(this, TogglActivity::class.java)
                startActivity(togglIntent)
                finish()
            },2000)
        })
        springAnim.spring.stiffness = SpringForce.STIFFNESS_LOW
        springAnim.spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
        springAnim.start() */
    }


    companion object {
        private val UI_DELAY = 3000
    }
}
