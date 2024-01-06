package ru.stan.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


private const val TAG = "BoxDrawingView"

class BoxDrawingView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var currentBox: Box? = null
    private val boxes = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000.toInt()
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }

    private var pointer1Id: Int? = null
    private var pointer2Id: Int? = null
    private var initialAngle: Float = 0f


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentBox = Box(PointF(event.x, event.y)).also {
                    boxes.add(it)
                }
                pointer1Id = event.getPointerId(0)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) {
                    pointer2Id = event.getPointerId(1)
                    initialAngle = calculateAngle(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 2) {
                    val angle = calculateAngle(event)
                    currentBox?.let { box ->
                        box.end?.let { endPoint ->
                            val center = PointF((box.start.x + endPoint.x) / 2, (box.start.y + endPoint.y) / 2)
                            val newAngle = angle - initialAngle
                            rotateBox(box, center, newAngle)
                            invalidate()
                        }
                    }
                } else {
                    updateCurrentBox(PointF(event.x, event.y))
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                pointer1Id = null
                pointer2Id = null
                currentBox?.let { updateCurrentBox(PointF(event.x, event.y)) }
                currentBox = null
            }
        }
        return true
    }

    private fun calculateAngle(event: MotionEvent): Float {
        val x1 = event.getX(event.findPointerIndex(pointer1Id ?: 0))
        val y1 = event.getY(event.findPointerIndex(pointer1Id ?: 0))
        val x2 = event.getX(event.findPointerIndex(pointer2Id ?: 1))
        val y2 = event.getY(event.findPointerIndex(pointer2Id ?: 1))

        return Math.toDegrees(Math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()
    }

    private fun rotateBox(box: Box, center: PointF, angle: Float) {
        val start = box.start
        val end = box.end
        if (start != null && end != null) {
            val newStart = rotatePoint(start, center, angle)
            val newEnd = rotatePoint(end, center, angle)
            box.start = newStart
            box.end = newEnd
        }
    }

    private fun rotatePoint(point: PointF, center: PointF, angle: Float): PointF {
        val newX = center.x + (point.x - center.x) * cos(angle) - (point.y - center.y) * sin(angle)
        val newY = center.y + (point.x - center.x) * sin(angle) + (point.y - center.y) * cos(angle)
        return PointF(newX.toFloat(), newY.toFloat())
    }

    private fun updateCurrentBox(current: PointF) {
        currentBox?.let {
            it.end = current
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)

        boxes.forEach { box ->
            box.left?.let {
                box.top?.let { it1 ->
                    box.right?.let { it2 ->
                        box.bottom?.let { it3 ->
                            canvas.drawRect(
                                it,
                                it1, it2, it3, boxPaint
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.boxes = boxes.toList()
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        boxes.clear()
        boxes.addAll(state.boxes)
    }

    private class SavedState : BaseSavedState {
        var boxes: List<Box> = emptyList()

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            boxes = parcel.createTypedArrayList(Box.CREATOR) ?: emptyList()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeTypedList(boxes)
        }


        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}