package controllers.my.upload

object Messages {

  sealed abstract trait Message

  case object Start extends Message

  case object QueryProgress extends Message

  case class WorkerProgress(filepartId: Int, status: ProgressStatus.Value, progress: Double)

  case class DocumentProgress(documentId: String, progress: Seq[WorkerProgress]) extends Message

  case object TimedOut extends Message

  case class Failed(msg: String) extends Message

  case object Completed extends Message

}

object ProgressStatus extends Enumeration {

  val PENDING = Value("PENDING")
  
  val IN_PROGRESS = Value("IN_PROGRESS")

  val COMPLETED = Value("COMPLETED")
  
  val FAILED = Value("FAILED")

}