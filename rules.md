
# Spring Data / JPA

## Entity Validation via JSR 303 annotations

While you may use `@javax.persistence.Column` to influence constraints on the database like Nullability or maximum
length of column, it is better to use JSR 303 annotations here, as Hibernate checks this already in its validator
implementation before attempting to insert to the database. You also get better validation errors then at runtime.
See [this article](https://www.baeldung.com/hibernate-notnull-vs-nullable) for more information.

A valid entity configuration would be something like this.

````kotlin
@Entity
class Project(

    @field:NotNull
    @field:Size(max = 30)
    val userId: String,

    @field:NotNull
    var lastModifiedDate: OffsetDateTime = OffsetDateTime.now(),
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @NotNull
    val createdDate: OffsetDateTime = OffsetDateTime.now()

    var orderedDate: OffsetDateTime? = null
}
````

See more details in the next chapters:

<a id="jpa-entities-not-null-instead-of-nullable"></a>
### Use @NotNull instead of @Column(nullable=false)
<sup>`Rule-ID: jpa-entities-not-null-instead-of-nullable`</sup>

Use `@javax.validation.NotNull` instead of `@Column(nullable=false`) to express that a column must not be null.

Be aware that if your field is used inside a Kotlin constructor, you need to prefix the annotation with `@field:`
as listed here:

````kotlin
@Entity
class Project(
    @Id
    val projectId: String,

    @field:NotNull
    val userId: String
) {}
````

<a id="jpa-entities-size-instead-of-length"></a>
### Use @Size instead of @Column(length=...)
<sup>`Rule-ID: jpa-entities-size-instead-of-length`</sup>

Use `@javax.validation.Size(max = 30)` instead of `@Column(length=30)` to express the maximum length of a column.

Be aware that if your field is used inside a Kotlin constructor, you need to prefix the annotation with `@field:`
as listed here:

````kotlin
@Entity
class Project(
    @Id
    val projectId: String,

    @field:NotNull
    @field:Size(max = 30)
    val userId: String
) {}
````

<a id="jpa-entities-no-column-names"></a>
### Don't use explicit column names 
<sup>`Rule-ID: jpa-entities-no-column-names`</sup>

Avoid using explicit column names by using `@Column(name="my_column_name")`, use the defaults from our appropriate
NamingStrategy instead. Exceptions might be that you need to access a legacy database where you have no control over
the database scheme.

<a id="kotlin-jpa-nullable-flag-of-kotlin-needs-to-match-jpa-specification"></a>
### Kotlin nullability should match JPA nullability
<sup>`Rule-ID: kotlin-jpa-nullable-flag-of-kotlin-needs-to-match-jpa-specification`</sup>

In case you are using Kotlin, you should also have the JSR 303 annotations in sync with Kotlin nullability feature:

Consider the following code:

````kotlin
@Entity
class MyEntity {
    // other columns including @Id have been suppressed here

    var name: String
    var country: String?
}
````

As you see here, the field `country` is nullable in Kotlin, whereas the field 'name' is not. Unfortunately,
this null-check is not reflected to the database, that is why you should add that explicitely with a
`@javax.validation.constraints.NotNull` annotation as follows:

````kotlin
@Entity
class MyEntity {
// other columns including @Id have been suppressed here

    @NotNull
    var name: String
    var country: String?
}
````

That way, also database generation (if activated) will mark that column as `NON NULL` in the database.

### Additional Considerations for @Id columns in Kotlin
From a database perspective a primary key column serving always has a `NOT NULL CONSTRAINT` associated with it.
Accordingly, the `@Id` annotated property should be a non nullable `val`.
But now hibernate generated keys feel problematic, because we will have to construct an entity *including the non nullable id*
before calling `JpaRepository.save()`, hence before hibernate can generate a key.

This can be alleviated though. Consider the following example:

````kotlin
@Entity
class MyEntity(
    var name: String
){
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    val id: Long = 0
}
````

When `MyEntity` is constructed it will have `myEntity.identifier == 0`,
after a  call to `JpaRepository.save()` a new ID will be generated. After a call to `JpaRepository.save()` the object supplied as
argument must not be used anymore, instead the returned Entity, which has the generated ID, must be used.
