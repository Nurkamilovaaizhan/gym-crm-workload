Feature: Workload component

  Scenario: Get trainer workload successfully
    Given workload component is ready
    When I request workload for trainer
    Then workload response contains 60 minutes

  Scenario: Get unknown trainer workload
    Given workload component is ready
    When I request workload for unknown trainer
    Then workload request returns not found