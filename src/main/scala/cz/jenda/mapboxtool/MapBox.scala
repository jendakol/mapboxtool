package cz.jenda.mapboxtool
import java.io.File
import java.nio.file.Files
import java.util.Locale

import cz.jenda.mapboxtool.MapBox._
import scalaj.http.Http
import xyz.hyperreal.polyline

import scala.language.postfixOps

class MapBox(apiKey: String) {
  def render(file: File)(center: LatLng,
                         width: Int,
                         height: Int,
                         zoom: Double,
                         pitch: Int,
                         pathPoints: Option[List[LatLng]],
                         mapPoints: Option[List[LatLng]]): Unit = {
    println {
      s"Requesting map render with ${mapPoints.map(_.size).getOrElse(0)} map points and ${pathPoints.map(_.size).getOrElse(0)} path points"
    }

    val polyLineStr = pathPoints
      .map(pp => urlEncode(polyline.encode(pp.map(ll => ll.lon -> ll.lat))))
      .map("path-5+f44-0.8(" + _ + ")")

    val pointsStr = mapPoints.map(_.map(ll => s"pin-s+ff6a00(${ll.lon},${ll.lat})"))

    val finalOverlayStr = (pointsStr.getOrElse(Nil) ++ polyLineStr).mkString(",")

    val centerStr = "%.5f,%.5f".formatLocal(Locale.US, center.lon, center.lat)

    val url = s"$rootUrl$finalOverlayStr/$centerStr,$zoom.0,0,$pitch/${width}x$height@2x?access_token=$apiKey"

    val resp = Http(url).asBytes

    if (!resp.is2xx) {
      println("Failed URL:" + url)
      sys.error(s"Map render failed with HTTP ${resp.statusLine}, msg: '${new String(resp.body)}'")
    }

    Files.write(file.toPath, resp.body)
  }
}

object MapBox {
  final val rootUrl: String = "https://api.mapbox.com/styles/v1/mapbox/satellite-streets-v10/static/"
}
