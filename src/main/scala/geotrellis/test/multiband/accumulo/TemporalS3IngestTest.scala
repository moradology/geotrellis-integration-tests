package geotrellis.test.multiband.accumulo

import geotrellis.raster.MultibandTile
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.test.AccumuloTest
import geotrellis.test.multiband.load.TemporalS3Load

import org.apache.spark.SparkContext

abstract class TemporalS3IngestTest extends AccumuloTest[TemporalProjectedExtent, SpaceTimeKey, MultibandTile] with TemporalS3Load

object TemporalS3IngestTest {
  def apply(implicit _sc: SparkContext) = new TemporalS3IngestTest {
    @transient implicit val sc = _sc
  }
}
