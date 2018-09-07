package eu.europa.ec.fisheries.uvms.movement.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;

@RunWith(Arquillian.class)
public class DateUtilTest extends TransactionalTests {

	
	@Test
	public void testGetDateFromString() throws ParseException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X");


		Instant testDate = OffsetDateTime.of(2018, 3, 9, 11, 26, 30, 0, ZoneOffset.ofHours(2)).toInstant();
		//System.out.println(sdf.format(testDate.getTime()));
		
		
		//Formats are in DateFormats.java
		//yyyy-MM-dd HH:mm:ss Z      					  2018-03-09 09:26:30 +0100
		Instant timestamp = DateUtil.getDateFromString("2018-03-09 10:26:30 +0100");
		assertTrue(testDate.equals(timestamp));
		
		timestamp = DateUtil.getDateFromString("2018-03-09 04:26:30 -0500");
		assertTrue(testDate.equals(timestamp));
		
		
		//EEE MMM dd HH:mm:ss z yyyy			Fri Mar 09 08:26:30 CET 2018
		timestamp = DateUtil.getDateFromString("Fri Mar 09 10:26:30 CET 2018");
		assertTrue(testDate.equals(timestamp));
		
		timestamp = DateUtil.getDateFromString("Fri Mar 09 18:26:30 JST 2018");
		assertTrue(testDate.equals(timestamp));
		
		
		//yyyy-MM-dd HH:mm:ss X
		timestamp = DateUtil.getDateFromString("2018-03-09 10:26:30 +01");
		assertTrue(testDate.equals(timestamp));
		
		timestamp = DateUtil.getDateFromString("2018-03-09 12:26:30 +03");
		assertTrue(testDate.equals(timestamp));

		
		//fails to come

		timestamp = DateUtil.getDateFromString("9:26:30");
		assertNull("Only time should not work", timestamp);



		timestamp = DateUtil.getDateFromString("2018-02-31");
		assertNull("Only date should not work", timestamp);

		timestamp = DateUtil.getDateFromString("2018-02-31 10:26");
		assertNull("Missing seconds", timestamp);

		timestamp = DateUtil.getDateFromString(null);
		assertNull("Null", timestamp);

		
	}
	
	
	@Test
	public void testParseToUTCDate() throws ParseException { //To UTC Date is somewhat missleading, the function simply parses a string into a date. 
		//This test is basicly a carbon copy of testGetDateFromString() since the only difference between them is the output. 

		Instant testDate = OffsetDateTime.of(2018, 3, 9, 11, 26, 30, 0, ZoneOffset.ofHours(2)).toInstant();
		//System.out.println(sdf.format(testDate.getTime()));
		
		
		//Formats are in DateFormats.java
		//yyyy-MM-dd HH:mm:ss Z      					  2018-03-09 09:26:30 +0100
		Instant timestamp = DateUtil.parseToUTCDate("2018-03-09 10:26:30 +0100");
		assertTrue(testDate.equals( timestamp));
		
		timestamp = DateUtil.parseToUTCDate("2018-03-09 04:26:30 -0500");
		assertTrue(testDate.equals( timestamp));
		
		
		//EEE MMM dd HH:mm:ss z yyyy			Fri Mar 09 08:26:30 CET 2018
		timestamp = DateUtil.parseToUTCDate("Fri Mar 09 10:26:30 CET 2018");
		assertTrue(testDate.equals( timestamp));
		
		timestamp = DateUtil.parseToUTCDate("Fri Mar 09 18:26:30 JST 2018");
		assertTrue(testDate.equals( timestamp));
		
		
		//yyyy-MM-dd HH:mm:ss X
		timestamp = DateUtil.parseToUTCDate("2018-03-09 10:26:30 +01");
		assertTrue(testDate.equals( timestamp));
		
		timestamp = DateUtil.parseToUTCDate("2018-03-09 12:26:30 +03");
		assertTrue(testDate.equals( timestamp));

		
		//fails to come
		timestamp = DateUtil.parseToUTCDate("9:26:30");
		assertNull("Only time should not work", timestamp);
		
		
		timestamp = DateUtil.parseToUTCDate("2018-02-31");
		assertNull("Only date should not work", timestamp);

		
		
		timestamp = DateUtil.parseToUTCDate("2018-02-31 10:26");
		assertNull("Missing seconds", timestamp);
		
		timestamp = DateUtil.parseToUTCDate(null);
		assertNull("Input = null", timestamp);
	}
	
	@Test
	public void testParseUTCDateToString() {
		Instant testDate = OffsetDateTime.of(2018, 3, 9, 11, 26, 30, 0, ZoneOffset.ofHours(2)).toInstant();

		String formatedDate = DateUtil.parseUTCDateToString(testDate);
		assertTrue(formatedDate.contentEquals("2018-03-09 09:26:30 +0000"));

		testDate = OffsetDateTime.of(2018,3, 9, 3, 26, 30, 00, ZoneOffset.ofHours(2)).toInstant();
		ZonedDateTime zonedTestDate = ZonedDateTime.of(2018,3, 9, 10, 26, 30, 00, ZoneId.of("CET"));			//Lets hop that it understand that this is supposed to be summer time internally
		zonedTestDate = zonedTestDate.withZoneSameInstant(ZoneId.of("CST", ZoneId.SHORT_IDS));

		formatedDate = DateUtil.parseUTCDateToString(zonedTestDate.toInstant());
		assertTrue(formatedDate, formatedDate.contentEquals("2018-03-09 09:26:30 +0000"));
		
		formatedDate = DateUtil.parseUTCDateToString(null);
		assertNull(formatedDate);
	}
	
	@Test
	public void testAddSecondsToDateWithXMLGregorianCalendarInput() throws DatatypeConfigurationException {
		Instant testDate = OffsetDateTime.of(2018, 3, 9, 9, 26, 59, 0, ZoneOffset.ofHours(2)).toInstant();

		Instant output = DateUtil.addSecondsToDate(testDate, 1);
		assertEquals(testDate.plusSeconds(1), output);
		
		testDate = testDate = OffsetDateTime.of(2018, 3, 9, 9, 26, 32, 0, ZoneOffset.ofHours(2)).toInstant();
		output = DateUtil.addSecondsToDate(testDate, 52);
		assertEquals(testDate.plusSeconds(52), output);
		
		Instant n = null;
		try {
			output = DateUtil.addSecondsToDate(n, 8);
			fail("Null input");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
		
	}
	
	@Test
	public void testAddSecondsToDateWithDateInput() throws DatatypeConfigurationException { //Almost a carbon copy of the one above
		Instant testDate = OffsetDateTime.of(2018, 3, 9, 9, 26, 59, 0, ZoneOffset.ofHours(2)).toInstant();

		Instant output = DateUtil.addSecondsToDate(testDate, 1);
		assertEquals(testDate.plusSeconds(1), output);

		testDate = OffsetDateTime.of(2018, 3, 9, 9, 26, 32, 0, ZoneOffset.ofHours(2)).toInstant();
		output = DateUtil.addSecondsToDate(testDate, 52);
		assertEquals(testDate.plusSeconds(52), output);
		
		Instant n = null;
		
		try {
			output = DateUtil.addSecondsToDate(n, 8);
			fail();
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
		
		
		
	}
	
	@Test
	public void testNowUTC() {
		Instant now = Instant.now();
		
		Instant output = DateUtil.nowUTC();
		
		assertEquals(now, output);
	}

	
}
