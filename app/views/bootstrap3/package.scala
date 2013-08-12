package views.html.helper

package object bootstrap3 {
  implicit val bootstrap3Field = new FieldConstructor {
    def apply(e: FieldElements) = views.html.bootstrap3.bootstrap3FieldConstructor(e)
  }
}
