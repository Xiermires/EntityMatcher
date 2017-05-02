# EntityMatcher

Create JPQL queries through matching Entity classes in the "hamcrest way".

The matcher user can extend the functionality with its own statements (created in similar fashion as in the Statements class).

By default it translates the queries into JPQL. Native SQL support can be enabled by calling the #nativeQuery(true) method.

TODOs :

* Allow nesting classes

-------------
Examples
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

        @Column(name = "Bar_v2")
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