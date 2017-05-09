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

import static org.entitymatcher.Statements.count;
import static org.entitymatcher.Statements.distinct;
import static org.entitymatcher.Statements.eq;
import static org.entitymatcher.Statements.gt;
import static org.entitymatcher.Statements.in;
import static org.entitymatcher.Statements.join;
import static org.entitymatcher.Statements.like;
import static org.entitymatcher.Statements.lt;
import static org.entitymatcher.Statements.max;
import static org.entitymatcher.Statements.min;
import static org.entitymatcher.Statements.not;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.entitymatcher.EntityMatcher.Builder;
import org.entitymatcher.EntityMatcher.PreparedQuery;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class EntityMatcherTest
{
    static EntityManager em;

    @BeforeClass
    public static void pre()
    {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:~/testDB");

        em = Persistence.createEntityManagerFactory("test").createEntityManager();

        try
        {
            em.getTransaction().begin();
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
    }

    @Test
    public void testLike()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), like("Hell%")).build();
        assertThat(query.getMatching(em).size(), is(1));
    }

    @Test
    public void testNotLike()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), not(like("Hell%"))).build();
        assertThat(query.getMatching(em).size(), is(2));
    }

    @Test
    public void testGreaterThan()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), gt(4)).build();
        assertThat(query.getMatching(em).size(), is(2));
    }

    @Test
    public void testNotGreaterThan()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), not(gt(4))).build();
        assertThat(query.getMatching(em).size(), is(1));
    }

    @Test
    public void testLowerThan()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), lt(4)).build();
        assertThat(query.getMatching(em).size(), is(1));
    }

    @Test
    public void testSameTableAnd()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getFoo(), lt(4)).and(tc.getBar(), eq("Bye")).build();
        assertThat(query.getMatching(em).size(), is(1));
    }

    @Test
    public void testSameTableOr()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), eq("Hello").or(eq("Bye"))).build();
        assertThat(query.getMatching(em).size(), is(2));
    }

    @Test
    public void testSingleJoin()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final TestJoin tj = EntityMatcher.matcher(TestJoin.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc, tj);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), join(tj.getBar())).build();
        assertThat(query.getMatching(em).size(), is(2));
    }

    @Test
    public void testMultipleJoin()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final TestJoin tj = EntityMatcher.matcher(TestJoin.class);
        final TestOther to = EntityMatcher.matcher(TestOther.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc, tj, to);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), join(tj.getBar())).and(tj.getBar(), join(to.getBar()))
                .build();
        assertThat(query.getMatching(em).size(), is(1));
    }

    @Test
    public void testIn()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), in(Arrays.asList("Hello", "Bye"))).build();
        assertThat(query.getMatching(em).size(), is(2));
    }
    
    @Test
    public void testInNative()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), in(Arrays.asList("Hello", "Bye"))).nativeQuery(true).build();
        assertThat(query.getMatching(em).size(), is(2));
    }

    @Test
    public void testNative()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> query = builder.match(tc.getBar(), like("Hell%").or(like("By%"))).nativeQuery(true)
                .build();
        assertThat(query.getMatching(em).size(), is(2));
    }

    @Test
    public void testCustomSelectUsingJavaBean()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestOther> useObjectWithBarProperty = builder.select(tc.getBar()).match(tc.getBar(), like("Hell%"))
                .build(TestOther.class);
        final TestOther to = useObjectWithBarProperty.getSingleMatching(em);

        assertThat(to.bar, is("Hello"));
        assertThat(to.foo, is(0));
    }

    @Test
    public void testCustomSelectDirectMapping()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<String> stringQuery = builder.select(tc.getBar()).match(tc.getBar(), like("Hell%"))
                .build(String.class);
        assertThat(stringQuery.getSingleMatching(em), is("Hello"));
    }

    @Test
    public void testMax()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<Integer> stringQuery = builder.select(max(tc.getFoo())).build(Integer.class);
        assertThat(stringQuery.getSingleMatching(em), is(6));
    }
    
    @Test
    public void testMin()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<Integer> stringQuery = builder.select(min(tc.getFoo())).build(Integer.class);
        assertThat(stringQuery.getSingleMatching(em), is(3));
    }
    
    @Test
    public void testCountDistinct()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        // FIXME: Conversion operations within the EntityMatcher (this should return an Integer).
        final PreparedQuery<BigInteger> stringQuery = builder.select(count(distinct(tc.getBar()))).nativeQuery(true).build(BigInteger.class);
        assertThat(stringQuery.getSingleMatching(em).intValue(), is(3));
    }
    
    @Test
    public void testCount()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        // FIXME: Conversion operations within the EntityMatcher (this should return an Integer).
        final PreparedQuery<BigInteger> stringQuery = builder.select(count(tc.getBar())).nativeQuery(true).build(BigInteger.class);
        assertThat(stringQuery.getSingleMatching(em).intValue(), is(3));
    }
    
    @Test
    public void testDistinct()
    {
        final TestOther to = EntityMatcher.matcher(TestOther.class);
        final Builder<TestOther> builder = EntityMatcher.builder(to);

        final PreparedQuery<Integer> stringQuery = builder.select(distinct(to.getBar())).build(Integer.class);
        assertThat(stringQuery.getMatching(em).size(), is(2));
    }
    
    @Test
    public void testMaxDistinct()
    {
        final TestOther to = EntityMatcher.matcher(TestOther.class);
        final Builder<TestOther> builder = EntityMatcher.builder(to);

        final PreparedQuery<Integer> stringQuery = builder.select(max(distinct(to.getFoo()))).nativeQuery(true).build(Integer.class);
        assertThat(stringQuery.getSingleMatching(em), is(6));
    }

    @Test
    @Ignore
    public void trySignatures()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final TestJoin tj = EntityMatcher.matcher(TestJoin.class);
        final TestOther to = EntityMatcher.matcher(TestOther.class);

        final EntityMatcher.Builder<TestClass>.StatementComposer composer = EntityMatcher.builder(tc, tj, to)
                .match(tc.getBar(), like("Hello").or(like("Bye"))) //
                .and(tc.getBar(), gt(1)) //
                .and(tc.getBar(), join(tj.getBar())).and(tj.getBar(), join(to.getBar()));

        System.out.println(composer.toString());
    }
    
    @Test
    public void orderBy()
    {
        final TestClass tc = EntityMatcher.matcher(TestClass.class);
        final Builder<TestClass> builder = EntityMatcher.builder(tc);

        final PreparedQuery<TestClass> stringQuery = builder.orderBy(tc.getFoo()).build();
        int next = Integer.MIN_VALUE;
        for (TestClass each : stringQuery.getMatching(em))
        {
            next = Math.max(each.getFoo(), next);
            assertThat(each.getFoo(), is(next));
        }
    }
}
