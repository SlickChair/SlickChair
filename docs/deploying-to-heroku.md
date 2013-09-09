Deploying to Heroku
===================

[Heroku][5] provides a handy infrastructure for hosting web applications. Within a few minutes you will be able to deploy your SlickChair instance online. Automatic database backups and scalability to multiple machines are also commercially available.

To get started:

- [Install Git][6]
- [Install the Heroku Toolbelt][7]
- [Sign up for a Heroku account][8]

Clone the SlickChair repository:

    git clone https://github.com/SlickChair/SlickChair

Create a new application on Heroku:
    
    heroku create

Deploy your new instance of SlickChair

    cd SlickChair
    git push heroku master
    
Open your SlickChair instance

    heroku open


[6]: http://git-scm.com/downloads
[7]: http://toolbelt.heroku.com/
[8]: http://heroku.com/signup
