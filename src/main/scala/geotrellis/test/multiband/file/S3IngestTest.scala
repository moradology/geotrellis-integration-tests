package geotrellis.test.multiband.file

import geotrellis.raster.MultibandTile
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.test.FileTest
import geotrellis.test.multiband.load.S3Load
import geotrellis.vector.ProjectedExtent

import org.apache.spark.SparkContext

abstract class S3IngestTest extends FileTest[ProjectedExtent, SpatialKey, MultibandTile] with S3Load

object S3IngestTest {
  def apply(implicit _sc: SparkContext) = new S3IngestTest {
    @transient implicit val sc = _sc
  }
}
