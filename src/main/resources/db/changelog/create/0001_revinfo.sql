-- liquibase formatted sql
-- changeset Krister.Bone@valtech.com:create-revinfo-table endDelimiter:;

CREATE TABLE `revinfo`
(
    `rev`      int NOT NULL AUTO_INCREMENT,
    `revtstmp` bigint ,
    PRIMARY KEY (`rev`)
)