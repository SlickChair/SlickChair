# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "TASKS" ("id" SERIAL NOT NULL PRIMARY KEY,"label" VARCHAR(254) NOT NULL);

# --- !Downs

drop table "TASKS";

