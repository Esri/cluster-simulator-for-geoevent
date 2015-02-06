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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

import com.esri.geoevent.clusterSimulator.EndOfFileException;

import au.com.bytecode.opencsv.CSVParser;

public class FileSource 
{
	private File simulationFile;
	private int skipLines;
	private int timeField;
	private long fileLength;
	private BufferedReader in;
	private String nextLine;
	private volatile boolean looping;
	private volatile boolean adjustEventToCurrentTime = false;
	private CSVParser parser = new CSVParser();

	public FileSource( File file, int skipLines, int timeField ) throws IOException
	{
		simulationFile = file;
		if( !simulationFile.exists() )
			throw new IOException( "File "+simulationFile.getName()+" not found." );
		this.skipLines = skipLines;
		this.timeField = timeField;
		this.fileLength = calculateFileLength();
		loadFile();
	}

	public void setLooping( boolean looping )
	{
		this.looping = looping;
	}

	public void setAdjustEventToCurrentTime( boolean adjust )
	{
		this.adjustEventToCurrentTime = adjust;
	}

	public String peekAtNextEvent()
	{
		return nextLine;
	}

	public String getNextEvent() throws EndOfFileException
	{
		if( nextLine == null )
			throw new EndOfFileException("Reached the end of "+simulationFile.getName());
		try
		{
			String currentEvent = nextLine;
			nextLine = in.readLine();
			if( nextLine == null && looping )
			{
				loadFile();
				nextLine = in.readLine();
			}
			return adjustTime(currentEvent);
		}catch(IOException ex)
		{
			throw new EndOfFileException("Reached the end of "+simulationFile.getName());
		}
	}

	private String adjustTime(String event)
	{
		if( !adjustEventToCurrentTime )
			return event;

		try
		{
			String[] fields = parser.parseLine(event);
			StringBuilder build = new StringBuilder();
			for( int i = 0; i < fields.length; i++ )
			{
				if( i == (timeField - 1) )
					build.append(Instant.now().toString());
				else
					build.append(fields[i]);
				if( i < (fields.length - 1) ) 
					build.append(",");
			}
			return build.toString();
		}catch(IOException ex)
		{
			return event;
		}
	}

	public void reset()
	{
		try {
			loadFile();
		} catch (IOException e) {
		}
	}

	public String timeStringOfNextEvent()
	{
		if( timeField <= 0 )
			return null;
		try
		{				
			String[] fields = parser.parseLine(nextLine);
			return fields[timeField-1];
		}catch( DateTimeParseException | IOException ex)
		{
			return null;
		}
	}

	public long timeInstantOfNextEvent()
	{
		String nextTime = timeStringOfNextEvent();
		return DateTimeParser.parseTime(nextTime);
	}

	public long getEventCount()
	{
		return fileLength;
	}

	private void loadFile() throws IOException
	{
		in = new BufferedReader( new FileReader( simulationFile ) );
		for( int i = 0; i < skipLines; i++ )
			in.readLine();
		nextLine = in.readLine();
	}

	private long calculateFileLength()
	{
		long count = 0 - skipLines;
		try(BufferedReader in = new BufferedReader( new FileReader( simulationFile ) ))
		{
			while( in.readLine() != null )
				count++;
			return count;
		}catch(IOException ex)
		{
			// do nothing, just return count.
		}
		return count;
	}

	public String getFilename() {
		return simulationFile.getName();
	}


}
