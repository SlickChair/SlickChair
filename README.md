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
- Write a class for Settings access with default value

Todo:
-----

- Email client for the chair with templates
- Invitation mechanism for chairs, reviewers and subreviewers
- Paper biding and assignement
- Good looking interface
- Setup instructions (requires some testing with postgresql)
- Wrap most common chair actions into web pages (instead of manual queries)
  - Edit settings
  - Logs for emails and events
  - Add topics
  - Delete papers, account, ...

Out of scope:
-------------

- Add and apply extra settings
- Phases abstraction for settings
- Internationalisation (should be easy with [Play][1])
- Client side stuff
- Write an out of scope list...

[1]: http://www.playframework.com/documentation/2.0/JavaI18N