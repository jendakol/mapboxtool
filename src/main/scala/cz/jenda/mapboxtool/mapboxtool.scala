package cz.jenda
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

package object mapboxtool {

  type GetRoutePoints = Seq[String] => List[LatLng]

  case class LatLng(lat: Double, lon: Double)

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, StandardCharsets.UTF_8.name())
  }
}
