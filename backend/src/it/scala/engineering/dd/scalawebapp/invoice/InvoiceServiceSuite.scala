package engineering.dd.scalawebapp.invoice

import engineering.dd.scalawebapp.IntegrationTestSuite

class InvoiceServiceSuite extends IntegrationTestSuite {
  test("sample test") {
    dependenciesFixture().invoiceService.getAllInvoices.assertEquals(List())
  }
}
