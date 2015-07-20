package dto

case class CategoryDto(
  name:String,
  description:String,
  id:Option[String] = None,
  parent:Option[String] = None
)
