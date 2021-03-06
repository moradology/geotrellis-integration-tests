package geotrellis.test.multiband.file

import geotrellis.raster.MultibandTile
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.test.FileTest
import geotrellis.test.multiband.load.HadoopLoad
import geotrellis.vector.ProjectedExtent

import org.apache.spark.SparkContext

abstract class HadoopIngestTest extends FileTest[ProjectedExtent, SpatialKey, MultibandTile] with HadoopLoad

object HadoopIngestTest {
  def apply(implicit _sc: SparkContext) = new HadoopIngestTest {
    @transient implicit val sc = _sc
  }
}
