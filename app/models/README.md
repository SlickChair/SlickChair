models.{ entities, relations, secureSocial, utils }
---------------------------------------------------

The model package hold all the classes related to the database access. This
project makes use of [Slick][6] to manipulate databases. It allows to write
database independent code to query and update tables with a syntax very close
to the one of the Scala collections. If you don't know Slick and want to learn
more about it, here are two good staring points:
- Very good [talk][1] explaining Slick from scratch (1 hour)
- The official [documentation][2] with lots of examples

Note that this package was not written with performances in mind. For
instance, some repeated operations generate multiple database query instead of
wrapping them all into a single transaction. This might be a bottleneck of the
system when used on a large scale.

Slick is supposed to be database independent. However switching from H2 to
PostgreSQL (to deploy the project [Heroku][3]) we had a few issues that could
only be solved with added verbosity:
- Slick's PostgreSQL driver maps Scala String to 254 character bounded String
  ([workaround][4])
- Auto-incrementing cannot be done by passing a None id ([workaround][5])

Hopefully the next versions of Slick will fix these issues and remove all
attribute duplication.

[1]: https://www.youtube.com/watch?v=mJ_mnEwZMR0
[2]: http://slick.typesafe.com/doc/1.0.1/gettingstarted.html
[3]: https://www.heroku.com/
[4]: https://groups.google.com/forum/#!topic/scalaquery/6OgrKS8PrKE
[5]: http://stackoverflow.com/questions/13199198/using-auto-incrementing-fields-with-postgresql-and-slick
[6]: http://slick.typesafe.com/
