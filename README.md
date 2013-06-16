A Conference Management System in Scala
=======================================

Done:
-----

- Login mechanism with Google/Facebook/email+password
- Page for paper submission with authors, topics and file upload
- View submitted information and edit submission
- SQL form


Todo:
-----

- Make a page per paper with possibility to write reviews
- Add edit and comments to the paper pages
- Email client for the chair with templates
- Invitation mechanism for chairs, reviewers and subreviewers
- Write a class for Settings access with default value
- Paper biding and assignement
- Good looking interface
- Setup instructions (requires some testing with postgresql)

- Wrap all the chair actions into web pages (instead of manual queries)
  - Logs for emails and events
  - Add topics
  - Delete papers, account, ...

Out of scope:
-------------

- Add and apply extra settings
- Phases abstraction for settings
- Internationalisation (should be easy with [Play](http://www.playframework.com/documentation/2.0/JavaI18N))
- Client side stuff
- Write an out of scope list...