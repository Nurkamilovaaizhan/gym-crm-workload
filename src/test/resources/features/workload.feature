Feature: Workload component

  Scenario: Add trainer workload
    Given workload component is ready
    When I add 60 minutes for trainer
    Then workload duration is 60 minutes

  Scenario: Reject invalid workload
    Given workload component is ready
    When I add invalid workload
    Then workload validation error is returned