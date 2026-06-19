package com.notifymarquee.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import com.notifymarquee.R
import com.notifymarquee.model.NotificationItem
import com.notifymarquee.utils.PreferenceManager

class MarqueeOverlayView(ctx: Context, private val prefs: PreferenceManager) : FrameLayout(ctx) {
    private val tv: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var animator: ObjectAnimator? = null
    private var onComplete: (() -> Unit)? = null

    // FIX #3: Flag để tránh gọi onComplete 2 lần
    private var completed = false

    init {
        LayoutInflater.from(ctx).inflate(R.layout.view_marquee_overlay, this, true)
        tv = findViewById(R.id.tvMarquee)
        applyStyle()
    }

    private fun applyStyle() {
        val bgColor = try { Color.parseColor(prefs.bgColor) } catch (_: Exception) { Color.parseColor("#CC1565C0") }
        val alpha = (prefs.transparency * 255 / 100).coerceIn(0, 255)
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.argb(alpha, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor)))
            cornerRadius = prefs.cornerRadius * resources.displayMetrics.density
        }
        tv.background = drawable
        tv.setTextColor(try { Color.parseColor(prefs.textColor) } catch (_: Exception) { Color.WHITE })
        tv.textSize = prefs.textSize
        val p = (8 * resources.displayMetrics.density).toInt()
        tv.setPadding(p * 2, p, p * 2, p)
    }

    fun setNotification(item: NotificationItem) {
        tv.text = if (prefs.showAppName) "【${item.appName}】${item.sender}: ${item.content}"
                  else "${item.sender}: ${item.content}"
    }

    fun setOnCompleteListener(l: () -> Unit) { onComplete = l }

    private fun finish() {
        // FIX #3: Chỉ gọi onComplete đúng 1 lần
        if (completed) return
        completed = true
        handler.removeCallbacksAndMessages(null)
        animator?.cancel()
        onComplete?.invoke()
    }

    fun start() {
        // FIX #2: Dùng doOnLayout thay vì post để đảm bảo tv.width đã đúng
        tv.doOnLayout {
            val sw = resources.displayMetrics.widthPixels.toFloat()
            val tw = tv.width.toFloat()

            // Nếu tw vẫn = 0 (hiếm gặp), dùng giá trị ước tính
            val textWidth = if (tw > 0) tw else sw * 1.5f

            tv.translationX = sw
            val speed = prefs.scrollSpeed.coerceIn(1, 10)
            val pps = 80f + (speed - 1) * 40f
            val dur = (((sw + textWidth) / pps) * 1000).toLong()

            animator = ObjectAnimator.ofFloat(tv, "translationX", sw, -textWidth).apply {
                duration = dur
                interpolator = LinearInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(a: Animator) {
                        handler.postDelayed({ finish() }, 200)
                    }
                    override fun onAnimationCancel(a: Animator) { /* bị cancel bởi finish(), không làm gì */ }
                })
                start()
            }

            // FIX #3: Timeout dự phòng - chỉ trigger nếu animation chưa xong
            handler.postDelayed({ finish() }, prefs.displayDuration)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        completed = true // Ngăn callback sau khi detach
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}
