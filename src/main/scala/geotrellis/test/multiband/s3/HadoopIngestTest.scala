package geotrellis.test.multiband.s3

import geotrellis.raster.MultibandTile
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.test.S3Test
import geotrellis.test.multiband.load.HadoopLoad
import geotrellis.vector.ProjectedExtent

import org.apache.spark.SparkContext

abstract class HadoopIngestTest extends S3Test[ProjectedExtent, SpatialKey, MultibandTile] with HadoopLoad

object HadoopIngestTest {
  def apply(implicit _sc: SparkContext) = new HadoopIngestTest {
    @transient implicit val sc = _sc
  }
}
