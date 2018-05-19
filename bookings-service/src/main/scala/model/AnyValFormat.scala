package model

import play.api.libs.json.{JsValue, Json, Reads, Writes}

case class AnyValFormat[I, T](box: I => T)(unbox: T => Option[I])(
    implicit reads: Reads[I],
    writes: Writes[I])
    extends Reads[T]
    with Writes[T] {
  def reads(js: JsValue) = js.validate[I] map box
  def writes(value: T) = Json.toJson(unbox(value))
}
