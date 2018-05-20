# EntityMatcher

Enhance an EntityManager to create JPQL queries through matchers.

The matchers can be created both using the names of the Entities properties, or a Entity matching "mock" conveniently prepared to capture method invokations.

```final Person mtch = BeanBasedMatcher.matcher(Person.class);```

The following matchers are equivalent.

```java matching("name", startsWith("X").or("Y")).or("eyes", eq("blue"))```
```java matching(mtch.getName(), startsWith("X").or("Y")).or(mtch.getEyes(), eq("blue"))```

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

The matchers use the Entity field names to identify 

### Step 1. Instantiate the matcher

```java 
final EntityManager em = Persistence.createEntityManagerFactory("test").createEntityManager();
final EntityMatcher matcher = new EntityMatcher(em);
```

### Step 2. Using the matcher

Let's assume a SQL Table 'Person' with properties 'name', 'surname', 'height_cm', 'weight_kg'

+ natural: find any person named Xiermires and who weights more than 70 kg
+ JPQL: ```sql SELECT person FROM Person person WHERE person.name = 'Xiermires' AND person.weight_kg > 70```
+ matcher: ```java matcher.findAny(Person.class, matching("name", eq("Xiermires")).and("weight_kg, gt(70)))```

In similar fashion we can use 

