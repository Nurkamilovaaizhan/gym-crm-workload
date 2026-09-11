package com.gymcrm.workload.bdd;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/workload.feature")
@org.junit.platform.suite.api.ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "com.gymcrm.workload.bdd"
)
class WorkTest {
}