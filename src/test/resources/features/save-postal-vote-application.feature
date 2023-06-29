@Postal @DeletePostalEntity @DeletePostalMessage
Feature: System process an approved/rejected postal vote application message

  Scenario Outline: The system process and saves a postal vote application message with signature into the database
    Given a postal vote application with the application id "<PostalApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
    When I send an sqs message to the postal application queue
    Then the "<ApplicationStatus>" postal vote application has been successfully saved with the application id "<PostalApplicationId>", signature and ballot addresses
    Examples:
      | PostalApplicationId      | EmsElectoralId                       | ApplicationStatus |
      | 502cf250036469154b4f85fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | APPROVED          |
      | 502cf250036469154b4f85fb | e87cbaea-0deb-4058-95c6-8240d426f5e2 | REJECTED          |

  Scenario Outline: The system process and saves a postal vote application message with signature waiver details
    Given a postal vote application with the application id "<PostalApplicationId>" and signature waiver reason "<WaiverReason>"
    When I send an sqs message to the postal application queue
    Then the postal vote application has been successfully saved with the signature waiver reason "<WaiverReason>"
    Examples:
      | PostalApplicationId      | WaiverReason |
      | 502cf250036469154b4f85fa | Disabled     |
      | 502cf250036469154b4f85fb | Other        |

  Scenario Outline: The system does not allow two different postal applications having same id and a different ems electoral id
    Given a postal vote application with the application id "<PostalApplicationId>" and electoral id "<EmsElectoralId>" exists
    When I send an sqs message to the postal application queue with an application id "<PostalApplicationId>" and electoral id "<EmsElectoralId2>"
    Then the postal vote application with id "<PostalApplicationId>" and electoral id "<EmsElectoralId2>" did not save
    Examples:
      | PostalApplicationId      | EmsElectoralId                       | EmsElectoralId2                      |
      | 502cf250036469154b4f86fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | e87cbaea-0deb-4058-95c6-8240d426f5e2 |


  Scenario Outline: The system does allow two different postal applications having a different application id and a same ems electoral id
    Given a postal vote application with the application id "<PostalApplicationId>" and electoral id "<EmsElectoralId>" exists
    When I send an sqs message to the postal application queue with an application id "<PostalApplicationId2>" and electoral id "<EmsElectoralId>"
    Then the postal vote application with id "<PostalApplicationId2>" was saved
    Examples:
      | PostalApplicationId      | EmsElectoralId                       | PostalApplicationId2     |
      | 502cf250036469154b4f87fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | 502cf250036469154b4f85fb |

  Scenario Outline: The system will reject a postal vote application message with an invalid application id, application id must be 24 characters
    Given a postal vote application with the application id "<InvalidPostalApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
    When I send an sqs message to the postal application queue
    Then the postal vote application with id "<InvalidPostalApplicationId>" did not save
    Examples:
      | InvalidPostalApplicationId | EmsElectoralId                       | ApplicationStatus |
      | 123456                     | e87cbaea-0deb-4058-95c6-8240d426f5e4 | APPROVED          |
      | 123457                     | e87cbaea-0deb-4058-95c6-8240d426f5e6 | REJECTED          |

  Scenario: The system process and saves a postal vote application message without ballot addresses
    Given a postal vote application with the application id "502cf250036469154b4f85fa" and no ballot addresses
    When I send an sqs message to the postal application queue
    Then the postal vote application has been successfully saved without ballot addresses
