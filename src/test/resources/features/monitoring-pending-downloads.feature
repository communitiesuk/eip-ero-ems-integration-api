@PendingDownloadsMonitoringJob
Feature: System reports on pending postal and proxy downloads

  @DeletePostalEntity
  Scenario: System does not report on pending postal downloads that are less than 5 days old
    Given a postal vote application with gss code "E00000001" was saved to EMS integration API 3 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 postal applications have been pending for more than PT120H." is logged

  @DeletePostalEntity
  Scenario: System does not report on postal downloads that have been deleted
    Given a postal vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "DELETED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 postal applications have been pending for more than PT120H." is logged

  @DeletePostalEntity
  Scenario: System does not report on pending postal downloads from excluded GSS codes
    Given a postal vote application with gss code "E99999999" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 postal applications have been pending for more than PT120H." is logged

  @DeletePostalEntity
  Scenario: System reports on postal downloads that have been pending for more than 5 days, grouped by gss code
    Given a postal vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" was saved to EMS integration API 8 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000002" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 4 postal applications have been pending for more than PT120H." is logged
    And the message "The gss code E00000001 has 3 postal applications that have been pending for more than PT120H." is logged
    And the message "The gss code E00000002 has 1 postal applications that have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System does not report on pending proxy downloads that are less than 5 days old
    Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 3 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 proxy applications have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System does not report on proxy downloads that have been deleted
    Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "DELETED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 proxy applications have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System does not report on pending proxy downloads from excluded GSS codes
    Given a proxy vote application with gss code "E99999999" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 proxy applications have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System reports on proxy downloads that have been pending for more than 5 days, grouped by gss code
    Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" was saved to EMS integration API 8 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000002" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 4 proxy applications have been pending for more than PT120H." is logged
    And the message "The gss code E00000001 has 3 proxy applications that have been pending for more than PT120H." is logged
    And the message "The gss code E00000002 has 1 proxy applications that have been pending for more than PT120H." is logged
