# EntityMatcher

Create JPQL queries through matching Entity classes in the "hamcrest way".

By default it translates the queries into JPQL. Native SQL support can be enabled by calling the #nativeQuery(true) method.

TODOs :

* Allow nesting classes
* Having, group by, order by

About...

1) Matchers

Matches are the classes which we'll use to match our statements.
To be able to use an Entity as a matcher, we need to create it using the EntityMatcher#matcher(Class<?> entity) method.

```
    final Person person = EntityMatcher.matcher(Person.class);
```

Once created we can attach it to a builder, which will create the JPQL / SQL statements for us.

```
    final Builder<Person> builder = EntityMatcher.builder(person);
```

We can use as many matchers as we want in our queries, so far we register them all while creating the builder.

```
    final Person person = EntityMatcher.matcher(Person.class);
    final Animal animal = EntityMatcher.matcher(Animal.class);

    final Builder<Person> builder = EntityMatcher.builder(person, animal);
```

2) Flexible return types.

Before we saw that each Builder is associated to one type. Per design the first matcher listed in the #builder(...) method is considered the return type. That is convenient whenever we are interested in the Entity itself.

For those cases that we want other return type we use the #select() method. For instance let's retrieve only the name of the Entity person.

```
    final EntityManager em = ...;
    final Person person = EntityMatcher.matcher(Person.class);
    final Builder<Person> builder = EntityMatcher.builder(person);

    // SELECT person.name FROM Person person
    final List<String> names = builder.select(person.getName()).build(String.class).getMatching(em);
```

We can also use Java Beans to store the retrived information. For instance assuming our Animal entity has a name and an age property, the following is valid.
```
    final EntityManager em = ...;
    final Person person = EntityMatcher.matcher(Person.class);
    final Builder<Person> builder = EntityMatcher.builder(person);

    // SELECT person.name, person.age FROM Person person
    final List<Animal> animerson = builder.select(person.getName(), person.getAge()).build(Animal.class).getMatching(em);
```

3) Statements

There are two kind of statements. LhsStatements (left-hand-side) and LhsRhsStatements (left-hand-side and right-hand-side). 
A LhsStatements receives information about one capture (table / column), while a LhsRhsStament receive information about two captures.

For instance { =, !=, like, >, >=, in, ... } are all <b>LhsStatements</b> since the statement composes of a table + column, an operation, plus maybe a value.

As an example, let's find within a table associated to an entity Person, all people called "Xiermires".

```
    // Get a matcher for some entity.
    final EntityManager em = ...;
    final Person person = EntityMatcher.matcher(Person.class);

    final List<Person> persons = EntityMatcher.builder(person) // 
                                    .match(person.getName(), eq("Xiermires")) // 
                                    .build().getMatching(em);
```

LhsStatements allow AND and OR chains to simplify building process. 
``` 
    final Person person = EntityMatcher.matcher(Person.class);

    // This is allowed but a bit verbose
    EntityMatcher.builder(person) // 
                    .match(person.getName(), eq("Xiermires")) // 
                    .or(person.getName(), eq("Serimreix"));

    // Same using an OR chain
    EntityMatcher.builder(person) // 
                    .match(person.getName(), eq("Xiermires").or(eq("Serimreix")))
```
      
Both these expressions translate into : SELECT person FROM Person person WHERE person.name = 'Xiermires' OR person.name = 'Serimreix'

An additional example of the same principle would be to find all people older than 30, which ages are odd. 

```
    EntityMatcher.builder(person).nativeQuery(true) // 
                    .match(person.getAge(), gt(30).and(isOdd()))
```

This would translate as : SELECT person FROM Person person WHERE person.age > 30 AND isOdd(person.age)

isOdd is a store procedure which checks if the age is odd (captain obvious here). Since JPQL doesn't accept stored procedures, we need to switch to native mode (see nativeQuery(true))

<b>LhsRhsStatements</b> on the other hand receive two tables and two columns to build the statement with. A typical example would be a join.

Let's for instance check which people share name with an animal (ehem... Indiana).

```
    final Person person = EntityMatcher.matcher(Person.class);
    final Animal animal = EntityMatcher.matcher(Animal.class);
    EntityMatcher.builder(person, animal) // 
                    .match(person.getName(), join(animal.getName()));
```

Unfortunately to join several tables, only the verbose option is available (that might change in the future).

```
    final Person person = EntityMatcher.matcher(Person.class);
    final Animal animal = EntityMatcher.matcher(Animal.class);
    final City city = EntityMatcher.matcher(City.class);
    EntityMatcher.builder(person, animal, city) // 
                    .match(person.getName(), join(animal.getName())).and(person.getName(), join(city.getName()));
```


Coding Statements is relatively straightforward. Both LhsStatement and LhsRhsStatement are instantiated with a Statement, where each statement implements the following interface.

```

public interface Statement
{
    List<Part> toJpql(String lhsTableAlias, String lhsColumn, String rhsTableAlias, String rhsColumn, ParameterBinding params);
    
    // syntax sugar to create parts, toString(), etc.
    ...
}
```

For instance this creates a LIKE statement.

```
    public static <T> LhsStatement<T> like(T t)
    {
        // This is a LhsStatement. { rhsTableAlias && rhsColumn } are both null.
        return new LhsStatement<T>((lhsTableAlias, lhsColumn, rhsTableAlias, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn), Connector.LIKE, valueOf(t)));
    }
```

There are a couple of things to comment here. 

a) Why forcing some weird Part thing and not return a String directly ? 

```(lhsAlias, lhsColumn, rhsAlias, rhsColumn, params) -> tableColumn(lhsTable, lhsColumn) + " LIKE " + valueOf(t));```

Basically due to the NOT nature. Consider how to acomplish the following if we were to use Strings. 

```...match(person.getName(), not(like("Xiermir%")).and(not(in(tabooNames)));``` 


Let's have a look at how the operations look like ?.

```    
    enum Connector implements Negatable
    {
        LIKE(" LIKE ", " NOT LIKE "), EQ(" = ", " != "), GT(" > ", " < "), LT(" < ", " > "), IN(" IN ", " NOT IN ");

        final String affirmed;
        final String negated;

        boolean isNegated;
        String conn;

        Connector(String affirmed, String negated)
        {
            this.affirmed = this.conn = affirmed;
            this.negated = negated;
            this.isNegated = false;
        }

        @Override
        public void negate()
        {
            conn = isNegated ? affirmed : negated;
            isNegated = !isNegated
        }

        @Override
        public String toString()
        {
            return conn;
        }
    }
```

b) What is params ?

The params interface allows to create parameters (?\d), which makes the statement creation easier in some cases. For instance, see the IN operation.

```
    public static <T> LhsStatement<T> in(Collection<T> ts)
    {
        return new LhsStatement<T>((lhsTable, lhsColumn, rhsTable, rhsColumn, params) -> Statement.create(
                tableColumn(lhsTable, lhsColumn), Connector.IN, params.bind(ts)));
    }
```

This can also be used to bind parameters to stored procedures.


4) Anything else ?

Let's just mention functions. Others, like having, group by, etc. follow similar principles. 

As an example, let's get the max age of all persons.

```
    final EntityManager em = ...;
    final Person person = EntityMatcher.matcher(Person.class);
    final Builder<Person> builder = EntityMatcher.builder(person);

    // SELECT max(person.age) FROM Person person
    final Integer age = builder.select(max(person.getAge())).build(Integer.class).getSingleMatching(em);
```

-------------
More Examples
-------------

```java
    public void like()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), like("Hell%")).build();
        final List<TestClass> tcs = query.getMatching(em);
    }
    
    public void not()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), not(like("Hell%"))).build();
        final List<TestClass> tcs = query.getMatching(em);
    }
    
    public void greaterThan()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), gt(4)).build();
        final List<TestClass> tcs = query.getMatching(em);
    }
    
    public void and()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), lt(4)).and(tc.getBar(), eq("Bye")).build();
        final List<TestClass> tcs = query.getMatching(em);
    }
    
    public void columnOr()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), eq("Hello").or(eq("Bye"))).build();
        final List<TestClass> tcs = query.getMatching(em);
    }   

    public void join()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final TestJoin tj = EntityMatcher.matcher(TestJoin.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc, tj);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), join(tj.getBar())).build();
        final List<TestClass> tcs = query.getMatching(em);
    }    
    
    public void flexibleReturnWrapInJavaBeanWithSameColumnName()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestJoin> query = builder.select(tc.getBar()).match(tc.getBar(), like("Hell%"))
                .build(TestJoin.class);
        final TestJoin to = query.getSingleMatching(em);
    }
    
    public void flexibleReturnDirectMapping()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<String> stringQuery = builder.select(tc.getBar()).match(tc.getBar(), like("Hell%"))
                .build(String.class);
        final String bar = stringQuery.getSingleMatching(em);
    }

    public void max()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<Integer> stringQuery = builder.select(max(tc.getFoo())).build(Integer.class);
        stringQuery.getSingleMatching(em);
    }
    
    public void distinct()
    {
        final TestOther to = EntityMatcher.matcher(TestOther.class);
        final Builder<TestOther> builder = EntityMatcher.builder(to);

        final PreparedQuery<Integer> stringQuery = builder.select(distinct(to.getBar())).build(Integer.class);
        stringQuery.getMatching(em);
    }
    
    public void maxDistinct()
    {
        final TestOther to = EntityMatcher.matcher(TestOther.class);
        final Builder<TestOther> builder = EntityMatcher.builder(to);

        // Hibernate JPQL complains about max(distinct(column)). Use native.
        final PreparedQuery<Integer> stringQuery = builder.select(max(distinct(to.getFoo()))).nativeQuery(true).build(Integer.class);
        stringQuery.getSingleMatching(em);
    }
    
    
    @Entity
    @Table(name = "TestClass")
    public static class TestClass
    {
        @Id
        @GeneratedValue
        long id;
        
        int foo;
        
        @Column(name = "Bar_v2")
        String bar;

        // test only
        TestClass(int foo, String bar)
        {
            this.foo = foo;
            this.bar = bar;
        }

        public TestClass()
        {   
        }
        
        public int getFoo()
        {
            return foo;
        }

        public void setFoo(int foo)
        {
            this.foo = foo;
        }

        public String getBar()
        {
            return bar;
        }

        public void setBar(String bar)
        {
            this.bar = bar;
        }
    }
    
    @Entity
    @Table(name = "TestJoin")
    public class TestJoin
    {
        @Id
        @GeneratedValue
        long id;

        int foo;

        @Column
        String bar;

        // test only
        TestJoin(int foo, String bar)
        {
            this.foo = foo;
            this.bar = bar;
        }

        public TestJoin()
        {
        }

        public int getFoo()
        {
            return foo;
        }

        public void setFoo(int foo)
        {
            this.foo = foo;
        }

        public String getBar()
        {
            return bar;
        }

        public void setBar(String bar)
        {
            this.bar = bar;
        }
    }    
```