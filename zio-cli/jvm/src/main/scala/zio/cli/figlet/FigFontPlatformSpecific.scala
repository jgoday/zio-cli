package zio.cli.figlet

import zio._
import zio.IO.{attemptBlockingIO}

import java.io.IOException
import scala.io.{Codec, Source}

trait FigFontPlatformSpecific { self: FigFont.type =>
  final def fromFile(name: String): IO[Either[IOException, String], FigFont] =
    fromSource(attemptBlockingIO(Source.fromFile(name)(Codec.ISO8859)))

  final def fromResource(name: String, loader: ClassLoader): IO[Either[IOException, String], FigFont] =
    fromSource(attemptBlockingIO(Source.fromInputStream(loader.getResourceAsStream(name))))

  final def fromURL(url: String): IO[Either[IOException, String], FigFont] =
    fromSource(attemptBlockingIO(Source.fromURL(url)(Codec.ISO8859)))

  final def fromSource[R, A, E >: IOException](
    source: => ZIO[R, E, Source]
  ): ZIO[R, Either[E, String], FigFont] =
    for {
      lines <- ZManaged
                 .fromAutoCloseable(source)
                 .use(s => attemptBlockingIO(s.getLines().toSeq))
                 .mapError(Left(_))
      font <- ZIO
                .fromEither(self.fromLines(lines))
                .mapError(Right(_))
    } yield font
}
