package controllers

import java.util.UUID
import javax.inject._

import dbconn.{Game, RestDBConn, Rule}
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
class GameController @Inject() extends Controller {

  val gameForm = Form(
    mapping(
      "gameId" -> text
    )(GameForm.apply)(GameForm.unapply)
  )

  val createRuleForm = Form(
    mapping(
      "keywords" -> text,
      "starRating" -> text,
      "response" -> text
    )(RuleForm.apply)(RuleForm.unapply)
  )

  val dbconn = new RestDBConn()
  var currentGame: Game = null

  def index = Action {
    dbconn.getGames()
    val rules = if(currentGame == null) null else dbconn.getRules(currentGame)
    Ok(views.html.index(dbconn.getGames(), currentGame, rules, gameForm, createRuleForm))
  }


  def select = Action(parse.form(gameForm)) {
    implicit request =>
      currentGame =  dbconn.getGame(request.body.gameId)
      Redirect(routes.GameController.index())
  }

  def createRule = Action(parse.form(createRuleForm)) {
    implicit request =>
      val rule = Rule(UUID.randomUUID().toString, currentGame.gameId, request.body.keywords, request.body.starRating, request.body.response)
      dbconn.createRule(rule)
      Redirect(routes.GameController.index())
  }

}


case class GameForm(gameId: String)
case class RuleForm(keywords: String, starRating: String, response: String)