package geotrellis.core.poly

import geotrellis.proj4.WebMercator
import geotrellis.raster.io.geotiff.GeoTiff
import geotrellis.raster.{MultibandTile, Raster, Tile}

import shapeless.Poly2
import scalaz.Functor
import scalaz.Scalaz._

object PolyWrite extends Poly2 {
  implicit def singleband[F[_]: Functor] = at[F[Raster[Tile]], String] { case (raster, dir) =>
    raster.map(GeoTiff(_, WebMercator).write(s"${dir}.tiff"))
    raster.map(_.tile.renderPng().write(s"${dir}.png"))
  }

  implicit def multiband[F[_]: Functor] = at[F[Raster[MultibandTile]], String] { case (raster, dir) =>
    raster.map(GeoTiff(_, WebMercator).write(s"${dir}.tiff"))
  }
}
