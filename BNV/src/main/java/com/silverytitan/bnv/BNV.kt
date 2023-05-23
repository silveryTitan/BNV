package com.silverytitan.bnv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import android.view.MotionEvent.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.silverytitan.bnv.adapter.FragmentAdapter
import com.silverytitan.bnv.annotationenum.BNVItemMode
import com.silverytitan.bnv.annotationenum.BNVMode
import com.silverytitan.bnv.common.ITEM_NORMAL
import com.silverytitan.bnv.common.MODE_BEZIER
import com.silverytitan.bnv.common.MODE_NORMAL
import com.silverytitan.bnv.expand.addSelectedFromDrawable
import com.silverytitan.bnv.expand.children
import com.silverytitan.bnv.expand.dp2Px
import com.silverytitan.bnv.expand.dp2PxInt
import com.silverytitan.bnv.view.reduceshape.RsTextView
import org.jetbrains.annotations.NotNull
import kotlin.math.abs

class BNV : LinearLayout {
    private val paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val path by lazy { Path() }
    private val width by lazy { resources.displayMetrics.widthPixels.toFloat() }
    private val layoutParams by lazy { LayoutParams((width / maxCount).toInt(), WRAP_CONTENT) }
    private val viewList = mutableListOf<ConstraintLayout>()
    private val fragmentList = mutableListOf<Fragment>()
    private var maxCount = 5//最大item
    private var continuousClickInterval = 250L //多次连续点击间隔
    private var downTime = 0L//按下时间
    private var lastTouchTime = 0L//缓存时间
    private var touchCount = 0L//记录点击次数
    private var downY = 0F//x轴坐标
    private var downX = 0F//y轴坐标
    private var isDrug = false//当前是否移动事件
    private var prePosition = 0//缓存上次点击事件
    private var click: ((position: Int) -> Unit)? = null
    private var longClick: ((position: Int) -> Unit)? = null
    private var multiClick: ((position: Int, touchCount: Long) -> Unit)? = null
    private var drugClick: ((position: Int) -> Unit)? = null

    @BNVMode
    var mode = MODE_NORMAL

    @BNVItemMode
    var itemMode = ITEM_NORMAL
    var isEnlarge = false //中间按钮是否超出父布局
    var longPressTime = 2000 //设置长按时间
    var textSize = 11f//字体大小
    var drawPadding = 2//字体图片间距
    var normalTextColor = Color.parseColor("#8A8A8A")//未选中字体颜色
    var pressTextColor = Color.parseColor("#B772FD")//选中字体颜色
    private var viewPager: ViewPager2? = null
    var isPrePosition = true //是否需要缓存上次点击事件（禁止重复点击切换）

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        initAttr(context, attr)
    }

    init {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = Color.WHITE
        setWillNotDraw(false)
    }

    private fun initAttr(context: Context, attr: AttributeSet?) {
        context.obtainStyledAttributes(attr, R.styleable.BNV)
                .apply {
                    mode = getInt(R.styleable.BNV_bnv_mode, MODE_NORMAL)
                    maxCount = getInt(R.styleable.BNV_bnv_max_count, 5)
                    isEnlarge = getBoolean(R.styleable.BNV_bnv_is_enlarge, false)
                    textSize = getFloat(R.styleable.BNV_bnv_textSize, 11f)
                    drawPadding = getInt(R.styleable.BNV_bnv_drawPadding, 2)
                    normalTextColor = getColor(R.styleable.BNV_bnv_normal_textColor,
                            Color.parseColor("#8A8A8A"))
                    pressTextColor = getColor(R.styleable.BNV_bnv_press_textColor,
                            Color.parseColor("#B772FD"))
                    recycle()
                }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val paddingHeight = (height - view.height) / 2//每个item的空白间距的一半
            val paddingWidth = (width / maxCount - view.width) / 2//每个item的空白间距的一半
            //当数量单数时 中间按钮需要超出父布局的测量
            childLayout(view)
            if (maxCount % 2 != 0 && i == maxCount / 2 && childCount == maxCount && isEnlarge) {
                view.visibility = GONE

            } else {
                view.layout((paddingWidth + (view.width + paddingWidth) * i).toInt(), paddingHeight,
                        (width / maxCount * (i + 1)).toInt(),
                        paddingHeight + view.height)//设置每个子布局的位置
                layoutParams.gravity = Gravity.CENTER
                view.layoutParams = layoutParams//将每个子布局设置居中
            }
        }
    }

    private fun childLayout(view: View) {
        if (view is ViewGroup && view is ConstraintLayout) {
            view.layoutParams = ConstraintLayout.LayoutParams(view.measuredWidth,
                    view.measuredHeight)
            view.forEach { v ->
                if (v.layoutParams is ConstraintLayout.LayoutParams) {
                    val set = ConstraintSet()
                    set.clone(view)
                    when (itemMode) {
                        ITEM_NORMAL -> {
                            set.connect(R.id.tv_text_icon, START, PARENT_ID, START)
                            set.connect(R.id.tv_text_icon, END, PARENT_ID, END)
                            set.connect(R.id.tv_text_icon, BOTTOM, PARENT_ID, BOTTOM)
                            set.connect(R.id.tv_text_unread, START, R.id.tv_text_icon, END)
                            set.connect(R.id.tv_text_unread, TOP, R.id.tv_text_icon, BOTTOM)
                            set.constrainCircle(R.id.tv_text_unread, R.id.tv_text_icon, 60, 35f)
                        }
                    }
                    set.applyTo(view)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthsize = MeasureSpec.getSize(widthMeasureSpec) //取出宽度的确切数值
        var widthmode = MeasureSpec.getMode(widthMeasureSpec) //取出宽度的测量模式

        val viewHeight = MeasureSpec.getSize(heightMeasureSpec) //取出高度的确切数值
        var heightmode = MeasureSpec.getMode(heightMeasureSpec) //取出高度的测量模式
        // 将 wrap_content 转换为 match_parent
        if (widthmode != MeasureSpec.EXACTLY && widthsize > 0) {
            widthmode = MeasureSpec.EXACTLY
        }
        if (heightmode != MeasureSpec.EXACTLY && viewHeight > 0) {
            heightmode = MeasureSpec.EXACTLY
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(widthsize, widthmode),
                MeasureSpec.makeMeasureSpec(viewHeight, heightmode))
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //绘制背景容器
        drawGroup(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val itemWidth = width / maxCount
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        when (event.action) {
            ACTION_DOWN -> {
                downTime = System.currentTimeMillis()
                downX = event.x
                downY = event.y
            }
            ACTION_MOVE -> {
                if (abs(downX - event.x) > touchSlop && abs(
                            downY - event.y) > touchSlop) isDrug = true
            }
            ACTION_UP -> {
                val upTime = System.currentTimeMillis()
                lastTouchTime = upTime
                for (i in 0 until childCount) {//循环点击位置
                    if (itemWidth * i < event.x && event.x < itemWidth * (i + 1)) {
                        synchronized(this) {
                            touchCount++
                        }
                        when {
                            isDrug -> {
                                drugClick?.invoke(i)
                                isDrug = false
                                touchCount = 0
                            }
                            upTime - downTime > longPressTime -> {
                                longClick?.invoke(i)
                                touchCount = 0
                            }
                            else -> Handler(Looper.getMainLooper()).postDelayed({
                                updateSelector(i)
                                if (upTime == lastTouchTime) {
                                    synchronized(this) {
                                        if (touchCount == 1L) click?.invoke(i)
                                        else multiClick?.invoke(i, touchCount)
                                        touchCount = 0
                                    }
                                }
                            }, continuousClickInterval)
                        }
                    }
                }
            }
        }
        return true
    }

    private fun addChildView(
        text: String, topDrawable: Drawable, fragment: Fragment, unread: Any? = null) {
        val constraintLayout = ConstraintLayout(context)
        constraintLayout.clipChildren = false
        when (itemMode) {
            ITEM_NORMAL -> {
                val tv = TextView(context)
                tv.id = R.id.tv_text_icon
                tv.tag = "textIcon"
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, topDrawable, null, null)
                tv.compoundDrawablePadding = drawPadding
                tv.text = text
                tv.textSize = textSize
                tv.gravity = Gravity.CENTER
                tv.isSelected = viewList.size == 0
                tv.setTextColor(if (viewList.size == 0) pressTextColor else normalTextColor)
                constraintLayout.addView(tv)
                addUnreadView(unread, constraintLayout)
            }
        }
        this.addView(constraintLayout)
        fragmentList.add(fragment)
        viewList.add(constraintLayout)
        maxCount = viewList.size
        requireNotNull(viewPager) { "You need to call the BNV.setViewPager() method first" }
        viewPager?.offscreenPageLimit = maxCount
        invalidate()
    }

    private fun addUnreadView(unread: Any?, constraintLayout: ConstraintLayout) {
        unread?.let {
            val rsTextView = RsTextView(context)
            rsTextView.id = R.id.tv_text_unread
            rsTextView.tag = "textUnread"
            rsTextView.setColor(Color.RED)
            rsTextView.setCornersRadius(10.dp2Px())
            rsTextView.setTextColor(Color.WHITE)
            val readText = when (it) {
                is Int -> if (it < 0) "" else it.toString()
                is String -> it
                else -> throw IllegalStateException("暂不支持此类型的未读消息")
            }
            if (readText == "") {
                rsTextView.textSize = 0.5f
                rsTextView.setPadding(3.dp2PxInt(), 2.dp2PxInt(), 2.dp2PxInt(), 2.dp2PxInt())
            } else {
                rsTextView.text = it.toString()
                rsTextView.textSize = 9f
                rsTextView.setPadding(3.dp2PxInt(), 0, 3.dp2PxInt(), 0)
            }
            constraintLayout.addView(rsTextView)
        }
    }

    private fun drawGroup(canvas: Canvas?) {
        val bezierHeight = -20//贝塞尔曲线最高点
        val bezierStart = width / 3 //120 在三分之一的地方开始贝塞尔
        paint.color = Color.WHITE
        paint.setShadowLayer(5f, 0f, -0.1f, Color.GRAY)//设置阴影
        paint.strokeWidth = 5f
        path.reset()
        path.moveTo(0f, 0f)
        //判断当前模式如果为贝塞尔曲线则进行绘制
        if (mode == MODE_BEZIER) {
            //顶线到圆弧位置 减30是为了弧度
            path.lineTo(bezierStart - 30, 0f)
            //一阶段二阶赛贝尔曲线
            //x1y1为控制点并非是画笔起始点 x2y2是结束点的xy轴点位
            //一阶段x2轴加了四分之一宽度那二阶段就要减四分之一
            //三阶段y1轴正向数值说明贝塞尔曲线弧度是向下
            path.quadTo(bezierStart + 30, 0f, bezierStart + bezierStart / 2 / 2,
                    bezierHeight.dp2Px() / 3 * 2)
            //二阶段二阶赛贝尔曲线
            path.quadTo(width / 2, bezierHeight.dp2Px() + bezierHeight.dp2Px() / 2,
                    bezierStart * 2 - bezierStart / 4, bezierHeight.dp2Px() / 3 * 2)
            //三阶段二阶赛贝尔曲线
            path.quadTo(bezierStart * 2 - 30, 0f, bezierStart * 2 + 30, 0f)
        }
        //绘制顶线
        path.lineTo(width, 0f)
        //绘制底线
        path.lineTo(width, height.toFloat())
        //绘制右边线
        path.lineTo(0f, height.toFloat())
        path.close()
        canvas?.drawPath(path, paint)
    }

    private fun updateSelector(position: Int) {
        if (viewPager == null) throw  IllegalStateException(
                "You need to call the BNV.setViewPager() method first")
        if (prePosition == position && isPrePosition) return
        viewList.forEach { cl ->
            cl.children.forEach { childView ->
                if (childView is TextView && childView.tag == "textIcon") {
                    childView.isSelected = false
                    childView.setTextColor(normalTextColor)
                }
            }
        }
        val cl = viewList[position]
        cl.children.forEach { childView ->
            if (childView is TextView && childView.tag == "textIcon") {
                childView.isSelected = true
                childView.setTextColor(pressTextColor)
            }
        }
        prePosition = position
        viewPager?.setCurrentItem(position, false)
        invalidate()
    }

    fun setOnClickListener(click: (position: Int) -> Unit) {
        this.click = click
    }

    fun setOnLongClickListener(longClick: (position: Int) -> Unit) {
        this.longClick = longClick
    }

    fun setOnMultiClickListener(multiClick: (position: Int, touchCount: Long) -> Unit) {
        this.multiClick = multiClick
    }

    fun setOnDrugListener(drugClick: (position: Int) -> Unit) {
        this.drugClick = drugClick
    }

    fun setMode(@BNVMode mode: Int): BNV {
        this.mode = mode
        return this
    }

    fun setLongPressTime(longPressTime: Int): BNV {
        this.longPressTime = longPressTime
        return this
    }

    fun setEnlarge(isEnlarge: Boolean): BNV {
        this.isEnlarge = isEnlarge
        return this
    }

    fun setPrePosition(isPrePosition: Boolean): BNV {
        this.isPrePosition = isPrePosition
        return this
    }

    fun setNormalTextColor(@NotNull @ColorRes normalTextColor: Int): BNV {
        this.normalTextColor = normalTextColor
        return this
    }

    fun setNormalTextColor(@NotNull normalTextColor: String): BNV {
        this.normalTextColor = Color.parseColor(normalTextColor)
        return this
    }

    fun setPressTextColor(@NotNull @ColorRes pressTextColor: Int): BNV {
        this.pressTextColor = pressTextColor
        return this
    }

    fun setPressTextColor(@NotNull pressTextColor: String): BNV {
        this.pressTextColor = Color.parseColor(pressTextColor)
        return this
    }

    fun setViewPager(viewPager: ViewPager2, isUserScroll: Boolean = false): BNV {
        this.viewPager = viewPager
        this.viewPager?.let {
            it.isUserInputEnabled = isUserScroll
            it.adapter = FragmentAdapter(context as FragmentActivity, fragmentList)
            /*it.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                }
            })*/
        }
        return this
    }

    fun setItemData(
        @NonNull text: String, @NonNull @DrawableRes selected: Int, @NonNull @DrawableRes normal: Int, @NotNull fragment: Fragment, unread: Any? = null): BNV {
        addChildView(text, context.addSelectedFromDrawable(selected, normal), fragment, unread)
        return this
    }
}
