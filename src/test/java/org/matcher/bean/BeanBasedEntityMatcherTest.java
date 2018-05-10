package org.matcher.bean;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.matcher.Expressions.closure;
import static org.matcher.Expressions.not;
import static org.matcher.bean.BeanBasedExpressions.between;
import static org.matcher.bean.BeanBasedExpressions.count;
import static org.matcher.bean.BeanBasedExpressions.distinct;
import static org.matcher.bean.BeanBasedExpressions.eq;
import static org.matcher.bean.BeanBasedExpressions.gt;
import static org.matcher.bean.BeanBasedExpressions.in;
import static org.matcher.bean.BeanBasedExpressions.like;
import static org.matcher.bean.BeanBasedExpressions.lt;
import static org.matcher.bean.BeanBasedExpressions.matching;
import static org.matcher.bean.BeanBasedExpressions.max;
import static org.matcher.bean.BeanBasedExpressions.min;
import static org.matcher.bean.BeanBasedExpressions.selection;

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
	final List<TestClass> testee = matcher.find(TestClass.class,
		matching(tc.getBar(), not(eq(null)).and(like("%e%"))));
	assertThat(testee.size(), is(2));
    }

    @Test
    public void testLike() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final TestClass testee = matcher.findUnique(TestClass.class, matching(tc.getBar(), like("Hell%")));
	assertThat(testee, is(Matchers.not(nullValue())));
	assertThat(testee.getBar(), startsWith("Hell"));
    }

    @Test
    public void testNotLike() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> testee = matcher.find(TestClass.class, matching(tc.getBar(), not(like("Hell%"))));
	assertThat(testee.size(), is(2));
	for (TestClass t : testee)
	    assertThat(t.getBar(), Matchers.not(startsWith("Hell")));
    }

    @Test
    public void testGreaterThan() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> testee = matcher.find(TestClass.class, matching(tc.getFoo(), gt(4)));

	assertThat(testee.size(), is(2));
	for (TestClass t : testee)
	    assertThat(t.getFoo(), is(greaterThan(4)));
    }

    @Test
    public void testNotGreaterThan() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> testee = matcher.find(TestClass.class, matching(tc.getFoo(), not(gt(4))));

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
	final List<TestClass> tcs = matcher.find(TestClass.class,
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
	final List<TestClass> tcs = matcher.find(TestClass.class, matching(tc.getBar(), eq("Hello").or(eq("Bye"))));
	final List<String> bars = Arrays.asList("Hello", "Bye");
	for (TestClass t : tcs)
	    assertThat(bars.contains(t.getBar()), is(true));
    }

    @Test
    public void testSingleJoin() {
	final TestJoin tj = BeanBasedMatcher.matcher(TestJoin.class);
	final List<TestClass> joins = matcher.find(TestClass.class, matching(tj.getBar()));
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
	final List<TestClass> tcs = matcher.find(TestClass.class, matching(tc.getBar(), in(bars)));
	for (TestClass t : tcs)
	    assertThat(bars.contains(t.getBar()), is(true));
    }

    @Test
    public void testNotNot() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> tcs = matcher.find(TestClass.class,
		matching(tc.getBar(), not(not(eq("Hello").or(eq("Bye"))))));
	final List<String> bars = Arrays.asList("Hello", "Bye");
	for (TestClass t : tcs)
	    assertThat(bars.contains(t.getBar()), is(true));
    }

    @Test
    public void testClosure() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final List<TestOther> tos = matcher.find(TestOther.class, matching(to.getBar(), eq("Snake")).and(to.getFoo(), closure(eq(5).or(eq(3)))));
	for (TestOther t : tos) {
	    assertThat(t.getBar(), is("Snake"));
	    assertThat(t.getFoo(), either(is(5)).or(is(3)));
	}
    }
    
    @Test
    public void testBetween() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<TestClass> tcs = matcher.find(TestClass.class, matching(tc.getFoo(), between(3, 5)));
	for (TestClass t : tcs) {
	    assertThat(t.getFoo(), is(greaterThanOrEqualTo(3)));
	    assertThat(t.getFoo(), is(lessThanOrEqualTo(5)));
	}
    }

    @Test
    public void testSelectSingleProperty() {
	final TestClass tc = BeanBasedMatcher.matcher(TestClass.class);
	final List<Integer> foos = matcher.find(Integer.class, selection(tc.getFoo()),
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
	final Object[] fooAndBar = matcher.findUnique(Object[].class, //
		selection(tc.getFoo(), tc.getBar()), matching(tc.getFoo(), between(3, 5)).and(tc.getBar(), eq("Hello")));

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
    public void testCountDistinct() {
	final TestOther to = BeanBasedMatcher.matcher(TestOther.class);
	final Long count = matcher.findUnique(Long.class, //
		count(distinct(to.getBar())), matching(to.getBar(), eq("Snake")));
	assertThat(count, is(1l));
    }
}
