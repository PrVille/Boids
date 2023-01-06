import scalafx.geometry.Point2D
import scalafx.Includes._
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color._
import scala.util.Random


class Simulation(width: Double, height: Double) extends Behaviour {

  private var predatorOption: Option[Predator] = None
  private var obstacleOption: Option[Obstacle] = None

  private var obstacleCheck = false
  private var predatorCheck = false
  private var allowDeadBoids = false
  private var turboMode = false
  private var autoSpawn = false
  private var racismCheck = false

  private var sizeFactor = 1.0
  private var speedLimit = 3.0
  private var deadBoidAmount = 0

  private val f = new Flock(width, height)
  f.fillRandoms(150, obstacleOption)

  def getBoidSize = sizeFactor

  def getDeadBoidAmount = deadBoidAmount

  def getBoidSpeed = speedLimit

  def getBoidAmount = f.getFlock.length

  def obstacle = obstacleOption.get

  def predator = predatorOption.get

  def toggleDeadBoids() = {
    if (!allowDeadBoids)
      allowDeadBoids = true
    else {
      allowDeadBoids = false
    }
  }

  def toggleAutoSpawn() = {
    if (!autoSpawn)
      autoSpawn = true
    else
      autoSpawn = false
  }

  def toggleTurboMode() = {
    if (!turboMode)
      turboMode = true
    else
      turboMode = false
  }

  def toggleRacism() = {
    if (!racismCheck)
      racismCheck = true
    else
      racismCheck = false
  }

  def addObstacle() = {
    obstacleOption = Some(createObstacle())
    obstacleCheck = true
  }

  def removeObstacle() = {
    obstacleOption = None
    obstacleCheck = false
  }

  def addPredator() = {
    predatorOption = Some(createPredator())
    predatorCheck = true
  }

  def removePredator() = {
    predatorOption = None
    predatorCheck = false
  }

  def removeRandoms(amount: Int) = f.removeRandomBoids(amount)

  def addRandoms(amount: Int) = f.addRandomBoids(amount, obstacleOption)

  def increaseSize() = if (sizeFactor < 2.0) sizeFactor += 0.1

  def decreaseSize() = if (sizeFactor > 0.51) sizeFactor -= 0.1

  def increaseSpeed() = if (speedLimit < 6.0) speedLimit += 0.2

  def decreaseSpeed() = if (speedLimit > 1.1) speedLimit -= 0.2

  def resetFlock() = f.resetFlock(obstacleOption)

  def createObstacle() = {
    val position = new Point2D(Random.between(100.0, width - 300.0), Random.between(100.0, height - 300.0))
    val mass = Random.between(25.0, 150.0)
    new Obstacle(position, mass)
  }

  def createPredator() = {
    val position = (new Point2D(Random.between(0.0, width), Random.between(0.0, height)))
    val velocity = (new Point2D(Random.between(-1.0,1.0), Random.between(-1.0, 1.0))).normalize().multiply(Random.between(2.0, 3.0))
    val mass = 5.0
    new Predator(position, velocity, Red, mass)
  }

  def update(separationFactor: Double, alignmentFactor: Double, cohesionFactor: Double, radius: Double) = {
    for (boid <- f.getFlock) {
      separate(boid, f.getFlock, separationFactor/10.0, racismCheck)
      align(boid, f.getFlock, alignmentFactor/10.0, radius, racismCheck)
      cohesion(boid, f.getFlock, cohesionFactor/250.0, radius, racismCheck)
      if (predatorCheck) avoidPredators(boid, predator, radius, allowDeadBoids)
      if (obstacleCheck) avoidObstacles(boid, obstacle, allowDeadBoids)
      speedLimitBoid(boid, speedLimit)
      applyEdgesBoid(boid, width, height)
      if (allowDeadBoids) {
        if (!boid.alive) {
          f.removeDeadBoid(boid)
          deadBoidAmount += 1
          if (autoSpawn) f.addRandomBoids(1, obstacleOption)
        }
      }
      boid.position = boid.position.add(boid.velocity)
    }
    if (predatorCheck) {
      if (turboMode) {
        predatorPursue(f.getFlock, predator, radius * 5)
        speedLimitPredator(predator, speedLimit + 5.0)
      }
      else {
        predatorPursue(f.getFlock, predator, radius)
        speedLimitPredator(predator, speedLimit - 0.9)
      }

      applyEdgesPredator(predator, width, height)
      predator.position = predator.position.add(predator.velocity)
    }
  }

  def draw(g: GraphicsContext) = {
    for (boid <- f.getFlock) {
      var angle = Math.atan2(boid.velocity.getY, boid.velocity.getX).toDegrees
      g.translate(boid.position.getX, boid.position.getY)
      g.rotate(angle)
      g.translate(-boid.position.getX, -boid.position.getY)
      g.fill = boid.color
      g.beginPath()
      g.moveTo(boid.position.getX, boid.position.getY)
      g.lineTo(boid.position.getX - (7.5 * sizeFactor), boid.position.getY + (2.5 * sizeFactor))
      g.lineTo(boid.position.getX - (7.5 * sizeFactor), boid.position.getY - (2.5 * sizeFactor))
      g.lineTo(boid.position.getX, boid.position.getY)
      g.closePath()
      g.stroke()
      g.fill()
      g.setTransform(1, 0, 0, 1, 0, 0)
      //Uncomment to draw collision spheres for obstacle avoidance
      /**
      val futureLoc = boid.position.add(boid.velocity.normalize().multiply(60.0))
      g.strokeOval(boid.position.x - boid.mass, boid.position.y - boid.mass, boid.mass * 2, boid.mass * 2)
      g.strokeOval(futureLoc.getX - 20.0, futureLoc.getY -20.0, 20.0 * 2, 20.0 *2)
      g.beginPath()
      g.moveTo(boid.position.x, boid.position.y)
      g.lineTo(futureLoc.getX, futureLoc.getY)
      g.closePath()
      g.stroke()
      */

    }
    if (predatorCheck) {
      var angle = Math.atan2(predator.velocity.getY, predator.velocity.getX).toDegrees
      g.translate(predator.position.getX, predator.position.getY)
      g.rotate(angle)
      g.translate(-predator.position.getX, -predator.position.getY)
      g.fill = predator.color
      g.beginPath()
      g.moveTo(predator.position.getX, predator.position.getY)
      g.lineTo(predator.position.getX - (7.5 * sizeFactor * 2), predator.position.getY + (2.5 * sizeFactor * 2))
      g.lineTo(predator.position.getX - (7.5 * sizeFactor * 2), predator.position.getY - (2.5 * sizeFactor * 2))
      g.lineTo(predator.position.getX, predator.position.getY)
      g.closePath()
      g.stroke()
      g.fill()
      g.setTransform(1, 0, 0, 1, 0, 0)
      //Uncomment for collision sphere
      //g.strokeOval(predator.position.x - predator.mass, predator.position.y - predator.mass, predator.mass * 2, predator.mass * 2)
    }
    if (obstacleCheck) {
      g.fill = Red
      g.fillOval(obstacle.position.x, obstacle.position.y, obstacle.mass, obstacle.mass)
      g.fill = Black
      g.strokeOval(obstacle.position.x, obstacle.position.y, obstacle.mass, obstacle.mass)

    }
  }

}
