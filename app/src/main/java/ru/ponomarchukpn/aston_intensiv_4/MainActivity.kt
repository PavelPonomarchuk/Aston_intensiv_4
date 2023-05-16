package ru.ponomarchukpn.aston_intensiv_4

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val clockView by bind<AnalogueClockView>(R.id.clock_view, this)
    private val buttonBlack by bind<AppCompatButton>(R.id.main_button_black, this)
    private val buttonColored by bind<AppCompatButton>(R.id.main_button_colored, this)
    private val buttonEqualize by bind<AppCompatButton>(R.id.main_button_equalize, this)
    private val buttonCustomize by bind<AppCompatButton>(R.id.main_button_customize, this)

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setButtonsListeners()
    }

    private fun setButtonsListeners() {
        buttonBlack.setOnClickListener {
            setBlackHands()
        }
        buttonColored.setOnClickListener {
            setColoredHands()
        }
        buttonEqualize.setOnClickListener {
            setEqualHandsLength()
        }
        buttonCustomize.setOnClickListener {
            setCustomHandsLength()
        }
    }

    private fun setBlackHands() {
        clockView.submitHourColor(Color.BLACK)
        clockView.submitMinuteColor(Color.BLACK)
        clockView.submitSecondColor(Color.BLACK)
    }

    private fun setColoredHands() {
        clockView.submitHourColor(Color.RED)
        clockView.submitMinuteColor(Color.GREEN)
        clockView.submitSecondColor(Color.BLUE)
    }

    private fun setEqualHandsLength() {
        clockView.submitHourPercent(DEFAULT_LENGTH_PERCENT)
        clockView.submitMinutePercent(DEFAULT_LENGTH_PERCENT)
        clockView.submitSecondPercent(DEFAULT_LENGTH_PERCENT)
    }

    private fun setCustomHandsLength() {
        clockView.submitHourPercent(HOUR_LENGTH_PERCENT)
        clockView.submitMinutePercent(MINUTE_LENGTH_PERCENT)
        clockView.submitSecondPercent(SECOND_LENGTH_PERCENT)
    }

    override fun onResume() {
        super.onResume()

        job = lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000)
                clockView.submitTime(Calendar.getInstance())
            }
        }
    }

    override fun onPause() {
        lifecycleScope.launch {
            job?.cancelAndJoin()
        }
        super.onPause()
    }

    companion object {

        private const val DEFAULT_LENGTH_PERCENT = 0.5F
        private const val HOUR_LENGTH_PERCENT = 0.55F
        private const val MINUTE_LENGTH_PERCENT = 0.7F
        private const val SECOND_LENGTH_PERCENT = 0.8F
    }
}
