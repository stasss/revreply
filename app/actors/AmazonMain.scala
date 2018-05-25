package actors

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3ClientBuilder

/**
  * Created by s_stashkevich on 1/16/2018.
  */
object AmazonMain extends App{


  import com.amazonaws.services.s3.AmazonS3
  import com.amazonaws.services.s3.AmazonS3ClientBuilder
  import com.amazonaws.services.s3.model.Bucket

  val s3 = AmazonS3ClientBuilder.defaultClient
  val buckets = s3.listBuckets
  System.out.println("Your Amazon S3 buckets are:")

  import scala.collection.JavaConversions._

  for (b <- buckets) {
    System.out.println("* " + b.getName)
  }


}
