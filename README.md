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

---

* https://dzone.com/articles/java-lambda-method-reference
* https://www.baeldung.com/java-method-references
