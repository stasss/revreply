package controllers

import java.nio.file.Paths
import java.util.UUID
import javax.inject._

import dbconn._
import model.{ServiceRepository, UserRepository}
import play.api.data.Form
import play.api.mvc._
import services.Counter
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi

/**
  * This controller demonstrates how to use dependency injection to
  * bind a component into a controller class. The class creates an
  * `Action` that shows an incrementing count to users. The [[Counter]]
  * object is injected by the Guice dependency injection system.
  */
@Singleton
class MenuController @Inject() extends Controller {

  val serviceForm = Form(
    mapping(
      "email" ->   text,
      "appName" -> text
    )(ServiceAccountForm.apply)(ServiceAccountForm.unapply)
  )


  def index = Action {
    implicit request =>
       BasicAuth.logged(request) match {
         case Some(u) => {

           println("SASA" + ServiceRepository.accountsByUser(u))

           Ok(views.html.mainView(u, ServiceRepository.accountsByUser(u), serviceForm))
         }
         case None    =>   Unauthorized("Authentication Failed")
       }
  }

  def addService = Action(parse.form(serviceForm)) {
    implicit request =>
      BasicAuth.logged(request) match {
        case Some(u) => {
          val appName = request.body.appName
          val email   = request.body.email
          ServiceRepository.addAccount(u, email, appName)
          Ok(views.html.mainView(u, ServiceRepository.accountsByUser(u), serviceForm))
        }
        case None    =>   Unauthorized("Authentication Failed")
      }
  }

}

case class ServiceAccountForm(email: String, appName: String)
