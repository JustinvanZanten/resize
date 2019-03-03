package resize

import org.imgscalr.Scalr
import java.awt.image.BufferedImage

class ScalrResizeService : ImageResizeService {
    override fun cropImage(image: BufferedImage, width: Int, height: Int, top: Float, left: Float): BufferedImage {
        if (
            width <= 0 ||
            height <= 0 ||
            top < 0 ||
            top > 1 ||
            left < 0 ||
            left > 1
        ) {
            throw Exception("Invalid input")
        }

        val originalWidth = image.width
        val originalHeight = image.height
        val widthRatio = originalWidth.toDouble() / width
        val heightRatio = originalHeight.toDouble() / height

        val subImage: BufferedImage
        when {
            widthRatio < heightRatio -> {
                val scaledHeight = (height * widthRatio).toInt()
                val gravityPoint = (originalHeight * top).toInt()
                val offset = Math.min(Math.max(gravityPoint - scaledHeight / 2, 0), originalHeight - scaledHeight)
                subImage = image.getSubimage(0, offset, originalWidth, scaledHeight)
            }
            widthRatio > heightRatio -> {
                val scaledWidth = (width * heightRatio).toInt()
                val gravityPoint = (originalWidth * left).toInt()
                val offset = Math.min(Math.max(gravityPoint - scaledWidth / 2, 0), originalWidth - scaledWidth)
                subImage = image.getSubimage(offset, 0, scaledWidth, originalHeight)
            }
            else ->
                subImage = image.getSubimage(0, 0, originalWidth, originalHeight)
        }
        return Scalr.resize(subImage, Scalr.Method.ULTRA_QUALITY, width, height)
    }
}
