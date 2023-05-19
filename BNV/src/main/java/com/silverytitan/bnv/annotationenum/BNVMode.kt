package com.silverytitan.bnv.annotationenum

import androidx.annotation.IntDef
import com.silverytitan.bnv.common.MODE_BEZIER
import com.silverytitan.bnv.common.MODE_NORMAL

@Retention(AnnotationRetention.SOURCE) @IntDef(MODE_NORMAL, MODE_BEZIER) annotation class BNVMode
