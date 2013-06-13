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
  authors: Seq[Author],
  topics: Seq[Int]
)

object Submission extends Controller with SecureSocial {
  // See playframework.com/documentation/2.1.1/ScalaForms (-> Constructing complex objects)
  // and github.com/playframework/Play20/blob/master/samples/scala/forms/app/controllers/SignUp.scala
  // to understand how Froms work in Play2.
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
      "authors" -> seq(authorMapping),
      "topics" -> seq(number).verifying("Please select at least one topic.", !_.isEmpty)
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
        // val es = submissionForm.fill(SubmissionForm(paper, Authors.of(paper.id.get), Seq()))
        val existingSubmission =  incBind(
          submissionForm.fill(SubmissionForm(paper, Authors.of(paper.id.get), Seq())),
          PaperTopics.of(paper.id.get).map(i => (s"topics[$i]", i.toString)).toMap
        )
        
        // es.bind(es.data ++ PaperTopics.of(paper.id.get).map(i => (s"topics[$i]", i.toString)))
        Ok(views.html.submission(request.user.email.get + "Edit Submission", existingSubmission))
    }
  }
  
  def make = UserAwareAction(parse.multipartFormData) { implicit request =>
    request.user match {
      case None =>
        Redirect(RoutesHelper.notAuthorized.absoluteURL())
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
                // Paper(Some(id), _, submissiondate, _, accepted, title, format, keywords, abstrct, fileid)
                  newFileId.map{ _ => dbPaper.fileid.map(i => Files.del(i)) }
                  Authors.del(dbPaper.id.get)
                  PaperTopics.del(dbPaper.id.get)
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
              
              formAuthors.foreach(author => Authors.ins(author.copy(paperId)))
              formTopics.foreach(topic => PaperTopics.ins(PaperTopic(paperId, topic)))
              Ok(Papers.withEmail(email).toString + "<br>" )
                // Authors.of(Papers.withEmail(email).map(_.id.get)
                  // + "<br>" + PaperTopics.of(Papers.withEmail(email).map(_.id.get))))
          }
        )
    }
  }
  
  def info = TODO
  
  // def edit(id: Int) = Action(
  //   Papers.withId(id) match {
  //     case None =>
  //       NotFound
  //     case Some(paper) =>
  //       val s = submissionForm.fill(SubmissionForm(paper, Authors.of(id), Seq()))
  //       val existingSubmission = s.bind(s.data ++ PaperTopics.of(id).map(i => (s"topics[$i]", i.toString)))
  //       Ok(views.html.submission("Edit Submission", routes.Submission.makeEdit(id), existingSubmission))
  //   }
  // )
  
  // def makeEdit(id: Int) =  Action(parse.multipartFormData) { implicit request =>
  //   submissionForm.bindFromRequest.fold(
  //     errors => Ok(views.html.submission(request.body.file("data").get.filename  + "Edit Submission: Errors found", routes.Submission.makeEdit(id), errors)),
  //     filled => filled match {
  //       case SubmissionForm(paper, authors, topics) => 
  //         val fileId = request.body.file("data").map{ file =>
  //           val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
  //           Files.ins(File(None, file.filename, blob.size, DateTime.now, blob))
  //         }
  //         val paperId: Int = Papers.ins(paper.copy(fileId))
  //         authors.foreach(author => Authors.ins(author.copy(paperId)))
  //         topics.foreach(topic => PaperTopics.ins(PaperTopic(paperId, topic)))
  //         Ok(filled.toString)
  //     }
  //   )
  // } 
}