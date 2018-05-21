package controller

import play.api.libs.json._

case class AnyValFormat[I, T](box: I => T)(unbox: T => Option[I])(
    implicit reads: Reads[I],
    writes: Writes[I])
    extends Format[T] {
  def reads(js: JsValue) = js.validate[I] map box
  def writes(value: T) = Json.toJson(unbox(value))
}

case class NonEmptyStringAnyValFormat[String, T](box: String => T)(
    unbox: T => Option[String])(implicit reads: Reads[String],
                                writes: Writes[String])
    extends Format[T] {
  def nonEmpty(value: String): JsResult[String] = value match {
    case "" =>
      JsError("must not be empty")
    case success =>
      JsSuccess(success)
  }

  def reads(js: JsValue) =
    js.validate[String]
      .flatMap(nonEmpty)
      .map(box)

  def writes(value: T) = Json.toJson(unbox(value))
}
