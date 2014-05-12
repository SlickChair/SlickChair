Work in progress
----------------

SlickChair is an open source conference management system written in Scala using the Play Framework.


Run it locally
--------------

To run SlickChair, you need the [Play Framework v2.2.0](http://www.playframework.com/documentation/2.2.0/Installing). Then simply clone this repository and start the application with play: 

- `git clone https://github.com/SlickChair/SlickChair`
- `cd SlickChair`
- `play start`
- `# open http://localhost:9000`


Deploying in production
-----------------------

This section describes the steps required to deploy SlickChair production in on Heroku. All configuration must be stored in `conf/prod.conf`.

- First create a new Google account (mandatory), and Facebook/Heroku accounts if needed:

    - <https://accounts.google.com/SignUp?service=mail>
    - <https://www.facebook.com/r.php>
    - <https://id.heroku.com/login>

- Enter the new Google credential in `conf/prod.conf` under `smtp` 

- Change `application.secret` to a randomly generated string; the following command can be used to generate one:

    - `tr -cd '[:alnum:]' < /dev/urandom | head -c64`

- Register your instance to login with Facebook accounts:

  - <https://developers.facebook.com/>
  - Apps; Register as Developer; pass the phone validation 
  - Apps; Create a New App
  - Settings; Basic; Provide a "Contact Email"
  - Settings; Advanced; Set "Valid OAuth redirect URIs" to <http://your-url/authenticate/facebook>
  - Status & Review; enable "make this app available to the general public?"
  - Dashboard; Copy "App ID" and "App Secret" to `securesocial.facebook.clientId/clientSecret`

- Register your instance to login with Google accounts:

  - <https://console.developers.google.com>
  - CREATE PROJECT; open it
  - APIs & auth; Credentials; CREAT NEW CLIENT ID
  - Set AUTHORIZED REDIRECT URI to <http://your-url/authenticate/google>
  - Copy "Client ID" and "Client secret" to `securesocial.google.clientId/clientSecret`

- Deploy to Heroku

  - Install <https://toolbelt.heroku.com/>
  - `git commit -am Configured`
  - `heroku login`
  - `heroku create`
  - `git push heroku master`
  - `heroku open`
