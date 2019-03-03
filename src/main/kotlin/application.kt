import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ButtonBar
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import resize.ImageResizeService
import resize.ScalrResizeService
import tornadofx.*
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class DragView : View("Select Image") {
    private val widthProp: IntegerProperty = SimpleIntegerProperty(800)
    private val heightProp: IntegerProperty = SimpleIntegerProperty(400)
    private val topProp: FloatProperty = SimpleFloatProperty(0.5f)
    private val leftProp: FloatProperty = SimpleFloatProperty(0.5f)
    private val imageProp: ObjectProperty<File> = SimpleObjectProperty(null)

    override val root = vbox {
        alignment = Pos.CENTER
        prefWidth = 500.0
        borderpane {
            prefWidth = 500.0
            prefHeight = 500.0
            center = DropArea(topProp, leftProp, imageProp)
        }

        form {
            maxWidth = 500.0
            fieldset("Target sizes") {
                field("Width") {
                    textfield {
                        addClass(Styles.numberField)
                        bind(widthProp)
                        filterInput { it.controlNewText.isInt() }
                    }

                }

                field("Height") {
                    textfield {
                        addClass(Styles.numberField)
                        bind(heightProp)
                        filterInput { it.controlNewText.isInt() }
                    }
                    buttonbar {
                        button("Open image", ButtonBar.ButtonData.APPLY) {
                            action {
                                ImageFragment(
                                    widthProp.get(),
                                    heightProp.get(),
                                    topProp.get(),
                                    leftProp.get(), imageProp.get()
                                ).openModal(resizable = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

class DropArea(
    private val top: FloatProperty,
    private val left: FloatProperty,
    private val imageProp: ObjectProperty<File>
) : Label() {
    init {
        addClass(Styles.dropField)
        onDragEntered = dragEntered(this)
        onDragExited = dragExited(this)
        onDragDropped = dragDropped(this)
        onDragOver = dragOver()
        text = "DROP HERE"
    }

    private fun dragOver(): EventHandler<DragEvent> {
        return EventHandler { event ->
            if (isValidEvent(event)) {
                event.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
            }
            event.consume()
        }
    }

    private fun dragEntered(node: Node): EventHandler<DragEvent> {
        return EventHandler { event ->
            if (isValidEvent(event)) {
                node.addClass(Styles.active)
            }
            event.consume()
        }
    }

    private fun dragExited(node: Node): EventHandler<DragEvent> {
        return EventHandler { event ->
            node.removeClass(Styles.active)
            event.consume()
        }
    }

    private fun dragDropped(node: Node): EventHandler<DragEvent> {
        return EventHandler { event ->
            if (isValidEvent(event)) {
                val file: File = event.dragboard.files[0]
                imageProp.set(file)
                val imageView: ImageView = imageview {
                    image = Image(file.toURI().toASCIIString())
                    isPreserveRatio = true
                    useMaxWidth = true
                    maxWidth = 500.0
                    maxHeight = 500.0
                    fitWidth = 500.0
                    fitHeight = 500.0
                    setOnMouseClicked {

                        val aspectRatio = image.width / image.height
                        left.set((it.x / Math.min(fitWidth, fitHeight * aspectRatio)).toFloat())
                        top.set((it.y / Math.min(fitHeight, fitWidth / aspectRatio)).toFloat())
                    }
                }
                val aspectRatio = Bindings.divide(
                    imageView.image.widthProperty(),
                    imageView.image.heightProperty()
                )
                val circle: Circle = circle(0, 0, 5) {
                    addClass(Styles.selectorCircle)
                    isManaged = false
                    centerXProperty().bind(
                        Bindings.add(
                            imageView.layoutXProperty(),
                            Bindings.multiply(
                                left,
                                Bindings.min(
                                    imageView.fitWidthProperty(),
                                    Bindings.multiply(
                                        imageView.fitHeightProperty(),
                                        aspectRatio
                                    )
                                )
                            )
                        )
                    )
                    centerYProperty().bind(
                        Bindings.add(
                            imageView.layoutYProperty(),
                            Bindings.multiply(
                                top,
                                Bindings.min(
                                    imageView.fitHeightProperty(),
                                    Bindings.divide(
                                        imageView.fitWidthProperty(),
                                        aspectRatio
                                    )
                                )
                            )
                        )
                    )
                }
                node.replaceWith(
                    stackpane {
                        onDragOver = dragOver()
                        onDragEntered = dragEntered(imageView)
                        onDragExited = dragExited(imageView)
                        onDragDropped = dragDropped(this)
                        addChildIfPossible(imageView)
                        addChildIfPossible(circle)
                    }
                )
            }
        }
    }

    private fun isValidEvent(event: DragEvent): Boolean {
        return event.gestureSource == null &&
                event.dragboard.hasString() &&
                event.dragboard.hasFiles() &&
                event.dragboard.files.size == 1 &&
                isImage(event.dragboard.files[0])
    }

    // TODO: Optimize isImage check. Reading the image slows down big image.
    private fun isImage(file: File): Boolean {
        return try {
            ImageIO.read(file) != null
        } catch (e: IOException) {
            false
        }
    }
}

class ImageFragment(width: Int, height: Int, top: Float, left: Float, file: File) : Fragment() {
    private val imageResizeService: ImageResizeService = ScalrResizeService()
    override val root = hbox {
        imageview(
            SwingFXUtils.toFXImage(
                imageResizeService.cropImage(
                    ImageIO.read(file),
                    width,
                    height,
                    top,
                    left
                ), null
            )
        )
    }
}

class Styles : Stylesheet() {
    companion object {
        val selectorCircle by cssclass()
        val numberField by cssclass()
        val dropField by cssclass()
        val active by cssclass()

        val activePrimaryColor: Color = Color.GREEN
        val inactivePrimaryColor: Color = Color.GRAY
        val activeSecondaryColor: Color = Color.LIGHTGREEN
        val inactiveSecondaryColor: Color = Color.LIGHTGRAY
    }

    init {

        selectorCircle {
            fill = Color.TRANSPARENT
            stroke = Color.RED
            strokeWidth = 2.px
        }

        textField {
            and(numberField) {
                alignment = Pos.CENTER_RIGHT
                maxWidth = 60.0.px
            }
        }

        label {
            and(dropField) {
                backgroundColor = multi(inactiveSecondaryColor)
                borderColor = multi(box(inactivePrimaryColor))
                borderStyle = multi(BorderStrokeStyle.DASHED)
                borderWidth = multi(box(2.px))
                fontWeight = FontWeight.EXTRA_BOLD
                padding = box(20.px)
                textFill = inactivePrimaryColor

                and(active) {
                    backgroundColor = multi(activeSecondaryColor)
                    borderColor = multi(box(activePrimaryColor))
                    textFill = activePrimaryColor
                }
            }
        }

        Stylesheet.imageView {
            and(dropField, active) {
                opacity = 0.5
            }
        }
    }
}


class DragApp : App(DragView::class, Styles::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.minWidth = stage.width
        stage.minHeight = stage.height
    }
}

fun main(args: Array<String>) {
    launch<DragApp>(args)
}
