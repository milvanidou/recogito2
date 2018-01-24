package storage.es

import org.elasticsearch.search.aggregations.bucket.terms.Terms
import scala.collection.JavaConverters._

trait HasAggregations {

  def parseTermsAggregation(terms: Terms) =
    terms.getBuckets.asScala.toSeq.map { bucket =>
      (bucket.getKeyAsString, bucket.getDocCount)
    }.toMap

}

