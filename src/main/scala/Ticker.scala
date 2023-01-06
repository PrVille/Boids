import javafx.animation.AnimationTimer

class Ticker(function: () => Unit) extends AnimationTimer {

    override def handle(now: Long): Unit = {function()}

}