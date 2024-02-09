package engineering.dd.scalawebapp.invoice

import cats.effect.IO
import doobie.*
import doobie.implicits.*

trait InvoiceService {
  def getAllInvoices: IO[List[Invoice]]
}

class InvoiceServiceImpl(xa: Transactor[IO]) extends InvoiceService {
  def getAllInvoices: IO[List[Invoice]] =
    sql"select id, amount from invoices".query[Invoice].to[List].transact(xa)
}
