@Postal @PostalMessage
Feature: Consume Postal Vote Application Message

  Scenario Outline: The application saves a postal vote application message to database
    Given a postal vote application with the application id "<PostalApplicationId>" and electoral id "<EmsElectoralId>"
    When I send an sqs message to the postal application queue
    And the postal vote application has been successfully saved with the application id "<PostalApplicationId>" and electoral id "<EmsElectoralId>"
    Examples:
      | PostalApplicationId      | EmsElectoralId                       |
      | 502cf250036469154b4f85fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 |
      | 502cf250036469154b4f85fb | e87cbaea-0deb-4058-95c6-8240d426f5e2 |

  Scenario: The application does not allow two different postal applications having same id
    Given a postal vote application with the application id "502cf250036469154b4f85fc" and electoral id "e87cbaea-0deb-4058-95c6-8240d426f5e3" exists
    When I send an sqs message to the postal application queue
    And the postal vote application has been successfully saved with the application id "<PostalApplicationId>" and electoral id "<EmsElectoralId>"
