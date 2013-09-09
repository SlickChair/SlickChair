Deploying to Heroku
===================

[Heroku][1] provides a handy infrastructure for hosting web applications.
Automatic database backups and scalability to multiple servers are also
commercially available.

To get started:

- [Install Git][2]
- [Install the Heroku Toolbelt][3]
- [Sign up for a Heroku account][4]

Clone the SlickChair repository:

    git clone https://github.com/SlickChair/SlickChair

Now edit `conf/prod.conf` with your Secret key, SMTP settings and
Google/Facebook login setting as described in the [running in production][5]
document. You can skip the database configuration, Heroku will take care of
it.

Commit your configuration changes:

    cd SlickChair
    git commit -am Configured

Create a new application on Heroku:
    
    heroku create

Deploy your new SlickChair instance:

    git push heroku master

On the first deployment, all dependencies will be downloaded, which takes a
while to complete. Finally you can open our application with:

    heroku open

Note that to use the Google/Facebook login you will likely have to set the
callback urls so that [Google][6] and [Facebook][7] redirect the user back to
SlickChair.

[1]: https://www.heroku.com/
[2]: http://git-scm.com/downloads
[3]: http://toolbelt.heroku.com/
[4]: http://heroku.com/signup
[5]: running-in-production.md
[6]: TODO
[7]: TODO
