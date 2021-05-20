package controllers

import models.{Career, CareerRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CareerController @Inject()(cc: MessagesControllerComponents, careerRepository: CareerRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val careerForm: Form[CreateCareerForm] = Form {
    mapping(
      "position" -> nonEmptyText,
      "description" -> nonEmptyText,
    )(CreateCareerForm.apply)(CreateCareerForm.unapply)
  }

  val updateCareerForm: Form[UpdateCareerForm] = Form {
    mapping(
      "id" -> longNumber,
      "position" -> nonEmptyText,
      "description" -> nonEmptyText,
    )(UpdateCareerForm.apply)(UpdateCareerForm.unapply)
  }

  def getCareers: Action[AnyContent] = Action.async { implicit request =>
    val fetchedCareers = careerRepository.list()
    fetchedCareers.map(careers => Ok(views.html.careers(careers)))
  }

  def removeCareer(id: Long): Action[AnyContent] = Action {
    careerRepository.delete(id)
    Redirect("/getcareers")
  }

  def updateCareer(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val produkt = careerRepository.getById(id)
    produkt.map(career => {
      val prodForm = updateCareerForm.fill(UpdateCareerForm(career.id, career.position, career.description))
      Ok(views.html.careerupdate(prodForm))
    })
  }

  def updateCareerHandle(): Action[AnyContent] = Action.async { implicit request =>
    updateCareerForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.careerupdate(errorForm))
        )
      },
      career => {
        careerRepository.update(career.id, Career(career.id, career.position, career.description)).map { _ =>
          Redirect(routes.CareerController.updateCareer(career.id)).flashing("success" -> "career updated")
        }
      }
    )

  }

  def addCareer(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.careeradd(careerForm))
  }

  def addCareerHandle(): Action[AnyContent] = Action.async { implicit request =>
    careerForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.careeradd(errorForm))
        )
      },
      career => {
        careerRepository.create(career.position, career.description).map { _ =>
          Redirect(routes.CareerController.addCareer()).flashing("success" -> "career created")
        }
      }
    )

  }

  def addCareerJson(): Action[AnyContent] = Action.async { implicit request =>
    val position = request.body.asJson.get("position").as[String]
    val description = request.body.asJson.get("description").as[String]

    careerRepository.create(position, description).map { career =>
      Ok(Json.toJson(career))
    }
  }

  def updateCareerJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val position = request.body.asJson.get("position").as[String]
    val description = request.body.asJson.get("description").as[String]

    careerRepository.update(id, Career(id,position, description)).map { career =>
      Ok("Career updated")
    }
  }

  def getCareerJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    careerRepository.getById(id).map { career =>
      Ok(Json.toJson(career))
    }
  }

  def getCareersJson: Action[AnyContent] = Action.async { implicit request =>
    careerRepository.list().map { careers =>
      Ok(Json.toJson(careers))
    }
  }

  def removeCareerJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    careerRepository.delete(id).map { _ =>
      Ok("Career removed")
    }
  }

}

case class CreateCareerForm(position: String, description: String)

case class UpdateCareerForm(id: Long, position: String, description: String)
