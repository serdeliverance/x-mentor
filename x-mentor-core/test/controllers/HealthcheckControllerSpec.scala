package controllers

import io.circe.syntax._
import models.dtos.responses.StatusResponseDTO
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import play.api.test._

class HealthcheckControllerSpec extends PlaySpec {

  private val healthcheckController = new HealthcheckController(Helpers.stubControllerComponents())

  "HealthcheckController" should {
    "return ok" in {
      // given
      val request = FakeRequest()

      // when
      val result = healthcheckController.healthcheck().apply(request)

      // then
      val expectedJson = StatusResponseDTO().asJson.noSpaces

      contentAsString(result) mustBe expectedJson
      contentType(result) mustBe Some("application/json")
      status(result) mustBe 200
    }
  }
}
