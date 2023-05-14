@file:Suppress("UNUSED_PARAMETER") // <- REMOVE
package galaxyraiders.core.physics

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties("unit", "normal", "degree", "magnitude")
data class Vector2D(val dx: Double, val dy: Double) {
  override fun toString(): String {
    return "Vector2D(dx=$dx, dy=$dy)"
  }

  val magnitude: Double
    get() = Math.sqrt(dx * dx + dy * dy)

  val radiant: Double
    get() = Math.atan2(dy, dx)

  val degree: Double
    get() = radiant * 180 / Math.PI

  val unit: Vector2D
    get() = Vector2D(dx, dy) / magnitude

  val normal: Vector2D
    get() = Vector2D(dy, -dx) / magnitude

  operator fun times(scalar: Double): Vector2D {
    val resultVector = Vector2D(dx * scalar, dy * scalar)
    return resultVector
  }

  operator fun div(scalar: Double): Vector2D {
    val resultVector = Vector2D(dx / scalar, dy / scalar)
    return resultVector
  }

  operator fun times(v: Vector2D): Double {
    val result: Double = dx * v.dx + dy * v.dy
    return result
  }

  operator fun plus(v: Vector2D): Vector2D {
    val resultVector = Vector2D(dx + v.dx, dy + v.dy)
    return resultVector
  }

  operator fun plus(p: Point2D): Point2D {
    val resultPoint = Point2D(p.x + dx, p.y + dy)
    return resultPoint
  }

  operator fun unaryMinus(): Vector2D {
    val resultVector = Vector2D(-dx, -dy)
    return resultVector
  }

  operator fun minus(v: Vector2D): Vector2D {
    val resultVector = Vector2D(dx - v.dx, dy - v.dy)
    return resultVector
  }

  fun scalarProject(target: Vector2D): Double {
    val result: Double = times(target) / target.magnitude
    return result
  }

  fun vectorProject(target: Vector2D): Vector2D {
    val ontoXAxis: Boolean = (target.dx != 0.0 && target.dy == 0.0)
    val ontoYAxis: Boolean = (target.dx == 0.0 && target.dy != 0.0)

    val resultVector: Vector2D
    if (ontoXAxis) {
      resultVector = Vector2D(dx, 0.0)
    } else if (ontoYAxis) {
      resultVector = Vector2D(0.0, dy)
    } else {
      resultVector = target * (scalarProject(target) / target.magnitude)
    }

    return resultVector
  }
}

operator fun Double.times(v: Vector2D): Vector2D {
  val resultVector = v.times(toDouble())
  return resultVector
}
