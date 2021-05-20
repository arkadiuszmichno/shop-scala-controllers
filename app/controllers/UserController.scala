package controllers

import models.{User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(cc: MessagesControllerComponents, userRepository: UserRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "login" -> nonEmptyText,
      "gender" -> nonEmptyText,
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }

  val updateUserForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> longNumber,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "login" -> nonEmptyText,
      "gender" -> nonEmptyText,
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }

  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    val fetchedUsers = userRepository.list()
    fetchedUsers.map(user => Ok(views.html.users(user)))
  }

  def removeUser(id: Long): Action[AnyContent] = Action {
    userRepository.delete(id)
    Redirect("/getusers")
  }

  def addUser(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.useradd(userForm))
  }

  def addUserHandle(): Action[AnyContent] = Action.async { implicit request =>
    userForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.useradd(errorForm))
        )
      },
      user => {
        userRepository.create(user.firstName, user.lastName, user.login, user.gender).map { _ =>
          Redirect(routes.UserController.addUser()).flashing("success" -> "user created")
        }
      }
    )

  }

  def updateUser(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepository.getById(id)
    user.map(usr => {
      val userForm = updateUserForm.fill(UpdateUserForm(usr.id, usr.firstName, usr.lastName, usr.login, usr.gender))
      Ok(views.html.userupdate(userForm))
    })
  }

  def updateUserHandle(): Action[AnyContent] = Action.async { implicit request =>
    updateUserForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userupdate(errorForm))
        )
      },
      usr => {
        userRepository.update(usr.id, User(usr.id, usr.firstName, usr.lastName, usr.login, usr.gender)).map { _ =>
          Redirect(routes.UserController.updateUser(usr.id)).flashing("success" -> "user updated")
        }
      }
    )
  }

  def addUserJson(): Action[AnyContent] = Action.async { implicit request =>
    val firstName = request.body.asJson.get("firstName").as[String]
    val lastName = request.body.asJson.get("lastName").as[String]
    val login = request.body.asJson.get("login").as[String]
    val gender = request.body.asJson.get("gender").as[String]

    userRepository.create(firstName, lastName, login, gender).map { user =>
      Ok(Json.toJson(user))
    }
  }

  def updateUserJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val firstName = request.body.asJson.get("firstName").as[String]
    val lastName = request.body.asJson.get("lastName").as[String]
    val login = request.body.asJson.get("login").as[String]
    val gender = request.body.asJson.get("gender").as[String]

    userRepository.update(id, User(id, firstName, lastName, login, gender)).map { user =>
      Ok("Updated user")
    }
  }

  def getUserJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userRepository.getById(id).map { user =>
      Ok(Json.toJson(user))
    }
  }

  def getUsersJson: Action[AnyContent] = Action.async { implicit request =>
    userRepository.list().map { users =>
      Ok(Json.toJson(users))
    }
  }

  def removeUserJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userRepository.delete(id).map { _ =>
      Ok("User removed")
    }
  }

}

case class CreateUserForm(firstName: String, lastName: String, login: String, gender: String)

case class UpdateUserForm(id: Long, firstName: String, lastName: String, login: String, gender: String)
