package eu.europa.ec.fisheries.uvms.movement.entity;

import com.tocea.easycoverage.framework.api.IInstanceProvider;
import com.tocea.easycoverage.framework.checkers.*;
import com.tocea.easycoverage.framework.junit.JUnitTestSuiteProvider;
import com.tocea.easycoverage.framework.providers.DefaultInstanceProvider;
import com.tocea.easycoverage.framework.providers.MultipleInstanceProvider;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EasyCoverageTest extends Assert {

	private static final String EXPECT_CLASSES_IN_PACKAGE = "Expect classes in package";
	private static final char PACKAGE_SEPARATOR = '.';
	private static final String CLASS_SUFFIX = ".class";

	@Test
	public static TestSuite suite() {

		JUnitTestSuiteProvider testSuiteProvider = new JUnitTestSuiteProvider();
		testSuiteProvider.setProvider(createInstanceProvider());

		assertTrue(EXPECT_CLASSES_IN_PACKAGE,
				checkAllClassesInPackage(testSuiteProvider, "eu.europa.ec.fisheries.uvms.movement.entity"));
		assertTrue(EXPECT_CLASSES_IN_PACKAGE,
				checkAllClassesInPackage(testSuiteProvider, "eu.europa.ec.fisheries.uvms.movement.entity.area"));
		assertTrue(EXPECT_CLASSES_IN_PACKAGE,
				checkAllClassesInPackage(testSuiteProvider, "eu.europa.ec.fisheries.uvms.movement.entity.group"));
		assertTrue(EXPECT_CLASSES_IN_PACKAGE,
				checkAllClassesInPackage(testSuiteProvider, "eu.europa.ec.fisheries.uvms.movement.entity.temp"));
		assertTrue(EXPECT_CLASSES_IN_PACKAGE,
				checkAllClassesInPackage(testSuiteProvider, "eu.europa.ec.fisheries.uvms.movement.dto"));

		testSuiteProvider.addClassChecker(BijectiveCompareToChecker.class);
		testSuiteProvider.addClassChecker(ToStringNotNullChecker.class);
		testSuiteProvider.addClassChecker(BijectiveEqualsChecker.class);
		testSuiteProvider.addClassChecker(CloneChecker.class);
		testSuiteProvider.addClassChecker(NPEConstructorChecker.class);
		testSuiteProvider.addClassChecker(NullValueEqualsChecker.class);
		testSuiteProvider.addMethodChecker(SetterChecker.class);
		testSuiteProvider.addMethodChecker(ArrayIndexOutOfBoundExceptionChecker.class);

		return testSuiteProvider.getTestSuite();
	}

	private static MultipleInstanceProvider createInstanceProvider() {
		MultipleInstanceProvider multipleInstanceProvider = new MultipleInstanceProvider();
		multipleInstanceProvider.addProvider(new SimpleInstanceProvider(
				new LineString(new CoordinateArraySequence(new Coordinate[0]), new GeometryFactory())));
		multipleInstanceProvider.addProvider(new SimpleInstanceProvider(
				new Point(new CoordinateArraySequence(new Coordinate[0]), new GeometryFactory())));
		multipleInstanceProvider
				.addProvider(new SimpleInstanceProvider(new CoordinateArraySequence(new Coordinate[0])));
		multipleInstanceProvider.addProvider(new SimpleInstanceProvider(new GeometryFactory()));
		multipleInstanceProvider.addProvider(new DefaultInstanceProvider());
		return multipleInstanceProvider;
	}

	private static class SimpleInstanceProvider implements IInstanceProvider {
		private final Object t;

		SimpleInstanceProvider(Object o) {
			super();
			this.t = o;
		}

		@Override
		public <T> T getInstance(Class<T> paramClass) {
			return (T) t;
		}

		@Override
		public <T> boolean canProvide(Class<T> paramClass) {
			return t.getClass().equals(paramClass);
		}
	}

	/**
	 * Check all classes in package.
	 *
	 * @param testSuiteProvider
	 *            the test suite provider
	 * @param packageName
	 *            the string
	 */
	private static boolean checkAllClassesInPackage(JUnitTestSuiteProvider testSuiteProvider, String packageName) {
		List<Class<?>> allClasses = getAllClasses(packageName);
		for (Class<?> class1 : allClasses) {
			testSuiteProvider.addClass(class1);
		}
		return !allClasses.isEmpty();
	}

	/**
	 * Gets the all classes.
	 *
	 * @param packageName
	 *            the pckgname
	 * @return the all classes
	 */
	private static List<Class<?>> getAllClasses(String packageName) {
		final List<Class<?>> classes = new ArrayList<>();
		File directory = new File(
				"target" + File.separatorChar + "classes" + File.separatorChar + packageName.replace(PACKAGE_SEPARATOR, File.separatorChar));
		if (directory.exists()) {
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				if (files[i].endsWith(CLASS_SUFFIX)) {
					try {
						String clazz = packageName + PACKAGE_SEPARATOR + files[i].substring(0, files[i].length() - 6);
						classes.add(Class.forName(clazz));
					} catch (ClassNotFoundException e) {
						fail("Error occurred while reading class: " + e.getMessage());
					}
				}
			}
		}
		return classes;
	}
}
