# Clean Code rules for the JDK
<a id="no-generic-exceptions"></a>
### Do not throw generic exceptions
<sup>`Rule-ID: jdk.no-generic-exceptions`</sup>

Do not throw generic exceptions like `java.lang.Exception` or `java.lang.RuntimeException`.
Create your own exception types instead or use more concrete exceptions like `IllegalArgumentException`.
This leads to more readable code and enables better exception handling.


<a id="no-jodatime"></a>
### Do not use JodaTime
<sup>`Rule-ID: jdk.no-jodatime`</sup>

Do not use JodaTime, instead use the Date API of Java 8.

<a id="bigdecimal-do-not-call-constructor-with-double-parameter"></a>
### Do not call BigDecimal constructor with double parameter
<sup>`Rule-ID: jdk.bigdecimal-do-not-call-constructor-with-double-parameter`</sup>

Do not use the double constructor for class `BigDecimal`. The results of this constructor can be somewhat unpredictable.

Instead, use kotlin-stdlib's extension function `Double.toBigDecimal()` or the String constructor.
