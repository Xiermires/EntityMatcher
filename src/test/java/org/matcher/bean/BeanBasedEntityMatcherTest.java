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
package org.matcher.bean;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.matcher.bean.BeanBasedExpressions.avg;
import static org.matcher.bean.BeanBasedExpressions.between;
import static org.matcher.bean.BeanBasedExpressions.count;
import static org.matcher.bean.BeanBasedExpressions.distinct;
import static org.matcher.bean.BeanBasedExpressions.endsWith;
import static org.matcher.bean.BeanBasedExpressions.eq;
import static org.matcher.bean.BeanBasedExpressions.groupBy;
import static org.matcher.bean.BeanBasedExpressions.gt;
import static org.matcher.bean.BeanBasedExpressions.in;
import static org.matcher.bean.BeanBasedExpressions.like;
import static org.matcher.bean.BeanBasedExpressions.lt;
import static org.matcher.bean.BeanBasedExpressions.matching;
import static org.matcher.bean.BeanBasedExpressions.max;
import static org.matcher.bean.BeanBasedExpressions.min;
import static org.matcher.bean.BeanBasedExpressions.orderBy;
import static org.matcher.bean.BeanBasedExpressions.selection;
import static org.matcher.bean.BeanBasedExpressions.startsWith;
import static org.matcher.bean.BeanBasedExpressions.sum;
import static org.matcher.expression.Expressions.closure;
import static org.matcher.expression.Expressions.count;
import static org.matcher.expression.Expressions.not;

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

public class BeanBasedEntityMatcherTest {

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
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final TestClass testee = matcher.findUnique(TestClass.class, matching(tc.getBar(), eq("Hello")));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), is("Hello"));
    }

    @Test
    public void testEqNull() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final TestClass testee = matcher.findUnique(TestClass.class, matching(tc.getBar(), eq(null)));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), is(nullValue()));
    }

    @Test
    public void testEqNotNull() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> testee = matcher.findAny(TestClass.class, matching(tc.getBar(), not(eq(null))));
	assertThat(testee.size(), is(3));
	for (TestClass t : testee) {
	    assertThat(t.getBar(), is(Matchers.not(nullValue())));
	}
    }

    @Test
    public void testLike() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final TestClass testee = matcher.findUnique(TestClass.class, matching(tc.getBar(), like("Hell%")));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), Matchers.startsWith("Hell"));
    }
    
    @Test
    public void testStartsWith() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final TestClass testee = matcher.findUnique(TestClass.class, matching(tc.getBar(), startsWith("Hell")));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), Matchers.startsWith("Hell"));
    }

    @Test
    public void testEndsWith() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final TestClass testee = matcher.findUnique(TestClass.class, matching(tc.getBar(), endsWith("ello")));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), Matchers.endsWith("ello"));
    }

    @Test
    public void testNotLike() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> testee = matcher.findAny(TestClass.class, matching(tc.getBar(), not(like("Hell%"))));
	assertThat(testee.size(), is(2));
	for (TestClass t : testee)
	    assertThat(t.getBar(), Matchers.not(Matchers.startsWith("Hell")));
    }

    @Test
    public void testGreaterThan() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> testee = matcher.findAny(TestClass.class, matching(tc.getFoo(), gt(4)));

	assertThat(testee.size(), is(2));
	for (TestClass t : testee)
	    assertThat(t.getFoo(), is(greaterThan(4)));
    }

    @Test
    public void testNotGreaterThan() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> testee = matcher.findAny(TestClass.class, matching(tc.getFoo(), not(gt(4))));

	assertThat(testee.size(), is(2));
	for (TestClass t : testee)
	    assertThat(t.getFoo(), is(Matchers.not(greaterThan(4))));
    }

    @Test
    public void testAndSameTableDifferentProperties() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final TestClass testee = matcher.findUnique(TestClass.class,
		matching(tc.getFoo(), lt(4)).and(tc.getBar(), eq("Bye")));
	assertThat(testee.getFoo(), is(lessThan(4)));
	assertThat(testee.getBar(), is("Bye"));
    }

    @Test
    public void testOrSameTableDifferentProperties() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> tcs = matcher.findAny(TestClass.class,
		matching(tc.getFoo(), lt(4)).or(tc.getBar(), eq("Bye")));
	for (TestClass t : tcs) {
	    final boolean lt4 = tc.getFoo() < 4;
	    final boolean isBye = "Bye".equals(t.getBar());
	    assertThat(lt4 || isBye, is(true));
	}
    }

    @Test
    public void testOrSameTableSameProperty() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> tcs = matcher.findAny(TestClass.class, matching(tc.getBar(), eq("Hello").or(eq("Bye"))));
	final List<String> bars = Arrays.asList("Hello", "Bye");
	for (TestClass t : tcs)
	    assertThat(bars.contains(t.getBar()), is(true));
    }

    @Test
    public void testSingleJoin() {
	final TestJoin tj = BeanBasedMatcher.matcher(TestJoin.class);
	final List<TestClass> joins = matcher.findAny(TestClass.class, matching(tj.getBar()));
	assertThat(joins.size(), is(2));
    }

    @Test
    public void testMultipleJoin() {
	final TestJoin tj = BeanBasedMatcher.matcher(TestJoin.class);
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final TestClass join = matcher.findUnique(TestClass.class, matching(tj.getBar()).and(matching(to.getBar())));
	assertThat(join, is(Matchers.not(nullValue())));
    }

    @Test
    public void testIn() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<String> bars = Arrays.asList("Hello", "Bye");
	final List<TestClass> tcs = matcher.findAny(TestClass.class, matching(tc.getBar(), in(bars)));
	for (TestClass t : tcs)
	    assertThat(bars.contains(t.getBar()), is(true));
    }

    @Test
    public void testNotNot() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> tcs = matcher.findAny(TestClass.class,
		matching(tc.getBar(), not(not(eq("Hello").or(eq("Bye"))))));
	final List<String> bars = Arrays.asList("Hello", "Bye");
	for (TestClass t : tcs)
	    assertThat(bars.contains(t.getBar()), is(true));
    }

    @Test
    public void testClosure() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final List<TestOther> tos = matcher.findAny(TestOther.class,
		matching(to.getBar(), eq("Snake")).and(to.getFoo(), closure(eq(5).or(eq(3)))));
	for (TestOther t : tos) {
	    assertThat(t.getBar(), is("Snake"));
	    assertThat(t.getFoo(), either(is(5)).or(is(3)));
	}
    }

    @Test
    public void testBetween() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> tcs = matcher.findAny(TestClass.class, matching(tc.getFoo(), between(3, 5)));
	for (TestClass t : tcs) {
	    assertThat(t.getFoo(), is(greaterThanOrEqualTo(3)));
	    assertThat(t.getFoo(), is(lessThanOrEqualTo(5)));
	}
    }

    @Test
    public void testSelectSingleProperty() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<Integer> foos = matcher.findAny(Integer.class, selection(tc.getFoo()),
		matching(tc.getFoo(), between(3, 5)));
	assertThat(foos, is(Matchers.not(nullValue())));
	for (Integer foo : foos) {
	    assertThat(foo, is(greaterThanOrEqualTo(3)));
	    assertThat(foo, is(lessThanOrEqualTo(5)));
	}
    }

    @Test
    public void testSelectMultipleProperty() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final Object[] fooAndBar = matcher
		.findUnique(
			Object[].class, //
			selection(tc.getFoo(), tc.getBar()),
			matching(tc.getFoo(), between(3, 5)).and(tc.getBar(), eq("Hello")));

	assertThat(fooAndBar, is(Matchers.not(nullValue())));
	assertThat((Integer) fooAndBar[0], is(5));
	assertThat((String) fooAndBar[1], is("Hello"));
    }

    @Test
    public void testMin() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final Integer min = matcher.findUnique(Integer.class, min(tc.getFoo()), matching(tc.getFoo(), between(3, 5)));
	assertThat(min, is(Matchers.not(nullValue())));
	assertThat(min, is(3));
    }

    @Test
    public void testMax() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final Integer max = matcher.findUnique(Integer.class, max(tc.getFoo()), matching(tc.getFoo(), between(3, 5)));
	assertThat(max, is(Matchers.not(nullValue())));
	assertThat(max, is(5));
    }
    
    @Test
    public void testAvg() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final Double avg = matcher.findUnique(Double.class, avg(tc.getFoo()));
	assertThat(avg, is(Matchers.not(nullValue())));
	assertThat(avg, is(4d));
    }

    @Test
    public void testSum() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final Long sum = matcher.findUnique(Long.class, sum(tc.getFoo()));
	assertThat(sum, is(Matchers.not(nullValue())));
	assertThat(sum, is(16l));
    }

    @Test
    public void testCount() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final Long count = matcher.findUnique(Long.class, count(tc.getFoo()), matching(tc.getFoo(), between(3, 5)));
	assertThat(count, is(Matchers.not(nullValue())));
	assertThat(count, is(2l));
    }

    @Test
    public void testDistinct() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final String snake = matcher.findUnique(String.class, //
		distinct(to.getBar()), matching(to.getBar(), eq("Snake")));
	assertThat(snake, is(Matchers.not(nullValue())));
	assertThat(snake, is("Snake"));
    }

    @Test
    public void testDistinctList() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final List<String> bars = matcher.findAny(String.class, distinct(to.getBar()));
	assertThat(bars.size(), is(2));
	assertThat(bars, containsInAnyOrder("Snake", "Hello"));
    }
    
    @Test
    public void testCountDistinct() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final Long count = matcher.findUnique(Long.class, //
		count(distinct(to.getBar())), matching(to.getBar(), eq("Snake")));
	assertThat(count, is(1l));
    }
    
    @Test
    public void testGroupBy() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final List<Object[]> tos = matcher.findAny(Object[].class, selection(to.getBar()).and(count(to.getBar())),
		groupBy(to.getBar()));
	assertThat(tos.size(), is(2));
    }

    @Test
    public void testOrderBy() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> tcs = matcher.findAny(TestClass.class, orderBy(tc.getFoo()));
	assertThat(tcs.size(), is(4));
	int min = Integer.MIN_VALUE;
	for (TestClass t : tcs) {
	    min = Math.min(t.getFoo(), min);
	    assertThat(min, is(lessThanOrEqualTo(t.getFoo())));
	}
    }

    @Test
    public void testOrderByFunction() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final List<String> tos = matcher.findAny(String.class, selection(to.getBar()),
		groupBy(to.getBar()).orderBy(count(to.getBar())));
	assertThat(tos.size(), is(2));
	assertThat(tos, contains("Hello", "Snake"));
    }

    @Test
    public void testHavingCount() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final String testee = matcher.findUnique(String.class, //
		selection(to.getBar()), //
		groupBy(to.getBar()). //
			having(count(to.getBar()), gt(1L)));
	assertThat(testee, is("Snake"));
    }

    @Test
    // FIXME
    public void testMultipleHaving() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final List<Object[]> testee = matcher.findAny(Object[].class, //
		selection(to.getBar()).and(sum(to.getFoo())), //
		groupBy(to.getBar(), to.getFoo()).//
			having(count(to.getBar()), gt(0L)).//
			and(sum(to.getFoo()), lt(20L)));

	assertThat(testee.size(), is(4));
    }
}
