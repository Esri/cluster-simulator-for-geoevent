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

import java.util.Observer;

import com.esri.geoevent.clusterSimulator.EndOfFileException;

public class RealTimeTimer implements SimulationTimer, Runnable
{
	private volatile Observer observer;
	private volatile boolean cancelled = false;
	private Thread thread;
	private FileSource fileSource;
	private float compression;

	public RealTimeTimer(FileSource fileSource, float realPlaybackRate) {
		this.fileSource = fileSource;
		compression = 100.0f / realPlaybackRate;
		thread = new Thread( this );
	}

	@Override
	public void start() {
		thread.start();
	}

	@Override
	public void cancel() {
		cancelled = true;
		thread.interrupt();
	}

	@Override
	public void setTimerObserver(Observer observer) {
		this.observer = observer;
	}
	
	@Override
	public void run()
	{
		try
		{
			long lastTimestamp = fileSource.timeInstantOfNextEvent();
			while( !cancelled )
			{
				long thisTimestamp = fileSource.timeInstantOfNextEvent();
				long delay = (long)( compression * (thisTimestamp - lastTimestamp) );
				if( delay > 0 )
					Thread.sleep(delay);
				observer.update( null, this );
				lastTimestamp = thisTimestamp;
			}
		}catch( InterruptedException ex)
		{				
		} 
	}

}
