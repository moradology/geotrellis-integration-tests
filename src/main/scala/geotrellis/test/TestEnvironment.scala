package geotrellis.test

import geotrellis.core.LayoutSchemeArg
import geotrellis.core.poly.{PolyWrite, PolyValidate, PolyCombine, PolyIngest}
import geotrellis.raster._
import geotrellis.spark.io._
import geotrellis.spark.io.avro.AvroRecordCodec
import geotrellis.spark.io.index.KeyIndexMethod
import geotrellis.spark.tiling.TilerKeyMethods
import geotrellis.spark._
import geotrellis.util.{S3Support, HadoopSupport, SparkSupport}
import geotrellis.vector.{ProjectedExtent, Extent}

import org.apache.spark.rdd.RDD
import org.joda.time.DateTime
import spray.json.JsonFormat
import shapeless._
import shapeless.poly._

import scala.reflect.ClassTag

abstract class TestEnvironment[
  I: ClassTag: ? => TilerKeyMethods[I, K]: Component[?, ProjectedExtent],
  K: SpatialComponent: Boundable: AvroRecordCodec: JsonFormat: ClassTag,
  V <: CellGrid: AvroRecordCodec: ClassTag
] extends SparkSupport with HadoopSupport with S3Support with Serializable {
  type M = TileLayerMetadata[K]

  // Poly functions cse types
  type Ingest      = String :: KeyIndexMethod[K] :: LayoutSchemeArg :: RDD[(I, V)] :: LayerWriter[LayerId] :: HNil
  type Validate    = TileLayerMetadata[K] :: String  :: LayerId :: Option[DateTime] :: ((LayerId, Option[Extent]) => RDD[(K, V)] with Metadata[M]) :: HNil
  type ValidateAux = (Option[Raster[V]], Option[Raster[V]], List[Raster[V]])
  type Combine     = LayerId :: RDD[(K, V)] with Metadata[M] :: HNil
  type OWrite      = Option[Raster[V]] :: String :: HNil
  type LWrite      = List[Raster[V]] :: String :: HNil

  val layerName: String
  val zoom: Int

  val writer: LayerWriter[LayerId]
  val reader: FilteringLayerReader[LayerId]
  val attributeStore: AttributeStore

  def loadTiles: RDD[(I, V)]

  def read(layerId: LayerId, extent: Option[Extent] = None): RDD[(K, V)] with Metadata[M] = {
    logger.info(s"reading ${layerId}...")
    extent.fold(reader.read[K, V, M](layerId))(e => reader.read[K, V, M](layerId,  new LayerQuery[K, M].where(Intersects(e))))
  }

  def ingest(layer: String, keyIndexMethod: KeyIndexMethod[K], lsa: LayoutSchemeArg = LayoutSchemeArg.default)
            (implicit pi: Case[PolyIngest.type, Ingest]): Unit = {
    conf.set("io.map.index.interval", "1")
    logger.info(s"ingesting tiles into accumulo (${layer})...")
    PolyIngest(layer, keyIndexMethod, lsa, loadTiles, writer)
  }

  def combine(layerId: LayerId)(implicit pc: Case[PolyCombine.type, Combine]) = {
    logger.info(s"combineLayer ${layerId}...")
    val rdd = read(layerId)
    PolyCombine(layerId, rdd)
  }

  def validate(layerId: LayerId, dt: Option[DateTime])
              (implicit pv: Case.Aux[PolyValidate.type, Validate, ValidateAux],
                        rw: Case[PolyWrite.type, OWrite],
                        lw: Case[PolyWrite.type, LWrite]): Unit = {
    val metadata = attributeStore.readMetadata[TileLayerMetadata[K]](layerId)
    val (ingestedRaster, expectedRasterResampled, diffRasters) =
      PolyValidate(metadata, mvValidationTiffLocal, layerId, dt, read _)

    PolyWrite(ingestedRaster, s"${validationDir}ingested.${this.getClass.getName}")
    PolyWrite(expectedRasterResampled, s"${validationDir}expected.${this.getClass.getName}")
    PolyWrite(diffRasters, s"${validationDir}diff.${this.getClass.getName}")
  }

  def ingest(keyIndexMethod: KeyIndexMethod[K])(implicit pi: Case[PolyIngest.type, Ingest]): Unit =
    ingest(layerName, keyIndexMethod)

  def combine()(implicit pc: Case[PolyCombine.type, Combine]): Unit = combine(LayerId(layerName, zoom))

  def validate(dt: Option[DateTime])
              (implicit pv: Case.Aux[PolyValidate.type, Validate, ValidateAux],
                        rw: Case[PolyWrite.type, OWrite],
                        lw: Case[PolyWrite.type, LWrite]): Unit =
    validate(LayerId(layerName, zoom), dt)
  
  def validate()(implicit pv: Case.Aux[PolyValidate.type, Validate, ValidateAux],
                          rw: Case[PolyWrite.type, OWrite],
                          lw: Case[PolyWrite.type, LWrite]): Unit = validate(None)
}
