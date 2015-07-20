package configuration

import java.util.ServiceLoader

import com.eny.rating.Agent

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

class ApplicationContext {
  def load[A](cls:Class[A]):Try[A] = {
    val loader: ServiceLoader[A] = ServiceLoader.load(cls)
    val instances = loader.iterator().toList
    instances.size match {
      case 0 => Failure(new Exception(s"No implementation of trait $cls found"))
      case 1 => Success(instances.head)
      case _ => Failure(new Exception(s"Multiple implementation of trait $cls found"))
    }
  }
  lazy val agent = load(classOf[Agent])
}

object ApplicationContext extends ApplicationContext
