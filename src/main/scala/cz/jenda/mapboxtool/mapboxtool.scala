package cz.jenda
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

package object mapboxtool {

  case class LatLng(lat: Double, lon: Double)

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, StandardCharsets.UTF_8.name())
  }
}
