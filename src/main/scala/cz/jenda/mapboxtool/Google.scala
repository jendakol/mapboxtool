package cz.jenda.mapboxtool
import cz.jenda.mapboxtool.Google._
import io.circe.Json
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.circe.parser.parse
import scalaj.http.Http
import xyz.hyperreal.polyline

class Google(apiKey: String) {
  private implicit val circeConf: Configuration = Configuration.default.withSnakeCaseMemberNames.withSnakeCaseConstructorNames

  def requestRoutePoints(waypoints: Seq[String]): List[LatLng] = {
    require(waypoints.size >= 2, "There has to be at least two waypoints for the path")

    val origin = waypoints.head
    val destination = waypoints.last
    val through = waypoints.drop(1).dropRight(1)

    val waypointsStr = through.map(w => "via:" + urlEncode(w)).mkString("|")

    val url = s"$rootUrl?origin=${urlEncode(origin)}&destination=${urlEncode(destination)}&waypoints=$waypointsStr&key=$apiKey"

    println("Requesting Google Direction API")

    val json = Http(url).asString.body

    val cursor = parse(json).getOrElse(sys.error("Could not parse Google json")).hcursor

    val allPoints = cursor
      .downField("routes")
      .downArray
      .downField("legs")
      .downArray
      .get[List[Json]]("steps")
      .getOrElse(sys.error("Could not decode route steps"))
      .map(_.hcursor.downField("polyline").get[String]("points").getOrElse(sys.error("Could not decode polyline points")))

    allPoints.flatMap(polyline.decode).map(t => LatLng(t._2, t._1))
  }
}

object Google {
  final val rootUrl: String = "https://maps.googleapis.com/maps/api/directions/json"

  case class RouteStep(polyline: PolyLine)
  case class PolyLine(points: String)
}
