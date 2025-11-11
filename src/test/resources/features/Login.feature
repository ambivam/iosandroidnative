Feature: eStratis Login Functionality
  As an eStratis user
  I want to be able to log into the application
  So that I can access the eStratis features

  Background:
    Given the user is on the eStratis login page

  @smoke @login @positive @ios
  Scenario: Successful iOS login with valid credentials
    Given the user has valid eStratis credentials
    When the user enters username "rajanikanth.bathula@mystratis.com" and password "Notallowed@123"
    And the user clicks the login button
    Then the user should be successfully logged in

  @smoke @login @positive @android
  Scenario: Successful Android login with valid credentials
    Given the user is on the eStratis Android login page
    When the user enters Android credentials "rajanikanth.bathula@mystratis.com" and "Notallowed@123"
    And the user clicks the Android login button
    Then the user should be successfully logged into Android app

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
