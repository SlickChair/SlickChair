Running in production
=====================

The instance that you obtain by following the getting started instructions is
configured for testing/development. A few additional steps are required to
setup your SlickChair instance in production.


Database
--------

The SlickChair demo works with an in-memory H2 database that should only be
used for testing purposes. The first thing to do to deploy deployment is to
setup more robust database. SlickChair uses [Slick][1] to access databases,
which means that it works with most of the [popular database systems][2].

Once you have a database system up and running, you need to configure
SlickChair to use it by editing the following lines of `conf/prod.conf`:

**TODO: create and link**

    # Database configuration
    db.default.driver=org.CHANGE_TO_DATABASE_NAME.Driver
    db.default.url="CHANGE_TO_DATABASE_URL"

Note that if you plan to [deploy your instance on Heroku][5], you can skip
this step entirely. The database driver (postgresql) and url are overwritten
by Heroku.


Secret key
----------

Play uses a secret key use to secure cryptographic functions. You first need
to generate a new one:

    $ cat /dev/urandom | tr -cd '[:alnum:]' | fold -w65 | head -n1
    RAfar4lHJ1fnJNaQ8yQemwrGuA0pISJ4qCxqNjRALRxNtcEaaEkgl3Rd0wsPPjgCH
    
And set it in `conf/prod.conf`:

    # Secret key
    application.secret="A_RANDOMELY_GENERATED_65_CHARACTERS_LONG_STRING_FOR_CRYPTOGRAPHY"


SMTP
----

To be able to send emails through SlickChair you need to configure a SMTP server:

    # SMTP settings
    smtp {
      host=smtp.gmail.com
      #port=25
      ssl=true
      user="CHANGE_TO_YOUR_EMAIL@gmail.com"
      password=CHANGE_TO_YOUR_PASSWORD
      from="CHANGE_TO_YOUR_EMAIL@gmail.com"
    }


Google/Facebook login
---------------------

The login via Google/Facebook requires your SlickChair instance to be
registered as a Google/Facebook application. If you don't have accounts on
these networks, first create one:

- [Google (mobile phone confirmation)][3]
- [Facebook (email confirmation)][4]

**TODO: Check names**

During the application registration, you will be asked to provide a call back
url. If you do not yet have a domain name for your conference, or simply want
to test locally, set it to `http://localhost:9000/login`. You can change it
later from the same interfaces.

- [Google application registration][3]
- [Facebook application registration][4]

Finally, set the `clientId` and `clientSecret` in `conf/prod.conf`:

**TODO: hide constants?**

    facebook {
      authorizationUrl="https://graph.facebook.com/oauth/authorize"
      accessTokenUrl="https://graph.facebook.com/oauth/access_token"
      clientId=CHANGE_TO_YOUR_CLIENT_ID
      clientSecret=CHANGE_TO_YOUR_CLIENT_SECRET
      scope=email
    }
    
    google {
      authorizationUrl="https://accounts.google.com/o/oauth2/auth"
      accessTokenUrl="https://accounts.google.com/o/oauth2/token"
      clientId=CHANGE_TO_YOUR_CLIENT_ID
      clientSecret=CHANGE_TO_YOUR_CLIENT_SECRET
      scope="https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email"
    }
  
**TODO: real link for [3, 4, 5]**

[1]: http://slick.typesafe.com/
[2]: http://slick.typesafe.com/doc/1.0.1/introduction.html#supported-database-systems
[3]: google.account
[4]: facebook.account
[5]: deploying-to-heroku.md

