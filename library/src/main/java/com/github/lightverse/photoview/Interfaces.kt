package com.github.lightverse.photoview

import android.view.MotionEvent

/**
* Created by lightverse on 2018/4/28.
*/

interface MotionEventDetector{
    fun onTouchEvent(event: MotionEvent):Boolean
}