/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.entitymatcher;

import static org.entitymatcher.JavaBeanBasedExpressions.between;
import static org.entitymatcher.JavaBeanBasedExpressions.eq;
import static org.entitymatcher.JavaBeanBasedExpressions.gt;
import static org.entitymatcher.JavaBeanBasedExpressions.in;
import static org.entitymatcher.JavaBeanBasedExpressions.like;
import static org.entitymatcher.JavaBeanBasedExpressions.lt;
import static org.entitymatcher.JavaBeanBasedExpressions.match;
import static org.entitymatcher.JavaBeanBasedExpressions.not;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.h2.jdbcx.JdbcDataSource;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

public class EntityMatcherTest
{
    static EntityMatcher matcher;

    @BeforeClass
    public static void pre()
    {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:~/testDB");

        final EntityManager em = Persistence.createEntityManagerFactory("test").createEntityManager();

        try
        {
            em.getTransaction().begin();
            em.persist(new TestClass(2, null));
            em.persist(new TestClass(5, "Hello"));
            em.persist(new TestClass(3, "Bye"));
            em.persist(new TestClass(6, "Lizard"));
            em.persist(new TestJoin(3, "Hello"));
            em.persist(new TestJoin(7, "Bye"));
            em.persist(new TestJoin(6, "Rabbit"));
            em.persist(new TestOther(6, "Hello"));
            em.persist(new TestOther(1, "Snake"));
            em.persist(new TestOther(2, "Snake"));
            em.persist(new TestOther(3, "Snake"));
            em.getTransaction().commit();
        }
        catch (Exception e)
        {
            if (em != null)
            {
                em.getTransaction().rollback();
            }
        }

        matcher = new EntityMatcher(em);
    }

    @Test
    public void testEq()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final TestClass testee = matcher.singleMatch(TestClass.class, match(tc.getBar(), eq("Hello")));
        assertThat(testee, is(Matchers.not(nullValue())));
        assertThat(testee.bar, is("Hello"));
    }

    @Test
    public void testEqNull()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final TestClass testee = matcher.singleMatch(TestClass.class, match(tc.getBar(), eq(null)));
        assertThat(testee, is(Matchers.not(nullValue())));
        assertThat(testee.bar, is(nullValue()));
    }

    @Test
    public void testEqNotNull()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getBar(), not(eq(null)).and(like("%e%"))));
        assertThat(testee.size(), is(2));
    }

    @Test
    public void testLike()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final TestClass testee = matcher.singleMatch(TestClass.class, match(tc.getBar(), like("Hell%")));
        assertThat(testee, is(Matchers.not(nullValue())));
        assertThat(testee.bar, startsWith("Hell"));
    }

    @Test
    public void testNotLike()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getBar(), not(like("Hell%"))));
        assertThat(testee.size(), is(2));
        for (TestClass t : testee)
            assertThat(t.bar, Matchers.not(startsWith("Hell")));
    }

    @Test
    public void testGreaterThan()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getFoo(), gt(4)));

        assertThat(testee.size(), is(2));
        for (TestClass t : testee)
            assertThat(t.foo, is(greaterThan(4)));
    }

    @Test
    public void testNotGreaterThan()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getFoo(), not(gt(4))));

        assertThat(testee.size(), is(2));
        for (TestClass t : testee)
            assertThat(t.foo, is(Matchers.not(greaterThan(4))));
    }

    @Test
    public void testAndSameTableDifferentProperties()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final TestClass testee = matcher.singleMatch(TestClass.class, match(tc.getFoo(), lt(4)).and(tc.getBar(), eq("Bye")));
        assertThat(testee.foo, is(lessThan(4)));
        assertThat(testee.bar, is("Bye"));
    }

    @Test
    public void testOrSameTableDifferentProperties()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getFoo(), lt(4)).or(tc.getBar(), eq("Bye")));
        for (TestClass t : testee)
        {
            final boolean lt4 = t.foo < 4;
            final boolean isBye = "Bye".equals(t.bar);
            assertThat(lt4 || isBye, is(true));
        }
    }

    @Test
    public void testOrSameTableSameProperty()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getBar(), eq("Hello").or(eq("Bye"))));
        final List<String> bars = Arrays.asList("Hello", "Bye");
        for (TestClass t : testee)
            assertThat(bars.contains(t.bar), is(true));
    }

    @Test
    public void testSingleJoin()
    {
        final TestJoin tj = JavaBeanBasedMatcher.matcher(TestJoin.class);
        final List<TestClass> joins = matcher.match(TestClass.class, match(tj.getBar()));
        assertThat(joins.size(), is(2));
    }

    @Test
    public void testMultipleJoin()
    {
        final TestJoin tj = JavaBeanBasedMatcher.matcher(TestJoin.class);
        final TestOther to = JavaBeanBasedMatcher.matcher(TestOther.class);
        final TestClass join = matcher.singleMatch(TestClass.class, match(tj.getBar()).and(match(to.getBar())));
        assertThat(join, is(Matchers.not(nullValue())));
    }

    @Test
    public void testIn()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<String> bars = Arrays.asList("Hello", "Bye");
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getBar(), in(bars)));
        for (TestClass t : testee)
            assertThat(bars.contains(t.bar), is(true));
    }

    @Test
    public void testNotNot()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getBar(), not(not(eq("Hello").or(eq("Bye"))))));
        final List<String> bars = Arrays.asList("Hello", "Bye");
        for (TestClass t : testee)
            assertThat(bars.contains(t.bar), is(true));
    }

    @Test
    public void testBetween()
    {
        final TestClass tc = JavaBeanBasedMatcher.matcher(TestClass.class);
        final List<TestClass> testee = matcher.match(TestClass.class, match(tc.getFoo(), between(3, 5)));
        for (TestClass t : testee)
        {
            assertThat(t.foo, is(greaterThanOrEqualTo(3)));
            assertThat(t.foo, is(lessThanOrEqualTo(5)));
        }
    }

    // @Test
    // public void testGreaterThan()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), gt(4)).build();
    // assertThat(query.getMatching(em).size(), is(2));
    // }
    //
    // @Test
    // public void testNotGreaterThan()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), not(gt(4))).build();
    // assertThat(query.getMatching(em).size(), is(1));
    // }
    //
    // @Test
    // public void testLowerThan()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), lt(4)).build();
    // assertThat(query.getMatching(em).size(), is(1));
    // }
    //
    // @Test
    // public void testSameTableAnd()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), lt(4)).and(tc.getBar(),
    // eq("Bye")).build();
    // assertThat(query.getMatching(em).size(), is(1));
    // }
    //
    // @Test
    // public void testSameTableOr()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getBar(),
    // eq("Hello").or(eq("Bye"))).build();
    // assertThat(query.getMatching(em).size(), is(2));
    // }
    //
    // @Test
    // public void testSingleJoin()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final TestJoin tj = JavaBeanMatcher.matcher(TestJoin.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc, tj);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getBar(), join(tj.getBar())).build();
    // assertThat(query.getMatching(em).size(), is(2));
    // }
    //
    // @Test
    // public void testMultipleJoin()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final TestJoin tj = JavaBeanMatcher.matcher(TestJoin.class);
    // final TestOther to = JavaBeanMatcher.matcher(TestOther.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc, tj, to);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getBar(),
    // join(tj.getBar())).and(tj.getBar(), join(to.getBar()))
    // .build();
    // assertThat(query.getMatching(em).size(), is(1));
    // }
    //
    // @Test
    // public void testIn()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getBar(), in(Arrays.asList("Hello",
    // "Bye"))).build();
    // assertThat(query.getMatching(em).size(), is(2));
    // }
    //
    // @Test
    // public void testInNative()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getBar(), in(Arrays.asList("Hello",
    // "Bye"))).nativeQuery(true)
    // .build();
    // assertThat(query.getMatching(em).size(), is(2));
    // }
    //
    // @Test
    // public void testNative()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> query = builder.match(tc.getBar(),
    // like("Hell%").or(like("By%"))).nativeQuery(true)
    // .build();
    // assertThat(query.getMatching(em).size(), is(2));
    // }
    //
    // @Test
    // public void testCustomSelectUsingJavaBean()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestOther> useObjectWithBarProperty =
    // builder.select(tc.getBar()).match(tc.getBar(), like("Hell%"))
    // .build(TestOther.class);
    // final TestOther to = useObjectWithBarProperty.getSingleMatching(em);
    //
    // assertThat(to.bar, is("Hello"));
    // assertThat(to.foo, is(0));
    // }
    //
    // @Test
    // public void testCustomSelectDirectMapping()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<String> stringQuery = builder.select(tc.getBar()).match(tc.getBar(),
    // like("Hell%"))
    // .build(String.class);
    // assertThat(stringQuery.getSingleMatching(em), is("Hello"));
    // }
    //
    // @Test
    // public void testMax()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<Integer> stringQuery =
    // builder.select(max(tc.getFoo())).build(Integer.class);
    // assertThat(stringQuery.getSingleMatching(em), is(6));
    // }
    //
    // @Test
    // public void testMin()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<Integer> stringQuery =
    // builder.select(min(tc.getFoo())).build(Integer.class);
    // assertThat(stringQuery.getSingleMatching(em), is(3));
    // }
    //
    // @Test
    // public void testCountDistinct()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // // FIXME: Conversion operations within the EntityMatcher (this should return an Integer).
    // final PreparedQuery<BigInteger> stringQuery =
    // builder.select(count(distinct(tc.getBar()))).nativeQuery(true)
    // .build(BigInteger.class);
    // assertThat(stringQuery.getSingleMatching(em).intValue(), is(3));
    // }
    //
    // @Test
    // public void testCount()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // // FIXME: Conversion operations within the EntityMatcher (this should return an Integer).
    // final PreparedQuery<BigInteger> stringQuery =
    // builder.select(count(tc.getBar())).nativeQuery(true)
    // .build(BigInteger.class);
    // assertThat(stringQuery.getSingleMatching(em).intValue(), is(3));
    // }
    //
    // @Test
    // public void testDistinct()
    // {
    // final TestOther to = JavaBeanMatcher.matcher(TestOther.class);
    // final Builder<TestOther> builder = JavaBeanMatcher.builder(to);
    //
    // final PreparedQuery<Integer> stringQuery =
    // builder.select(distinct(to.getBar())).build(Integer.class);
    // assertThat(stringQuery.getMatching(em).size(), is(2));
    // }
    //
    // @Test
    // public void testMaxDistinct()
    // {
    // final TestOther to = JavaBeanMatcher.matcher(TestOther.class);
    // final Builder<TestOther> builder = JavaBeanMatcher.builder(to);
    //
    // final PreparedQuery<Integer> stringQuery =
    // builder.select(max(distinct(to.getFoo()))).nativeQuery(true)
    // .build(Integer.class);
    // assertThat(stringQuery.getSingleMatching(em), is(6));
    // }
    //
    // @Test
    // @Ignore
    // public void trySignatures()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final TestJoin tj = JavaBeanMatcher.matcher(TestJoin.class);
    // final TestOther to = JavaBeanMatcher.matcher(TestOther.class);
    //
    // final Builder<TestClass>.LhsRhsStatementBuilder composer = JavaBeanMatcher.builder(tc, tj,
    // to)
    // .match(tc.getBar(), like("Hello").or(like("Bye"))) //
    // .and(tc.getBar(), gt(1)) //
    // .and(tc.getBar(), join(tj.getBar())).and(tj.getBar(), join(to.getBar()));
    //
    // System.out.println(composer.toString());
    // }
    //
    // @Test
    // public void orderBy()
    // {
    // final TestClass tc = JavaBeanMatcher.matcher(TestClass.class);
    // final Builder<TestClass> builder = JavaBeanMatcher.builder(tc);
    //
    // final PreparedQuery<TestClass> stringQuery = builder.orderBy(tc.getFoo()).build();
    // int next = Integer.MIN_VALUE;
    // for (TestClass each : stringQuery.getMatching(em))
    // {
    // next = Math.max(each.getFoo(), next);
    // assertThat(each.getFoo(), is(next));
    // }
    // }
    //
    // @Test
    // // TODO Review
    // public void havingCount()
    // {
    // final TestOther to = JavaBeanMatcher.matcher(TestOther.class);
    // final Builder<TestOther> builder = JavaBeanMatcher.builder(to);
    //
    // final PreparedQuery<Integer> stringQuery =
    // builder.select(count(to.getFoo())).groupBy(to.getBar())
    // .having(count(to.getFoo()), gt(2)).nativeQuery(true).build(Integer.class);
    //
    // final Integer res = stringQuery.getSingleMatching(em);
    // assertThat(res.intValue(), is(3));
    // }
}
