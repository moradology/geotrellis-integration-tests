package geotrellis.test.singleband.load

import geotrellis.raster.Tile
import geotrellis.spark.{SpaceTimeKey, TemporalProjectedExtent}
import geotrellis.spark.etl.s3.TemporalGeoTiffS3Input
import geotrellis.test.TestEnvironment

import org.apache.spark.rdd.RDD

trait TemporalS3Load { self: TestEnvironment[TemporalProjectedExtent, SpaceTimeKey, Tile] =>
  val layerName: String = "s3Ingest"
  val zoom: Int = 8

  def loadTiles: RDD[(TemporalProjectedExtent, Tile)] = {
    logger.info("loading tiles from s3...")
    val s3Input = new TemporalGeoTiffS3Input()
    s3Input(s3Params)
  }
}
