Done:
=====

- Login mechanism with Google/Facebook/email+password
- Page for paper submission with authors, topics and file upload
- View submitted information and edit submission
- SQL form
- Make a page per paper with possibility to write reviews
- Add edit and comments to the paper pages
- Write a classes for settings with get set and default value
- Email client for the chair with templates
- Manage members (invitations, delete, promotion..)
- Paper biding
- Getting started instructions
- Use a modal style login

Todo:
=====

- Build a menu for each type of user 
- Manual Paper/Reviewer assignement
- Config page(s)
- Decision page (accepted/rejected)
- Variables for the email client
- Use the [POST-Redirect-GET pattern](http://www.theserverside.com/news/1365146/Redirect-After-Post)
  in all the forms to prevent repost on page reload
- SSL

Out of scope:
=============

- Logs, tests
- More detailed settings
- Phases abstraction for settings (1 Submission, 2 Bidding, 3 Review, ...)
- Client side stuff (starting with form validation)
- Use [Scala-JS](https://github.com/lampepfl/scala-js) instead of CoffeScript
- Prefill the submission form with data extracted from the PDF
- Automatic Paper/Reviewer assignement ([starting point](http://140.123.102.14:8080/reportSys/file/paper/scfu/scfu_21_paper.pdf))
- Extract abstract authors and keywords from PDF ([starting](http://pdfx.cs.man.ac.uk/)
   [points](http://www.cs.cornell.edu/cdlrg/reference%20linking/extraction.pdf))
- ...
