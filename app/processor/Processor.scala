package processor

import sentiments.{SentimentAnalyzer, SimpleStrategy}

object Processor {
  val analyzer = new SentimentAnalyzer(new SimpleStrategy(false))

  def transform(text: String): String ={
    val score = avg(analyzer.transform(text).sentencesScore)
    score match {
      case s if(s >= 0d && s < 1d) =>  "very negative"
      case s if(s >= 1d && s < 2d) =>  "negative"
      case s if(s >= 2d && s < 3d) =>  "neutral"
      case s if(s >= 3d && s < 4d) =>  "positive"
      case s if(s == 4d) =>            "very positive"
    }
  }

  def avg(inputList: List[Double]): Double = {
    if(inputList.isEmpty) -1d else inputList.sum / inputList.size
  }

}

//
class Processor {
  import Processor._

}
