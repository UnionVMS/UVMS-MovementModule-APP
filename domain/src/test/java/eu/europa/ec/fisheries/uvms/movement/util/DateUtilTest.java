package eu.europa.ec.fisheries.uvms.movement.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;

@RunWith(Arquillian.class)
public class DateUtilTest extends TransactionalTests {

	@Test
	public void testGetDateFromString() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
		Calendar testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 30); 
		testDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		//System.out.println(sdf.format(testDate.getTime()));

		//Formats are in DateFormats.java
		//yyyy-MM-dd HH:mm:ss Z      					  2018-03-09 09:26:30 +0100
		Timestamp timestamp = DateUtil.getDateFromString("2018-03-09 10:26:30 +0100");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());
		
		timestamp = DateUtil.getDateFromString("2018-03-09 04:26:30 -0500");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());

		//EEE MMM dd HH:mm:ss z yyyy			Fri Mar 09 08:26:30 CET 2018
		timestamp = DateUtil.getDateFromString("Fri Mar 09 10:26:30 CET 2018");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());
		
		timestamp = DateUtil.getDateFromString("Fri Mar 09 18:26:30 JST 2018");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());

		//yyyy-MM-dd HH:mm:ss X
		timestamp = DateUtil.getDateFromString("2018-03-09 10:26:30 +01");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());
		
		timestamp = DateUtil.getDateFromString("2018-03-09 12:26:30 +03");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());

		//yyyy-MM-dd HH:mm:ss
		timestamp = DateUtil.getDateFromString("2018-03-09 9:26:30");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());
		
		//fails to come
		try {
			timestamp = DateUtil.getDateFromString("9:26:30");
			fail("Only time should not work");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
		try {
			timestamp = DateUtil.getDateFromString("2018-02-31");
			fail("Only date should not work");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
		try {
			timestamp = DateUtil.getDateFromString("2018-02-31 10:26");
			fail("Missing seconds");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
		try {
			timestamp = DateUtil.getDateFromString(null);
			fail("Null");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testParseToUTCDate() throws ParseException { //To UTC Date is somewhat missleading, the function simply parses a string into a date. 
		//This test is basicly a carbon copy of testGetDateFromString() since the only difference between them is the output. 

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
		Calendar testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 30); 
		testDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		//System.out.println(sdf.format(testDate.getTime()));

		//Formats are in DateFormats.java
		//yyyy-MM-dd HH:mm:ss Z      					  2018-03-09 09:26:30 +0100
		Date timestamp = DateUtil.convertDateTimeInUTC("2018-03-09 10:26:30 +0100");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());
		
		timestamp = DateUtil.convertDateTimeInUTC("2018-03-09 04:26:30 -0500");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());

		//EEE MMM dd HH:mm:ss z yyyy			Fri Mar 09 08:26:30 CET 2018
		timestamp = DateUtil.convertDateTimeInUTC("Fri Mar 09 10:26:30 CET 2018");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());
		
		timestamp = DateUtil.convertDateTimeInUTC("Fri Mar 09 18:26:30 JST 2018");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());

		//yyyy-MM-dd HH:mm:ss X
		timestamp = DateUtil.convertDateTimeInUTC("2018-03-09 10:26:30 +01");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());
		
		timestamp = DateUtil.convertDateTimeInUTC("2018-03-09 12:26:30 +03");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());

		//yyyy-MM-dd HH:mm:ss
		timestamp = DateUtil.convertDateTimeInUTC("2018-03-09 9:26:30");
		assertEquals(testDate.getTimeInMillis(), timestamp.getTime());

		//fails to come
		timestamp = DateUtil.convertDateTimeInUTC("9:26:30");
		assertNull("Only time should not work", timestamp);

		timestamp = DateUtil.convertDateTimeInUTC("2018-02-31");
		assertNull("Only date should not work", timestamp);

		timestamp = DateUtil.convertDateTimeInUTC("2018-02-31 10:26");
		assertNull("Missing seconds", timestamp);
		
		timestamp = DateUtil.convertDateTimeInUTC(null);
		assertNull("Input = null", timestamp);
	}
	
	@Test
	public void testParseUTCDateToString() {
		Calendar testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 30);
		testDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		String formatedDate = DateUtil.parseUTCDateToString(testDate.getTime());
		assertTrue(formatedDate.contentEquals("2018-03-09 09:26:30 +0000"));
		
		testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 3, 26, 30);
		testDate.setTimeZone(TimeZone.getTimeZone("CST")); //UTC -6
		
		formatedDate = DateUtil.parseUTCDateToString(testDate.getTime());
		assertTrue(formatedDate.contentEquals("2018-03-09 09:26:30 +0000"));
		
		formatedDate = DateUtil.parseUTCDateToString(null);
		assertNull(formatedDate);
	}
	
	@Test
	public void testAddSecondsToDateWithXMLGregorianCalendarInput() throws DatatypeConfigurationException {
		GregorianCalendar testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 59);
		XMLGregorianCalendar input = DatatypeFactory.newInstance().newXMLGregorianCalendar(testDate);
		
		//This method will add one second to the calendar no mater what you give as the second input
		XMLGregorianCalendar output = DateUtil.addSecondsToDate(input, 0);
		assertEquals((input.toGregorianCalendar().getTime().getTime() + 1000L), output.toGregorianCalendar().getTime().getTime());
		
		testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 32);
		input = DatatypeFactory.newInstance().newXMLGregorianCalendar(testDate);
		output = DateUtil.addSecondsToDate(input, 52);
		assertEquals((input.toGregorianCalendar().getTime().getTime() + 1000L), output.toGregorianCalendar().getTime().getTime());
		
		XMLGregorianCalendar n = null;
		try {
			output = DateUtil.addSecondsToDate(n, 8);
			fail("Null input");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testAddSecondsToDateWithDateInput() throws DatatypeConfigurationException { //Almost a carbon copy of the one above
		GregorianCalendar testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 59);
		Date input = testDate.getTime();
		//This method will add one second to the calendar no mater what you give as the second input
		Date output = DateUtil.addSecondsToDate(input, 0);
		assertEquals(input.getTime() + 1000L, output.getTime());
		
		testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 32);
		input = testDate.getTime();
		output = DateUtil.addSecondsToDate(input, 52);
		assertEquals(input.getTime() + 1000L, output.getTime());
		
		Date n = null;
		
		try {
			output = DateUtil.addSecondsToDate(n, 8);
			fail();
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testParsePositionTimeWithDateInput() {
		GregorianCalendar testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 59);
		Date input = testDate.getTime();
		
		XMLGregorianCalendar output = DateUtil.parsePositionTime(input);
		
		assertEquals(input.getTime(), output.toGregorianCalendar().getTime().getTime());
		
		input = null;
		output = DateUtil.parsePositionTime(input);
		assertNull(output);
	}
	
	@Test
	public void testNowUTC() {
		Date now = new Date();
		Date output = DateUtil.nowUTC();
		assertEquals(now.getTime(), output.getTime());
	}
	
	@Test
	public void testGetXMLGregorianCalendarInUTC() {
		GregorianCalendar testDate = new GregorianCalendar(2018, Calendar.MARCH, 9, 9, 26, 59);
		Date input = testDate.getTime();
		
		XMLGregorianCalendar output = DateUtil.getXMLGregorianCalendarInUTC(input);
		assertEquals(input.getTime(), output.toGregorianCalendar().getTime().getTime());
		
		input = null;
		output = DateUtil.getXMLGregorianCalendarInUTC(input);
		assertNull(output);
	}
}
