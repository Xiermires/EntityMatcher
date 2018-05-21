# EntityMatcher

Enhance an EntityManager to create JPQL queries through matchers.

The matchers can be created both using the names of the Entities properties, or a Entity matching "mock" conveniently prepared to capture method invokations.

This WHERE statement...

```WHERE person.name LIKE 'X%' OR person.surname LIKE 'Y%' OR person.eyes = 'blue'```

is equivalent to the following generated matchers.

```matching(Person.class, "name", startsWith("X").or("surname", startsWith("Y")).or("eyes", eq("blue")))```
```
final Person mtch = BeanBasedMatcher.matcher(Person.class);
matching(mtch.getName(), startsWith("X").or(startsWith("Y")).or(mtch.getEyes(), eq("blue")))
```

From now on, we will focus only in the using names approach, although examples can be easily switched to its bean alternative.

### Step 0. Configuration

An EntityManager is required to instantiate an EntityMatcher.

We can obtain an EntityManager both in container and stand-alone scenarios since JPA works in both of them. In the project tests we use the former to exemplify the EntityMatcher uses. 

The following persistence.xml placed under 'src/test/java/META-INF/' enables JPA in stand-alone.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="1.0"
	xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
	<persistence-unit name="test" transaction-type="RESOURCE_LOCAL">
		<provider>org.hibernate.jpa.HibernatePersistenceProvider</provider> <!-- use Hibernate as the JPA persistence provider -->
		<properties>
			<property name="hibernate.connection.url" value="jdbc:h2:~/testDB" /> <!-- use H2 as the underlying relational DB -->
			<property name="dialect" value="org.hibernate.dialect.H2Dialect" />
			<property name="javax.persistence.schema-generation.database.action"
				value="drop-and-create" />
			<!--  property name="hibernate.show_sql" value="true" />
			<property name="hibernate.format_sql" value="true" />
			<property name="hibernate.use_sql_comments" value="true" /-->
		</properties>
	</persistence-unit>
</persistence>
``` 

### Step 1. Instantiate the matcher

```java 
final EntityManager em = Persistence.createEntityManagerFactory("test").createEntityManager();
final EntityMatcher matcher = new EntityMatcher(em);
```

### Step 2. The find and findAny methods

The find method retrieves a single element from the underlying DB satisfying the matchers. The element must be unique.
The findAny method retrieves any elements from the underlying DB satisfying the matchers. 

Both methods follow a similar structure.

matcher.find(return type, selection, matching, [ group by, having, order by] ).

Pretty much everything except the return type is optional.

For instance

1) ```matcher.findAny(Person.class)``` returns all elements inside the Person table.
2) ```matcher.find(Person.class, matching("name", eq("Xiermires")))``` returns the only person named Xiermires.

### Step 3. Usage

Let's assume the following two SQL Tables

1. 'Person' with properties 'name', 'height_cm', 'weight_kg', 'age'
2. 'Pet' with properties 'name', 'owner', 'species', 'age'

#### The basics

+ natural: find any person named Xiermires and who weights more than 70 kg
+ JPQL: ```SELECT person FROM Person person WHERE person.name = 'Xiermires' AND person.weight_kg > 70```
+ matcher: ```matcher.findAny(Person.class, matching("name", eq("Xiermires")).and("weight_kg", gt(70)))```
<br>

+ natural: find person names which start by X and whose aged is between 25 and 50
+ JPQL: ```SELECT person FROM Person person WHERE person.name LIKE 'X%' AND person.age BETWEEN 25 AND 50```
+ matcher: ```matcher.findAny(String.class, selection(Person.class, "name"), matching("name", startsWith("X")).and("age", between(25, 50)))```
<br>

+ natural: find any person which name is also a pet name
+ JPQL: ```SELECT person FROM Person person, Pet pet WHERE person.name = pet.name```
+ matcher: ```matcher.findAny(Person.class, matching("name", Pet.class))```
<br>

+ natural: find any pet which name is also a pet name
+ JPQL: ```SELECT person FROM Person person, Pet pet WHERE person.name = pet.name```
+ matcher: ```matcher.findAny(Person.class, matching("name", Pet.class))```

#### Parenthesis I

Matchers use implicit precedence, where any AND or OR right side terms are automatically between parenthesis, the following expressions look alike but their their precedence and expected results are different.

+ natural: find any person older than 20 and named either Xiermires or Serimreix
+ JPQL: ```SELECT person FROM Person person WHERE person.age > 20 AND ( person.name 'Xiermires' OR person.name = 'Serimreix' )```
+ matcher: ```matcher.findAny(Person.class, matching("age", gt(20)).and("name", eq("Xiermires").or("Serimreix")))```
<br>

+ natural: find any person older than 20 named Xiermires, or any other named Serimreix
+ JPQL: ```SELECT person FROM Person person WHERE person.age > 20 AND person.name = 'Xiermires' OR person.name = 'Serimreix'```
+ matcher: ```matcher.findAny(Person.class, matching("age", gt(20).and("name", eq("Xiermires").or("Serimreix"))))```
<br>

Parenthesis can be explicitly defined if desired using the closure() method.

+ matcher: ```matcher.findAny(Person.class, closure(matching("name", eq("Xiermires")).or("name", eq("Serimreix")))).and("age", gt(20)))```

#### Parenthesis II

Parenthesis also determine the leading table or column of an expression.

matching(Person.class, "name", eq("Xiermires")).and(matching(Pet.class, "name", eq("Fluffy"))).and("age", gt(10))

> WHERE person.name = 'Xiermires' AND pet.name = 'Fluffy' AND person.age > 10

matching(Person.class, "name", eq("Xiermires")).and(matching(Pet.class, "name", eq("Fluffy").and("age", gt(10))))

> WHERE person.name = 'Xiermires' and pet.name = 'Fluffy' and pet.age > 10

#### Not support

Any expression, or collection of expressions, can be negated by using the not() method. 

+ natural: find any person not aged more than 25
+ matcher: ```matcher.findAny(Person.class, matching("age", not(gt(25))))```
+ JPQL: ```SELECT person FROM Person person WHERE person.age < 25```

#### Group by, Order by, Having

Group by, order by and having clauses are all supported.

+ natural: find any person total owned pets
+ JPQL: ```SELECT COUNT(person.name) FROM Person person, Pet pet WHERE person.name = pet.owner GROUP BY person.name```
+ matcher: ```matcher.findAny(Long.class, count(Person.class, "name"), matching("name", matching(Pet.class, "owner")), groupBy("name"))```
<br>

+ natural: find all person order by name
+ JPQL: ```SELECT person FROM Person person ORDER BY person.name```
+ matcher: ```matcher.findAny(Person.class, orderBy("name"))```
<br>

+ natural: find person names of people owning more than 2 pets 
+ JPQL: ```SELECT person.name FROM Person person, Pet pet WHERE person.name = pet.owner GROUP BY person.name HAVING COUNT(person.name) > 2```
+ matcher: ```matcher.findAny(selection(Person.class, "name"), matching("name", matching(Pet.class, "owner")), groupBy("name").having(count("name"), gt(2)))```

#### Functions

The following functions are supported { DISTINCT, COUNT, MIN, MAX, SUM, AVG }. 

+ natural: find the max age of any person
+ JPQL: ```SELECT MAX(person.age) FROM Person person```
+ matcher: ```matcher.find(Long.class, max(Person.class, "age"))```
<br>

+ natural: find any person order by amount of owned pets 
+ JPQL: ```SELECT person, person.name FROM Person person, Pet pet WHERE person.name = pet.owner GROUP BY person.name ORDER BY COUNT(person.name)```
+ matcher: ```matcher.findAny(Person.class, matching("name", matching(Pet.class, "owner")), groupBy("name").orderBy(count("name")))```

### Step 4. Syntax sugar

Once you have an established data model, it is easy to write syntax sugar matchers that wrap the standard ones and produce closer to natural language expressions.

For instance, let's assume a couple of tables.

1) Person { id, name }
2) Address { id, person_id, location }

```matcher.findAny(Person.class, named("Xiermires").and(livesIn("Dresden")))```

looks clearer than...

```matcher.findAny(Person.class, matching("name", "Xiermires").and(Address.class, "location", eq("Dresden")).and(matching("id", matching(Address.class, "person_id")))))```

Matchers would like as follows...

```java
    public static NameBasedWhereBuilder named(String name) {
	return matching(null, "name", eq(name));
    }
    
    public static NameBasedWhereBuilder livesIn(String city) {
	return matching("id", matching(Address.class, "person_id")).and(location, eq(city));
    }
```
