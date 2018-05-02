package com.github.lightverse.photoview

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewConfiguration

/**
 * Created by lightverse on 2018/4/28.
 *
 * version 1: only focus move event
 */
class TranslateDetector(configuration: ViewConfiguration): MotionEventDetector {

    private var mMoveAble = false

    private var mPointerCount:Int = 0
        set(value) {
            field = value
            mMoveAble = mPointerCount == 1
        }
    private val mTouchSlop = configuration.scaledTouchSlop

    private var mIsBeingDragged = false

    private val mDownPoint = PointF(-1f,-1f)
    private val mTempPoint = PointF(-1f,-1f)

    private var mIsReseated = false //need this in case of mLastMatrix set multi

    var mTranslateMatrix = Matrix()

    var mTranslateChangeListener: OnTranslateChangeListener? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val lastPointerCount = mPointerCount
        mPointerCount = event.pointerCount
        if(lastPointerCount != mPointerCount){
            if(mPointerCount > 1){
                endTranslate()
            }
            resetData()
        }
        if(!mMoveAble) return false

        var handler = false
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                mDownPoint.set(event.x,event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                if(mDownPoint.x < 0|| mDownPoint.y < 0){
                    mDownPoint.set(event.x,event.y)
                }
                mTempPoint.set(event.x,event.y)
                checkTouchSlop(mTempPoint.x - mDownPoint.x,mTempPoint.y - mDownPoint.y)
                if(mIsBeingDragged){
                    if(mIsReseated){
                        mIsReseated = false
                    }
                    mTranslateMatrix.setTranslate((mTempPoint.x - mDownPoint.x),mTempPoint.y - mDownPoint.y)
                    mTranslateChangeListener?.onTranslate()
                    handler = true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if(mIsBeingDragged){
                    endTranslate()
                    handler = true
                }
                resetData()
            }
            MotionEvent.ACTION_UP -> {
                if(mIsBeingDragged){
                    endTranslate()
                    handler = true
                }
                resetData()
            }

        }

        return handler
    }

    private fun checkTouchSlop(deltaX:Float,deltaY:Float){
        if(mIsBeingDragged) return
        if(Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop){
            mIsBeingDragged = true
            //重设DownPoint
            mDownPoint.offset(deltaX,deltaY)
        }
    }

    private fun endTranslate(){
        if(!mIsReseated){
            mTranslateChangeListener?.onTranslateEnd()
        }
    }

    private fun resetData() {
        if(!mIsReseated){
            mDownPoint.set(-1f,-1f)
            mTranslateMatrix.reset()
            mIsBeingDragged = false
            mIsReseated = true
        }
    }


    interface OnTranslateChangeListener{
        fun onTranslateStart()
        fun onTranslate()
        fun onTranslateEnd()
    }
}

interface SimpleTranslateChangeListener: TranslateDetector.OnTranslateChangeListener {
    override fun onTranslateStart() {}

    override fun onTranslate() {}

    override fun onTranslateEnd() {}
}