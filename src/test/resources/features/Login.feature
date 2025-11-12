Feature: eStratis Login Functionality
  As an eStratis user
  I want to be able to log into the application
  So that I can access the eStratis features

  Background:
    Given the user is on the eStratis login page

  @ios @smoke @login @positive
  Scenario: Successful login with valid credentials on iOS
    Given the user has valid eStratis credentials
    When the user enters username "rajanikanth.bathula@mystratis.com" and password "Notallowed@123"
    And the user clicks the login button
    Then the user should be successfully logged in

  @android @smoke @login @positive
  Scenario: Successful login with valid credentials on Android
    Given the user has valid eStratis credentials
    When the user enters username "rajanikanth.bathula@mystratis.com" and password "Notallowed@123"
    And the user clicks the login button
    Then the user should be successfully logged in

  # @login @negative
  # Scenario: Login failure with invalid credentials
  #   Given the user has invalid eStratis credentials
  #   When the user enters invalid credentials
  #   And the user clicks the login button
  #   Then the user should see an error message

  # @login @negative
  # Scenario: Login failure with empty username
  #   When the user enters username "" and password "Notallowed@123"
  #   And the user clicks the login button
  #   Then the user should see an error message

  # @login @negative
  # Scenario: Login failure with empty password
  #   When the user enters username "rajanikanth.bathula@mystratis.com" and password ""
  #   And the user clicks the login button
  #   Then the user should see an error message

  # @login @negative
  # Scenario Outline: Login failure with various invalid credentials
  #   When the user enters username "<username>" and password "<password>"
  #   And the user clicks the login button
  #   Then the user should see an error message

  #   Examples:
  #     | username                    | password        |
  #     | invalid@email.com          | wrongpassword   |
  #     | test@test.com              | 123456          |
  #     | user@domain.com            | password        |
