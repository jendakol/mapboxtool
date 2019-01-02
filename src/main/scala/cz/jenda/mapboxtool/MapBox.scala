package cz.jenda.mapboxtool
import java.io.File
import java.nio.file.Files
import java.util.Locale

import cz.jenda.mapboxtool.MapBox._
import scalaj.http.Http
import xyz.hyperreal.polyline

import scala.language.postfixOps

class MapBox(apiKey: String) {
  def render(file: File)(style: String,
                         center: LatLng,
                         width: Int,
                         height: Int,
                         zoom: Double,
                         pitch: Int,
                         pathPoints: Option[List[LatLng]],
                         mapPoints: Option[List[LatLng]]): Unit = {
    val polyLineStr = pathPoints
      .map(pp => urlEncode(polyline.encode(pp.map(ll => ll.lon -> ll.lat))))
      .map("path-5+f44-0.8(" + _ + ")")

    val pointsStr = mapPoints.map(_.map(ll => s"pin-s+ff6a00(${ll.lon},${ll.lat})"))

    val finalOverlayStr = (pointsStr.getOrElse(Nil) ++ polyLineStr).mkString(",")

    val centerStr = "%.5f,%.5f".formatLocal(Locale.US, center.lon, center.lat)

    val url = s"$rootUrl/$style/static/$finalOverlayStr/$centerStr,$zoom.0,0,$pitch/${width}x$height@2x?access_token=$apiKey"

    println {
      val mapPC = mapPoints.map(_.size).getOrElse(0)
      val pathPC = pathPoints.map(_.size).getOrElse(0)

      s"Requesting map render with $mapPC map points and $pathPC path points; URL $url"
    }

    val resp = Http(url).asBytes

    if (!resp.is2xx) {
      sys.error(s"Map render failed with HTTP ${resp.statusLine}, msg: '${new String(resp.body)}'")
    }

    Files.write(file.toPath, resp.body)
  }
}

object MapBox {
  final val rootUrl: String = "https://api.mapbox.com/styles/v1/mapbox"
}
