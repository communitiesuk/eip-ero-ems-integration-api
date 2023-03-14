@WIP
Feature: Consume Postal Vote Application Message

  Scenario Outline: The application saves a postal vote application message to database
    Given a postal vote application with the application id "<PostalApplicationId> and electoral id "<EmsElectoralId>"
    When I send an sqs message to the postal application queue
    Then the message is successfully processed
    And a postal vote application has been saved with application id "<PostalApplicationId>" and electoral id "<EmsElectoralId>"
    Examples:
      | PostalApplicationId | EmsElectoralId      |
      | 12345363434634      | 2424ADFASDFASFDASDF |