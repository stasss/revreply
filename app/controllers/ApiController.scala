package controllers

import javax.inject.{Inject, Singleton}

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import java.io.{File, IOException}
import java.security.GeneralSecurityException
import java.util.Collections

import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.mvc.{Action, Controller}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.model.ReviewsReplyRequest
import com.google.api.services.androidpublisher.AndroidPublisher
import java.io.IOException
import java.security.GeneralSecurityException

import play.api.mvc._
import akka.actor._
import javax.inject._

import actors.{HelloActor, PubApiActor, RestDBConnector}
import actors.HelloActor.SayHello
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import play.api.routing.JavaScriptReverseRouter
import processor.Processor

import scala.collection.JavaConversions._
/**
  * Created by s_stashkevich on 11/20/2017.
  */
@Singleton
class ApiController @Inject() (system: ActorSystem) extends Controller {


  val secretPath = "resources/secret.p12"
  val appName = "com.EJG.NineScreens"
  val pubApiActor = system.actorOf(PubApiActor.props, "pub-api-actor")


  def hello = Action.async {
    implicit val timeout: Timeout = 10.seconds
    (pubApiActor ? PubApiActor.ReviewApi).mapTo[String].map { message =>
      Ok(message)
    }
  }

  def submit(text: String) = Action {
      val res = Processor.transform(text)
      RestDBConnector.write(text, res)
      Ok("Submitted")
  }

  def history = Action {
    RestDBConnector.read()
    Ok(RestDBConnector.read())
  }

  def secret = Action{
    Ok(new File(secretPath).exists().toString)
  }

  def reply = Action {
    Ok("Blabla")
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.ApiController.submit,
        routes.javascript.ApiController.history
      )
    ).as("text/javascript")
  }

}
