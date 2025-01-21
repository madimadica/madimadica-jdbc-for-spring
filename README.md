# Madimadica JDBC for Spring®

**Java Version: 21+**

A wrapper around `JdbcTemplate` and `NamedParameterJdbcTemplate` for Spring JDBC.
The goal of this dependency is to add additional object-oriented capabilities to
what has already provided by Spring JDBC.


## Installation and Getting Started
This library is available in Maven Central.
### Maven
```xml
<dependency>
    <groupId>com.madimadica</groupId>
    <artifactId>madimadica-jdbc-for-spring</artifactId>
    <version>1.2.2</version>
</dependency>
```

### Getting Started
This dependency assumes the user will provide their own `spring-context` and `spring-jdbc` on version 6.
These are only `provided` by the user and are not included as transitive dependencies.

To use this, you need to register the beans included by this library by annotating your main class with:
```java
@Import(InitializeJdbc.class)
```

You are now ready to use the library, please see the examples and documentation below to learn more.

---

## Documentation

### Logging
You can configure the logging level of package `com.madimadica.jdbc.web` using SLF4J.
To see queries, use `DEBUG` logging. To see additional info when opening a fluent API,
use `TRACE` logging. Currently, there is nothing that uses `INFO` level.

An example `logback-spring.xml` is as follows. Without `additivity="false"` then logs
could be duplicated with the INFO config.

```xml
<logger name="com.madimadica.jdbc.web" level="DEBUG" additivity="false">
    <appender-ref ref="Console" />
</logger>
```

### Supported Dialects
Out of the box, the supported dialects are
* MySQL / MariaDB (`MySqlJdbc`)
* SQL Server (`SqlServerJdbc`)

### Adding a new Dialect
The `interface` at the base of the inheritence chain is `MadimadicaJdbc`. Most of the functionality
is defined by this interface with default methods. However, due to
the weirdness around batch inserts across different databases,
you should actually extend from `JdbcWithExplicitBatchInsertID` or `JdbcWithImplicitBatchInsertID`
to inherit the proper behavior for them. In either case you will need to implement `wrapIdentifier(String)`
for the new dialect, which handles special quote characters. Also, remember to make the new implementation
a bean for ease of access.

### Using a Dialect
To use a dialect implementation, such as in your service layer, simply add it
as an expected bean to that class and Spring will autowire it. For example,

```java
import com.madimadica.jdbc.web.MySqlJdbc;
import org.springframework.stereotype.Component;

@Component
public class MyService {
    private final MySqlJdbc mysql;
    
    public MyService(MySqlJdbc mysql) {
        this.mysql = mysql;
    }
}
```

## Parameters Documentation
To avoid SQL Injection, you should escape your parameters. Like in Spring JDBC,
this library supports **positional parameters** and **named parameters**.
Positional parameters are single `?` values where a variable should go,
and named parameters are a string prefixed by a `:` such as `:foo`.

This library provides 3 overloads for most methods starting with `query` or `update`. These are
1. `(String sql)`
2. `(String sql, Object... params)`
3. `(String sql, Map<String, ?> namedParams)`

### `String sql`
The first is the most straightforward. It just directly queries/updates using whatever SQL `String` is given.
All other overloads require this argument, and it is just expected to be plain SQL.

### Understanding `Object... params`
The second overload is where things get interesting. Here, parameters are 'flattened according to `FlattenedParameters#of`' in the JavaDoc.
It is also explained here, for convenience. 

Each index of the varargs corresponds to a *single* positional parameter (`?`) in the `String sql` argument.
This varargs argument can contain however many parameters, but must exactly match the number of positional parameters,
otherwise an exception will be thrown.

Suppose you write `query("SELECT * FROM foo WHERE a = ? AND b = ?", a, b)`,
this will assign `a` to the first `?`, and `b` to the second `?`.

Now, suppose you wish to run a `DELETE` query with an `IN` clause.
Instead of having to know exactly how many elements are in a list to apply in the `IN` clause,
simple use a single `?` and then provide a `java.util.Collection<?>` as the corresponding varargs.

For example,
```java
update("DELETE FROM foo WHERE id IN (?)", List.of(5, 6, 7));
```
will internally be converted into effectively writing
```java
update("DELETE FROM foo WHERE id IN (?, ?, ?)", 5, 6, 7);
```
You may also mix-and-match between collections and non-collections, such as
```java
update("DELETE FROM foo WHERE bar = ? AND id IN (?)", someBar, List.of(1, 2, 3));
```
to create the query like
```java
update("DELETE FROM foo WHERE bar = ? AND id IN (?, ?, ?)", someBar, 1, 2, 3);
```

As a fair warning, you cannot carelessly use this feature.
If the collection contains no elements, then the positional `?` will be replaced.
Such as `id IN (?)` to `id IN ()`, which is likely not supported by you data source.
Anytime a situation like this *could* occur, I recommend first checking the size, and early
returning if necessary.
```java
public List<User> findAllByIds(List<Long> ids) {
    if (ids.isEmpty()) {
        return List.of();
    }
    return userJdbc.query("SELECT * FROM users WHERE id IN (?)", ids);
}
```

### `Map<String, ?> params`
This argument is the same as a `NamedParameterJdbcTemplate` would use.
A key-value mapping of named parameters (without the `:` prefix) to their escaped values, such as `Map.of("foo", foo, "bar", bar)`.

## Using `TypedJdbc<T>`
For read operations, this generic class is a nice wrapper around queries to avoid redundant RowMapper arguments.

This class is *not* a bean. To construct it, you need to pass it a `MadimadicaJdbc` instance and a `RowMapper<T>`.
This is likely done in a constructor for another component.

```java
@Component
public class MyService {
    private final MySqlJdbc mysql;
    private final TypedJdbc<Dog> dogJdbc;

    public MyService(MySqlJdbc mysql) {
        this.mysql = mysql;
        this.dogJdbc = new TypedJdbc<Dog>(mysql, new DogRowMapper());
    }
}
```

### Methods `query` and `queryOne`
The `TypedJdbc` offers two methods, `query` and `queryOne`, each with the 3 common overloads.

`query` is used to query a `List<T>`, which can be 0 or more rows.

`queryOne` is used to query an `Optional<T>`, which expects 0 or 1 rows. If more than 1 row is returned
by the query, an `IncorrectResultSizeDataAccessException` exception is thrown. This behavior is consistent with Spring JDBC's `queryForObject` methods.
If no rows are returned, an `Optional.empty()` is given back. If exactly 1 row is returned, a non-empty `Optional<T>` is returned. 

### Examples of `query` and `queryOne`
The `query` methods have the standard overloads
```java
@Component
public class UserService {
    private final MySqlJdbc mysql;
    private final TypedJdbc<User> userJdbc;

    public UserService(MySqlJdbc mysql) {
        this.mysql = mysql;
        this.userJdbc = new TypedJdbc<User>(mysql, new UserRowMapper());
    }
    
    // query with no parameters
    public List<User> findAll() {
        return userJdbc.query("SELECT * FROM users");
    }

    // query with varargs parameters
    public List<User> findAllByFoo(String foo) {
        return userJdbc.query("SELECT * FROM users WHERE foo = ?", foo);
    }
    
    // query with named parameters 
    public List<User> findAllByBar(String bar) {
        return userJdbc.query("SELECT * FROM users WHERE bar = :bar", Map.of("bar", bar));
    }

    // queryOne with no parameters
    public Optional<User> findById(Long id) {
        return userJdbc.queryOne("SELECT * FROM users WHERE id = ?", id);
    }

    // queryOne with varargs parameters
    public Optional<User> findByEmail(String email) {
        return userJdbc.queryOne("SELECT * FROM users WHERE email = ?", email);
    }

    // queryOne with named parameters
    public Optional<User> findByFirstAndLastName(String firstName, String lastName) {
        return userJdbc.queryOne(
                "SELECT * FROM users WHERE first_name = :first AND last_name = :last",
                Map.of("first", firstName, "last", lastName)
        );
    }
}
```

## Using a `MadimadicaJdbc` Implementation
Anytime you don't need to query for a specific class type or need to perform updates,
an implementation of `MadimadicaJdbc`, such as `SqlServerJdbc` can be used effectively.

Many of the methods provide the common 3 parameter overloads, which are listed here:
* `update` - Run an arbitrary update statement to mutate the database. Note that this is the method used for inserts/updates/deletes, as in most JDBCs.
* `query` - Used by `TypedJdbc<T>`, except the `RowMapper<T>` is explicitly provided here.
* `queryOne` - Used by `TypedJdbc<T>`, except the `RowMapper<T>` is explicitly provided here.
* `queryLongs` Fetch a `List<Long>`
* `queryInts` Fetch a `List<Integer>`
* `queryStrings` Fetch a `List<String>`
* `queryLong` Fetch an `Optional<Long>`, behaves like `queryOne`
* `queryInt` Fetch an `Optional<Integer>`, behaves like `queryOne`
* `queryString` Fetch an `Optional<String>`, behaves like `queryOne`

The `execute(String sql)` method behaves exactly like `JdbcTemplate#execute`, and accepts no arguments and has no return type.


### Methods for Insert, Update, and Delete
The rest of the methods provide convenient ways to insert, update, and delete.
The intended usage is to use the provided fluent-builder API to execute these statements,
but the direct methods accepting a parameter object are available. These parameter objects are
* `RowUpdate` for performing a single UPDATE query
* `BatchUpdate` for performing a batch of UPDATE queries based on a `List<T>`
* `RowInsert` for performing a single INSERT query
* `BatchInsert` for performing a batch of INSERT queries based on a `List<T>`
* `DeleteFrom` for performing a single DELETE query

---

### Row Update Fluent API
This is used to perform a single `UPDATE` query, which can affect however many rows.
The number of rows affected is returned as an `int` on the terminal operation.

To open the API, begin with `updateTable(String tableName)`. This returns an instance
of a fluent builder.

Next, you can `SET` as many columns to values as you like, but it must be at least one.

Finally, you terminate the API and execute the query by calling `where`, `whereIdEquals`, or `whereIdIn`.

To set columns, you can either set a value as escaped or unescaped. Unescaped values can introduce SQL injection,
so they should only be used with hardcoded or trusted constants. To set an escaped value, use `set(String column, Object value)`,
and to set an unescaped value, use `setUnescaped(String column, Object value)`. The first argument defines
the column to update, and the second argument defines the new value.

You can also set multiple things in a single method call with `set(Map<String, ?> changes)` and `setUnescaped(Map<String, ?> changes)`.
These behave the same in terms of escaping, and take keys as the column name, and values as the value to assign.

The terminal `where` method expects a `String sql` and `Object... args`, flattened in the same way as other varargs queries.
The SQL string expects the raw SQL that goes *after* the `WHERE` keyword. So you would use `"id = ?"` to perform have a `WHERE id = ?`.
The `whereIdEquals(id)` method is shorthand for `where("id = ?", id)`, and the `whereIdIn(List<?> ids)` is shorthand for `where("id IN (?)", ids)`.
The shorthands assume a primary key column named `id` exists, so they might be useless for your use case. In any case,
all 3 methods perform the update statement when invoked.



#### Examples
```java
// UPDATE users SET email = ?, phone = ?, updated_by = ?, updated_at = GETDATE() WHERE id = ?
int rowsAffected = jdbcImpl.updateTable("users")
        .set("email", dto.email())
        .set("phone", dto.phone())
        .set("updated_by", dto.actor())
        .setUnescaped("updated_at", "GETDATE()") // unescaped function
        .where("id = ?", dto.id());
```

```java
// UPDATE users SET foo = foo + 1 WHERE id = ? 
int rowsAffected = jdbcImpl.updateTable("users")
        .setUnescaped("foo", "foo + 1")
        .whereIdEquals(5);
```

---

### Batch Update Fluent API
This is used to perform multiple `UPDATE` queries, each depending on a `T row`.
The returned value is an `int[]` with the number of rows affected by each query in the batch.

To open the API, begin with `batchUpdate(String tableName, List<T> rows)`. This returns an instance
of a fluent builder. The `List<T>` rows is the list of objects to derive mapped updates from.

After starting the fluent API, the next step is to assign column name-value mappings.
The values can be escaped constants `set(String, Object)`, unescaped constants `setUnescaped(String, Object)`,
or a mapping `set(String, Function<? super T, Object>)`. The real power of this API comes from the functional
mapping.

Finally, the API is terminated by a where method, like in `RowUpdate`.
There primary method is `where(String, List<Function>)`, with the others being shortcuts.
They are all `Function<? super T, Object>`, but I'll use the `Function` erasure for brevity.
`whereIdEquals(function)` is short for `where("id = ?", function)`.
There are also 4 overloads to not require a `List<Function>`.
* `where(String, Function)`
* `where(String, Function, Function)`
* `where(String, Function, Function, Function)`
* `where(String, Function, Function, Function, Function)`

Recall that these all perform the operation, thus executing the batch of queries. However, if the `List<T> rows` is empty,
no queries are performed.

In the rare event that you need an *escaped constant* in the where clause, use a lambda expression like `ignored -> myConst`

#### Examples
```java
List<User> users = ...;
userJdbc.batchUpdate("users", users)
        .set("first_name", User::getFirstName) // Determined by row
        .set("last_name", User::getLastName) // Determined by row
        .setUnescaped("updated_at", "GETDATE()") // Unescaped constant for all rows
        .set("updated_by", actor) // Constant for all rows
        .whereIdEquals(User::getId);
```

```java
List<Foo> fooList = ...;
fooJdbc.batchUpdate("foo", fooList)
        .set("bar", Foo::getBar)
        .set("baz", Foo::getBaz)
        .where("id = ?", Foo::getId);
```

---

### Row Insert Fluent API
This is used to perform a single `INSERT` query. The return value depends on the terminal operation.

To open the API, begin with `insertInto(String tableName)`. This returns an instance
of a fluent builder.

Next, assign at least one column-value pair. These can be escaped with `value(String, Object)`,
or unescaped with `valueUnescaped(String, Object)`.

To terminate the API, use either `insert()`, `insertReturningNumber()`, `insertReturningInt()`, or `insertReturningLong()`.
If the table has no auto-generated columns, use `insert()`, which returns the number of rows inserted (should be 1).
If you do expect an auto-generated column, use one of the "returning" methods to retrieve that generated value
as the specific type.

* `int insertReturningInt()`
* `long insertReturningLong()`
* `Number insertReturningNumber()`

These terminal methods close the API and execute the insert query.

#### Examples
```java
long userId = jdbcImpl.insertInto("users")
        .value("first_name", dto.getFirstName())
        .value("last_name", dto.getLastName())
        .valueUnescaped("inserted_at", "GETDATE()")
        .value("inserted_by", actor)
        .insertReturningLong();
```

```java
jdbcImpl.insertInto("serves")
        .value("pizzeria_id", dto.getPizzeriaId())
        .value("pizza_type_id", dto.getPizzaTypeId())
        .insert();
```


---

### Batch Insert Fluent API
This is used to perform a batch of `INSERT` queries. The return value depends on the terminal operation.

Note that there are two variations of this API, one for explicit generated IDs, and one for implicit generated IDs.
This only makes a difference if you have generated IDs and want to fetch them.

To open the API, begin with `batchInsertInto(String tableName, List<T> rows)`. This returns an instance
of a fluent builder. The `List<T>` rows is the list of objects to derive mapped inserts from.

Next, specify at least one column value mapping. This can be an escaped constant with `value(String, Object)`,
an unescaped constant with `valueUnescaped(String, Object)`, or a functionally mapping to an escaped value
with `value(String column, Function<? super T, Object> valueMapper)`.

Finally, close the API with a terminal insert method, similar to a Row Insert. This can be `int[] insert()`
to insert and return the list of rows affected per query (should be an array of all 1s). It can also be
one of the methods to return generated ids, per row, returned in the same encounter order as the original
`List<T> rows` iterator. These depend on whether you have implicit or explicit ID handling.

For implicit IDs, the signatures are
* `List<Integer> insertReturningInts()`
* `List<Long> insertReturningLongs()`
* `List<Number> insertReturningNumbers()`

For explicit IDs, the signatures are
* `List<Integer> insertReturningInts(String generatedColumn)`
* `List<Long> insertReturningLongs(String generatedColumn)`
* `List<Number> insertReturningNumbers(String generatedColumn)`

Any of these terminal methods will perform the batch insert queries. However, if the `List<T> rows` is empty,
no queries are performed.

#### Examples
```java
List<Long> userIds = mySqlImpl.batchInsertInto("users", newUsers)
        .value("first_name", CreateUserDTO::firstName)
        .value("last_name", CreateUserDTO::lastName)
        .value("email", CreateUserDTO::email)
        .value("inserted_by", actor)
        .valueUnescaped("inserted_at", "GETDATE()")
        .insertReturningLongs();
```

```java
List<Long> userIds = sqlServerImpl.batchInsertInto("users", newUsers)
        .value("first_name", CreateUserDTO::firstName)
        .value("last_name", CreateUserDTO::lastName)
        .value("email", CreateUserDTO::email)
        .value("inserted_by", actor)
        .valueUnescaped("inserted_at", "GETDATE()")
        .insertReturningLongs("id");
```

```java
jdbcImpl.batchInsertInto("serves", offerings)
        .value("pizzeria_id", Serves::getPizzeriaId)
        .value("pizza_type_id", Serves::getPizzaTypeId)
        .insert();
```

---

### Delete From Fluent API
This is used to perform a single `DELETE` queries.
The returned value is an `int` with the number of rows affected (deleted) by the query.

This has a very short API. To open it, use `jdbcImpl.deleteFrom(String tableName)` to specify the table
the `DELETE` should be on.

Next, a single `where(String sql, Object... args)` method defines the where clause, similar to a Row Update,
where the `"WHERE"` string is not needed, and the parameters are flattened. This is the terminal operation
and will execute the generated statement, returning the number of rows affected.

#### Examples
```java
jdbcImpl.deleteFrom("users").where("id = ?", id);
```
```java
jdbcImpl.deleteFrom("foo").where("bar = ? AND baz = ?", a, b);
```
```java
jdbcImpl.deleteFrom("users").where("id IN (?)", ids);
```


---


This project is in no way affiliated with or endorsed by Spring®.

Spring is a trademark of Broadcom Inc. and/or its subsidiaries.
