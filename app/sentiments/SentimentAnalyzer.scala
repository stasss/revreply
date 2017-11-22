package sentiments

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import edu.stanford.nlp.simple.Sentence
import edu.stanford.nlp.util.CoreMap

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
  * Created by s_stashkevich on 8/1/2017.
  */

case class Probability(veryNegative: Double, negative: Double, neutral: Double, positive: Double, veryPositive: Double){
  def toSeq = Seq(veryNegative, negative, neutral, positive, veryPositive)
}
case class ScoreResult(sentencesScore: List[Double], probability: Option[List[Probability]])


abstract class SentenceStrategy(probability: Boolean) {

  def score(sentence: CoreMap) = {
    val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
    val coeff = RNNCoreAnnotations.getPredictions(tree)
    Probability(coeff.get(0), coeff.get(1), coeff.get(2), coeff.get(3), coeff.get(4))

  }
  def apply(sentences: List[CoreMap], fn: CoreMap => Double):ScoreResult
}

class LongestSentenceStrategy(probability: Boolean) extends SentenceStrategy(probability) {
  override def apply(sentences: List[CoreMap], fn: CoreMap => Double): ScoreResult = {
    val maxSent =sentences.maxBy(new Sentence(_).text())
    ScoreResult(List(fn(maxSent)), if(probability) Some(List(score(maxSent))) else None)
  }
}

class SimpleStrategy(probability: Boolean) extends SentenceStrategy(probability) {
  override def apply(sentences: List[CoreMap], fn: CoreMap => Double): ScoreResult = {
    ScoreResult(sentences.map(fn(_) ), Some(sentences.map(score(_))))
  }
}

class WeightedStrategy(probability: Boolean) extends SentenceStrategy(probability) {
  override def apply(sentences: List[CoreMap], fn: CoreMap => Double): ScoreResult = {
    //val lenghtW = sentences.map(sent => (new Sentence(sent).text().length, fn(sent)))
    // lenghtW
    null
  }
}


class SentimentAnalyzer(strategy: SentenceStrategy) {

  val pipelineProps = new Properties
  var tokenizerProps = new Properties
  pipelineProps.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  pipelineProps.setProperty("enforceRequirements", "false")

  val pipeline = new StanfordCoreNLP(pipelineProps)

  def label(sentence: CoreMap) = {
    val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
    RNNCoreAnnotations.getPredictedClass(tree).toDouble
  }

  def score(sentence: CoreMap) = {
    val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
    val coeff = RNNCoreAnnotations.getPredictions(tree)
    Probability(coeff.get(0), coeff.get(1), coeff.get(2), coeff.get(3), coeff.get(4))

  }

  def labelByDelta(sentence: CoreMap): Double = {
    val prbb = score(sentence)
    val max = prbb.toSeq.zipWithIndex.maxBy(_._1)
    val result = if(max._2 != 2){
      val res = if(max._1 - prbb.neutral < 0.25) 2d else max._2.toDouble
      res
    }else{
      2d
    }
    result
  }

  /**
    * Reads an annotation from the given filename using the requested input.
    */
  private def getAnnotations(tokenizer: StanfordCoreNLP, text: String, filterUnknown: Boolean): List[Annotation] = {
    val annotation = new Annotation(text.trim)
    tokenizer.annotate(annotation)

    annotation.get(classOf[CoreAnnotations.SentencesAnnotation]).map( sentence =>{
      new Annotation(sentence.get(classOf[CoreAnnotations.TextAnnotation]))
    }).toList
  }



  def transform(text: String, filterUnknown: Boolean = true): ScoreResult = {
    val ann = new Annotation(text)
    pipeline.annotate(ann)
    val sentences: List[CoreMap] = ann.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala.toList
    strategy(sentences, label)
  }

}
