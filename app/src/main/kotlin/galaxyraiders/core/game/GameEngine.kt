package galaxyraiders.core.game

import galaxyraiders.Config
import galaxyraiders.ports.RandomGenerator
import galaxyraiders.ports.ui.Controller
import galaxyraiders.ports.ui.Controller.PlayerCommand
import galaxyraiders.ports.ui.Visualizer
import kotlin.system.measureTimeMillis
import java.io.File
import kotlin.math.roundToInt
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.Scanner

const val MILLISECONDS_PER_SECOND: Int = 1000

object GameEngineConfig {
  private val config = Config(prefix = "GR__CORE__GAME__GAME_ENGINE__")

  val frameRate = config.get<Int>("FRAME_RATE")
  val spaceFieldWidth = config.get<Int>("SPACEFIELD_WIDTH")
  val spaceFieldHeight = config.get<Int>("SPACEFIELD_HEIGHT")
  val asteroidProbability = config.get<Double>("ASTEROID_PROBABILITY")
  val coefficientRestitution = config.get<Double>("COEFFICIENT_RESTITUTION")

  val msPerFrame: Int = MILLISECONDS_PER_SECOND / this.frameRate
}

@Suppress("TooManyFunctions")
class GameEngine(
  val generator: RandomGenerator,
  val controller: Controller,
  val visualizer: Visualizer,
) {
  val field = SpaceField(
    width = GameEngineConfig.spaceFieldWidth,
    height = GameEngineConfig.spaceFieldHeight,
    generator = generator
  )

  // app/src/main/kotlin/galaxyraiders/core/score/
  var scoreboardFile = File("./src/main/kotlin/galaxyraiders/core/score/Scoreboard.json")
  var leaderboardFile = File("./src/main/kotlin/galaxyraiders/core/score/Leaderboard.json")
  var scoreboardFormatBefore: String = ""
  var scoreboardFormatAfter: String = ""
  var leaderboardFormatBefore: String = ""
  var leaderboardFormatAfter: String = ""

  var playing = true

  var formattedDate: String = ""
  var score: Int = 0
  var asteroidsDestroyed: Int = 0

  var leaderboardDate1: String = ""
  var leaderboardScore1: Int = 0
  var leaderboardDestroyed1: Int = 0
  var leaderboardDate2: String = ""
  var leaderboardScore2: Int = 0
  var leaderboardDestroyed2: Int = 0
  var leaderboardDate3: String = ""
  var leaderboardScore3: Int = 0
  var leaderboardDestroyed3: Int = 0

  var currentLeaderboardPosition: Int = 4

  fun execute() {
    setupScoreFiles()

    while (true) {
      val duration = measureTimeMillis { this.tick() }

      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
    }
  }

  fun execute(maxIterations: Int) {
    repeat(maxIterations) {
      this.tick()
    }
  }

  fun setupScoreFiles() {
    val isScoreboardCreated: Boolean = scoreboardFile.createNewFile()
    val isLeaderboardCreated: Boolean = leaderboardFile.createNewFile()

    val currentDate = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    formattedDate = currentDate.format(formatter)

    setupScoreboard(isScoreboardCreated)
    setupLeaderboard(isLeaderboardCreated)
  }

  fun setupScoreboard(isNewFile: Boolean) {
    if (isNewFile) {
      scoreboardFormatBefore += "{\n    \"games\": [\n"
      scoreboardFormatAfter += "    ]\n}"

      val scoreboardFormatNow = makeJsonScoreFormat(formattedDate, score, asteroidsDestroyed) + "\n"

      scoreboardFile.writeText(scoreboardFormatBefore + scoreboardFormatNow + scoreboardFormatAfter)
    } else {
      val sc = Scanner(scoreboardFile)

      while (sc.hasNextLine()) {
        val line = sc.nextLine()
        if (!line.startsWith("    ]") && !line.startsWith("}")) {
          scoreboardFormatBefore += line + "\n"
        } else {
          scoreboardFormatAfter += line + "\n"
        }
      }
      scoreboardFormatBefore = scoreboardFormatBefore.dropLast(1)
      scoreboardFormatAfter = scoreboardFormatAfter.dropLast(1)
      scoreboardFormatBefore += ",\n"

      val scoreboardFormatNow = makeJsonScoreFormat(formattedDate, score, asteroidsDestroyed) + "\n"

      scoreboardFile.writeText(scoreboardFormatBefore + scoreboardFormatNow + scoreboardFormatAfter)
    }
  } 

  fun setupLeaderboard(isNewFile: Boolean) {
    leaderboardFormatBefore += "{\n    \"games\": [\n"
    leaderboardFormatAfter += "\n    ]\n}"

    if (isNewFile) {
      leaderboardDate1 = formattedDate
      leaderboardDate2 = formattedDate
      leaderboardDate3 = formattedDate
      val leaderboardFormatNow1: String = makeJsonScoreFormat(formattedDate, 0, 0)
      val leaderboardFormatNow2: String = makeJsonScoreFormat(formattedDate, 0, 0)
      val leaderboardFormatNow3: String = makeJsonScoreFormat(formattedDate, 0, 0)

      leaderboardFile.writeText(leaderboardFormatBefore + leaderboardFormatNow1 + ",\n" + leaderboardFormatNow2 + ",\n" + leaderboardFormatNow3 + leaderboardFormatAfter)
    } else {
      val sc = Scanner(leaderboardFile)

      while (sc.hasNextLine()) {
        val line = sc.nextLine()
        if (line.startsWith("            \"date_time\": ")) {
          leaderboardDate1 = leaderboardDate2
          leaderboardDate2 = leaderboardDate3
          leaderboardDate3 = line.removePrefix("            \"date_time\": \"").dropLast(2)
        } else if (line.startsWith("            \"score\": ")) {
          leaderboardScore1 = leaderboardScore2
          leaderboardScore2 = leaderboardScore3
          leaderboardScore3 = line.removePrefix("            \"score\": ").dropLast(1).toInt()
        } else if (line.startsWith("            \"asteroids_destroyed\": ")) {
          leaderboardDestroyed1 = leaderboardDestroyed2
          leaderboardDestroyed2 = leaderboardDestroyed3
          leaderboardDestroyed3 = line.removePrefix("            \"asteroids_destroyed\": ").toInt()
        }
      }
    }
  }

  fun addScore(mass: Double, velocity: Double) {
    val doubleScore: Double = 200 * mass + 300 * velocity
    score += doubleScore.roundToInt()
    asteroidsDestroyed += 1
    updateScoreboard()
    updateLeaderboard()
  }

  fun updateScoreboard() {
    val scoreboardFormatNow = makeJsonScoreFormat(formattedDate, score, asteroidsDestroyed) + "\n"
    scoreboardFile.writeText(scoreboardFormatBefore + scoreboardFormatNow + scoreboardFormatAfter)
  }

  fun updateLeaderboard() {
    if (currentLeaderboardPosition == 4 && score > leaderboardScore3) {
      leaderboardDate3 = formattedDate
      leaderboardScore3 = score
      leaderboardDestroyed3 = asteroidsDestroyed

      currentLeaderboardPosition = 3
    }
    if (currentLeaderboardPosition == 3) {
      leaderboardDate3 = formattedDate
      leaderboardScore3 = score
      leaderboardDestroyed3 = asteroidsDestroyed

      if (score > leaderboardScore2) {
        leaderboardDate3 = leaderboardDate2
        leaderboardScore3 = leaderboardScore2
        leaderboardDestroyed3 = leaderboardDestroyed2

        leaderboardDate2 = formattedDate
        leaderboardScore2 = score
        leaderboardDestroyed2 = asteroidsDestroyed

        currentLeaderboardPosition = 2
      }
    }
    if (currentLeaderboardPosition == 2) {
      leaderboardDate2 = formattedDate
      leaderboardScore2 = score
      leaderboardDestroyed2 = asteroidsDestroyed

      if (score > leaderboardScore1) {
        leaderboardDate2 = leaderboardDate1
        leaderboardScore2 = leaderboardScore1
        leaderboardDestroyed2 = leaderboardDestroyed1

        leaderboardDate1 = formattedDate
        leaderboardScore1 = score
        leaderboardDestroyed1 = asteroidsDestroyed

        currentLeaderboardPosition = 1
      }
    }
    if (currentLeaderboardPosition == 1) {
      leaderboardDate1 = formattedDate
      leaderboardScore1 = score
      leaderboardDestroyed1 = asteroidsDestroyed
    }

    val leaderboardFormatNow1 = makeJsonScoreFormat(leaderboardDate1, leaderboardScore1, leaderboardDestroyed1) + ",\n"
    val leaderboardFormatNow2 = makeJsonScoreFormat(leaderboardDate2, leaderboardScore2, leaderboardDestroyed2) + ",\n"
    val leaderboardFormatNow3 = makeJsonScoreFormat(leaderboardDate3, leaderboardScore3, leaderboardDestroyed3)
    leaderboardFile.writeText(leaderboardFormatBefore + leaderboardFormatNow1 + leaderboardFormatNow2 + leaderboardFormatNow3 + leaderboardFormatAfter)
  }

  fun tick() {
    this.processPlayerInput()
    this.updateSpaceObjects()
    this.renderSpaceField()
  }

  fun processPlayerInput() {
    this.controller.nextPlayerCommand()?.also {
      when (it) {
        PlayerCommand.MOVE_SHIP_UP ->
          this.field.ship.boostUp()
        PlayerCommand.MOVE_SHIP_DOWN ->
          this.field.ship.boostDown()
        PlayerCommand.MOVE_SHIP_LEFT ->
          this.field.ship.boostLeft()
        PlayerCommand.MOVE_SHIP_RIGHT ->
          this.field.ship.boostRight()
        PlayerCommand.LAUNCH_MISSILE ->
          this.field.generateMissile()
        PlayerCommand.PAUSE_GAME ->
          this.playing = !this.playing
      }
    }
  }

  fun updateSpaceObjects() {
    if (!this.playing) return
    this.handleCollisions()
    this.moveSpaceObjects()
    this.trimSpaceObjects()
    this.generateAsteroids()
  }

  fun handleCollisions() {
    this.field.spaceObjects.forEachPair {
        (first, second) ->
      if (first.impacts(second)) {
        first.collideWith(second, GameEngineConfig.coefficientRestitution)

        if (first.symbol == '^' && second.symbol == '.') {
          val asteroid: Asteroid = second as Asteroid
          this.field.explodeAsteroid(asteroid)
          addScore(asteroid.mass, asteroid.velocity.magnitude)
        }
      }
    }
  }

  fun moveSpaceObjects() {
    this.field.moveShip()
    this.field.moveAsteroids()
    this.field.moveMissiles()
    this.field.moveExplosions()
  }

  fun trimSpaceObjects() {
    this.field.trimAsteroids()
    this.field.trimMissiles()
    this.field.trimExplosions()
  }

  fun generateAsteroids() {
    val probability = generator.generateProbability()

    if (probability <= GameEngineConfig.asteroidProbability) {
      this.field.generateAsteroid()
    }
  }

  fun renderSpaceField() {
    this.visualizer.renderSpaceField(this.field)
  }

  private fun makeJsonScoreFormat(displayDateTime: String, displayScore: Int, displayDestroyed: Int): String {
    var scoreFormat: String = "        {\n"
    scoreFormat += "            \"date_time\": \"" + displayDateTime + "\",\n            \"score\": "
    scoreFormat += displayScore.toString() + ",\n            \"asteroids_destroyed\": " + displayDestroyed.toString() + "\n        }"
    return scoreFormat
  }
}

fun <T> List<T>.forEachPair(action: (Pair<T, T>) -> Unit) {
  for (i in 0 until this.size) {
    for (j in i + 1 until this.size) {
      action(Pair(this[i], this[j]))
    }
  }
}
