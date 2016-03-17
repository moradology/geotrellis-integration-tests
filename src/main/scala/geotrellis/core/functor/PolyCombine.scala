package geotrellis.core.functor

import geotrellis.raster.{MultibandTile, Tile}
import geotrellis.spark._
import geotrellis.spark.io.avro.AvroRecordCodec

import org.slf4j.{Logger, LoggerFactory}
import shapeless.Poly2
import spray.json.JsonFormat

import scala.reflect.ClassTag
import scala.util.Random

object PolyCombine extends Poly2 {
  @transient lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  implicit def singleband[K: SpatialComponent: Boundable: AvroRecordCodec: JsonFormat: ClassTag] =
    at[LayerId, TileLayerRDD[K]] { case (layerId, rdd) =>
      val crdd =
        (rdd union rdd)
          .map { case (k, v) => (k, (k, v)) }
          .combineByKey(createTiles[K, Tile], mergeTiles1[K, Tile], mergeTiles2[K, Tile])
          .map { case (key: K, seq: Seq[(K, Tile)]) =>
            val tiles = seq.map(_._2)
            key -> tiles(0).combine(tiles(1))(_ + _)
          }

      crdd.cache()

      val keys = crdd.keys.collect()
      val key = keys(Random.nextInt(keys.length))

      val ctile = crdd.lookup(key).map(_.toArray).head
      val tile = rdd.lookup(key).map(t => t.combine(t)(_ + _).toArray).head

      if (!ctile.sameElements(tile)) {
        logger.error(s"Incorrect combine layers ${layerId}")
        throw new Exception(s"Incorrect combine layers ${layerId}")
      }

      key
    }

  implicit def multiband[K: SpatialComponent: Boundable: AvroRecordCodec: JsonFormat: ClassTag] =
    at[LayerId, MultibandTileLayerRDD[K]] { case (layerId, rdd) =>
      val crdd =
        (rdd union rdd)
          .map { case (k, v) => (k, (k, v)) }
          .combineByKey(createTiles[K, MultibandTile], mergeTiles1[K, MultibandTile], mergeTiles2[K, MultibandTile])
          .map { case (key: K, seq: Seq[(K, MultibandTile)]) =>
            val tiles = seq.map(_._2)
            key -> tiles(0).band(0).combine(tiles(1).band(0))(_ + _)
          }

      crdd.cache()

      val keys = crdd.keys.collect()
      val key = keys(Random.nextInt(keys.length))

      val ctile = crdd.lookup(key).map(_.toArray).head
      val tile = rdd.lookup(key).map(t => t.band(0).combine(t.band(0))(_ + _).toArray).head

      if (!ctile.sameElements(tile)) {
        logger.error(s"Incorrect combine layers ${layerId}")
        throw new Exception(s"Incorrect combine layers ${layerId}")
      }

      key
    }
}
