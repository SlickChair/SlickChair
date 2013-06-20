A Conference Management System in Scala
=======================================

Done:
-----

- Login mechanism with Google/Facebook/email+password
- Page for paper submission with authors, topics and file upload
- View submitted information and edit submission
- SQL form
- Make a page per paper with possibility to write reviews
- Add edit and comments to the paper pages
- Write a classes for settings with get set and default value
- Email client for the chair with templates
- Manage members (invitations, delete, promotion..)

Todo:
-----

- Paper biding and assignement
- Good looking interface
- Setup instructions (requires some testing with postgresql)
- Wrap most settings in a config page
- Use the [POST-Redirect-GET pattern][1] in all the forms to prevent repost on
  page reload

Out of scope:
-------------

- Logs
- More settings
- Phases abstraction for settings
- Client side stuff (starting with form validation)
- Write an out of scope list...

[1]: http://www.theserverside.com/news/1365146/Redirect-After-Post