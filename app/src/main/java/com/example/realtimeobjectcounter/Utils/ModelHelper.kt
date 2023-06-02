package com.example.realtimeobjectcounter.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.realtimeobjectcounter.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class ModelHelper {

    private val imageProcessor: ImageProcessor =
        ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
    private val paint = Paint()
    private var colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )

    data class Result(val bitmap: Bitmap, val names: MutableSet<String>, val count: Int)

    fun getObjectsCount(
        labels: List<String>,
        model: SsdMobilenetV11Metadata1,
        bitmap: Bitmap
    ): Result {

        // Setup paint
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5.0f

        // Creates inputs for reference.
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        // Runs model inference and gets result.
        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray[0].toInt()

        var objectCount = 0
        for (i in 0 until numberOfDetections) {
            if (scores[i] > 0.5) {
                objectCount++
            }
        }

        // Mutable bitmap
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Pass mutable bitmap to canvas
        val canvas = Canvas(mutable)

        // Image height and width
        val h = mutable.height
        val w = mutable.width

        // Set text size in image
        paint.textSize = h / 15f
        // Set stroke width in image
        paint.strokeWidth = h / 85f

        // create an empty set to store unique strings
        val uniqueStrings = mutableSetOf<String>()

        var objectName = ""

        scores.forEachIndexed { index, fl ->
            if (fl > 0.5) {
                var x = index
                x *= 4

                objectName = labels[classes[index].toInt()]
                uniqueStrings.add(objectName)

                paint.color = colors[index]
                paint.style = Paint.Style.STROKE

                // Drawing shape on image
                canvas.drawRect(
                    RectF(
                        locations[x + 1] * w,
                        locations[x] * h,
                        locations[x + 3] * w,
                        locations[x + 2] * h
                    ), paint
                )
                paint.style = Paint.Style.FILL
                canvas.drawText(
                    labels[classes[index].toInt()],
                    locations[x + 1] * w,
                    locations[x] * h,
                    paint
                )
            }
        }

        return Result(mutable, uniqueStrings, objectCount)

    }

}



