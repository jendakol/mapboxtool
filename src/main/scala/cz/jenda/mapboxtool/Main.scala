package cz.jenda.mapboxtool
import java.io.File

import io.circe.generic.extras.auto._

import scala.language.postfixOps
import scala.util.control.NonFatal

object Main extends App {
  val configFile = args.headOption.getOrElse(sys.error("Missing config file path!"))
  val configuration = ConfigLoader.load(configFile)

  val google = new Google(configuration.googleApiKey)
  val mapBox = new MapBox(configuration.mapBoxApiKey)

  val maxPoints = configuration.maxPointsBase.getOrElse(450) // see docs for the magic constants...

  configuration.maps.foreach { mapConfig =>
    import mapConfig._

    try {
      println(s"Processing map: $mapConfig")

      val points = pathPoints.map(google.requestRoutePoints)

      val filteredPoints = points.map { allPoints =>
        val filterModulo = pointsFilterModulo.getOrElse {
          val mapPointsCount = mapPoints.map(_.size).getOrElse(0)

          math.ceil(allPoints.size / (maxPoints - mapPointsCount).toDouble).toInt
        }

        allPoints.zipWithIndex.filter(_._2 % filterModulo == 0).map(_._1)
      }

      println(s"Rendering map to $fileName")

      mapBox.render(new File(fileName))(center, width, height, zoom, pitch, filteredPoints, mapPoints.map(_.map(_.coords)))
    } catch {
      case NonFatal(e) =>
        println(s"Processing of map to $fileName failed")
        e.printStackTrace()
    }
  }

  sys.exit(0)
}
