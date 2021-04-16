package models.json

import io.circe.Printer
import io.circe.generic.extras.Configuration

trait CirceImplicits {
  implicit val customPrinter: Printer      = Printer.noSpaces.copy(dropNullValues = true)
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
}
