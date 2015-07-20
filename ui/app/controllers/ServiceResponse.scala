package controllers

class ServiceResponse[T](message:String, code:Int, data:Option[T])

object ServiceResponse {
  def apply[T](message:String, code:Int) = new ServiceResponse[T](message, code, None)
  def apply[T](message:String, code:Int, value:T) = new ServiceResponse[T](message, code, Some(value))
}
