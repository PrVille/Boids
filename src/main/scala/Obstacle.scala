import scalafx.geometry.Point2D

class Obstacle(val position: Point2D, val mass: Double) {

  def center = new Point2D(position.x + (mass / 2), position.y + (mass / 2))

}
