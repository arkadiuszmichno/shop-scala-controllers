package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BookRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val categoryRepository: CategoryRepository)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class BookTable(tag: Tag) extends Table[Book](tag, "book") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def category = column[Int]("category")

    def categoryFk = foreignKey("cat_fk", category, cat)(_.id)

    def * = (id, name, description, category) <> ((Book.apply _).tupled, Book.unapply)
  }

  import categoryRepository.CategoryTable

  val book = TableQuery[BookTable]
  val cat = TableQuery[CategoryTable]


  def create(name: String, description: String, category: Int): Future[Book] = db.run {
    (book.map(b => (b.name, b.description, b.category))
      returning book.map(_.id)
      into { case ((name, description, category), id) => Book(id, name, description, category) }
      ) += (name, description, category)
  }

  def list(): Future[Seq[Book]] = db.run {
    book.result
  }

  def getByCategory(categoryId: Int): Future[Seq[Book]] = db.run {
    book.filter(_.category === categoryId).result
  }

  def getById(id: Long): Future[Book] = db.run {
    book.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Book]] = db.run {
    book.filter(_.id === id).result.headOption
  }

  def getByCategories(categoryIds: List[Int]): Future[Seq[Book]] = db.run {
    book.filter(_.category inSet categoryIds).result
  }

  def delete(id: Long): Future[Unit] = db.run(book.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newBook: Book): Future[Unit] = {
    val productToUpdate: Book = newBook.copy(id)
    db.run(book.filter(_.id === id).update(productToUpdate)).map(_ => ())
  }
}
