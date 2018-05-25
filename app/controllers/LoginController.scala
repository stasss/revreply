package controllers

import javax.inject.Inject
import java.util.UUID
import javax.inject._

import dbconn._
import model.{User, UserRepository}
import play.api.mvc._
import services.Counter
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import model.UserRepository
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc.{Action, Controller}
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi

/**
  * Created by s_stashkevich on 5/25/2018.
  */

case class LoginForm(name: String, password: String)

class LoginController @Inject() extends Controller {

  val loginForm = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(LoginForm.apply)(LoginForm.unapply)
  )


  def index = Action {
    implicit request =>
    val logged = BasicAuth.logged(request)
    logged match {
      case Some(u) => Redirect("/menu")
      case None    => Ok(views.html.login(loginForm, null))
    }
  }

  def logoff = Action {
    implicit request => {
      val logged = BasicAuth.logged(request)
      logged match {
        case Some(u) => {
          Redirect("/").discardingCookies(DiscardingCookie("auth"))
        }
        case None => Unauthorized("Authentication Failed")
      }
    }
  }

  def login = Action(parse.form(loginForm)) {
    implicit request =>
      val user = UserRepository.login(request.body.name, request.body.password)
      user match {
        case Some(user) => {
          Redirect("/menu").withCookies(Cookie("auth", user.id))
        }
        case None =>  Ok(views.html.login(loginForm, "Sorry, wrong user password combination"))
      }
  }
}
