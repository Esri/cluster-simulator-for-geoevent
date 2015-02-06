/*
  Copyright 1995-2014 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.clusterSimulator.simulator;

import java.text.DateFormat;
import java.text.ParseException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeParser 
{

	public static long parseTime( String timeString )
	{
		if( timeString == null )
			return 0;
	    try
	    {
	      DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser().withZoneUTC();
	      return formatter.parseMillis(timeString);
	    }
	    catch (IllegalArgumentException e) {}

	    try
	    {
	      return DateFormat.getDateInstance().parse(timeString).getTime();
	    }
	    catch (ParseException | NumberFormatException e) {}

	    try
	    {
	      DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yy hh:mm:ss aa");
	      return formatter.parseMillis(timeString);
	    }
	    catch (IllegalArgumentException e) {}

	    try
	    {
	      DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss");
	      return formatter.parseMillis(timeString);
	    }
	    catch (IllegalArgumentException e) {}

	    return 0;
	}
	
}
