package uk.gov.dluhc.emsintegrationapi.config

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.ext.javatime.deser.InstantDeserializer
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.KotlinModule
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.TimeZone

@Configuration
class JacksonConfiguration {
    @Bean
    fun jsonMapper(): JsonMapper =
        JsonMapper
            .builder()
            .addModule(SimpleModule().addDeserializer(OffsetDateTime::class.java, CustomOffsetDateTimeDeserializer))
            .addModule(KotlinModule.Builder().build())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .changeDefaultPropertyInclusion({ incl -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL) })
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build()

    // EROPSPT-190: IDOX were sending applicationCreatedAt as a local time, without timezone
    // information, and this cannot be parsed into an OffsetDateTime. If we receive such a
    // date-time, then we assume that it refers to a London local time.
    object CustomOffsetDateTimeDeserializer : ValueDeserializer<OffsetDateTime>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext,
        ): OffsetDateTime =
            try {
                InstantDeserializer.OFFSET_DATE_TIME.deserialize(p, ctxt)
            } catch (e: InvalidFormatException) {
                val localDateTime = LocalDateTimeDeserializer.INSTANCE.deserialize(p, ctxt)
                val offsetMillis =
                    TimeZone
                        .getTimeZone(
                            "Europe/London",
                        ).getOffset(localDateTime.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
                localDateTime.atOffset(ZoneOffset.ofTotalSeconds(offsetMillis / 1000))
            }
    }
}
