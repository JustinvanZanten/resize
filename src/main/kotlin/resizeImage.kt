import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import resize.ImageResizeService
import resize.ScalrResizeService
import tornadofx.*

class ResizeImageView : View("ResizeImage") {
    private val resizeService: ImageResizeService = ScalrResizeService()
    override val root = borderpane {
        center {
            imageview(
                SwingFXUtils.toFXImage(
                    resizeService.cropImage(
                        SwingFXUtils.fromFXImage(Image(resources["coffee.jpg"]), null),
                        400,
                        200,
                        0.5f,
                        0.5f
                    ), null
                )
            )
        }
    }
}

class ResizeImageApp : App(ResizeImageView::class, Styles::class)

fun main(args: Array<String>) {
    launch<ResizeImageApp>(args)
}
