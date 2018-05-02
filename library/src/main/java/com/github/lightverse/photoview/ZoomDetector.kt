package com.github.lightverse.photoview

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration

/**
 * Created by lightverse on 2018/4/27.
 */

class ZoomMotionDetector(configuration: ViewConfiguration): MotionEventDetector {

    val DEBUG = false

    private var mZoomAble = false

    private var mPointerCount:Int = 0
    set(value) {
        field = value
        mZoomAble = mPointerCount == 2
    }

    private val mDownRect = Rect(-1,-1,-1,-1)
    private val mTempRect = Rect(-1,-1,-1,-1)

    private var mIsBeingDragged = false
    private val mTouchSlop = configuration.scaledTouchSlop

    private val mTouchSlopSquare = mTouchSlop*mTouchSlop

    var mZoomMatrix = Matrix()

    private val mLastMatrix = Matrix()


    var mZoomChangeListener: OnZoomChangeListener? = null


    var zoomScaleValue = FloatArray(9)
    var lastScaleValue = FloatArray(9)
    var outScaleValue = FloatArray(9)


    private var mIsReseated = false //need this in case of mLastMatrix set multi

    override fun onTouchEvent(event:MotionEvent):Boolean{
        mPointerCount = event.pointerCount
        val result = event.action and  MotionEvent.ACTION_MASK
        if(mIsBeingDragged && ((event.action == MotionEvent.ACTION_CANCEL) || (event.action == MotionEvent.ACTION_UP)
                || (result == MotionEvent.ACTION_POINTER_UP))){
            mZoomChangeListener?.onZoomEnd()
            resetData()
            return true
        }
        if(!mZoomAble) return false

        //暂时先考虑最前面两个点
        //重置数据，但保留矩阵
        if(mDownRect.left < 0 || mDownRect.top < 0){
                mDownRect.left = event.getX(0).toInt();mDownRect.top = event.getY(0).toInt()}
        if(mDownRect.right < 0 || mDownRect.bottom < 0){
                mDownRect.right = event.getX(1).toInt();mDownRect.bottom = event.getY(1).toInt()}

        mTempRect.set(
                event.getX(0).toInt(),
                event.getY(0).toInt(),
                event.getX(1).toInt(),
                event.getY(1).toInt()
        )


        var handler = false
        val firstDeltaX = mTempRect.left - mDownRect.left
        val firstDeltaY = mTempRect.top - mDownRect.top

        val secondDeltaX = mTempRect.right - mDownRect.right
        val secondDeltaY = mTempRect.bottom - mDownRect.bottom

        checkTouchSlop(firstDeltaX,firstDeltaY,secondDeltaX,secondDeltaY)
        if(mIsBeingDragged){
            mIsReseated = false
            //make the matrix

            val scale = Math.sqrt( mTempRect.diagonalSquare().toDouble()/mDownRect.diagonalSquare()).toFloat()

            //focus center of the beginning always
            val transX = - Math.abs(mDownRect.centerX())*(scale - 1)
            val transY = - Math.abs(mDownRect.centerY())*(scale - 1)

            mZoomMatrix.reset()
            mZoomMatrix.setScale(scale,scale)
            mZoomMatrix.postTranslate(transX,transY)

            mZoomMatrix.getValues(zoomScaleValue)
            mLastMatrix.getValues(lastScaleValue)

            Log.e("ZoomMotionDetector","zoom=${zoomScaleValue[2]}\n" +
                    "last=${lastScaleValue[2]}\n" +
                    "out=${outScaleValue[2]}")

            mZoomChangeListener?.onZoomChange()
            handler = true
        }

        return handler
    }

    private fun resetData() {
        if(!mIsReseated){
            mIsReseated = true
            mDownRect.set(-1,-1,-1,-1)
            mZoomAble = false
            mIsBeingDragged = false
            mLastMatrix.set(mZoomMatrix)
            mZoomMatrix.reset()
            mLastMatrix.getValues(lastScaleValue)
       }
    }


    private fun checkTouchSlop(x1:Int,y1:Int,x2:Int,y2:Int){
        if(mIsBeingDragged) return
        if((x1*x1 + y1*y1) > mTouchSlopSquare || (x2*x2 + y2*y2) > mTouchSlopSquare){
            mIsBeingDragged = true
            //重设DownX
            mDownRect.set(mTempRect)
        }
    }

    interface OnZoomChangeListener{
        fun onZoomStart()
        fun onZoomChange()
        fun onZoomEnd()
    }

    //only can be called if isBeingDragged
    fun getTransPointFByScale(scale:Float,inPointF: PointF? = null):PointF?{
        if(!mIsBeingDragged){
            return null
        }
        val transX = - Math.abs(mDownRect.centerX())*(scale - 1)
        val transY = - Math.abs(mDownRect.centerY())*(scale - 1)
        inPointF?.set(transX,transY)
        return inPointF?:PointF(transX,transY)
    }

}

fun Rect.diagonalSquare():Int{
    val width = Math.abs(width())
    val height = Math.abs(height())
   return width*width + height*height
}

interface SimpleZoomChangeListener: ZoomMotionDetector.OnZoomChangeListener {
    override fun onZoomStart() {}

    override fun onZoomChange() {}

    override fun onZoomEnd() {}
}


