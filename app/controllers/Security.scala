package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import models.Users
import models.Roles._
import models.generated.tables.records.UsersRecord
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.{ ClassTag, classTag }
import play.api.mvc.{ Result, Results, RequestHeader }

trait Security extends AuthConfig { self: HasDatabase =>
  
  private val NO_PERMISSION = "No permission"

  type Id = String
 
  type User = UsersRecord

  type Authority = Role

  val idTag: ClassTag[Id] = classTag[Id]
  
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] =
    Users.findByUsername(id)(db)

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Results.Redirect(routes.MyRecogito.index()))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Results.Redirect(landing.routes.LandingController.index))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Results.Redirect(landing.routes.LandingController.index))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Results.Forbidden(NO_PERMISSION))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    true
  }
  
}
