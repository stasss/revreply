package openie

import java.util.Properties
/*
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.simple.Document
import scala.collection.JavaConversions._

/**
  * Created by s_stashkevich on 8/10/2017.
  */
object OpenIE {


  import edu.stanford.nlp.pipeline.StanfordCoreNLP

   def propsSplitDocument(documentText: String): Unit ={

     val plops = new Properties
     plops.setProperty("annotators","tokenize,ssplit,pos,lemma,depparse,natlog,openie")
     val pipeline = new StanfordCoreNLP(plops)

     val doc: Annotation = new Annotation(documentText)
     pipeline.annotate(doc)

     doc.get(classOf[CoreAnnotations.SentencesAnnotation]).foreach( sentence => {

       println(sentence)

       println( sentence.get(classOf[NaturalLogicAnnotations.RelationTriplesAnnotation]))

       sentence.get(classOf[NaturalLogicAnnotations.RelationTriplesAnnotation]).foreach( triple => {
         println(s"Sentence: ${documentText}")
         println(s"Confidence: ${triple.confidence}")
         println(s"Subject: ${triple.subjectLemmaGloss}")
         println(s"Relation: ${triple.relationLemmaGloss}")
         println(s"Object: ${triple.objectLemmaGloss}")
       })
     })
   }



}
*/