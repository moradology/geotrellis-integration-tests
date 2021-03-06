package geotrellis.test.multiband.s3

import geotrellis.raster.MultibandTile
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.test.S3Test
import geotrellis.test.multiband.load.S3Load
import geotrellis.vector.ProjectedExtent

import org.apache.spark.SparkContext

abstract class S3IngestTest extends S3Test[ProjectedExtent, SpatialKey, MultibandTile] with S3Load

object S3IngestTest {
  def apply(implicit _sc: SparkContext) = new S3IngestTest {
    @transient implicit val sc = _sc
  }
}
