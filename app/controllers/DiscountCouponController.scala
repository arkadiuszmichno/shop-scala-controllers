package controllers

import models.DiscountCouponRepository
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiscountCouponController @Inject()(cc: MessagesControllerComponents, discountCouponRepository: DiscountCouponRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val discountCouponForm: Form[CreateDiscountCouponForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "value" -> number,
    )(CreateDiscountCouponForm.apply)(CreateDiscountCouponForm.unapply)
  }

  def getDiscountCoupons: Action[AnyContent] = Action.async { implicit request =>
    val fetchedDiscountCoupons = discountCouponRepository.list()
    fetchedDiscountCoupons.map(discountCoupons => Ok(views.html.discountcoupons(discountCoupons)))
  }

  def removeDiscountCoupon(id: Long): Action[AnyContent] = Action {
    discountCouponRepository.delete(id)
    Redirect("/getdiscountcoupons")
  }

  def addDiscountCoupon(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.discountcouponadd(discountCouponForm))
  }

  def addDiscountCouponHandle(): Action[AnyContent] = Action.async { implicit request =>
    discountCouponForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.discountcouponadd(errorForm))
        )
      },
      discountCoupon => {
        discountCouponRepository.create(discountCoupon.name, discountCoupon.value).map { _ =>
          Redirect(routes.DiscountCouponController.addDiscountCoupon()).flashing("success" -> "discountCoupon created")
        }
      }
    )

  }

  def addDiscountCouponJson(): Action[AnyContent] = Action.async { implicit request =>
    val name = request.body.asJson.get("name").as[String]
    val value = request.body.asJson.get("value").as[Int]

    discountCouponRepository.create(name, value).map { book =>
      Ok(Json.toJson(book))
    }
  }

  def getDiscountCouponJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    discountCouponRepository.getById(id).map { discountCoupon =>
      Ok(Json.toJson(discountCoupon))
    }
  }

  def getDiscountCouponsJson: Action[AnyContent] = Action.async { implicit request =>
    discountCouponRepository.list().map { discountCoupons =>
      Ok(Json.toJson(discountCoupons))
    }
  }

  def removeDiscountCouponJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    discountCouponRepository.delete(id).map { _ =>
      Ok("Discount coupon removed")
    }
  }

}

case class CreateDiscountCouponForm(name: String, value: Int)
