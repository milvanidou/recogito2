package models.contribution

import com.sksamuel.elastic4s.{ HitAs, RichSearchHit }
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.Indexable
import java.util.UUID
import models.{ HasDate, Page }
import org.elasticsearch.search.aggregations.bucket.filter.Filter
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.nested.Nested
import org.elasticsearch.search.sort.SortOrder
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.Logger
import play.api.libs.json.Json
import scala.collection.JavaConverters._
import scala.concurrent.{ Future, ExecutionContext }
import storage.ES

object ContributionService extends HasDate {

  private val CONTRIBUTION = "contribution"

  private def MAX_RETRIES = 10 // Max number of retries in case of failure

  implicit object ContributionIndexable extends Indexable[Contribution] {
    override def json(c: Contribution): String = Json.stringify(Json.toJson(c))
  }

  implicit object ContributionHitAs extends HitAs[Contribution] {
    override def as(hit: RichSearchHit): Contribution =
      Json.fromJson[Contribution](Json.parse(hit.sourceAsString)).get
  }

  /** Inserts a contribution record into the index **/
  def insertContribution(contribution: Contribution)(implicit context: ExecutionContext): Future[Boolean] =
    ES.client execute {
      index into ES.IDX_RECOGITO / CONTRIBUTION source contribution
    } map {
      _.isCreated
    } recover { case t: Throwable =>
      Logger.error("Error recording contribution event")
      Logger.error(contribution.toString)
      t.printStackTrace
      false
    }

  /** Inserts a list of contributions, automatically dealing with retries. **/
  def insertContributions(contributions: Seq[Contribution], retries: Int = MAX_RETRIES)(implicit context: ExecutionContext): Future[Boolean] =
    contributions.foldLeft(Future.successful(Seq.empty[Contribution])) { case (future, contribution) =>
      future.flatMap { failed =>
        insertContribution(contribution).map { success =>
          if (success) failed else failed :+ contribution
        }
      }
    } flatMap { failed =>
      if (failed.size > 0 && retries > 0) {
        Logger.warn(failed.size + " annotations failed to import - retrying")
        insertContributions(failed, retries - 1)
      } else {
        if (failed.size > 0) {
          Logger.error(failed.size + " contribution events failed without recovery")
          Future.successful(false)
        } else {
          Future.successful(true)
        }
      }
    }

  /** Returns the contribution history on a given document, as a paged result **/
  def getHistory(documentId: String, offset: Int = 0, limit: Int = 20)(implicit context: ExecutionContext): Future[Page[(Contribution)]] =
    ES.client execute {
      search in ES.IDX_RECOGITO / CONTRIBUTION query nestedQuery("affects_item").query (
        termQuery("affects_item.document_id" -> documentId)
      ) sort (
        field sort "made_at" order SortOrder.DESC
      ) start offset limit limit
    } map { response =>
      val contributions = response.as[Contribution].toSeq
      Page(response.getTook.getMillis, response.getHits.getTotalHits, offset, limit, contributions)
    }

  /** Returns the contributions associated with a specific annotation version **/
  def getContributions(annotationId: UUID, versionId: UUID)(implicit context: ExecutionContext): Future[Seq[Contribution]] =
    ES.client execute {
      search in ES.IDX_RECOGITO / CONTRIBUTION query nestedQuery("affects_item").query {
        bool {
          must (
            termQuery("affects_item.annotation_id" -> annotationId.toString),
            termQuery("affects_item.annotation_version_id" -> versionId.toString)
          )
        }
      }
    } map { _.as[Contribution] }

  /** Deletes the contribution history after a given timestamp **/
  def deleteHistoryAfter(documentId: String, after: DateTime)(implicit context: ExecutionContext): Future[Boolean] = {
    Logger.info("Purging history for " + documentId + " after " + timestamp)

    // The usual 2-step process find->delete
    def findContributionsAfter()= ES.client execute {
      search in ES.IDX_RECOGITO / CONTRIBUTION query filteredQuery query {
        nestedQuery("affects_item").query(termQuery("affects_item.document_id" -> documentId))
      } postFilter {
        rangeFilter("made_at").gt(formatDate(after))
      } limit Int.MaxValue
    } map { _.getHits.getHits }

    findContributionsAfter().flatMap { hits =>
      if (hits.size > 0) {
        ES.client execute {
          bulk ( hits.map(h => delete id h.getId from ES.IDX_RECOGITO / CONTRIBUTION) )
        } map {
          !_.hasFailures
        } recover { case t: Throwable =>
          t.printStackTrace()
          false
        }
      } else {
        // Nothing to delete
        Future.successful(true)
      }
    }
  }

  /** Returns the system-wide contribution stats **/
  def getGlobalStats()(implicit context: ExecutionContext) =
    ES.client execute {
      search in ES.IDX_RECOGITO / CONTRIBUTION aggs (
        aggregation terms "by_user" field "made_by",
        aggregation terms "by_action" field "action",
        aggregation nested("by_item_type") path "affects_item" aggs (
          aggregation terms "item_type" field "affects_item.item_type"
        ),
        aggregation filter "contribution_history" filter (rangeFilter("made_at") from "now-30d") aggs (
           aggregation datehistogram "last_30_days" field "made_at" minDocCount 0 interval DateHistogram.Interval.DAY
        )
      ) limit 0
    } map { response =>
      val byUser = response.getAggregations.get("by_user").asInstanceOf[Terms]
      val byAction = response.getAggregations.get("by_action").asInstanceOf[Terms]
      val byItemType = response.getAggregations.get("by_item_type").asInstanceOf[Nested]
        .getAggregations.get("item_type").asInstanceOf[Terms]
      val contributionHistory = response.getAggregations.get("contribution_history").asInstanceOf[Filter]
        .getAggregations.get("last_30_days").asInstanceOf[DateHistogram]

      ContributionStats(
        response.getTookInMillis,
        response.getHits.getTotalHits,
        byUser.getBuckets.asScala.map(bucket =>
          (bucket.getKey, bucket.getDocCount)),
        byAction.getBuckets.asScala.map(bucket =>
          (ContributionAction.withName(bucket.getKey), bucket.getDocCount)),
        byItemType.getBuckets.asScala.map(bucket =>
          (ItemType.withName(bucket.getKey), bucket.getDocCount)),
        contributionHistory.getBuckets.asScala.map(bucket =>
          (new DateTime(bucket.getKeyAsDate.getMillis, DateTimeZone.UTC), bucket.getDocCount))
      )
    }

}