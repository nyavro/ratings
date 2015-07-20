/**
 * Copyright 2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.lang.reflect.Constructor

import akka.actor.ActorSystem
import com.eny.service.impl.ActorsBasedUsersService
import configuration.ApplicationContext
import controllers.CustomRoutesService
import provider.{FacebookProviderEx, GoogleProviderEx}
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.{FacebookProvider, GoogleProvider, LinkedInProvider, TwitterProvider}
import service._

import scala.collection.immutable.ListMap

object Global extends play.api.GlobalSettings {

  object CustomRuntimeEnv extends RuntimeEnvironment.Default[MultiProfileUser] {
    lazy val actorSystem = ActorSystem("actorSystem")
    override lazy val routes = new CustomRoutesService()
    override lazy val userService = new PersistentUserService(new ActorsBasedUsersService(actorSystem))
    override lazy val eventListeners = List(new CustomEventListener())
    override lazy val viewTemplates = new CustomViewTemplates(this)
    override lazy val providers = ListMap(
      // oauth 2 client providers
      include(new FacebookProviderEx(routes, cacheService, oauth2ClientFor(FacebookProvider.Facebook))),
      include(new GoogleProviderEx(routes, cacheService, oauth2ClientFor(GoogleProvider.Google))),
      // oauth 1 client providers
      include(new LinkedInProvider(routes, cacheService, oauth1ClientFor(LinkedInProvider.LinkedIn))),
      include(new TwitterProvider(routes, cacheService, oauth1ClientFor(TwitterProvider.Twitter)))
    )
  }

  /**
   * An implementation that checks if the controller expects a RuntimeEnvironment and
   * passes the instance to it if required.
   *
   * This can be replaced by any DI framework to inject it differently.
   *
   * @param controllerClass
   * @tparam A
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[MultiProfileUser]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(CustomRuntimeEnv)
    }.getOrElse {
      controllerClass.getConstructors.find { c =>
        val params: Array[Class[_]] = c.getParameterTypes
        params.length == 3 &&
        params(0) == classOf[RuntimeEnvironment[MultiProfileUser]] &&
        params(1) == classOf[ActorSystem] &&
        params(2) == classOf[ApplicationContext]
      }.map {
        _.asInstanceOf[Constructor[A]].newInstance(CustomRuntimeEnv, CustomRuntimeEnv.actorSystem, ApplicationContext)
      }.getOrElse(
        super.getControllerInstance(controllerClass)
      )
    }
  }
}
