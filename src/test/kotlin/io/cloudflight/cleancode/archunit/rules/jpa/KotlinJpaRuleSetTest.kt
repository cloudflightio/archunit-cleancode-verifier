package io.cloudflight.cleancode.archunit.rules.jpa

import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty


internal class KotlinJpaRuleSetTest {

    private val rules = JpaRuleSet()

    @Test
    fun `@Id on nullable property should fail`() {
        val importer = ClassFileImporter().importClasses(NullableIdEntity::class.java)
        assertThrows<AssertionError> {
            rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
        }
    }

    @Test
    fun `@Id on non nullable property should pass`() {
        val importer = ClassFileImporter().importClasses(NonNullableIdEntity::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `@Id on nullable property with @GeneratedValue should pass`() {
        val importer = ClassFileImporter().importClasses(GeneratedValueOnNonNullableIdEntity::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `@Id on non nullable property with @GeneratedValue should fail`() {
        val importer = ClassFileImporter().importClasses(GeneratedValueOnNullableIdEntity::class.java)
        assertThrows<AssertionError> {
            rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
        }
    }

    @Test
    fun `@Embedded Entity should pass when it is null even if all sub columns are nullable`() {
        val importer = ClassFileImporter().importClasses(EntityWithNullableEmbeddable::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    @Disabled("CCV-104")
    fun `@Embedded Entity should not pass if embeddable is nullable but columns inside are not nullable`() {
        val importer = ClassFileImporter().importClasses(WarehouseNullableComponentEntity::class.java)
        assertThrows<AssertionError> {
            rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
        }
    }

    @Test
    fun `@Embedded Entity with correct @Column(nullable = false) should pass`() {
        val importer = ClassFileImporter().importClasses(WarehouseComponentEntity::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `@Id on non nullable property of type 'Long' with @GeneratedValue should pass`() {
        val importer = ClassFileImporter().importClasses(GeneratedValueIdEntityWithNotNullableTypeLong::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `@Id on nullable property of type 'Long' with @GeneratedValue should fail`() {
        val importer = ClassFileImporter().importClasses(GeneratedValueIdEntityWithNullableTypeLong::class.java)
        assertThrows<AssertionError> {
            rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
        }
    }

    @Test
    fun `non-nullable fields without NonNull should fail`() {
        val importer = ClassFileImporter().importClasses(EntityWithNonNullableColumnButNoAnnotation::class.java)
        assertThrows<AssertionError> {
            rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
        }
    }

    @Test
    fun `project entity with NonNull specs should pass`() {
        val importer = ClassFileImporter().importClasses(Project::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }


    @Test
    fun `project entity with NotEmpty specs should pass`() {
        val importer = ClassFileImporter().importClasses(EntityWithNonNullableColumnAndNotEmptyAnnotation::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `project entity with NonBlank specs should pass`() {
        val importer = ClassFileImporter().importClasses(EntityWithNonNullableColumnAndNotBlankAnnotation::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `oneToMany relation should not be checked at all`() {
        val importer = ClassFileImporter().importClasses(Person::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

}


@Entity
class NullableIdEntity(
    @Id
    val uuid: UUID?
)

@Entity
class NonNullableIdEntity(
    @Id
    val uuid: UUID
)

@Entity
class GeneratedValueOnNonNullableIdEntity(
    @GeneratedValue
    @Id
    val uuid: UUID?
)

@Entity
class GeneratedValueOnNullableIdEntity(
    @GeneratedValue
    @Id
    val uuid: UUID
)


@Entity
class WarehouseNullableComponentEntity(
    @Embedded
    var visualData: WarehouseComponentVisualDataEntity?
)

@Entity
class WarehouseComponentEntity(
    @Embedded
    var visualData: WarehouseComponentVisualDataEntity
)

@Embeddable
class WarehouseComponentVisualDataEntity(
    @NotNull
    var x: Double,
    @NotNull
    var y: Double
)


@Entity
class GeneratedValueIdEntityWithNotNullableTypeLong(
    @GeneratedValue
    @Id
    val longID: Long = 0
)

@Entity
class GeneratedValueIdEntityWithNullableTypeLong(
    @GeneratedValue
    @Id
    val longID: Long?
)

@Entity
class EntityWithNonNullableColumnButNoAnnotation(
    @Id
    val id: Long = 0,
    val x: String
)

@Entity
class EntityWithNonNullableColumnAndNotEmptyAnnotation(
    @Id
    val id: Long = 0,
    @field:NotEmpty
    val x: String
)

@Entity
class EntityWithNonNullableColumnAndNotBlankAnnotation(
    @Id
    val id: Long = 0,
    @field:NotBlank
    val x: String
)

@Entity
class Person(
    @Id
    val id: Long = 0,
    @OneToMany
    val addresses: Collection<Address>
)

@Entity
class Address(
    @Id
    val id: Long = 0,
    val town: String

)


@Entity
data class EntityWithNullableEmbeddable(
    @GeneratedValue
    @Id
    val longID: Long = 0,

    @Embedded
    var budget: Budget?
)

@Embeddable
data class Budget(
    @field:NotNull
    var value: Double
)

