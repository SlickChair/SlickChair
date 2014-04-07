// package controllers

// import org.joda.time.{DateTime, Seconds}
// import play.api.templates.Html
// import models.{Author, Authors, NewPaper, Paper, PaperType}
// import models.{Papers, Topics}
// import models.PaperType.PaperType
// import models.User
// import models.{PaperTopic, PaperTopics}
// import models.{Files, NewFile}
// import play.api.data.Form
// import play.api.data.Forms.{ignored, list, mapping, nonEmptyText, number, text}
// import play.api.data.Mapping
// import play.api.mvc.{Controller, Cookie}
// import securesocial.core.SecureSocial

// import play.api.db.slick.Config.driver.simple._
// import play.api.Play.current
// import play.api.db.slick.DB

// case class SubmissionForm(
//   paper: Paper,
//   authors: List[Author],
//   topics: List[Int]
// )

// object Submitting extends Controller with SecureSocial {
//   val PaperTypeMapping: Mapping[PaperType] = mapping(
//     "value" -> nonEmptyText)(PaperType.withName(_))(Some(_).map(_.toString))
  
//   val paperMapping: Mapping[Paper] = mapping(
//     // Sementicaly values at null.asInstanceOf[] need to be set when handling
//     // the POST on a form before storing the Paper in the database.
//     "id" -> ignored(null.asInstanceOf[Int]),
//     "contactemail" -> ignored(null.asInstanceOf[String]),
//     "contactfirstname" -> ignored(null.asInstanceOf[String]),
//     "contactlastname" -> ignored(null.asInstanceOf[String]),
//     "submissiondate" -> ignored(null.asInstanceOf[DateTime]),
//     "lastupdate" -> ignored(null.asInstanceOf[DateTime]),
//     "accepted" -> ignored(null.asInstanceOf[Option[Boolean]]),
//     "title" -> nonEmptyText,
//     "format" -> PaperTypeMapping,
//     "keywords" -> nonEmptyText,
//     "abstrct" -> nonEmptyText,
//     "fileid" -> ignored(null.asInstanceOf[Option[Int]])
//   )(Paper.apply _)(Paper.unapply _)
  
//   val authorMapping: Mapping[Author] = mapping(
//     "paperid" -> ignored(null.asInstanceOf[Int]),
//     "position" -> ignored(null.asInstanceOf[Int]),
//     "firstname" -> text,
//     "lastname" -> text,
//     "organization" -> text,
//     "email" -> text
//   )(Author.apply _)(Author.unapply _)
    
//   /** Form for submissions with appropriate type mapping. */
//   val submissionForm: Form[SubmissionForm] = Form(
//     mapping(
//       "paper" -> paperMapping,
//       "nauthors" -> number,
//       "authors" -> list(authorMapping),
//       "topics" -> list(number).verifying("Please select at least one topic.", _.nonEmpty)
//     )((paper, nauthors, authors, topics) =>
//         SubmissionForm(paper, authors.take(nauthors).zipWithIndex.map{case (a, i) => a.copy(position = i)}, topics))
//       // After calling this function authors positions are constistant but
//       // authors paperid and paper {id, contactemail, submissiondate,
//       // lastupdate, accepted, fileid} are still null.
//      (submissionForm =>
//         Some((submissionForm.paper, submissionForm.authors.size, submissionForm.authors, submissionForm.topics)))
//   )
  
//   def form = SecuredAction { implicit request =>
//     val email = request.user.email.get
//     val page = Papers.withEmail(email) match {
//       case None =>
//         views.html.submissiontemplate("New Submission", submissionForm, Some(email)) _
//       case Some(paper) =>
//         // TODO: Authors are not populated...
//         def incBind[T](form: Form[T], data: Map[String, String]) = form.bind(form.data ++ data)
//         val existingSubmissionForm = incBind(
//           submissionForm.fill(SubmissionForm(paper, Authors.of(paper), List())),
//           Topics.of(paper).map(topic => ("topics[%s]".format(topic.id), topic.id.toString)).toMap
//         )
//         views.html.submissiontemplate("Edit Submission", existingSubmissionForm, Some(email)) _
//       }
//     val CookieName = "not-first-login"
//     request.cookies.get(CookieName) match {
//       case None =>
//         val now = DateTime.now()
//         val maxAge = Some(Seconds.secondsBetween(now, now.plusYears(1)).getSeconds())
//         val modal = views.html.modal("First login", withCloseButton=true)(
//           Html("Warnning, this is your first login in play")
//         )
//         Ok(page(modal)).withCookies(Cookie(CookieName, "", maxAge=maxAge))
//       case Some(c) =>
//         Ok(page(Html("")))
//     }
    
//   }
  
//   /** Handles submissions. Creates or update database entry with data of the form. */
//   def make = SecuredAction(parse.multipartFormData) { implicit request =>
//   // def make = SecuredAction(false, None, parse.multipartFormData) { implicit request =>
//     val user = User.fromIdentity(request.user)
//     val email = user.email
//     submissionForm.bindFromRequest.fold(
//       // TODO: if the form is not js validated we might want to save the
//       //       uploaded file in case of errors. Otherwise the user will
//       //       have to select it again.
//       errors => Ok(views.html.submissiontemplate(email + "Submission: Errors found", errors, Some(email))(Html(""))),
//       form => {
//         // Ok(form.toString)  
//         val newFileId: Option[Int] = request.body.file("data").map{ file =>
//           val blob = scalax.io.Resource.fromFile(file.ref.file).byteArray
//           Files.ins(NewFile(file.filename, blob.size, DateTime.now, blob))
//         }
        
//         val paperId = Papers.withEmail(email) match {
//           case None =>
//             Papers.ins(NewPaper(
//               contactemail = email,
//               contactfirstname = request.user.firstName,
//               contactlastname = request.user.lastName,
//               submissiondate = DateTime.now,
//               lastupdate = DateTime.now,
//               accepted = None,
//               form.paper.title,
//               form.paper.format,
//               form.paper.keywords,
//               form.paper.abstrct,
//               fileid = newFileId
//             ))
//           case Some(dbPaper) =>
//             newFileId.map{ _ => dbPaper.fileid.map(i => Files.delete(i)) }
//             Authors.deleteFor(dbPaper)
//             PaperTopics.deleteFor(dbPaper)
//             Papers.updt(form.paper.copy(
//               id = dbPaper.id,
//               contactemail = dbPaper.contactemail, // == email by construction
//               submissiondate = dbPaper.submissiondate,
//               lastupdate = DateTime.now,
//               accepted = dbPaper.accepted,
//               fileid = newFileId.orElse(dbPaper.fileid)
//             ))
//             dbPaper.id
//         }
        
//         Authors.insertAll(form.authors.map(_.copy(paperId)))
//         PaperTopics.insertAll(form.topics.map(PaperTopic(paperId, _)))
//         Redirect(routes.Submitting.info)
//       }
//     )
//   }
  
//   // TODO: Remove?
//   def info = SecuredAction { implicit request =>
//     DB.withSession{ implicit s: Session =>
//       Papers.withEmail(request.user.email.get) match {
//         case None => // TODO: Needed??
//           Redirect(routes.Submitting.form)
//         case Some(paper) =>
//           Ok(views.html.submissioninfo(
//             paper,
//             Authors.of(paper),
//             Topics.of(paper)
//           ))
//       }
//     }
//   }
// }
