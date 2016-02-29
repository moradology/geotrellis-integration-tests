package geotrellis.test.file

import geotrellis.test.hadoop.HadoopLoad
import geotrellis.util.{HadoopSupport, S3Support}
import org.apache.spark.SparkContext

class HadoopIngestFileTests(@transient implicit val sc: SparkContext) extends FileTests with HadoopSupport with S3Support with HadoopLoad
