package com.silverytitan.bnv.annotationenum

import androidx.annotation.IntDef
import com.silverytitan.bnv.common.ITEM_IV_AND_TV
import com.silverytitan.bnv.common.ITEM_NORMAL

@Retention(AnnotationRetention.SOURCE) @IntDef(ITEM_NORMAL, ITEM_IV_AND_TV)
annotation class BNVItemMode
