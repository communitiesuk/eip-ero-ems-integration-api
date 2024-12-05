package uk.gov.dluhc.emsintegrationapi.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import jakarta.validation.constraints.Size
import org.hibernate.Hibernate
import org.hibernate.annotations.JdbcTypeCode
import org.springframework.data.annotation.CreatedDate
import java.sql.Types
import java.time.Instant
import java.util.UUID

@Table
@Entity
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(Types.CHAR)
    var id: UUID? = null,

    @field:Size(max = 255)
    val street: String,

    @field:Size(max = 255)
    var property: String? = null,

    @field:Size(max = 255)
    var locality: String? = null,

    @field:Size(max = 255)
    var town: String? = null,

    @field:Size(max = 255)
    var area: String? = null,

    @field:Size(max = 10)
    val postcode: String,

    @field:Size(max = 12)
    var uprn: String? = null,

    @CreatedDate
    var dateCreated: Instant? = null,

    @Enumerated(EnumType.STRING)
    val createdBy: SourceSystem? = null,

    @Version
    var version: Long? = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Address

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , dateCreated = $dateCreated)"
    }
}
