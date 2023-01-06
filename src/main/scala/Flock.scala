import scalafx.geometry.Point2D
import scalafx.scene.paint.Color._
import scalafx.Includes._
import scala.util.Random

class Flock(width: Double, height: Double) {

  private var flock = Array[Boid]()
  private val colors = Vector(Black, Gray, Brown, Orange) //common colors in birds

  def getFlock = flock

  def addRandomBoids(amount: Int, obstacle: Option[Obstacle]) = {
    val maxAmount = 500
    if (flock.length <= maxAmount - amount) {
      val randoms = Seq.fill(amount)(createRandomBoid(obstacle)).toArray
      flock = flock ++ randoms
    } else {
      val randoms = Seq.fill((maxAmount - flock.length))(createRandomBoid(obstacle)).toArray
      flock = flock ++ randoms
    }
  }

  def removeDeadBoid(boid: Boid) = {
    flock = flock.filter(_ != boid)
  }

  def removeRandomBoids(amount: Int) = {
    val toTake = flock.length - amount
    flock = flock.take(toTake)
  }

  def fillRandoms(amount: Int, obstacle: Option[Obstacle]) = {
    flock = Seq.fill(amount)(createRandomBoid(obstacle)).toArray
  }

  def resetFlock(obstacle: Option[Obstacle]) = {
    val boidAmount = flock.length
    flock = Seq.fill(boidAmount)(createRandomBoid(obstacle)).toArray
  }

  private def createRandomBoid(obstacle: Option[Obstacle]): Boid = {
    //add check for obstacles
    val position = (new Point2D(Random.between(0.0, width), Random.between(0.0, height)))
    val velocity = (new Point2D(Random.between(-1.0,1.0), Random.between(-1.0, 1.0))).normalize().multiply(Random.between(2.0, 3.0))
    val color = Random.shuffle(colors).head
    val mass = 5.0
    val boid = new Boid(position, velocity, color, true, mass)
    if (obstacle.isDefined) {
      val check = spawnCheck(obstacle.get, boid)
      if (check) createRandomBoid(obstacle)
      else boid
    } else boid
  }

  def spawnCheck(obstacle: Obstacle, boid: Boid) = {
    val boidCollisionRadius = boid.mass + 1.0
    val minX = obstacle.center.x - obstacle.mass / 2
    val maxX = obstacle.center.x + obstacle.mass / 2
    val minY = obstacle.center.y - obstacle.mass / 2
    val maxY = obstacle.center.y - obstacle.mass / 2
    if ((boid.position.x > minX && boid.position.x < maxX) || (boid.position.y > minY && boid.position.y < maxY)) true
    else false
  }

}
