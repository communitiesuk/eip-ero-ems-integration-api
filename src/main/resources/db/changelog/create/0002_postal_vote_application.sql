-- liquibase formatted sql
-- changeset Krister.Bone@valtech.com:create_postal_vote_application_table endDelimiter:;

DROP TABLE IF EXISTS `postal_vote_application`;
CREATE TABLE `postal_vote_application`
(
    `application_id`            varchar(24)  NOT NULL,
    `created_at`                timestamp    NOT NULL,
    `gss_code`                  varchar(9)   NOT NULL,
    `source`                    varchar(50)  NOT NULL,
    `authorised_at`             timestamp    NOT NULL,
    `authorising_staff_id`      varchar(255) NOT NULL,
    `first_name`                varchar(35)  NOT NULL,
    `middle_names`              varchar(100),
    `surname`                   varchar(35)  NOT NULL,
    `dob`                       date,
    `email`                     varchar(255),
    `phone`                     varchar(50),
    `registered_address_id`     char(36)     NOT NULL,
    `reference_number`          varchar(10)  NOT NULL,
    `ip_address`                varchar(45)  NOT NULL,
    `ems_elector_id`            varchar(255),
    `language`                  char(2),
    `ballot_address_id`         char(36),
    `ballot_address_reason`     varchar(500),
    `vote_until_further_notice` bit(1),
    `vote_for_single_date`      date,
    `vote_start_date`           date,
    `vote_end_date`             date,
    `signature_base64`          text,
    `removal_date_time`         timestamp,
    `retention_status`          varchar(20)  NOT NULL,
    `date_created`              timestamp    NOT NULL,
    `date_updated`              timestamp,
    `created_by`                varchar(255) NOT NULL,
    `updated_by`                varchar(255) NULL,
    `version`                   bigint       NOT NULL,
    `status`                    varchar(20)  NOT NULL COMMENT 'status of the record, used for soft deletion',
    `application_status`        varchar(20)  NOT NULL,
    `signature_waived`          bit(1),
    `signature_waived_reason`   varchar(250),
    PRIMARY KEY (`application_id`),
    UNIQUE KEY `postal_vote_application_ems_elector_id_unique_idx` (`ems_elector_id`),
    KEY                         `postal_vote_application_application_id_status_idx` (`application_id`,`status`),
    KEY                         `postal_vote_application_application_id_gss_code_idx` (`application_id`,`gss_code`),
    KEY                         `postal_vote_application_gss_code_idx` (`gss_code`)
)
