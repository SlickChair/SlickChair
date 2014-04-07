// package controllers

// import securesocial.core._
// import play.api.mvc.{Session, RequestHeader}
// import play.api.{Application, Logger}

// class LoginEventListener(app: Application) extends EventListener {
//   override def id: String = "my_event_listener"

//   def onEvent(event: Event, request: RequestHeader, session: Session): Option[Session] = {
//     event match {
//       case LoginEvent(identity) =>
//         Logger.info("LOGINEVENTLISTENER DETECTED A LOGINEVENT." + identity.avatarUrl.map(x => "avatarUrl")) // TODO REMOVE
//         identity.avatarUrl.map(x => session + (controllers.LoginWrapper.WELCOMED -> ""))
//       case _ => None
//       // case SignUpEvent(identity) =>
//       // case LogoutEvent(identity) => "logout"
//       // case PasswordResetEvent(identity) => "password reset"
//       // case PasswordChangeEvent(identity) => "password change" // user: Identity
//     }
//   }
// }
