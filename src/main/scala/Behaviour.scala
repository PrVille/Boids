import scalafx.geometry.Point2D
import scalafx.Includes._
import scala.collection.mutable.Buffer

trait Behaviour {

  //for boids to bounce from edges
  def applyEdgesBoid(boid: Boid, width: Double, height: Double) = {
    val margin = 50.0
    val turnFactor = 0.4
    if (boid.position.x <= margin)          boid.velocity = boid.velocity.add(turnFactor, 0.0)
    if (boid.position.x >= width - margin)  boid.velocity = boid.velocity.subtract(turnFactor, 0.0)
    if (boid.position.y <= margin)          boid.velocity = boid.velocity.add(0.0, turnFactor)
    if (boid.position.y >= height - margin) boid.velocity = boid.velocity.subtract(0.0, turnFactor)
    //Code for wrapping failsafe
    if (boid.position.x > width + 200)  boid.position = new Point2D(0, boid.position.y)
    if (boid.position.x < -200)         boid.position = new Point2D(width, boid.position.y)
    if (boid.position.y > height + 200) boid.position = new Point2D(boid.position.x, 0)
    if (boid.position.y < -200)         boid.position = new Point2D(boid.position.x, height)
  }

  //for predators to bounce from edges
  def applyEdgesPredator(predator: Predator, width: Double, height: Double) = {
    val margin = 50.0
    val turnFactor = 0.4
    if (predator.position.x <= margin)          predator.velocity = predator.velocity.add(turnFactor, 0.0)
    if (predator.position.x >= width - margin)  predator.velocity = predator.velocity.subtract(turnFactor, 0.0)
    if (predator.position.y <= margin)          predator.velocity = predator.velocity.add(0.0, turnFactor)
    if (predator.position.y >= height - margin) predator.velocity = predator.velocity.subtract(0.0, turnFactor)
    //Code for wrapping failsafe
    if (predator.position.x > width + 200)      predator.position = new Point2D(0, predator.position.y)
    if (predator.position.x < -200)             predator.position = new Point2D(width, predator.position.y)
    if (predator.position.y > height + 200)     predator.position = new Point2D(predator.position.x, 0)
    if (predator.position.y < -200)             predator.position = new Point2D(predator.position.x, height)
  }

  //boids seperate rule
  def separate(boid: Boid, neighbors: Array[Boid], separateFactor: Double, racist: Boolean) = {
    var force = Point2D.Zero
    var total = 0.0
    for (neighbor <- neighbors) {
      val distance = boid.position.distance(neighbor.position)
      if (boid != neighbor && distance <= 10.0) {
        var d = boid.position.subtract(neighbor.position)
        if (racist) {
          if (neighbor.color != boid.color) d = d.multiply(20.0)
        }
        force = force.add(d)
        total += 1.0
      }
    }
    boid.velocity = boid.velocity.add(force.multiply(separateFactor))
  }

  //boids alignment rule
  def align(boid: Boid, neighbors: Array[Boid], alignmentFactor: Double, radius: Double, racist: Boolean) = {
    var force = Point2D.Zero
    var total = 0.0
    for (neighbor <- neighbors) {
      val distance = boid.position.distance(neighbor.position)
      if (boid != neighbor && distance <= radius) {
        if (racist) {
          if (neighbor.color == boid.color) {
            force = force.add(neighbor.velocity)
            total += 1.0
          }
        } else {
          force = force.add(neighbor.velocity)
          total += 1.0
        }
      }
    }
    if (total > 0.0) {
      force = force.multiply((1.0/total))
      force = force.subtract(boid.velocity)
      force = force.multiply(alignmentFactor)
    }
    boid.velocity = boid.velocity.add(force)
  }

  //boids cohesion rule
  def cohesion(boid: Boid, neighbors: Array[Boid], cohesionFactor: Double, radius: Double, racist: Boolean) = {
    var force = Point2D.Zero
    var total = 0.0
    for (neighbor <- neighbors) {
      val distance = boid.position.distance(neighbor.position)
      if (boid != neighbor && distance <= radius) {
        if (racist) {
          if (neighbor.color == boid.color) {
            force = force.add(neighbor.position)
            total += 1.0
          }
        } else {
          force = force.add(neighbor.position)
          total += 1.0
        }
      }
    }
    if (total > 0.0) {
      force = force.multiply((1.0/total))
      force = force.subtract(boid.position)
      force = force.multiply(cohesionFactor)
    }
    boid.velocity = boid.velocity.add(force)
  }

  //predator seek behaviour
  def predatorPursue(neighbors: Array[Boid], predator: Predator, radius: Double) = {
    val prey = nearestPrey(neighbors, predator, radius * 4)
    if (prey.x > 0 && prey.y > 0 ) predator.velocity = predator.velocity.add(prey.subtract(predator.position))
  }

  //helper method for predatorPursue to find closest prey
  def nearestPrey(neighbors: Array[Boid], predator: Predator, radius: Double): Point2D = {
    var boids = Buffer[(Boid, Double)]()
    for (neighbor <- neighbors) {
      val distance = predator.position.distance(neighbor.position)
      if (distance <= radius) {
        boids += ((neighbor, distance))
      }
    }
    if (boids.nonEmpty) {
      boids.minBy(pair => pair._2)._1.position
    } else Point2D.Zero
  }

  //boids behaviour to flee from predator
  def avoidPredators(boid: Boid, predator: Predator, predatorRange: Double, boidDeath: Boolean) = {
    var predatorTurnFactor = 0.4
    val predatorCollisionRadius = predator.mass
    val boidCollisionRadius = boid.mass
    var force = Point2D.Zero
    var total = 0.0
    val distance = boid.position.distance(predator.position)
    if (boidDeath) {
      if (distance < predatorCollisionRadius + boidCollisionRadius) {
        boid.alive = false
      }
    }
    if (distance <= predatorRange) {
      force = force.add(predator.velocity.add(boid.position.subtract(predator.position)))
    }
    if (force.y > 0)
      boid.velocity = boid.velocity.add(0.0, predatorTurnFactor)
    if (force.y < 0)
      boid.velocity = boid.velocity.subtract(0.0, predatorTurnFactor)
    if (force.x > 0)
      boid.velocity = boid.velocity.add(predatorTurnFactor, 0.0)
    if (force.x < 0)
      boid.velocity = boid.velocity.subtract(predatorTurnFactor, 0.0)
  }

  //boids behaviour for avoiding obstacle
  def avoidObstacles(boid: Boid, obstacle: Obstacle, boidDeath: Boolean) = {
    val oPos = obstacle.center // object position center
    val m = obstacle.mass // object radius/mass
    val BRadius = boid.mass * 6 //Boid future collision sphere
    val ORadius = boid.mass //Boid collision sphere
    val futureLoc = boid.position.add(boid.velocity.normalize().multiply(60.0))
    val dist = boid.position.distance(oPos)
    val fDist = futureLoc.distance(oPos)
    val fCol = m*0.5 + BRadius
    if (dist < m*0.5 + ORadius) { //collision
      if (boidDeath) boid.alive = false
      boid.velocity = boid.velocity.add(boid.position.subtract(oPos)).multiply(0.5)
    }
    if (fDist <= fCol) { //future collision
      val distance = oPos.distance(boid.position.add(boid.velocity))
      val turnFactor = ((fCol*fCol)/distance)*0.0005
      //if steering in the verticle direction
      if (math.abs(boid.velocity.x) <= math.abs(boid.velocity.y)) {
        val steer = new Point2D((boid.position.x - oPos.x), boid.velocity.y).multiply(turnFactor)
        boid.velocity = boid.velocity.add(steer)
      } else { //if steering horizontal
        val steer = new Point2D(boid.velocity.x, (boid.position.y - oPos.y)).multiply(turnFactor)
        boid.velocity = boid.velocity.add(steer)
      }
    }
  }

  //for setting min and max speed
  def speedLimitBoid(boid: Boid, maxLimit: Double) = {
    val minSpeed = 2.0
    val maxSpeed = maxLimit
    val speed = boid.velocity.magnitude()
    if (speed > maxSpeed)
      boid.velocity = boid.velocity.multiply((1.0/speed)).multiply(maxSpeed)
    if (speed < minSpeed)
      boid.velocity = boid.velocity.multiply((1.0/speed)).multiply(minSpeed)
  }

  //for setting min and max speed
  def speedLimitPredator(predator: Predator, maxLimit: Double) = {
    val minSpeed = 1.0
    val maxSpeed = maxLimit
    val speed = predator.velocity.magnitude()
    if (speed > maxSpeed)
      predator.velocity = predator.velocity.multiply((1.0/speed)).multiply(maxSpeed)
    if (speed < minSpeed)
      predator.velocity = predator.velocity.multiply((1.0/speed)).multiply(minSpeed)
  }

}
