package engineering.dd.scalawebapp.invoice

import cats.effect.IO
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.typelevel.log4cats.LoggerFactory

object InvoiceRoutes {
  def make(invoiceService: InvoiceService)(implicit
      loggerFactory: LoggerFactory[IO]
  ): HttpRoutes[IO] = HttpRoutes.of[IO] { case req @ GET -> Root / "invoices" =>
    for {
      body <- req.as[String]
      _ <- loggerFactory.getLogger.warn("getting invoices...")
      invoices <- invoiceService.getAllInvoices
      res <- Ok(invoices.asJson.noSpaces)
    } yield res
  }
}
