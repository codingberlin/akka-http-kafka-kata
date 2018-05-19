package model.api

case class ValidationErrors(path: Path, errors: Seq[ValidationError])
