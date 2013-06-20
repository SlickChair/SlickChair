Database tables:
================

Chairs have access to a SQL form at `your-website/sql` where abitrary queries can be executed. This page could be use to extracting relevant data or performing actions that are not doable via the web interface (such as breaking the system).

The system uses a total of 16 table:

    TOPICS (
      ID             AUTOINCEMENT INTEGER
      NAME           TEXT
      DESCRIPTION    TEXT
    )

    FILES (
      ID             AUTOINCEMENT INTEGER
      NAME           TEXT
      SIZE           INT
      UPLOADED       TIMESTAMP
      DATA           BLOB
    )

    MEMBER_BIDS (
      PAPERID        INTEGER
      MEMBERID       INTEGER
      BID            INTEGER
    )

    COMMENTS (
      ID             AUTOINCEMENT INTEGER
      PAPERID        INTEGER
      MEMBERID       INTEGER
      SUBMISSIONDATE TIMESTAMP
      CONTENT        TEXT
    )

    SETTINGS (
      NAME           TEXT
      VALUE          TEXT
    )

    SECURE_SOCIAL_USERS (
      UID            TEXT
      PID            TEXT
      EMAIL          TEXT
      FIRSTNAME      TEXT
      LASTNAME       TEXT
      AUTHMETHOD     TEXT
      HASHER         TEXT
      PASSWORD       TEXT
      SALT           TEXT
    )

    SECURE_SOCIAL_TOKENS (
      UUID           TEXT
      EMAIL          TEXT
      CREATIONTIME   TIMESTAMP
      EXPIRATIONTIME TIMESTAMP
      ISSIGNUP       BOOLEAN
      ISINVITATION   BOOLEAN
    )

    TEMPLATES (
      ID             AUTOINCEMENT INTEGER
      NAME           TEXT
      SUBJECT        TEXT
      BODY           TEXT
    )

    SENT_EMAILS (
      ID             AUTOINCEMENT INTEGER
      TO             TEXT
      SUBJECT        TEXT
      BODY           TEXT
      SENT           TIMESTAMP
    )

    LOGS (
      ID             AUTOINCEMENT INTEGER
      DATE           TIMESTAMP
      ENTRY          TEXT
    )

    PAPER_TOPICS (
      PAPERID        INTEGER
      TOPICID        INTEGER
    )

    MEMBERS (
      ID             AUTOINCEMENT INTEGER
      EMAIL          TEXT
      INVITEDAS      TEXT
      FIRSTLOGINDATE TIMESTAMP
      LASTLOGINDATE  TIMESTAMP
      ROLE           INTEGER
      FIRSTNAME      TEXT
      LASTNAME       TEXT
    )

    AUTHORS (
      PAPERID        INTEGER
      POSITION       INTEGER
      FIRSTNAME      TEXT
      LASTNAME       TEXT
      ORGANIZATION   TEXT
      EMAIL          TEXT
    )

    REVIEWS (
      PAPERID        INTEGER
      MEMBERID       INTEGER
      SUBMISSIONDATE TIMESTAMP
      LASTUPDATE     TIMESTAMP
      CONFIDENCE     INTEGER
      EVALUATION     INTEGER
      CONTENT        TEXT
    )

    MEMBER_TOPICS (
      MEMBERID       INTEGER
      TOPICID        INTEGER
    )

    PAPERS (
      ID             AUTOINCEMENT INTEGER
      CONTACTEMAIL   TEXT
      SUBMISSIONDATE TIMESTAMP
      LASTUPDATE     TIMESTAMP
      ACCEPTED       BOOLEAN
      TITLE          TEXT
      FORMAT         INTEGER
      KEYWORDS       TEXT
      ABSTRCT        TEXT
      FILEID         INTEGER
    )