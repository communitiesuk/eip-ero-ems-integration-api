-- liquibase formatted sql
-- changeset Krister.Bone@valtech.com:create_registered_address_aud endDelimiter:;

DROP TABLE IF EXISTS `address_aud`;
CREATE TABLE `address_aud`
(
    `id`           char(36) NOT NULL,
    `rev`          int      NOT NULL,
    `revtype`      tinyint,
    `street`       varchar(255),
    `property`     varchar(255),
    `locality`     varchar(255),
    `town`         varchar(255),
    `area`         varchar(255),
    `postcode`     varchar(10),
    `uprn`         varchar(12),
    `date_created` timestamp,
    `created_by`   varchar(255),
    PRIMARY KEY (`id`, `rev`),
    KEY            `fk_address_aud_revinfo` (`rev`),
    CONSTRAINT `fk_address_aud_revinfo` FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
)

