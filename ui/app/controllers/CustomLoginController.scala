package controllers

import play.api.Logger
import play.api.mvc.{Action, AnyContent, RequestHeader}
import securesocial.controllers.BaseLoginPage
import securesocial.core.services.RoutesService
import securesocial.core.{IdentityProvider, RuntimeEnvironment}
import service.MultiProfileUser

class CustomLoginController(implicit override val env: RuntimeEnvironment[MultiProfileUser]) extends BaseLoginPage[MultiProfileUser] {
  override def login: Action[AnyContent] = {
    Logger.debug("using CustomLoginController")
    super.login
  }
}

class CustomRoutesService extends RoutesService.Default {
  override def loginPageUrl(implicit req: RequestHeader): String = controllers.routes.CustomLoginController.login().absoluteURL(IdentityProvider.sslEnabled)
}