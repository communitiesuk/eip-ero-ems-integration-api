-- liquibase formatted sql
-- changeset Krister.Bone@valtech.com:create_address endDelimiter:;

CREATE TABLE `address`
(
    `id`           char(36)     NOT NULL,
    `street`       varchar(255) NOT NULL,
    `property`     varchar(255),
    `locality`     varchar(255),
    `town`         varchar(255),
    `area`         varchar(255),
    `postcode`     varchar(10)  NOT NULL,
    `uprn`         varchar(12),
    `date_created` timestamp    NOT NULL,
    `created_by`   varchar(255) NOT NULL,
    `version`      bigint       NOT NULL,
    PRIMARY KEY (`id`)
);
