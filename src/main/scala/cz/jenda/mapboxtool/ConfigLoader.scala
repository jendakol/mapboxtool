package cz.jenda.mapboxtool

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader

object ConfigLoader {
  private implicit val r: ValueReader[GetRoutePoints] = configValueReader.map { config =>
    val directionsProvider = directions.DirectionsProvider(config.getString("provider"), config.getString("apiKey"))
    directionsProvider.getRoutePoints(config.getString("mode"))
  }

  private def mapReader(defaults: Config): ValueReader[MapDescription] = configValueReader.map { config =>
    config
      .withFallback(defaults)
      .withFallback(ConfigFactory.parseString("path.waypoints=[]"))
      .as[MapDescription](arbitraryTypeValueReader[MapDescription])
  }

  def load(file: String): AppConfiguration = {
    val config = ConfigFactory.parseFile(new File(file)).resolve()
    implicit val mapDescReader: ValueReader[MapDescription] = mapReader(config.getConfig("defaults"))

    config.as[AppConfiguration]
  }
}

case class AppConfiguration(mapBoxApiKey: String, maxPointsBase: Option[Int], maps: Seq[MapDescription])

case class MapDescription(fileName: String,
                          style: String,
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

case class Defaults(style: String, width: Int, height: Int, zoom: Double, pitch: Int, path: PathDefaults)

case class PathDefaults(directionsProvider: GetRoutePoints)
