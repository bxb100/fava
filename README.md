# Fava - advanced functional Java

JDK 11+


# Java Lambda: Method Reference

[from](https://dzone.com/articles/java-lambda-method-reference)

| Type                                                                        | Syntax                          | Lambda                                                                         |
|:----------------------------------------------------------------------------|:--------------------------------|:-------------------------------------------------------------------------------|
| Reference to a *static method*                                              | `ClassName::staticMethodName`   | `(args) -> ClassName.staticMethodName(args)`                                   |
| Reference to an instance method of an existing object                       | `object::instanceMethodName`    | `(args) -> object.instanceMethodName(args)`                                    |
| Reference to an instance method of an arbitrary object of a particular type | `ClassName::instanceMethodName` | `(arg0,rest) -> arg0.instanceMethodName(rest)` Note: argo is of type ClassName |
| Reference to a constructor                                                  | `ClassName::new`                | `(args) -> new ClassName(args)`                                                |

# Lambda Examples

```java
@FunctionalInterface
public interface IF2<T1, T2, R> {
	R apply(T1 t1, T2 t2);
}
```

means two input parameters and one output parameter

===> `(a, b) -> a.split(b)`

===> `String::split`

===> `IF2<String, String, String[]> if2 = String::split`

## Other modules

* [vavr](https://github.com/vavr-io/vavr) not recommend
* [java-promise](https://github.com/riversun/java-promise)


# Test with Spock

* [Spock Document](https://spockframework.org/spock/docs/1.0/interaction_based_testing.html)
* [Spock Examples](https://github.com/spockframework/spock-example)
* [Groovy List](https://www.baeldung.com/groovy-lists)
* [Book: Making Java Groovy](https://www.amazon.com/Making-Java-Groovy-Ken-Kousen/dp/1935182943)

---

* https://dzone.com/articles/java-lambda-method-reference
* https://www.baeldung.com/java-method-references
