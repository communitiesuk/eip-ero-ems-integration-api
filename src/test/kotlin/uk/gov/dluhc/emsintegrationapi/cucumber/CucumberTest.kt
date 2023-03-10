package uk.gov.dluhc.emsintegrationapi.cucumber

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "uk.gov.dluhc.emsintegrationapi.cucumber"
)
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @WIP")
class CucumberTest
