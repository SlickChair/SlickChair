package views.html.helper

object FieldConstructors {
  implicit val horizontalForm = new FieldConstructor {
    def apply(e: FieldElements) = views.html.bootstrap3.bootstrap3FieldConstructor(e)
  }

  implicit val table = new FieldConstructor {
    def apply(e: FieldElements) = e.input
  }
}
