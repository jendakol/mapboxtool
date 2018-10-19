package cz.jenda.mapboxtool

import java.io.File

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

object ConfigLoader {
  def load(file: String): AppConfiguration = {
    ConfigFactory.parseFile(new File(file)).resolve().as[AppConfiguration]
  }
}

case class AppConfiguration(googleApiKey: String, mapBoxApiKey: String, maxPointsBase: Option[Int], maps: Seq[MapDescription])

case class MapDescription(fileName: String,
                          width: Int,
                          height: Int,
                          center: LatLng,
                          zoom: Double,
                          pitch: Int,
                          pointsFilterModulo: Option[Int],
                          pathPoints: Option[List[String]],
                          mapPoints: Option[List[MapPoint]])

case class MapPoint(name: String, coords: LatLng)
