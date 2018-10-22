package cz.jenda.mapboxtool.directions
import cz.jenda.mapboxtool.LatLng
import OpenRouteService._
import io.circe.parser.parse
import scalaj.http.Http
import xyz.hyperreal.polyline

class OpenRouteService(apiKey: String) extends DirectionsProvider {
  override def getRoutePoints(mode: String)(waypoints: Seq[String]): List[LatLng] = {
    val coordinatesStr = waypoints.mkString("%7C")
    val url = s"$rootUrl?api_key=$apiKey&coordinates=$coordinatesStr&profile=$mode&geometry_format=encodedpolyline"

    println(s"Requesting OpenRoute Service Direction API, URL $url")

    val resp = Http(url).asString

    if (!resp.is2xx) sys.error(s"OpenRoute Service request has failed, status ${resp.statusLine}, resp:\n${resp.body}")

    val jsonStr = resp.body

    val cursor = parse(jsonStr).getOrElse(sys.error("Could not parse ORS json")).hcursor

    polyline
      .decode {
        cursor.downField("routes").downArray.get[String]("geometry").getOrElse(sys.error("Could not extract polyline from ORS response"))
      }
      .map(t => LatLng(t._2, t._1))
  }
}

object OpenRouteService {
  val rootUrl = "https://api.openrouteservice.org/directions"
}
