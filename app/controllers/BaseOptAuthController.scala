package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.document.{ DocumentAccessLevel, DocumentService }
import models.generated.tables.records.{ DocumentRecord, DocumentFilepartRecord }
import play.api.cache.CacheApi
import play.api.mvc.Result
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger
import scala.concurrent.Future
import storage.DB

abstract class BaseOptAuthController extends BaseController with HasCache with HasDatabase with OptionalAuthElement with Security {
  
  /** Helper that covers the boilerplate for all document views
    *
    * Just hand this method a function that produces an HTTP OK result for a document, while
    * the method handles Forbidden/Not Found error cases.
    */
  protected def documentResponse(documentId: String, maybeUser: Option[String],
      response: (DocumentRecord, Seq[DocumentFilepartRecord], DocumentAccessLevel) => Future[Result])(implicit cache: CacheApi, db: DB) = {

    DocumentService.findByIdWithFileparts(documentId, maybeUser).flatMap(_ match {
      case Some((document, fileparts, accesslevel)) => response(document, fileparts, accesslevel)
      case None => Future.successful(NotFoundPage)
    }).recover { case t =>
      t.printStackTrace()
      InternalServerError(t.getMessage)    
    }
  }
  
  /** Helper that covers the boilerplate for all document part views **/
  protected def documentPartResponse(documentId: String, partNo: Int, maybeUser: Option[String],
      response: (DocumentRecord, Seq[DocumentFilepartRecord], DocumentFilepartRecord, DocumentAccessLevel) => Future[Result])(implicit cache: CacheApi, db: DB) = {
    
    documentResponse(documentId, maybeUser, { case (document, fileparts, accesslevel) =>
      val selectedPart = fileparts.filter(_.getSequenceNo == partNo)
      if (selectedPart.isEmpty) {
        Future.successful(NotFoundPage)
      } else if (selectedPart.size == 1) {
        response(document, fileparts, selectedPart.head, accesslevel)
      } else {
        // More than one part with this sequence number - DB integrity broken!
        Logger.warn("Invalid document part:" + documentId + "/" + partNo) 
        Future.successful(InternalServerError)
      }
    })
  }
  
}