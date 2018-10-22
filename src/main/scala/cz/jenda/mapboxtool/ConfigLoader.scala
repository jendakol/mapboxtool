package cz.jenda.mapboxtool

import java.io.File

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader

object ConfigLoader {
  implicit val r: ValueReader[GetRoutePoints] = configValueReader.map { config =>
    val directionsProvider = directions.DirectionsProvider(config.getString("provider"), config.getString("apiKey"))
    directionsProvider.getRoutePoints(config.getString("mode"))
  }

  def load(file: String): AppConfiguration = {
    ConfigFactory.parseFile(new File(file)).resolve().as[AppConfiguration]
  }
}

case class AppConfiguration(mapBoxApiKey: String, maxPointsBase: Option[Int], maps: Seq[MapDescription])

case class MapDescription(fileName: String,
                          width: Int,
                          height: Int,
                          center: LatLng,
                          zoom: Double,
                          pitch: Int,
                          pointsFilterModulo: Option[Int],
                          path: Option[PathConfig],
                          mapPoints: Option[List[MapPoint]])

case class PathConfig(directionsProvider: GetRoutePoints, waypoints: List[String])

case class MapPoint(name: String, coords: LatLng)
