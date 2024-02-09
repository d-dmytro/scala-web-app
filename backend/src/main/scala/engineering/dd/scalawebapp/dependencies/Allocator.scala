package engineering.dd.scalawebapp.dependencies

import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.effect.unsafe.IORuntime

class Allocator(implicit runtime: IORuntime) {
  // Track resources to release using a ref:
  // release last IO[Unit] *> ... *> release first IO[Unit]
  private val resourceReleaseOps: Ref[IO, IO[Unit]] = Ref.unsafe(IO.unit)

  def allocate[A](resource: Resource[IO, A]): A =
    resource.allocated
      .flatMap { case (resource, release) =>
        resourceReleaseOps
          // Release latest resource then release prev then release prev ...
          // or in other words, release latest resource N <- ... <- release resource N - 1
          .update(otherReleaseOps => release *> otherReleaseOps)
          .as(resource)
      }
      .unsafeRunSync()

  def shutdownAll: IO[Unit] = resourceReleaseOps.getAndSet(IO.unit).flatten
}
