package controllers

import models.{Category, CategoryRepository, GiftCardRepository}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class GiftCardController @Inject()(cc: MessagesControllerComponents, giftCardRepository: GiftCardRepository, categoryRepository: CategoryRepository)
                                  (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val giftCardForm: Form[CreateGiftCardForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "value" -> number,
      "category" -> number,
    )(CreateGiftCardForm.apply)(CreateGiftCardForm.unapply)
  }

  def getGiftCards: Action[AnyContent] = Action.async { implicit request =>
    val fetchedGiftCards = giftCardRepository.list()
    fetchedGiftCards.map(giftCards => Ok(views.html.giftcards(giftCards)))
  }

  def removeGiftCard(id: Long): Action[AnyContent] = Action {
    giftCardRepository.delete(id)
    Redirect("/getgiftcards")
  }

  def addGiftCard(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepository.list()
    categories.map(cat => Ok(views.html.giftcardadd(giftCardForm, cat)))
  }

  def addGiftCardHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    categoryRepository.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    giftCardForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.giftcardadd(errorForm, categ))
        )
      },
      giftCard => {
        giftCardRepository.create(giftCard.name, giftCard.value, giftCard.category).map { _ =>
          Redirect(routes.BookController.addBook()).flashing("success" -> "gift card created")
        }
      }
    )

  }

  def addGiftCardJson(): Action[AnyContent] = Action.async { implicit request =>
    val name = request.body.asJson.get("name").as[String]
    val value = request.body.asJson.get("value").as[Int]
    val category = request.body.asJson.get("category").as[Int]

    giftCardRepository.create(name, value, category).map { giftCard =>
      Ok(Json.toJson(giftCard))
    }
  }

  def getGiftCardJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    giftCardRepository.getById(id).map { giftCard =>
      Ok(Json.toJson(giftCard))
    }
  }

  def getGiftCardByCategoryJson(categoryId: Int): Action[AnyContent] = Action.async { implicit request =>
    giftCardRepository.getByCategory(categoryId).map { giftCard =>
      Ok(Json.toJson(giftCard))
    }
  }

  def getGiftCardsJson: Action[AnyContent] = Action.async { implicit request =>
    giftCardRepository.list().map { giftCards =>
      Ok(Json.toJson(giftCards))
    }
  }

  def removeGiftCardJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    giftCardRepository.delete(id).map { _ =>
      Ok("Gift card removed")
    }
  }

}

case class CreateGiftCardForm(name: String, value: Int, category: Int)
