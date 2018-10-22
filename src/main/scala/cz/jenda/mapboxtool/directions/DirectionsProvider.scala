package cz.jenda.mapboxtool.directions
import cz.jenda.mapboxtool.LatLng

trait DirectionsProvider {
  def getRoutePoints(mode: String)(waypoints: Seq[String]): List[LatLng]
}

object DirectionsProvider {
  def apply(provider: String, key: String): DirectionsProvider = provider.toLowerCase match {
    case "google" => new Google(key)
  }
}
