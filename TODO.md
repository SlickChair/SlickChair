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

Todo:
=====

- Use a modal style login
- Build a menu for each type of user 
- Paper/Reviewer assignement
- Config page(s)
- Decision page (accepted/rejected)
- Getting started instructions
- Variables for the email client
- Use the [POST-Redirect-GET pattern][1] in all the forms to prevent repost on
  page reload
- SSL

Out of scope:
=============

- Logs
- More detailed settings
- Phases abstraction for settings (1 Submission, 2 Bidding, 3 Review, ...)
- Client side stuff (starting with form validation)
- Prefill the submission form with data extracted from the PDF
- ...

[1]: http://www.theserverside.com/news/1365146/Redirect-After-Post
