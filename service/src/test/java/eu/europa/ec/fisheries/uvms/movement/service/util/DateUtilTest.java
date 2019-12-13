package eu.europa.ec.fisheries.uvms.movement.service.util;

import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class DateUtilTest extends TransactionalTests {

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetDateFromString() throws ParseException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X");


		Instant testDate = OffsetDateTime.of(2018, 3, 9, 11, 26, 30, 0, ZoneOffset.ofHours(2)).toInstant();
		//System.out.println(sdf.format(testDate.getTime()));

		//Formats are in DateFormats.java
		//yyyy-MM-dd HH:mm:ss Z      					  2018-03-09 09:26:30 +0100
		Instant timestamp = DateUtils.stringToDate("2018-03-09 10:26:30 +0100");
		assertTrue(testDate.equals(timestamp));

		timestamp = DateUtils.stringToDate("2018-03-09 09:26:30 Z");
		assertTrue(testDate.equals(timestamp));
		
		timestamp = DateUtils.stringToDate("2018-03-09 04:26:30 -0500");
		assertTrue(testDate.equals(timestamp));

		//EEE MMM dd HH:mm:ss z yyyy			Fri Mar 09 08:26:30 CET 2018
		timestamp = DateUtils.stringToDate("Fri Mar 09 10:26:30 CET 2018");
		assertTrue(testDate.equals(timestamp));
		
		timestamp = DateUtils.stringToDate("Fri Mar 09 18:26:30 JST 2018");
		assertTrue(testDate.equals(timestamp));

		//yyyy-MM-dd HH:mm:ss X
		timestamp = DateUtils.stringToDate("2018-03-09 10:26:30 +01");
		assertTrue(testDate.equals(timestamp));
		
		timestamp = DateUtils.stringToDate("2018-03-09 12:26:30 +03");
		assertTrue(testDate.equals(timestamp));

		//yyyy-MM-dd HH:mm:ss
		timestamp = DateUtils.stringToDate("2018-03-09 09:26:30");
		assertTrue(testDate.equals(timestamp));

		
		//fails to come

		timestamp = DateUtils.stringToDate("9:26:30");
		assertNull("Only time should not work", timestamp);



		timestamp = DateUtils.stringToDate("2018-02-31");
		assertNull("Only date should not work", timestamp);

		timestamp = DateUtils.stringToDate("2018-02-31 10:26");
		assertNull("Missing seconds", timestamp);

		timestamp = DateUtils.stringToDate(null);
		assertNull("Null", timestamp);

		
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testParseToUTCDate() throws ParseException { //To UTC Date is somewhat missleading, the function simply parses a string into a date. 
		//This test is basicly a carbon copy of testGetDateFromString() since the only difference between them is the output. 

		Instant testDate = OffsetDateTime.of(2018, 3, 9, 11, 26, 30, 0, ZoneOffset.ofHours(2)).toInstant();
		//System.out.println(sdf.format(testDate.getTime()));

		//Formats are in DateFormats.java
		//yyyy-MM-dd HH:mm:ss Z      					  2018-03-09 09:26:30 +0100
		Instant timestamp = DateUtils.stringToDate("2018-03-09 10:26:30 +0100");
		assertTrue(testDate.equals( timestamp));
		
		timestamp = DateUtils.stringToDate("2018-03-09 04:26:30 -0500");
		assertTrue(testDate.equals( timestamp));

		//EEE MMM dd HH:mm:ss z yyyy			Fri Mar 09 08:26:30 CET 2018
		timestamp = DateUtils.stringToDate("Fri Mar 09 10:26:30 CET 2018");
		assertTrue(testDate.equals( timestamp));
		
		timestamp = DateUtils.stringToDate("Fri Mar 09 18:26:30 JST 2018");
		assertTrue(testDate.equals( timestamp));

		//yyyy-MM-dd HH:mm:ss X
		timestamp = DateUtils.stringToDate("2018-03-09 10:26:30 +01");
		assertTrue(testDate.equals( timestamp));
		
		timestamp = DateUtils.stringToDate("2018-03-09 12:26:30 +03");
		assertTrue(testDate.equals( timestamp));

		//yyyy-MM-dd HH:mm:ss
		timestamp = DateUtils.stringToDate("2018-03-09 09:26:30");
		assertTrue(testDate.equals(timestamp));

		
		//fails to come
		timestamp = DateUtils.stringToDate("09:26:30");
		assertNull("Only time should not work", timestamp);

		timestamp = DateUtils.stringToDate("2018-02-31");
		assertNull("Only date should not work", timestamp);

		timestamp = DateUtils.stringToDate("2018-02-31 10:26");
		assertNull("Missing seconds", timestamp);
		
		timestamp = DateUtils.stringToDate(null);
		assertNull("Input = null", timestamp);
	}
	
	@Test
    @OperateOnDeployment("movementservice")
	public void testParseUTCDateToString() {
		Instant testDate = OffsetDateTime.of(2018, 3, 9, 11, 26, 30, 0, ZoneOffset.ofHours(2)).toInstant();

		String formatedDate = DateUtils.dateToHumanReadableString(testDate);
		assertTrue(formatedDate.contentEquals("2018-03-09 09:26:30 Z"));

		testDate = OffsetDateTime.of(2018,3, 9, 3, 26, 30, 00, ZoneOffset.ofHours(2)).toInstant();
		ZonedDateTime zonedTestDate = ZonedDateTime.of(2018,3, 9, 10, 26, 30, 00, ZoneId.of("CET"));			//Lets hop that it understand that this is supposed to be summer time internally
		zonedTestDate = zonedTestDate.withZoneSameInstant(ZoneId.of("CST", ZoneId.SHORT_IDS));

		formatedDate = DateUtils.dateToHumanReadableString(zonedTestDate.toInstant());
		assertTrue(formatedDate, formatedDate.contentEquals("2018-03-09 09:26:30 Z"));
		
		formatedDate = DateUtils.dateToHumanReadableString(null);
		assertNull(formatedDate);
	}
}
