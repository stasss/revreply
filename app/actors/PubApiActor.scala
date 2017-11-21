package actors

import java.io.{BufferedInputStream, File, IOException}
import java.net.{HttpURLConnection, URL}
import java.security.GeneralSecurityException
import java.util.Collections

import akka.actor.{Actor, Props}
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.model.ReviewsReplyRequest
import com.google.api.services.androidpublisher.{AndroidPublisher, AndroidPublisherScopes}
import java.io.BufferedInputStream
import java.io.InputStream
import scala.collection.JavaConversions._
import java.io.OutputStreamWriter


object RestDBConnector {


  def write(): Unit = {
    val url = new URL("https://geocheck-65fc.restdb.io/rest/reviews")
    val urlConnection: HttpURLConnection = url.openConnection.asInstanceOf[HttpURLConnection]
    urlConnection.setRequestMethod("POST")
    urlConnection.setRequestProperty("content-type", "application/json")
    urlConnection.setRequestProperty("x-apikey", "59ea1e0e16d89bb778329415")
    urlConnection.setRequestProperty("cache-control", "no-cache")

    import java.io.OutputStreamWriter
    val wr = new OutputStreamWriter(urlConnection.getOutputStream)
    wr.write("{\"rev_id\":\"xyz\",\"comment\":\"abc\",\"sentiment\":\"abc\",\"reply\":\"abc\",\"meta\":\"abc\"}")

    val in = new BufferedInputStream(urlConnection.getInputStream)
    val res = scala.io.Source.fromInputStream(in).getLines().mkString("\n")
    in.close()
    urlConnection.disconnect()

    println(res)

  }

  def read(): Unit ={
    val url = new URL("https://geocheck-65fc.restdb.io/rest/reviews")
    val urlConnection: HttpURLConnection = url.openConnection.asInstanceOf[HttpURLConnection]
    urlConnection.setRequestMethod("GET")
    urlConnection.setRequestProperty("x-apikey", "59ea1e0e16d89bb778329415")
    val in = new BufferedInputStream(urlConnection.getInputStream)
    val res = scala.io.Source.fromInputStream(in).getLines().mkString("\n")

    in.close()
    urlConnection.disconnect()

    println(res)

  }

  import java.io.BufferedInputStream
  import java.io.InputStream



}


/**
  * Created by s_stashkevich on 11/21/2017.
  */
object PubApiActor {

  def props = Props[PubApiActor]

  val secretPath = "resources/secret.p12"

  val transport = GoogleNetHttpTransport.newTrustedTransport
  val jsonFactory = JacksonFactory.getDefaultInstance
  val serviceEmail = "ss-942@api-7501192816201744243-1920.iam.gserviceaccount.com"
  val appName = "com.EJG.NineScreens"
  val aPublisher = init()

  //init publishing connector
  @throws[IOException]
  @throws[GeneralSecurityException]
  protected def init(): AndroidPublisher = {
    println("Initialized")

    // Authorization.
    // Set up and return API client.
    val credential = authorizeWithServiceAccount(serviceEmail)
    new AndroidPublisher.Builder(transport, jsonFactory, credential).setApplicationName(appName).build
  }


  //authorize as service account
  @throws[GeneralSecurityException]
  @throws[IOException]
  private def authorizeWithServiceAccount(serviceAccountEmail: String) = {
    //log.info(String.format("Authorizing using Service Account: %s", serviceAccountEmail))
    // Build service account credential.
    val credential = new GoogleCredential.Builder().setTransport(transport).setJsonFactory(jsonFactory).
      setServiceAccountId(serviceAccountEmail).setServiceAccountScopes(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER)).
      setServiceAccountPrivateKeyFromP12File(new File("resources/secret.p12")).build
    credential
  }

  def replyRev(revId: String): Unit = {
    aPublisher.reviews().reply(appName, revId, new ReviewsReplyRequest().setReplyText("Thanks for your feedback")).execute()
  }

  case class ReviewApi()
  case class Review(id: String, comment: String, reply: String, meta: String)
  case class ReviewResp(id: String, comment: String, sentiment: String, reply: String, meta: String)

}

class PubApiActor extends Actor {
  import  PubApiActor._

  def receive = {
    case Review(id: String, comment: String, reply: String, meta: String) => {
      /*
      val reviews = aPublisher.reviews().list(appName).execute().getReviews.toList
      val rsp = reviews.map(review => {
        //val reply = replyRev(review.getReviewId)
        val text = review.getComments.map(_.toString).mkString(",")

        println("REEEEEEEEEEAD : " + RestDBConnector.read())

        s"Reply: ${"Any reple"} \n text: ${text}"
      })
      */
      sender() ! ReviewResp(id, comment, reply, "test", meta)
    }
  }

}
