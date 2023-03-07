-- liquibase formatted sql
-- changeset Krister.Bone@valtech.com:create_approved_postal_application_aud_table endDelimiter:;

DROP TABLE IF EXISTS `approved_postal_application_aud`;
CREATE TABLE `approved_postal_application_aud`
(
    `id`                        char(36) NOT NULL,
    `rev`                       int      NOT NULL,
    `revtype`                   tinyint,
    `application_id`            varchar(24),
    `created_at`                timestamp,
    `gss_code`                  varchar(9),
    `source`                    varchar(50),
    `authorised_at`             timestamp,
    `authorising_staff_id`      varchar(255),
    `first_name`                varchar(35),
    `middle_names`              varchar(100),
    `surname`                   varchar(35),
    `dob`                       date,
    `email`                     varchar(255),
    `phone`                     varchar(50),
    `registered_address_id`     char(36),
    `reference_number`          varchar(10),
    `ip_address`                varchar(45),
    `ems_elector_id`            varchar(255),
    `language`                  char(2),
    `ballot_address_id`         char(36),
    `ballot_reason`             varchar(500),
    `vote_until_further_notice` bit(1) NULL,
    `vote_for_single_date`      date,
    `vote_start_date`           date,
    `vote_end_date`             date,
    `signature_base64`          text,
    `removal_date_time`         timestamp,
    `retention_status`          varchar(20),
    `date_created`              timestamp,
    `created_by`                varchar(255),
    `version`                   bigint,
    `status`                    varchar(20) COMMENT 'status of the record, used for soft deletion',
    PRIMARY KEY (`id`, `rev`),
    KEY                         `k_approved_postal_application_aud_revinfo` (`rev`),
    CONSTRAINT `fk_approved_postal_application_aud_revinfo` FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
)