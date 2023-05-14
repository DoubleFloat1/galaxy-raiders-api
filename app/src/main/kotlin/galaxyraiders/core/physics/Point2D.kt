@file:Suppress("UNUSED_PARAMETER") // <- REMOVE
package galaxyraiders.core.physics

data class Point2D(val x: Double, val y: Double) {
  operator fun plus(p: Point2D): Point2D {
    val resultPoint = Point2D(x + p.x, y + p.y)
    return resultPoint
  }

  operator fun plus(v: Vector2D): Point2D {
    val resultPoint = Point2D(x + v.dx, y + v.dy)
    return resultPoint
  }

  override fun toString(): String {
    return "Point2D(x=$x, y=$y)"
  }

  fun toVector(): Vector2D {
    val resultVector = Vector2D(x, y)
    return resultVector
  }

  fun impactVector(p: Point2D): Vector2D {
    val resultVector = Vector2D(Math.abs(x - p.x), Math.abs(y - p.y))
    return resultVector
  }

  fun impactDirection(p: Point2D): Vector2D {
    val resultVector: Vector2D = impactVector(p)
    return resultVector
  }

  fun contactVector(p: Point2D): Vector2D {
    val resultVector: Vector2D = impactVector(p).normal
    return resultVector
  }

  fun contactDirection(p: Point2D): Vector2D {
    val resultVector: Vector2D = contactVector(p)
    return resultVector
  }

  fun distance(p: Point2D): Double {
    val result: Double = Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y))
    return result
  }
}
