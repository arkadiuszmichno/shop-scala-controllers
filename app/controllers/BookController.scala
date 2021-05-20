package controllers

import models.{Book, BookRepository, Category, CategoryRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class BookController @Inject()(bookRepository: BookRepository, categoryRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val bookForm: Form[CreateBookForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> number,
    )(CreateBookForm.apply)(CreateBookForm.unapply)
  }

  val updateBookForm: Form[UpdateBookForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> number,
    )(UpdateBookForm.apply)(UpdateBookForm.unapply)
  }

  def getBooks: Action[AnyContent] = Action.async { implicit request =>
    val fetchedBooks = bookRepository.list()
    fetchedBooks.map(books => Ok(views.html.books(books)))
  }

  def getBook(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val book = bookRepository.getByIdOption(id)
    book.map {
      case Some(p) => Ok(views.html.book(p))
      case None => Redirect(routes.BookController.getBooks())
    }
  }

  def removeBook(id: Long): Action[AnyContent] = Action {
    bookRepository.delete(id)
    Redirect("/getbooks")
  }

  def updateBook(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var categ: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    val produkt = bookRepository.getById(id)
    produkt.map(book => {
      val prodForm = updateBookForm.fill(UpdateBookForm(book.id, book.name, book.description, book.category))
      Ok(views.html.bookupdate(prodForm, categ))
    })
  }

  def updateBookHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    updateBookForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.bookupdate(errorForm, categ))
        )
      },
      book => {
        bookRepository.update(book.id, Book(book.id, book.name, book.description, book.category)).map { _ =>
          Redirect(routes.BookController.updateBook(book.id)).flashing("success" -> "book updated")
        }
      }
    )

  }

  def addBook(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepo.list()
    categories.map(cat => Ok(views.html.bookadd(bookForm, cat)))
  }

  def addBookHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    bookForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.bookadd(errorForm, categ))
        )
      },
      book => {
        bookRepository.create(book.name, book.description, book.category).map { _ =>
          Redirect(routes.BookController.addBook()).flashing("success" -> "book created")
        }
      }
    )

  }


  def addBookJson(): Action[AnyContent] = Action.async { implicit request =>
    val book_name = request.body.asJson.get("name").as[String]
    val book_description = request.body.asJson.get("description").as[String]
    val book_category = request.body.asJson.get("category").as[Int]

    bookRepository.create(book_name, book_description, book_category).map { book =>
      Ok(Json.toJson(book))
    }
  }

  def updateBookJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val book_name = request.body.asJson.get("name").as[String]
    val book_description = request.body.asJson.get("description").as[String]
    val book_category = request.body.asJson.get("category").as[Int]
    bookRepository.update(id, Book(id, book_name, book_description, book_category)).map {book =>
      Ok("Book updated")
    }
  }

  def getBookJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    bookRepository.getById(id).map { book =>
      Ok(Json.toJson(book))
    }
  }

  def getBooksJson: Action[AnyContent] = Action.async { implicit request =>
    bookRepository.list().map { books =>
      Ok(Json.toJson(books))
    }
  }

  def removeBookJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    bookRepository.delete(id).map { _ =>
      Ok("Book removed")
    }
  }

}

case class CreateBookForm(name: String, description: String, category: Int)

case class UpdateBookForm(id: Long, name: String, description: String, category: Int)
