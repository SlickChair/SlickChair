Models
======
The model package hold all the classes related to the database access. This project makes use of Slick, which to some extents allows to manipulate databases with a syntax similar to the Scala collections. If you don't know Slick and want to learn more about it here are two good staring points:
- Very good [talk][1] explaining Slick from scratch (1 hour long).
- The official [documentation][2] with lots of good examples.

Slick is supposed to be database independent. However switching from H2 to PostgreSQL (for the [Heroku][3] deployment) we had a few issues that could only be solved with some added verbosity:
- Slick's PostgreSQL driver maps Scala String to 254 character bounded String. ([workaround][4])
- Auto-incrementing cannot be done by passing a None id. ([workaround][5])

[1]: https://www.youtube.com/watch?v=mJ_mnEwZMR0
[2]: http://slick.typesafe.com/doc/1.0.1/gettingstarted.html
[3]: https://www.heroku.com/
[4]: https://groups.google.com/forum/#!topic/scalaquery/6OgrKS8PrKE
[5]: http://stackoverflow.com/questions/13199198/using-auto-incrementing-fields-with-postgresql-and-slick
