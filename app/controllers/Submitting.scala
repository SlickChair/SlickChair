package controllers

import org.joda.time.DateTime
import models.entities.{Author, Authors, Paper, PaperType}
import models.entities.{Papers, Topics}
import models.entities.PaperType.PaperType
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, nonEmptyText, number, text}
import play.api.data.Mapping
import play.api.mvc.Controller
import securesocial.core.SecureSocial

case class SubmissionForm(
  paper: Paper,
  authors: List[Author],
  topics: List[Int]
)

object Submitting extends Controller with SecureSocial {
  val PaperTypeMapping: Mapping[PaperType] = mapping(
    "value" -> nonEmptyText)(PaperType.withName(_))(Some(_).map(_.toString))
  
  val paperMapping: Mapping[Paper] = mapping(
    // Sementicaly values at null.asInstanceOf[] need to be set when handling
    // the POST on a form before storing the Paper in the database.
    "id" -> ignored(null.asInstanceOf[Int]),
    "contactemail" -> ignored(null.asInstanceOf[String]),
    "submissiondate" -> ignored(null.asInstanceOf[DateTime]),
    "lastupdate" -> ignored(null.asInstanceOf[DateTime]),
    "accepted" -> ignored(null.asInstanceOf[Option[Boolean]]),
    "title" -> nonEmptyText,
    "format" -> PaperTypeMapping,
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
      "topics" -> list(number).verifying("Please select at least one topic.", _.nonEmpty)
    )((paper, nauthors, authors, topics) =>
        SubmissionForm(paper, authors.take(nauthors).zipWithIndex.map{case (a, i) => a.copy(position = i)}, topics))
      // After calling this function authors positions are constistant but
      // authors paperid and paper {id, contactemail, submissiondate,
      // lastupdate, accepted, fileid} are still null.
     (submissionForm =>
        Some(submissionForm.paper, submissionForm.authors.size, submissionForm.authors, submissionForm.topics))
  )
  
  def form = SecuredAction { implicit request =>
    Papers.withEmail(request.user.email.get) match {
      case None =>
        Ok(views.html.submission("New Submission", submissionForm))
      case Some(paper) =>
        def incBind[T](form: Form[T], data: Map[String, String]) = form.bind(form.data ++ data)
        val existingSubmissionForm = incBind(
          submissionForm.fill(SubmissionForm(paper, Authors.of(paper), List())),
          Topics.of(paper).map(topic => ("topics[%s]".format(topic.id), topic.id.toString)).toMap
        )
        Ok(views.html.submission("Edit Submission", existingSubmissionForm))
    }
  }
  
  def make = UserAwareAction(parse.multipartFormData) { implicit request =>
    request.user match {
      case None =>
        Redirect(routes.Submitting.form)
      case Some(u) =>
        val email = u.email.get
        submissionForm.bindFromRequest.fold(
          // TODO: if the form is not js validated we might want to save the
          //       uploaded file in case of errors. Otherwise the user will
          //       have to select it again.
          errors => Ok(views.html.submission(email + "Submission: Errors found", errors)),
          form => {
            Ok(form.toString)
            /*val SubmissionForm(formPaper, formAuthors, formTopics) = form 
            val newFileId: Option[Int] = request.body.file("data").map{ file =>
              val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
              Files.ins(NewFile(file.filename, blob.size, DateTime.now, blob))
            }
            
            val paperId = Papers.withEmail(email) match {
              case None =>
                Papers.ins(NewPaper(
                  contactemail = email,
                  submissiondate = DateTime.now,
                  lastupdate = DateTime.now,
                  accepted = None,
                  formPaper.title,
                  formPaper.format,
                  formPaper.keywords,
                  formPaper.abstrct,
                  fileid = newFileId
                ))
              case Some(dbPaper) =>
                newFileId.map{ _ => dbPaper.fileid.map(i => Files.delete(i)) }
                Authors.deleteFor(dbPaper)
                PaperTopics.deleteFor(dbPaper)
                Papers.updt(formPaper.copy(
                  id = dbPaper.id,
                  contactemail = dbPaper.contactemail, // == email by construction
                  submissiondate = dbPaper.submissiondate,
                  lastupdate = DateTime.now,
                  accepted = dbPaper.accepted,
                  fileid = newFileId.orElse(dbPaper.fileid)
                ))
                dbPaper.id
            }
            
            Authors.insertAll(formAuthors.map(_.copy(paperId)))
            PaperTopics.insertAll(formTopics.map(PaperTopic(paperId, _)))
            Redirect(routes.Submitting.info)*/
          }
        )
    }
  }
  
  def info = SecuredAction { implicit request =>
    Papers.withEmail(request.user.email.get) match {
      case None =>
        Redirect(routes.Submitting.form)
      case Some(paper) =>
        Ok(views.html.submissioninfo(
          paper,
          Authors.of(paper),
          Topics.of(paper)
        ))
    }
  }
}