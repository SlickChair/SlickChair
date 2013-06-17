# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "AUTHORS" ("PAPERID" INTEGER NOT NULL,"POSITION" INTEGER NOT NULL,"FIRSTNAME" TEXT NOT NULL,"LASTNAME" TEXT NOT NULL,"ORGANIZATION" TEXT NOT NULL,"EMAIL" TEXT NOT NULL);
alter table "AUTHORS" add constraint "AUTHORS_PK" primary key("PAPERID","POSITION");
create table "Files" ("ID" SERIAL NOT NULL PRIMARY KEY,"NAME" TEXT NOT NULL,"SIZE" BIGINT NOT NULL,"UPLOADED" TIMESTAMP NOT NULL,"DATA" BYTEA NOT NULL);
create table "MEMBERS" ("id" SERIAL NOT NULL PRIMARY KEY,"email" TEXT NOT NULL,"firstlogindate" TIMESTAMP NOT NULL,"lastlogindate" TIMESTAMP NOT NULL,"role" INTEGER NOT NULL,"firstname" TEXT NOT NULL,"lastname" TEXT NOT NULL,"organization" VARCHAR(254),"positiontitle" VARCHAR(254));
create table "PAPERS" ("ID" SERIAL NOT NULL PRIMARY KEY,"CONTACTEMAIL" TEXT NOT NULL,"SUBMISSIONDATE" TIMESTAMP NOT NULL,"LASTUPDATE" TIMESTAMP NOT NULL,"ACCEPTED" BOOLEAN,"TITLE" TEXT NOT NULL,"FORMAT" INTEGER NOT NULL,"KEYWORDS" TEXT NOT NULL,"ABSTRCT" TEXT NOT NULL,"FILEID" INTEGER);
create table "TOPICS" ("ID" SERIAL NOT NULL PRIMARY KEY,"NAME" TEXT NOT NULL,"DESCRIPTION" TEXT NOT NULL);
create table "COMMENTS" ("ID" SERIAL NOT NULL PRIMARY KEY,"PAPERID" INTEGER NOT NULL,"MEMBERID" INTEGER NOT NULL,"SUBMISSIONDATE" TIMESTAMP NOT NULL,"LASTUPDATE" TIMESTAMP NOT NULL,"CONTENT" TEXT NOT NULL);
create table "MEMBER_BIDS" ("PAPERID" INTEGER NOT NULL,"MEMBERID" INTEGER NOT NULL,"BID" INTEGER NOT NULL);
alter table "MEMBER_BIDS" add constraint "MEMBERBIDS_PK" primary key("PAPERID","MEMBERID");
create table "MEMBER_TOPICS" ("MEMBERID" INTEGER NOT NULL,"TOPICID" INTEGER NOT NULL);
alter table "MEMBER_TOPICS" add constraint "MEMBERTOPICS_PK" primary key("MEMBERID","TOPICID");
create table "PAPER_TOPICS" ("PAPERID" INTEGER NOT NULL,"TOPICID" INTEGER NOT NULL);
alter table "PAPER_TOPICS" add constraint "PAPERTOPICS_PK" primary key("PAPERID","TOPICID");
create table "REVIEWS" ("PAPERID" INTEGER NOT NULL,"MEMBERID" INTEGER NOT NULL,"SUBMISSIONDATE" TIMESTAMP,"LASTUPDATE" TIMESTAMP,"CONFIDENCE" INTEGER NOT NULL,"EVALUATION" INTEGER NOT NULL,"CONTENT" TEXT NOT NULL);
alter table "REVIEWS" add constraint "MEMBERS_PK" primary key("PAPERID","MEMBERID");
create table "SECURE_SOCIAL_TOKENS" ("UUID" VARCHAR(254) NOT NULL PRIMARY KEY,"EMAIL" TEXT NOT NULL,"CREATIONTIME" TIMESTAMP NOT NULL,"EXPIRATIONTIME" TIMESTAMP NOT NULL,"ISSIGNUP" BOOLEAN NOT NULL);
create table "SECURE_SOCIAL_USERS" ("UID" TEXT NOT NULL,"PID" TEXT NOT NULL,"EMAIL" TEXT NOT NULL,"FIRSTNAME" TEXT NOT NULL,"LASTNAME" TEXT NOT NULL,"AUTHMETHOD" TEXT NOT NULL,"HASHER" VARCHAR(254),"PASSWORD" VARCHAR(254),"SALT" VARCHAR(254));
alter table "SECURE_SOCIAL_USERS" add constraint "SECURESOCIALUSERS_PK" primary key("UID","PID");
create table "LOGS" ("ID" SERIAL NOT NULL PRIMARY KEY,"DATE" TIMESTAMP NOT NULL,"ENTRY" TEXT NOT NULL);
create table "SENTMAILS" ("ID" SERIAL NOT NULL PRIMARY KEY,"SENT" TIMESTAMP NOT NULL,"TO" TEXT NOT NULL,"SUBJECT" TEXT NOT NULL,"BODY" TEXT NOT NULL);
create table "SETTINGS" ("name" VARCHAR(254) NOT NULL PRIMARY KEY,"value" TEXT NOT NULL);
create table "TEMPLATES" ("ID" SERIAL NOT NULL PRIMARY KEY,"NAME" TEXT NOT NULL,"SUBJECT" TEXT NOT NULL,"BODY" TEXT NOT NULL);
alter table "AUTHORS" add constraint "AUTHORS_PAPERID_FK" foreign key("PAPERID") references "PAPERS"("ID") on update NO ACTION on delete NO ACTION;
alter table "PAPERS" add constraint "PAPERS_FILEID_FK" foreign key("FILEID") references "Files"("ID") on update NO ACTION on delete NO ACTION;
alter table "COMMENTS" add constraint "COMMENTS_PAPERID_FK" foreign key("PAPERID") references "PAPERS"("ID") on update NO ACTION on delete NO ACTION;
alter table "COMMENTS" add constraint "COMMENTS_MEMBERID_FK" foreign key("MEMBERID") references "MEMBERS"("id") on update NO ACTION on delete NO ACTION;
alter table "MEMBER_BIDS" add constraint "MEMBERBIDS_PAPERID_FK" foreign key("PAPERID") references "PAPERS"("ID") on update NO ACTION on delete NO ACTION;
alter table "MEMBER_BIDS" add constraint "MEMBERBIDS_MEMBERID_FK" foreign key("MEMBERID") references "MEMBERS"("id") on update NO ACTION on delete NO ACTION;
alter table "MEMBER_TOPICS" add constraint "MEMBERTOPICS_TOPIC_FK" foreign key("TOPICID") references "TOPICS"("ID") on update NO ACTION on delete NO ACTION;
alter table "MEMBER_TOPICS" add constraint "MEMBERTOPICS_MEMBERID_FK" foreign key("MEMBERID") references "MEMBERS"("id") on update NO ACTION on delete NO ACTION;
alter table "PAPER_TOPICS" add constraint "PAPERTOPICS_PAPERID_FK" foreign key("PAPERID") references "PAPERS"("ID") on update NO ACTION on delete NO ACTION;
alter table "PAPER_TOPICS" add constraint "PAPERTOPICS_TOPICID_FK" foreign key("TOPICID") references "TOPICS"("ID") on update NO ACTION on delete NO ACTION;
alter table "REVIEWS" add constraint "MEMBERS_PAPERID_FK" foreign key("PAPERID") references "PAPERS"("ID") on update NO ACTION on delete NO ACTION;
alter table "REVIEWS" add constraint "MEMBERS_MEMBERID_FK" foreign key("MEMBERID") references "MEMBERS"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "AUTHORS" drop constraint "AUTHORS_PAPERID_FK";
alter table "PAPERS" drop constraint "PAPERS_FILEID_FK";
alter table "COMMENTS" drop constraint "COMMENTS_PAPERID_FK";
alter table "COMMENTS" drop constraint "COMMENTS_MEMBERID_FK";
alter table "MEMBER_BIDS" drop constraint "MEMBERBIDS_PAPERID_FK";
alter table "MEMBER_BIDS" drop constraint "MEMBERBIDS_MEMBERID_FK";
alter table "MEMBER_TOPICS" drop constraint "MEMBERTOPICS_TOPIC_FK";
alter table "MEMBER_TOPICS" drop constraint "MEMBERTOPICS_MEMBERID_FK";
alter table "PAPER_TOPICS" drop constraint "PAPERTOPICS_PAPERID_FK";
alter table "PAPER_TOPICS" drop constraint "PAPERTOPICS_TOPICID_FK";
alter table "REVIEWS" drop constraint "MEMBERS_PAPERID_FK";
alter table "REVIEWS" drop constraint "MEMBERS_MEMBERID_FK";
alter table "AUTHORS" drop constraint "AUTHORS_PK";
drop table "AUTHORS";
drop table "Files";
drop table "MEMBERS";
drop table "PAPERS";
drop table "TOPICS";
drop table "COMMENTS";
alter table "MEMBER_BIDS" drop constraint "MEMBERBIDS_PK";
drop table "MEMBER_BIDS";
alter table "MEMBER_TOPICS" drop constraint "MEMBERTOPICS_PK";
drop table "MEMBER_TOPICS";
alter table "PAPER_TOPICS" drop constraint "PAPERTOPICS_PK";
drop table "PAPER_TOPICS";
alter table "REVIEWS" drop constraint "MEMBERS_PK";
drop table "REVIEWS";
drop table "SECURE_SOCIAL_TOKENS";
alter table "SECURE_SOCIAL_USERS" drop constraint "SECURESOCIALUSERS_PK";
drop table "SECURE_SOCIAL_USERS";
drop table "LOGS";
drop table "SENTMAILS";
drop table "SETTINGS";
drop table "TEMPLATES";

