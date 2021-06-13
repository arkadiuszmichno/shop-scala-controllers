package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BookReviewRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, bookRepository: BookRepository)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import bookRepository.BookTable
  import dbConfig._
  import profile.api._

  private class BookReviewTable(tag: Tag) extends Table[BookReview](tag, "bookReview") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def book = column[Long]("book")

    def bookFk = foreignKey("book_fk", book, bk)(_.id)

    def review = column[String]("review")

    def * = (id, book, review) <> ((BookReview.apply _).tupled, BookReview.unapply)
  }

  private val bkReview = TableQuery[BookReviewTable]
  private val bk = TableQuery[BookTable]


  def create(book: Long, review: String): Future[BookReview] = db.run {
    (bkReview.map(b => (b.book, b.review))
      returning bkReview.map(_.id)
      into { case ((book, review), id) => BookReview(id, book, review) }
      ) += (book, review)
  }

  def list(): Future[Seq[BookReview]] = db.run {
    bkReview.result
  }

  def getById(id: Long): Future[BookReview] = db.run {
    bkReview.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Unit] = db.run(bkReview.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newBookReview: BookReview): Future[Unit] = {
    val bookReviewToUpdate: BookReview = newBookReview.copy(id)
    db.run(bkReview.filter(_.id === id).update(bookReviewToUpdate)).map(_ => ())
  }
}
