# SlickChair

SlickChair is an open-source conference management system written in Scala. Built with the Play framework and the Slick database access library, SlickChair provides a highly flexible and extensible solution to manage a peer review process. More information on the [project report](https://github.com/OlivierBlanvillain/SlickChair-report/blob/master/report/main.pdf).


## What Makes It Different?

- **Simple authentication.** SlickChair allows to login in one click using a Facebook or Google account in addition to the traditional email and password authentication. We made thing very simple here: one user equals one email!

- **Extensible workflow.** By default, the peer review process is organised in seven phases: *Setup, Submission, Bidding, Assignment, Review, Notification* and *Finished*. Workflow customisation such as adding an *Abstract Submission* phase or merging *Submission* and *Bidding* are very easy to make.

- **Full control over your data.** SlickChair instances are completely autonomous, which means that you get the option to deploy yours on a local server with a private database.


## Show Me A Demo!

Running the demo locally requires [JDK 6+](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [sbt](http://www.scala-sbt.org/). Then it's as simple as:

    sbt run


## Deploying in production

This section describes the steps required to deploy SlickChair production in on Heroku. All configuration must be stored in `conf/prod.conf`.

First create a new Google account (mandatory, the password is stored in the config file), and new Facebook/Heroku accounts if necessary:

- <https://accounts.google.com/SignUp?service=mail>
- <https://www.facebook.com/r.php>
- <https://id.heroku.com/login>

Enter the new Google credential in `conf/prod.conf` under `smtp`.

Change `application.secret` to a randomly generated string; this command can be used to generate one:

- `tr -cd '[:alnum:]' < /dev/urandom | head -c64`

Register your instance to login with Facebook accounts:

- <https://developers.facebook.com/>
- Apps; Register as Developer; pass the phone validation 
- Apps; Create a New App
- Settings; Basic; Provide a "Contact Email"
- Settings; Advanced; Set "Valid OAuth redirect URIs" to <http://your-url/authenticate/facebook>
- Status & Review; enable "make this app available to the general public?"
- Dashboard; Copy "App ID" and "App Secret" to `securesocial.facebook.clientId/clientSecret`

Register your instance to login with Google accounts:

- <https://console.developers.google.com>
- "create project"; open it
- APIs & auth; Credentials; "create new client id"
- Set "authorized redirect URI" to <http://your-url/authenticate/google>
- Copy "Client ID" and "Client secret" to `securesocial.google.clientId/clientSecret`
- <https://accounts.google.com/>
- click /!\ next to Phone; Verify number now; verify now

Deploy to Heroku:

- Install <https://toolbelt.heroku.com/>
- `git commit -am Configured`
- `heroku login`
- `heroku create`
- `git push heroku master`
- `heroku open`
