Authentication:
===============

Acmss supports two authentication methods:
- Using an email address and a password
- Using an external identity provider

Email/password
--------------
Email/password login works like on most web services: after providing his email address the user has to prove that he owns the address by clicking on a link sent to his mailbox. After that  the used is asked to provide his name and the desired password. The system also provides a procedure similar to sign up in case of forgotten password.

Identity Provider
-----------------
The use of an identity provider simplifies the user sign in process. After picking an identity provider the user is redirected to the provider website and asked for a confirmation. Once back on to the conference manager the system will have access to basic informations about the user such has his name and his email address.

Changing email, multiple identities and account merging
-------------------------------------------------------
We made the choice to uniquely identify users by their email address. 


Why only Google and Facebook?
-----------------------------
During the design of Acmss we decided only use Google and Facebook as identity provider because of how overwhelmingly popular their are compared to other networks.
