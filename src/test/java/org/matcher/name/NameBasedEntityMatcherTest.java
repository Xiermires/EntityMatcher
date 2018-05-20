/*******************************************************************************
 * Copyright (c) 2018, Xavier Miret Andres <xavier.mires@gmail.com>
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
package org.matcher.name;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.matcher.expression.Expressions.closure;
import static org.matcher.expression.Expressions.not;
import static org.matcher.name.NameBasedExpressions.avg;
import static org.matcher.name.NameBasedExpressions.between;
import static org.matcher.name.NameBasedExpressions.count;
import static org.matcher.name.NameBasedExpressions.distinct;
import static org.matcher.name.NameBasedExpressions.eq;
import static org.matcher.name.NameBasedExpressions.groupBy;
import static org.matcher.name.NameBasedExpressions.gt;
import static org.matcher.name.NameBasedExpressions.in;
import static org.matcher.name.NameBasedExpressions.like;
import static org.matcher.name.NameBasedExpressions.lt;
import static org.matcher.name.NameBasedExpressions.matching;
import static org.matcher.name.NameBasedExpressions.max;
import static org.matcher.name.NameBasedExpressions.min;
import static org.matcher.name.NameBasedExpressions.orderBy;
import static org.matcher.name.NameBasedExpressions.selection;
import static org.matcher.name.NameBasedExpressions.sum;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.h2.jdbcx.JdbcDataSource;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matcher.EntityMatcher;
import org.matcher.TestClass;
import org.matcher.TestJoin;
import org.matcher.TestOther;
import org.matcher.parameter.ParameterBindingImpl;

public class NameBasedEntityMatcherTest {
    static EntityMatcher matcher;

    @BeforeClass
    public static void pre() {
	final JdbcDataSource ds = new JdbcDataSource();
	ds.setURL("jdbc:h2:~/testDB");

	final EntityManager em = Persistence.createEntityManagerFactory("test").createEntityManager();

	try {
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
	} catch (Exception e) {
	    if (em != null) {
		em.getTransaction().rollback();
	    }
	}

	matcher = new EntityMatcher(em);
    }

    @Test
    public void testEq() {
	final TestClass testee = matcher.findUnique(TestClass.class, matching("bar", eq("Hello")));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), is("Hello"));
    }

    @Test
    public void testEqNull() {
	final TestClass testee = matcher.findUnique(TestClass.class, matching("bar", eq(null)));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), is(nullValue()));
    }

    @Test
    public void testEqNotNull() {
	final List<TestClass> testee = matcher.find(TestClass.class, matching("bar", not(eq(null))));
	assertThat(testee.size(), is(3));
	for (TestClass t : testee) {
	    assertThat(t.getBar(), is(Matchers.not(nullValue())));
	}
    }

    @Test
    public void testLike() {
	final TestClass testee = matcher.findUnique(TestClass.class, matching("bar", like("Hell%")));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), startsWith("Hell"));
    }

    @Test
    public void testNotLike() {
	final List<TestClass> testee = matcher.find(TestClass.class, matching("bar", not(like("Hell%"))));
	assertThat(testee.size(), is(2));
	for (TestClass tc : testee)
	    assertThat(tc.getBar(), Matchers.not(startsWith("Hell")));
    }

    @Test
    public void testGreaterThan() {
	final List<TestClass> testee = matcher.find(TestClass.class, matching("foo", gt(4)));

	assertThat(testee.size(), is(2));
	for (TestClass tc : testee)
	    assertThat(tc.getFoo(), is(greaterThan(4)));
    }

    @Test
    public void testNotGreaterThan() {
	final List<TestClass> testee = matcher.find(TestClass.class, matching("foo", not(gt(4))));

	assertThat(testee.size(), is(2));
	for (TestClass tc : testee)
	    assertThat(tc.getFoo(), is(Matchers.not(greaterThan(4))));
    }

    @Test
    public void testAndSameTableDifferentProperties() {
	final TestClass tc = matcher.findUnique(TestClass.class, matching("foo", lt(4)).and("bar", eq("Bye")));
	assertThat(tc.getFoo(), is(lessThan(4)));
	assertThat(tc.getBar(), is("Bye"));
    }

    @Test
    public void testOrSameTableDifferentProperties() {
	final List<TestClass> tcs = matcher.find(TestClass.class, matching("foo", lt(4)).or("bar", eq("Bye")));
	for (TestClass tc : tcs) {
	    final boolean lt4 = tc.getFoo() < 4;
	    final boolean isBye = "Bye".equals(tc.getBar());
	    assertThat(lt4 || isBye, is(true));
	}
    }

    @Test
    public void testOrSameTableSameProperty() {
	final List<TestClass> tcs = matcher.find(TestClass.class, matching("bar", eq("Hello").or(eq("Bye"))));
	final List<String> bars = Arrays.asList("Hello", "Bye");
	for (TestClass tc : tcs)
	    assertThat(bars.contains(tc.getBar()), is(true));
    }

    @Test
    public void testSingleJoin() {
	final List<TestClass> joins = matcher.find(TestClass.class, matching(TestJoin.class, "bar"));
	assertThat(joins.size(), is(2));
    }

    @Test
    public void testMultipleJoin() {
	final TestClass join = matcher.findUnique(TestClass.class, matching(TestJoin.class, "bar").//
		and(matching(TestOther.class, "bar")));
	assertThat(join, is(Matchers.not(nullValue())));
    }

    @Test
    public void testIn() {
	final List<String> bars = Arrays.asList("Hello", "Bye");
	final List<TestClass> tcs = matcher.find(TestClass.class, matching("bar", in(bars)));
	for (TestClass tc : tcs)
	    assertThat(bars.contains(tc.getBar()), is(true));
    }

    @Test
    public void testNotNot() {
	final List<TestClass> tcs = matcher.find(TestClass.class, matching("bar", not(not(eq("Hello").or(eq("Bye"))))));
	final List<String> bars = Arrays.asList("Hello", "Bye");
	for (TestClass tc : tcs)
	    assertThat(bars.contains(tc.getBar()), is(true));
    }

    @Test
    public void testClosure() {
	final List<TestOther> tos = matcher.find(TestOther.class,
		matching("bar", eq("Snake")).and("foo", closure(eq(5).or(eq(3)))));
	for (TestOther to : tos) {
	    assertThat(to.getBar(), is("Snake"));
	    assertThat(to.getFoo(), either(is(5)).or(is(3)));
	}
    }

    @Test
    public void testBetween() {
	final List<TestClass> tcs = matcher.find(TestClass.class, matching("foo", between(3, 5)));
	for (TestClass tc : tcs) {
	    assertThat(tc.getFoo(), is(greaterThanOrEqualTo(3)));
	    assertThat(tc.getFoo(), is(lessThanOrEqualTo(5)));
	}
    }

    @Test
    public void testSelectSingleProperty() {
	final List<Integer> foos = matcher.find(Integer.class, selection(TestClass.class, "foo"),
		matching("foo", between(3, 5)));
	assertThat(foos, is(Matchers.not(nullValue())));
	for (Integer foo : foos) {
	    assertThat(foo, is(greaterThanOrEqualTo(3)));
	    assertThat(foo, is(lessThanOrEqualTo(5)));
	}
    }

    @Test
    public void testSelectMultipleProperty() {
	final Object[] fooAndBar = matcher.findUnique(Object[].class, //
		selection(TestClass.class, "foo", "bar"), matching("foo", between(3, 5)).and("bar", eq("Hello")));

	assertThat(fooAndBar, is(Matchers.not(nullValue())));
	assertThat((Integer) fooAndBar[0], is(5));
	assertThat((String) fooAndBar[1], is("Hello"));
    }

    @Test
    public void testMin() {
	final Integer min = matcher.findUnique(Integer.class, //
		min(TestClass.class, "foo"), matching("foo", between(3, 5)));
	assertThat(min, is(Matchers.not(nullValue())));
	assertThat(min, is(3));
    }

    @Test
    public void testMax() {
	final Integer max = matcher.findUnique(Integer.class, //
		max(TestClass.class, "foo"), matching("foo", between(3, 5)));
	assertThat(max, is(Matchers.not(nullValue())));
	assertThat(max, is(5));
    }

    @Test
    public void testAvg() {
	final Double avg = matcher.findUnique(Double.class, avg(TestClass.class, "foo"));
	assertThat(avg, is(Matchers.not(nullValue())));
	assertThat(avg, is(4d));
    }

    @Test
    public void testSum() {
	final Long sum = matcher.findUnique(Long.class, sum(TestClass.class, "foo"));
	assertThat(sum, is(Matchers.not(nullValue())));
	assertThat(sum, is(16l));
    }

    @Test
    public void testCount() {
	final Long count = matcher
		.findUnique(Long.class, count(TestClass.class, "foo"), matching("foo", between(3, 5)));
	assertThat(count, is(Matchers.not(nullValue())));
	assertThat(count, is(2l));
    }

    @Test
    public void testDistinct() {
	final String snake = matcher.findUnique(String.class, distinct(TestOther.class, "bar"),
		matching("bar", eq("Snake")));
	assertThat(snake, is(Matchers.not(nullValue())));
	assertThat(snake, is("Snake"));
    }

    @Test
    public void testDistinctList() {
	final List<String> bars = matcher.find(String.class, distinct(TestOther.class, "bar"));
	assertThat(bars.size(), is(2));
	assertThat(bars, containsInAnyOrder("Snake", "Hello"));
    }

    @Test
    public void testCountDistinct() {
	final Long count = matcher.findUnique(Long.class, //
		count(distinct(TestOther.class, "bar")), matching("bar", eq("Snake")));
	assertThat(count, is(1l));
    }

    @Test
    public void testGroupBy() {
	final List<Object[]> tos = matcher.find(Object[].class, selection(TestOther.class, "bar").and(count("bar")),
		groupBy("bar"));
	assertThat(tos.size(), is(2));
    }

    @Test
    public void testOrderBy() {
	final List<TestClass> tos = matcher.find(TestClass.class, orderBy("foo"));
	assertThat(tos.size(), is(4));
	int min = Integer.MIN_VALUE;
	for (TestClass t : tos) {
	    assertThat(t.getFoo(), is(greaterThanOrEqualTo(min)));
	    min = Math.min(t.getFoo(), min);
	}
    }

    @Test
    public void testOrderByFunction() {
	final List<String> tos = matcher.find(String.class, selection(TestOther.class, "bar"),
		groupBy("bar").orderBy(count("bar")));
	assertThat(tos.size(), is(2));
	assertThat(tos, contains("Hello", "Snake"));
    }

    @Test
    public void testHavingCount() {
	final String testee = matcher.findUnique(String.class, //
		selection(TestOther.class, "bar"), //
		groupBy("bar").//
			having(count("bar"), gt(1L)));
	assertThat(testee, is("Snake"));
    }

    @Test
    // FIXME
    public void testMultipleHaving() {
	final List<Object[]> testee = matcher.find(Object[].class, //
		selection(TestOther.class, "bar").and(sum("foo")), //
		groupBy("bar", "foo").//
			having(count("bar"), gt(0L)).//
			and(sum("foo"), lt(20L)));

	assertThat(testee.size(), is(4));
    }

    @Test
    public void tryMatchingSignatures() {
	NameBasedWhereBuilder builder = matching("bar", like("Hell%")).//
		and(TestOther.class, "bar", like("Hell%").or(like("Bye"))).//
		or("foo", gt(5).//
			and(matching(TestOther.class, "bar")));

	builder.overwriteNullReferenceAndProperties(TestClass.class, null);
	final String queryTxt = builder.build(new ParameterBindingImpl());

	System.out.println(queryTxt);

    }
}
