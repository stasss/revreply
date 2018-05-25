package controllers

import java.util.UUID
import javax.inject._

import dbconn._
import model.UserRepository
import play.api.data.Form
import play.api.mvc._
import services.Counter
import play.api.data.Forms._
import play.api.libs.ws._
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.libs.json._


import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

/**
  * This controller demonstrates how to use dependency injection to
  * bind a component into a controller class. The class creates an
  * `Action` that shows an incrementing count to users. The [[Counter]]
  * object is injected by the Guice dependency injection system.
  */
@Singleton
class GameController @Inject()(ws: WSClient)(implicit ec: ExecutionContext) extends Controller {

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
    //println(UserRepository.login("admin", "admin"))
    dbconn.getGames()
    val rules = if(currentGame == null) null else dbconn.getRules(currentGame)
    val history = if(currentGame == null) null else dbconn.getReplies(currentGame)
    Ok(views.html.index(dbconn.getGames(), currentGame, rules, gameForm, createRuleForm, testForm, history))
  }

  case class Review(name: String, review: String, date: String, rating: String)

  def playGame(game: String, from: Int, to: Int) = Action{
    val listRes = ListBuffer[Review]()
    (from to to).map( page => {
      val post = s"https://play.google.com/store/getreviews?authuser=0&reviewType=0&pageNum=${page}&id=${game}&reviewSortOrder=2&xhr=1&hl=en"
      val future = ws.url(post).withMethod("POST").withHeaders(("Content-length", "0")).execute()

      val pattern = """ class\\u003d\\\"tiny-star star-rating-non-editable-container\\\" aria-label\\u003d\\\" (.*) \\\"\\u003e \\u003cdiv class\\u003d\\\"current-rating\\""".r
      val reviewPattern = """class\\u003d\\\"review-body with-review-wrapper\\\"\\u003e \\u003cspan class\\u003d\\\"review-title\\\"\\u003e\\u003c/span\\u003e (.*) \\u003cdiv class\\u003d\\\"review-link\\\"""".r
      val datePattern = """\\u003cspan class\\u003d\\\"review-date\\\"\\u003e(.*)\\u003c/span\\u003e \\u003ca class\\u003d\\\"reviews-permalink\\\"""".r
      val namePattern = """class\\u003d\\"review-info\\"\\u003e \\u003cspan class\\u003d\\"author-name\\"\\u003e (.*) \\u003c/span\\u003e  \\u003cspan class\\u003d\\"review-date\\"""".r
      val mapped = future.map( resp => {
        resp.body.split("single-review").drop(1).toSeq.map( e => {
          //println(e)
          val star = pattern.findAllIn(e).matchData map { m => m.group(1)}
          val review = reviewPattern.findAllIn(e).matchData map { m => m.group(1)}
          val date = datePattern.findAllIn(e).matchData map { m => m.group(1)}
          val name = namePattern.findAllIn(e).matchData map { m => m.group(1)}
          listRes += Review(name.toSeq.head, review.toSeq.head, date.toSeq.head, star.toSeq.head)
        })
      }).recover{
        case timeout: java.util.concurrent.TimeoutException => ""
      }
      Await.result(future, 15000 millis )
    })
    val js = Json.toJson(listRes.toSeq)
    Ok(Json.prettyPrint(js))
  }


  implicit val locationWrites = new Writes[Review] {
    def writes(review: Review) = Json.obj(
      "name" -> review.name,
      "text" -> review.review,
      "date" -> review.date,
      "rate" -> review.rating
    )
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