package resize

import java.awt.image.BufferedImage

interface ImageResizeService {
    fun cropImage(image: BufferedImage, width: Int, height: Int, top: Float, left: Float): BufferedImage
}
