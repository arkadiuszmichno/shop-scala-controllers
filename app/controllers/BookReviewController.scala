package controllers

import models.{Book, BookRepository, BookReview, BookReviewRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class BookReviewController @Inject()(cc: MessagesControllerComponents, bookReviewRepository: BookReviewRepository,
                                     bookRepository: BookRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val bookReviewForm: Form[CreateBookReviewForm] = Form {
    mapping(
      "book" -> number,
      "review" -> nonEmptyText,
    )(CreateBookReviewForm.apply)(CreateBookReviewForm.unapply)
  }

  val updateBookReviewForm: Form[UpdateBookReviewForm] = Form {
    mapping(
      "id" -> longNumber,
      "book" -> longNumber,
      "review" -> nonEmptyText,
    )(UpdateBookReviewForm.apply)(UpdateBookReviewForm.unapply)
  }

  def getBookReviews: Action[AnyContent] = Action.async { implicit request =>
    val fetchedBookReviews = bookReviewRepository.list()
    fetchedBookReviews.map(bookReviews => Ok(views.html.bookreviews(bookReviews)))
  }

  def removeBookReview(id: Long): Action[AnyContent] = Action {
    bookReviewRepository.delete(id)
    Redirect("/getbookreviews")
  }

  def addBookReview(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val books = bookRepository.list()
    books.map(book => Ok(views.html.bookreviewadd(bookReviewForm, book)))
  }

  def addBookReviewHandle(): Action[AnyContent] = Action.async { implicit request =>
    var book: Seq[Book] = Seq[Book]()
    val books = bookRepository.list().onComplete {
      case Success(bk) => book = bk
      case Failure(_) => print("fail")
    }

    bookReviewForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.bookreviewadd(errorForm, book))
        )
      },
      bookReview => {
        bookReviewRepository.create(bookReview.book, bookReview.review).map { _ =>
          Redirect(routes.BookReviewController.addBookReview()).flashing("success" -> "book review created")
        }
      }
    )
  }

  def updateBookReview(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var book: Seq[Book] = Seq[Book]()
    val books = bookRepository.list().onComplete {
      case Success(bk) => book = bk
      case Failure(_) => print("fail")
    }

    val produkt = bookReviewRepository.getById(id)
    produkt.map(bookReview => {
      val prodForm = updateBookReviewForm.fill(UpdateBookReviewForm(bookReview.id, bookReview.book, bookReview.review))
      Ok(views.html.bookreviewupdate(prodForm, book))
    })
  }

  def updateBookReviewHandle(): Action[AnyContent] = Action.async { implicit request =>
    var book: Seq[Book] = Seq[Book]()
    val books = bookRepository.list().onComplete {
      case Success(bk) => book = bk
      case Failure(_) => print("fail")
    }

    updateBookReviewForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.bookreviewupdate(errorForm, book))
        )
      },
      bookReview => {
        bookReviewRepository.update(bookReview.id, BookReview(bookReview.id, bookReview.book, bookReview.review)).map { _ =>
          Redirect(routes.BookReviewController.updateBookReview(bookReview.id)).flashing("success" -> "book updated")
        }
      }
    )

  }

  def addReviewJson(): Action[AnyContent] = Action.async { implicit request =>
    val book = request.body.asJson.get("book").as[Long]
    val review = request.body.asJson.get("review").as[String]

    bookReviewRepository.create(book, review).map { bookReview =>
      Ok(Json.toJson(bookReview))
    }
  }

  def updateReviewJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val book = request.body.asJson.get("book").as[Long]
    val review = request.body.asJson.get("review").as[String]

    bookReviewRepository.update(id, BookReview(id, book, review)).map { bookReview =>
      Ok("Book review updated")
    }
  }

  def getReviewJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    bookReviewRepository.getById(id).map { bookReview =>
      Ok(Json.toJson(bookReview))
    }
  }

  def getReviewsJson: Action[AnyContent] = Action.async { implicit request =>
    bookReviewRepository.list().map { bookReviews =>
      Ok(Json.toJson(bookReviews))
    }
  }

  def removeReviewJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    bookReviewRepository.delete(id).map { _ =>
      Ok("Book review removed")
    }
  }

}

case class CreateBookReviewForm(book: Int, review: String)

case class UpdateBookReviewForm(id: Long, book: Long, review: String)
