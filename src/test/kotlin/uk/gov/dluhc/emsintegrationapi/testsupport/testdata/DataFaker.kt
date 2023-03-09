package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import net.datafaker.Faker
import java.util.Locale

class DataFaker {
    companion object {
        val faker: Faker = Faker(Locale.UK)
    }
}
