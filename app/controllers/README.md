See playframework.com/documentation/2.1.1/ScalaForms (-> Constructing complex
objects) and github.com/playframework/Play20/blob/master/samples/scala/forms/a
pp/controllers/SignUp.scala to understand how Froms work in Play2.

    def incBind[T](form: Form[T], data: Map[String, String]) =
      form.bind(form.data ++ data)
    val existingBidForm = incBind(
      Topics.of(member).map(topic => ("topics[%s]".format(topic.id), topic.id.toString)).toMap)
