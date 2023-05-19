package com.silverytitan.bnv.expand

import android.animation.Animator
import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

fun Int.dp2Px() = (this * Resources.getSystem().displayMetrics.density + 0.5f)//自定义view使用

fun Int.dp2PxInt() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
fun Float.dp2PxInt() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
fun Int.px2DpInt() = (this / Resources.getSystem().displayMetrics.density + 0.5f).toInt()

fun Context.addSelectedFromDrawable(
    @DrawableRes selected: Int, @DrawableRes normal: Int): Drawable {
    val drawableNormal = AppCompatResources.getDrawable(this, normal)
    drawableNormal?.setBounds(0, 0, drawableNormal.intrinsicWidth.px2DpInt(),
            drawableNormal.intrinsicHeight.px2DpInt())
    val drawableSelected = AppCompatResources.getDrawable(this, selected)
    drawableSelected?.setBounds(0, 0, drawableSelected.intrinsicWidth.px2DpInt(),
            drawableSelected.intrinsicHeight.px2DpInt())
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_selected), drawableSelected)
        addState(intArrayOf(-android.R.attr.state_selected), drawableNormal)
    }
}
/**
 *View扩展类相关
 */
/**
 * 设置View的高度
 */
fun View.height(height: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    params.height = height
    layoutParams = params
    return this
}

/**
 * 设置View高度，限制在min和max范围之内
 * @param h
 * @param min 最小高度
 * @param max 最大高度
 */
fun View.limitHeight(h: Int, min: Int, max: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    when {
        h < min -> params.height = min
        h > max -> params.height = max
        else -> params.height = h
    }
    layoutParams = params
    return this
}

/**
 * 设置View的宽度
 */
fun View.width(width: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    params.width = width
    layoutParams = params
    return this
}

/**
 * 设置View宽度，限制在min和max范围之内
 * @param w
 * @param min 最小宽度
 * @param max 最大宽度
 */
fun View.limitWidth(w: Int, min: Int, max: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    when {
        w < min -> params.width = min
        w > max -> params.width = max
        else -> params.width = w
    }
    layoutParams = params
    return this
}

/**
 * 设置View的宽度和高度
 * @param width 要设置的宽度
 * @param height 要设置的高度
 */
fun View.widthAndHeight(width: Int, height: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    params.width = width
    params.height = height
    layoutParams = params
    return this
}

/**
 * 设置View的margin
 * @param leftMargin 默认保留原来的
 * @param topMargin 默认是保留原来的
 * @param rightMargin 默认是保留原来的
 * @param bottomMargin 默认是保留原来的
 */
fun View.margin(
    leftMargin: Int = Int.MAX_VALUE, topMargin: Int = Int.MAX_VALUE, rightMargin: Int = Int.MAX_VALUE, bottomMargin: Int = Int.MAX_VALUE): View {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    if (leftMargin != Int.MAX_VALUE) params.leftMargin = leftMargin
    if (topMargin != Int.MAX_VALUE) params.topMargin = topMargin
    if (rightMargin != Int.MAX_VALUE) params.rightMargin = rightMargin
    if (bottomMargin != Int.MAX_VALUE) params.bottomMargin = bottomMargin
    layoutParams = params
    return this
}

/**
 * 设置宽度，带有过渡动画
 * @param targetValue 目标宽度
 * @param duration 时长
 * @param action 可选行为
 */
fun View.animateWidth(
    targetValue: Int, duration: Long = 400, listener: Animator.AnimatorListener? = null, action: ((Float) -> Unit)? = null) {
    post {
        ValueAnimator.ofInt(width, targetValue)
                .apply {
                    addUpdateListener {
                        width(it.animatedValue as Int)
                        action?.invoke((it.animatedFraction))
                    }
                    if (listener != null) addListener(listener)
                    setDuration(duration)
                    start()
                }
    }
}

/**
 * 设置高度，带有过渡动画
 * @param targetValue 目标高度
 * @param duration 时长
 * @param action 可选行为
 */
fun View.animateHeight(
    targetValue: Int, duration: Long = 400, listener: Animator.AnimatorListener? = null, action: ((Float) -> Unit)? = null) {
    post {
        ValueAnimator.ofInt(height, targetValue)
                .apply {
                    addUpdateListener {
                        height(it.animatedValue as Int)
                        action?.invoke((it.animatedFraction))
                    }
                    if (listener != null) addListener(listener)
                    setDuration(duration)
                    start()
                }
    }
}

/**
 * 设置宽度和高度，带有过渡动画
 * @param targetWidth 目标宽度
 * @param targetHeight 目标高度
 * @param duration 时长
 * @param action 可选行为
 */
fun View.animateWidthAndHeight(
    targetWidth: Int, targetHeight: Int, duration: Long = 400, listener: Animator.AnimatorListener? = null, action: ((Float) -> Unit)? = null) {
    post {
        val startHeight = height
        val evaluator = IntEvaluator()
        ValueAnimator.ofInt(width, targetWidth)
                .apply {
                    addUpdateListener {
                        widthAndHeight(it.animatedValue as Int,
                                evaluator.evaluate(it.animatedFraction, startHeight, targetHeight))
                        action?.invoke((it.animatedFraction))
                    }
                    if (listener != null) addListener(listener)
                    setDuration(duration)
                    start()
                }
    }
}

/**
 * 设置点击监听, 并实现事件节流
 */
var _viewClickFlag = false
var _clickRunnable = Runnable { _viewClickFlag = false }
fun View.click(action: (view: View) -> Unit) {
    setOnClickListener {
        if (!_viewClickFlag) {
            _viewClickFlag = true
            action(it)
        }
        removeCallbacks(_clickRunnable)
        postDelayed(_clickRunnable, 350)
    }
}

/**
 * 设置长按监听
 */
fun View.longClick(action: (view: View) -> Boolean) {
    setOnLongClickListener {
        action(it)
    }
}


/*** 可见性相关 ****/
fun View.gone() {
    visibility = View.GONE

}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

val View.isGone: Boolean
    get() {
        return visibility == View.GONE
    }

val View.isVisible: Boolean
    get() {
        return visibility == View.VISIBLE
    }

val View.isInvisible: Boolean
    get() {
        return visibility == View.INVISIBLE
    }

/**
 * 切换View的可见性
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
}

/**
 * 设置圆形背景
 */
fun View.setRoundRectBg(
    color: Int = Color.WHITE, cornerRadius: Int = 10.dp2PxInt(), strokeWidth: Int = -1, strokeColor: Int = color) {
    background = GradientDrawable().apply {
        setColor(color)
        setCornerRadius(cornerRadius.toFloat())
        if (strokeWidth != -1) {
            setStroke(strokeWidth, strokeColor)
        }
    }
}

// 所有子View
inline val ViewGroup.children
    get() = (0 until childCount).map { getChildAt(it) }

/**
 * 以下为fragment事物扩展方法、添加替换显示隐藏fragment、批量添加隐藏、显示隐藏fragment
 */
private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
        beginTransaction().func()
                .commit()

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) =
        supportFragmentManager.inTransaction { add(frameId, fragment) }

fun AppCompatActivity.replaceFragment(
    fragment: Fragment, frameId: Int, inAnim: Int? = null, outAnim: Int? = null) =
        if (inAnim != null && outAnim != null) supportFragmentManager.inTransaction {
            setCustomAnimations(inAnim, outAnim).replace(frameId, fragment)
        }
        else supportFragmentManager.inTransaction {
            replace(frameId, fragment)
        }


fun AppCompatActivity.hideFragment(fragment: Fragment) =
        supportFragmentManager.inTransaction { hide(fragment) }

fun AppCompatActivity.showFragment(fragment: Fragment) =
        supportFragmentManager.inTransaction { show(fragment) }

/**
 * 展示第0个fragment
 * 隐藏其他fragment
 */
fun AppCompatActivity.showHideFragment(
    mutableList: List<Fragment>, inAnim: Int? = null, outAnim: Int? = null) {
    if (mutableList.isNotEmpty()) {
        if (inAnim != null && outAnim != null) supportFragmentManager.inTransaction {
            setCustomAnimations(inAnim, outAnim).show(mutableList[0])
        }
        else supportFragmentManager.inTransaction {
            show(mutableList[0])
        }
        for (index in 1 until mutableList.size) {
            supportFragmentManager.inTransaction { hide(mutableList[index]) }
        }
    }
}

/**
 * 添加第0个fragment
 * 隐藏其他fragment
 */
fun AppCompatActivity.addHideFragment(
    mutableList: List<Fragment>, frameId: Int, inAnim: Int? = null, outAnim: Int? = null) {
    if (mutableList.isNotEmpty()) {
        for (element in mutableList) {
            if (inAnim != null && outAnim != null) supportFragmentManager.inTransaction {
                setCustomAnimations(inAnim, outAnim).add(frameId, element)
            }
            else supportFragmentManager.inTransaction {
                add(frameId, element)
            }
        }
        for (index in 1 until mutableList.size) {
            supportFragmentManager.inTransaction { hide(mutableList[index]) }
        }
    }
}

inline fun <reified A : Activity> Context.startToActivity() {
    startActivity(Intent(this, A::class.java))
}

inline fun <reified A : Activity> Context.startToPutActivity(init: Intent.() -> Unit) {
    val intent = Intent(this, A::class.java)
    intent.init()
    startActivity(intent)
}

fun TextView.setUnderlineSpan() {
    val spannableString = SpannableString(this.text.toString())
    spannableString.setSpan(UnderlineSpan(), 0, spannableString.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    this.text = spannableString
}

fun String.setTextColorSpan(
    context: Context, @ColorRes color: Int, startPosition: Int, endPosition: Int): SpannableString {
    val spannableString = SpannableString(this)
    spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, color)),
            startPosition, endPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    return spannableString
}

/**
 * 代码写布局扩展函数
 *
 * 例子：var editText = view {
hint = "请输入密码"
setHintTextColor(ContextCompat.getColor(context, R.color.hint_color))
background = null
setTextColor(ColorUtils.getColor(R.color.hint_color))
}
 */
inline fun <reified V : View> Context.view(init: V.() -> Unit): V {
    val constr = V::class.java.getConstructor(Context::class.java)
    val tv = constr.newInstance(this)
    tv.init()
    return tv
}

/**
 * 设置带文字布局控件宽度
 */
fun String.setTextWidth2View(view: View) {
    val layoutParams = view.layoutParams
    layoutParams.width = Paint().measureText(this)
                                 .toInt() * length + 14
    view.layoutParams = layoutParams
}

/**
 * 设置带文字布局控件宽度
 */
fun String.getTextWidth() = Paint().measureText(this)
        .toInt()