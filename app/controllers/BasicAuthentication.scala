package controllers

import model.{User, UserRepository}
import play.api.mvc.{Action, AnyContent, Controller, Request}

object BasicAuth {
  def logged[T](request: Request[T]): Option[User] = {
    request.cookies.get("auth").map(auth => {
      UserRepository.userExists(auth.value)
    }).headOption.getOrElse(None)
  }
}