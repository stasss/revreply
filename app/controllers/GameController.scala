package controllers

import java.util.UUID
import javax.inject._

import dbconn.{Game, Reply, RestDBConn, Rule}
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

  val testForm = Form(
    mapping(
      "message" -> text,
      "starRating" -> text
    )(TestForm.apply)(TestForm.unapply)
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
    val history = if(currentGame == null) null else dbconn.getReplies(currentGame)
    Ok(views.html.index(dbconn.getGames(), currentGame, rules, gameForm, createRuleForm, testForm, history))
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

  def createMessage = Action(parse.form(testForm)) {
    implicit request =>
      val message = request.body.message
      val starRating = request.body.starRating

      val rules = dbconn.getRules(currentGame)

      val fired = rules.filter( rule => {
        val kwds = rule.keywords.split(",").map(_.trim.toLowerCase())
        starRating == rule.starRating && kwds.forall( wrd => message.toLowerCase.contains(wrd))
      }).headOption
     dbconn.createReply(Reply(UUID.randomUUID().toString, message, starRating, fired.map(_.ruleId).getOrElse("EMPTY"), currentGame.gameId))
     Redirect(routes.GameController.index)
  }

  //Currently in controller
  def matchRule(): Rule = {
     null
  }

}


case class GameForm(gameId: String)
case class RuleForm(keywords: String, starRating: String, response: String)
case class TestForm(message: String, starRating: String)