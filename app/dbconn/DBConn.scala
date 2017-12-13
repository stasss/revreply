package dbconn

import java.io.{BufferedInputStream, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}
import java.util.UUID

import play.api.libs.json._


sealed trait Writable
case class Account(accId: String) extends Writable
case class Game(gameId: String, name: String) extends Writable
case class Rule(ruleId: String, gameId: String, keywords: String, starRating: String, response: String) extends Writable
case class Reply(replyId: String, message: String, starRating: String, ruleId: String, gameId: String) extends Writable

trait DBConn {
  //for now we have only one account
  val currentAccId = "123"
  def getGames(): Seq[Game]
  def getGame(gameId: String): Game
  def getRules(game: Game): Seq[Rule]
  def getRule(ruleId: String): Rule
  def getReplies(ruleId: String): Rule
  def createRule(rule: Rule): Unit
  def createReply(reply: Reply): Unit
}


object RestDBConn{

  val endpoint = "https://geocheck-65fc.restdb.io/rest/"
  val xApiKey = "59ea1e0e16d89bb778329415"

  val accountsCollection = "accounts"
  val gamesCollection =    "games"
  val ruleCollections =    "rules"

  import play.api.libs.functional.syntax._

  implicit val gameReads = Json.reads[Game]
  implicit val ruleReads = Json.reads[Rule]
  //implicit val accountReads = Json.reads[Account]

  implicit val accountWrites = new Writes[Account] {
    def writes(account: Account) = Json.obj(
      "accId" -> account.accId
    )
  }

  implicit val gameWrites = new Writes[Game] {
    def writes(game: Game) = Json.obj(
      "gameId" -> game.gameId,
      "name"   -> game.name
    )
  }

  implicit val replyWrites = new Writes[Reply] {
    def writes(reply: Reply) = Json.obj(
      "replyId"   -> reply.replyId,
      "message"   -> reply.message,
      "starRating" -> reply.starRating,
      "ruleId" -> reply.ruleId,
      "gameId" -> reply.gameId
    )
  }

  implicit val ruleWrites = new Writes[Rule] {
    def writes(rule: Rule) = Json.obj(
      "ruleId"   -> rule.ruleId,
      "gameId"   -> rule.gameId,
      "keywords" -> rule.keywords,
      "starRating" -> rule.starRating,
      "response" -> rule.response
    )
  }

  def mapToJson(writeEntity: Writable): String = {
    writeEntity match {
      case acc : Account =>    accountWrites.writes(acc.asInstanceOf[Account]).toString()
      case game : Game    =>   gameWrites.writes(game.asInstanceOf[Game]).toString()
      case rule : Rule    =>   ruleWrites.writes(rule.asInstanceOf[Rule]).toString()
      case _ => throw new RuntimeException("Not writable entity")
    }
  }

  def mapCollection(writeEntity: Writable): String ={
    writeEntity match {
      case _ : Account => "accounts"
      case _ : Game    => "games"
      case _ : Rule    => "rules"
      case _ => throw new RuntimeException("Not writable entity")
    }
  }

  def buildPost(writeEntity: Writable): Unit ={
    val url = new URL(s"${endpoint}${mapCollection(writeEntity)}")
    val urlConnection: HttpURLConnection = url.openConnection.asInstanceOf[HttpURLConnection]
    urlConnection.setRequestMethod("POST")
    urlConnection.setRequestProperty("content-type", "application/json")
    urlConnection.setRequestProperty("x-apikey", xApiKey)
    urlConnection.setRequestProperty("cache-control", "no-cache")
    urlConnection.setDoOutput(true)
    val wr = new OutputStreamWriter(urlConnection.getOutputStream)

    val json = mapToJson(writeEntity)
    println(json.toString)

    wr.write(json.toString())
    wr.close()

    val in = new BufferedInputStream(urlConnection.getInputStream)
    val res = scala.io.Source.fromInputStream(in).getLines().mkString("\n")

    in.close()
    urlConnection.disconnect()
  }

  def buildQuery(collection: String, query: Option[String] = None): String = {
    val url = new URL(s"${endpoint}${collection}${query.map(q => s"?q=${q}").getOrElse("")}")

    println(url.toString)

    val urlConnection: HttpURLConnection = url.openConnection.asInstanceOf[HttpURLConnection]
    urlConnection.setRequestMethod("GET")
    urlConnection.setRequestProperty("x-apikey", "59ea1e0e16d89bb778329415")
    val in = new BufferedInputStream(urlConnection.getInputStream)
    val res = scala.io.Source.fromInputStream(in).getLines().mkString("\n")

    println(res)

    in.close()
    urlConnection.disconnect()


    return res
  }

}

class RestDBConn extends DBConn{
  import RestDBConn._

  val esc = "\""

  override def getGames(): Seq[Game] = {
    Json.fromJson[Seq[Game]](Json.parse(buildQuery(gamesCollection))).get
  }

  override def getGame(gameId: String): Game = {
    Json.fromJson[Seq[Game]](Json.parse(buildQuery(gamesCollection, Some(s"{${esc}gameId${esc}:${esc}${gameId}${esc}}")))).get.head
  }

  override def getRules(game: Game): Seq[Rule] = {
    Json.fromJson[Seq[Rule]](Json.parse(buildQuery(ruleCollections))).get
  }

  override def getRule(ruleId: String): Rule = {
    Json.fromJson[Seq[Rule]](Json.parse(buildQuery(ruleCollections, Some(s"{${esc}ruleId${esc}:${esc}${ruleId}${esc}")))).get.head
  }

  override def createRule(rule: Rule): Unit = {
    buildPost(rule)
  }

  override def getReplies(ruleId: String): Rule = ???

  override def createReply(reply: Reply): Unit = ???
}
