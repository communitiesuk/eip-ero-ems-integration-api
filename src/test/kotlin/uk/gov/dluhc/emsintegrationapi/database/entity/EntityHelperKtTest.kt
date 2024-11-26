package uk.gov.dluhc.emsintegrationapi.database.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomAlphaNumeric

internal class EntityHelperKtTest {

    @Test
    fun `should return true if two entities ids are equal`() {
        val entityId = getRandomAlphaNumeric(24)
        val entity1 = createMockEntity(entityId)
        val entity2 = createMockEntity(entityId)
        assertThat(entity1 === entity2).isFalse
        assertThat(areEqual(entity1, entity2, PostalVoteApplication::applicationId)).isTrue
    }

    @Test
    fun `should return true if two entities reference are same`() {
        val entityId = getRandomAlphaNumeric(24)
        val entity1 = createMockEntity(entityId)
        assertThat(areEqual(entity1, entity1, PostalVoteApplication::applicationId)).isTrue
    }

    @Test
    fun `should return false if second entity is null`() {
        val entityId = getRandomAlphaNumeric(24)
        val entity1 = createMockEntity(entityId)
        assertThat(areEqual(entity1, null, PostalVoteApplication::applicationId)).isFalse
    }

    @Test
    fun `should return false if second entity class is different`() {
        val d1 = Dummy1("1")
        val d2 = Dummy2("1", "Test")
        assertThat(areEqual(d1, d2, Dummy1::id)).isFalse
    }

    @Test
    fun `should return false if id is null`() {
        val entity1 = Dummy1(id = null)
        val entity2 = Dummy2(id = null, "Test")
        assertThat(areEqual(entity1, entity2, Dummy1::id)).isFalse
    }

    private fun createMockEntity(applicationId: String): PostalVoteApplication {
        val mockObject: PostalVoteApplication = mock()
        return mockObject.let {
            given(mockObject.applicationId).willReturn(applicationId)
            mockObject
        }
    }

    internal open class Dummy1(open var id: String?)
    internal class Dummy2(override var id: String?, val name: String) : Dummy1(id)
}
