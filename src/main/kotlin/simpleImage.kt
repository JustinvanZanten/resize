import javafx.scene.image.Image
import tornadofx.*

class SimpleImageView : View("Image") {
    override val root = borderpane {
        center {
            imageview(Image(resources["coffee.jpg"]))
        }
    }
}

class SimpleImageApp : App(SimpleImageView::class, Styles::class)

fun main(args: Array<String>) {
    launch<SimpleImageApp>(args)
}
