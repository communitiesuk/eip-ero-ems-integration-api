@PendingDownloadsMonitoringJob
Feature: System reports on pending postal and proxy downloads

  @DeletePostalEntity
  Scenario: System does not report on pending postal downloads that are less than 5 days old
    Given a postal vote application with gss code "E00000001" was saved to EMS integration API 3 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged

  @DeletePostalEntity
  Scenario: System does not report on postal downloads that have been deleted
    Given a postal vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "DELETED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged

  @DeletePostalEntity
  Scenario: System does not report on pending postal downloads from excluded GSS codes
    Given a postal vote application with gss code "E99999999" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged

  @DeletePostalEntity
  Scenario: System reports on postal downloads that have been pending for more than 5 days, grouped by gss code
    Given a postal vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" was saved to EMS integration API 8 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000002" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 4 postal applications (4 with EMS Elector Ids) have been pending for more than PT120H." is logged
    And the message "The gss code E00000001 has 3 postal applications (3 with EMS Elector Ids) that have been pending for more than PT120H." is logged
    And the message "The gss code E00000002 has 1 postal applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged

  @DeletePostalEntity
  Scenario: System reports number of pending postal downloads with an EMS Elector ID separately
    Given a postal vote application with gss code "E00000001" with an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 8 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000002" without an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 4 postal applications (1 with EMS Elector Ids) have been pending for more than PT120H." is logged
    And the message "The gss code E00000001 has 3 postal applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged
    And the message "The gss code E00000002 has 1 postal applications (0 with EMS Elector Ids) that have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System does not report on pending proxy downloads that are less than 5 days old
    Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 3 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System does not report on proxy downloads that have been deleted
    Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "DELETED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System does not report on pending proxy downloads from excluded GSS codes
    Given a proxy vote application with gss code "E99999999" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System reports on proxy downloads that have been pending for more than 5 days, grouped by gss code
    Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" was saved to EMS integration API 8 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000002" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 4 proxy applications (4 with EMS Elector Ids) have been pending for more than PT120H." is logged
    And the message "The gss code E00000001 has 3 proxy applications (3 with EMS Elector Ids) that have been pending for more than PT120H." is logged
    And the message "The gss code E00000002 has 1 proxy applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged

  @DeleteProxyEntity
  Scenario: System reports number of pending proxy downloads with an EMS Elector ID separately
    Given a proxy vote application with gss code "E00000001" with an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 8 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000002" without an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then the message "A total of 4 proxy applications (1 with EMS Elector Ids) have been pending for more than PT120H." is logged
    And the message "The gss code E00000001 has 3 proxy applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged
    And the message "The gss code E00000002 has 1 proxy applications (0 with EMS Elector Ids) that have been pending for more than PT120H." is logged

  @DeletePostalEntity @DeleteProxyEntity
  Scenario: System sends an email reporting postal and proxy pending downloads
    Given a postal vote application with gss code "E00000001" with an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 8 days ago and has record status "RECEIVED"
    And a postal vote application with gss code "E00000002" without an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" with an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
    And a proxy vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 7 days ago and has record status "RECEIVED"
    When the pending downloads monitoring job runs
    Then an email is sent from "sender@domain.com" to "recipient1@domain.com" with subject line "Postal and Proxy Pending Downloads Monitoring" and email body
      """
      <!DOCTYPE html>
      <html lang="en">
      <head>
          <meta charset="UTF-8">
          <title>Pending EMS Integration Downloads</title>
      </head>
      <body>
          <p>A total of 4 postal applications have been pending download for more than 5 days.</p>
          <p>Of these, 1 applications have an EMS Elector ID.</p>
          <br>
          <table>
              <thead>
              <tr>
                  <th>GSS code</th>
                  <th>Pending postal downloads</th>
                  <th>Pending with EMS Elector ID</th>
              </tr>
              </thead>
              <tbody>
                  <tr>
                      <td>E00000001</td>
                      <td>3</td>
                      <td>1</td>
                  </tr>
                  <tr>
                      <td>E00000002</td>
                      <td>1</td>
                      <td>0</td>
                  </tr>
              </tbody>
          </table>
          <p>A total of 2 proxy applications have been pending download for more than 5 days.</p>
          <p>Of these, 1 applications have an EMS Elector ID.</p>
          <br>
          <table>
              <thead>
              <tr>
                  <th>GSS code</th>
                  <th>Pending proxy downloads</th>
                  <th>Pending with EMS Elector ID</th>
              </tr>
              </thead>
              <tbody>
                  <tr>
                      <td>E00000001</td>
                      <td>2</td>
                      <td>1</td>
                  </tr>
              </tbody>
          </table>
      </body>
      </html>
      """
