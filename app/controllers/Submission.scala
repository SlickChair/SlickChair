package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import securesocial.core.SecureSocial
import org.joda.time.DateTime

import models._
import models.entities._
import models.relations._
import models.entities.PaperFormat._
import securesocial.core.providers.utils._

case class SubmissionForm(
  paper: Paper,
  authors: List[Author],
  topics: List[Int]
)

object Submission extends Controller with SecureSocial {
  val paperFormatMapping: Mapping[PaperFormat] = mapping(
    "value" -> nonEmptyText)(PaperFormat.withName(_))(Some(_).map(_.toString))
  
  val paperMapping: Mapping[Paper] = mapping(
    // Sementicaly values at null.asInstanceOf[] need to be set when handling
    // the POST on a form before storing the Paper in the database.
    "id" -> ignored(null.asInstanceOf[Option[Int]]),
    "contactemail" -> ignored(null.asInstanceOf[String]),
    "submissiondate" -> ignored(null.asInstanceOf[DateTime]),
    "lastupdate" -> ignored(null.asInstanceOf[DateTime]),
    "accepted" -> ignored(null.asInstanceOf[Option[Boolean]]),
    "title" -> nonEmptyText,
    "format" -> paperFormatMapping,
    "keywords" -> nonEmptyText,
    "abstrct" -> nonEmptyText,
    "fileid" -> ignored(null.asInstanceOf[Option[Int]])
  )(Paper.apply _)(Paper.unapply _)
  
  val authorMapping: Mapping[Author] = mapping(
    "paperid" -> ignored(null.asInstanceOf[Int]),
    "position" -> ignored(null.asInstanceOf[Int]),
    "firstname" -> text,
    "lastname" -> text,
    "organization" -> text,
    "email" -> text
  )(Author.apply _)(Author.unapply _)
    
  val submissionForm: Form[SubmissionForm] = Form(
    mapping(
      "paper" -> paperMapping,
      "nauthors" -> number,
      "authors" -> list(authorMapping),
      "topics" -> list(number).verifying("Please select at least one topic.", !_.isEmpty)
    )((paper, nauthors, authors, topics) =>
        SubmissionForm(paper, authors.take(nauthors).zipWithIndex.map{case (a, i) => a.copy(position = i)}, topics))
      // After calling this function authors positions are constistant but
      // authors paperid and paper {id, contactemail, submissiondate,
      // lastupdate, accepted, fileid} are still null.
     (submissionForm =>
        Some(submissionForm.paper, submissionForm.authors.size, submissionForm.authors, submissionForm.topics))
  )
  
  private def incBind[T](form: Form[T], data: Map[String, String]) = form.bind(form.data ++ data)
  
  def form = SecuredAction{ implicit request =>
    Papers.withEmail(request.user.email.get) match {
      case None =>
        Ok(views.html.submission(request.user.email + "New Submission", submissionForm))
      case Some(paper) =>
        val existingSubmissionForm = incBind(
          submissionForm.fill(SubmissionForm(paper, Authors.of(paper), List())),
          PaperTopics.of(paper.id.get).map(t => ("topics[%s]".format(t.topicid), t.topicid.toString)).toMap
        )
        Ok(views.html.submission(request.user.email.get + "Edit Submission", existingSubmissionForm))
    }
  }
  
  def make = UserAwareAction(parse.multipartFormData) { implicit request =>
    request.user match {
      case None =>
        Redirect(routes.Submission.form)
      case Some(u) =>
        val email = u.email.get
        submissionForm.bindFromRequest.fold(
          // TODO: if the form is not js validated we might want to save the
          //       uploaded file in case of errors. Otherwise the user will
          //       have to select it again.
          errors => Ok(views.html.submission(email + "Submission: Errors found", errors)),
          form => {
              val SubmissionForm(formPaper, formAuthors, formTopics) = form 
              val newFileId: Option[Int] = request.body.file("data").map{ file =>
                val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
                Files.ins(File(None, file.filename, blob.size, DateTime.now, blob))
              }
              
              val paperId = Papers.withEmail(email) match {
                case None =>
                  Papers.ins(formPaper.copy(
                    id = None, // auto-increment
                    contactemail = email,
                    submissiondate = DateTime.now,
                    lastupdate = DateTime.now,
                    accepted = None, // default
                    fileid = newFileId
                  ))
                case Some(dbPaper) =>
                  newFileId.map{ _ => dbPaper.fileid.map(i => Files.delete(i)) }
                  Authors.deleteFor(dbPaper)
                  PaperTopics.deleteFor(dbPaper)
                  Papers.updt(formPaper.copy(
                    id = dbPaper.id,
                    contactemail = email, // == dbPaper.email by construction
                    submissiondate = dbPaper.submissiondate,
                    lastupdate = DateTime.now,
                    accepted = dbPaper.accepted,
                    fileid = newFileId.orElse(dbPaper.fileid)
                  ))
                  dbPaper.id.get
              }
              
              Authors.createAll(formAuthors.map(_.copy(paperId)))
              PaperTopics.createAll(formTopics.map(PaperTopic(paperId, _)))
              Redirect(routes.Submission.info)
          }
        )
    }
  }
  
  def info = SecuredAction{ implicit request =>
    Papers.withEmail(request.user.email.get) match {
      case None =>
        Redirect(routes.Submission.form)
      case Some(paper) =>
        Ok(views.html.submissioninfo(
          paper,
          Authors.of(paper),
          PaperTopics.ofPaper(paper)
        ))
    }
  }
}