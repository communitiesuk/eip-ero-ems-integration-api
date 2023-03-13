-- liquibase formatted sql
-- changeset Krister.Bone@valtech.com:create_proxy_vote_application_table endDelimiter:;

DROP TABLE IF EXISTS `proxy_vote_application`;
CREATE TABLE `proxy_vote_application`
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
    `ems_elector_id`            varchar(255) NOT NULL,
    `language`                  char(2),
    `proxy_first_name`          varchar(35)  NOT NULL,
    `proxy_middle_names`        varchar(100),
    `proxy_surname`             varchar(35)  NOT NULL,
    `proxy_family_relationship` varchar(500),
    `proxy_address_id`          char(36)     NOT NULL,
    `proxy_reason`              varchar(500) NOT NULL,
    `vote_until_further_notice` bit(1),
    `vote_for_single_date`      date,
    `vote_start_date`           date,
    `vote_end_date`             date,
    `signature_base64`          text         NOT NULL,
    `removal_date_time`         timestamp,
    `retention_status`          varchar(20)  NOT NULL,
    `date_created`              timestamp    NOT NULL,
    `created_by`                varchar(255) NOT NULL,
    `version`                   bigint       NOT NULL,
    `status`                    varchar(20)  NOT NULL COMMENT 'status of the record, used for soft deletion',
    PRIMARY KEY (`application_id`),
    UNIQUE KEY `proxy_vote_application_ems_elector_id_unique_idx` (`ems_elector_id`),
    KEY                         `proxy_vote_application_application_id_status_idx` (`application_id`,`status`)
)