package cz.jenda.mapboxtool.directions

import cz.jenda.mapboxtool.directions.Google._
import cz.jenda.mapboxtool.{urlEncode, LatLng}
import io.circe.Json
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.circe.parser.parse
import scalaj.http.Http
import xyz.hyperreal.polyline

class Google(apiKey: String) extends DirectionsProvider {
  private implicit val circeConf: Configuration = Configuration.default.withSnakeCaseMemberNames.withSnakeCaseConstructorNames

  def getRoutePoints(mode: String)(waypoints: Seq[String]): List[LatLng] = {
    require(waypoints.size >= 2, "There has to be at least two waypoints for the path")

    val origin = waypoints.head
    val destination = waypoints.last
    val through = waypoints.drop(1).dropRight(1)

    val waypointsStr = through.map(urlEncode).mkString("|")

    val url = s"$rootUrl?mode=$mode&origin=${urlEncode(origin)}&destination=${urlEncode(destination)}&waypoints=$waypointsStr&key=$apiKey"

    println(s"Requesting Google Direction API, URL $url")

    val jsonStr = Http(url).asString.body

    val cursor = parse(jsonStr).getOrElse(sys.error("Could not parse Google json")).hcursor

    cursor.get[String]("status").getOrElse(sys.error("Could not decode response JSON")) match {
      case "OK" => // ok
      case status =>
        sys.error(s"Wrong JSON returned, status $status:\n$jsonStr")
    }

    val allPoints = cursor
      .downField("routes")
      .downArray
      .get[List[Json]]("legs")
      .getOrElse(sys.error(s"Could not decode route legs; response:\n$jsonStr"))
      .flatMap {
        _.hcursor
          .get[List[Json]]("steps")
          .getOrElse(throw new RuntimeException(s"Could not decode route steps; response:\n$jsonStr"))
      }
      .map(_.hcursor.downField("polyline").get[String]("points").getOrElse(sys.error("Could not decode polyline points")))

    allPoints.flatMap(polyline.decode).map(t => LatLng(t._2, t._1))
  }
}

object Google {
  final val rootUrl: String = "https://maps.googleapis.com/maps/api/directions/json"

  case class RouteStep(polyline: PolyLine)
  case class PolyLine(points: String)
}
