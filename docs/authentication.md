Authentication
===============================================================================

Acmss supports two authentication methods:
- With an email address and a password
- With an external identity provider

Email/password
-------------------------------------------------------------------------------
Email/password login works like on most web services: after providing his
email address the user has to prove that he owns the address by clicking on a
link sent to his mailbox. After that  the used is asked to provide his name
and the desired password. The system also provides a procedure similar to sign
up in case of forgotten password.

Identity Provider
-------------------------------------------------------------------------------
The use of an identity provider greatly simplifies the user sign in process.
After picking an identity provider the user is redirected to the provider's
website and asked for a confirmation. Once back on to the conference manager
the system will have access to basic information (name and email) without any
additional steps.

Changing email, multiple identities
-------------------------------------------------------------------------------
We made the choice to uniquely identify users by their email address. During
the first connection to the system the users are warned of this definitive
coupling. Note that if a user logs in using different provides, it will only
be identified to be the same user only if the associated email addresses are
the same.

Why only Google and Facebook?
-------------------------------------------------------------------------------
From a code point a view, enabling additional identity providers should be
matter of uncommenting. However each new provider requires to register the
application with the provider. In order to minimise the set up efforts, and
taking into account the [high popularity][1] of these two networks we decided
to stick with Google and Facebook.

[1]: http://www.google.com/trends/explore?q=Facebook,%20Google,%20Twitter,%20LinkedIn,%20GitHub