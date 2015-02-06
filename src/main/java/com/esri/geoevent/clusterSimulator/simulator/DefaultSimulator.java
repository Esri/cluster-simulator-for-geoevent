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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

import com.esri.geoevent.clusterSimulator.EndOfFileException;
import com.esri.geoevent.clusterSimulator.MessageDestination;

public class DefaultSimulator implements Simulator
{
	private SimulationTimer timer;
	private FileSource fileSource;
	private CopyOnWriteArrayList<MessageDestination> listeners = new CopyOnWriteArrayList<>();
	private CopyOnWriteArrayList<Observer> endOfSimulationListeners = new CopyOnWriteArrayList<>();
	private Iterator<MessageDestination> iterator;
	private volatile long sentEventCount;
	private PlayMode playbackMode;
	private int batchSize;
	private int period;
	private float realPlaybackRate;
	private boolean looping;
	
	private enum PlayMode{ FIXED, REAL };

	@Override
	public synchronized void start() {
		if( timer != null )
			return;
		switch( playbackMode )
		{
		case FIXED:
			timer = new FixedTimer(batchSize,period);
			break;
		case REAL:
			timer = new RealTimeTimer(fileSource,realPlaybackRate);
			break;
		default:
			break;
		}
		timer.setTimerObserver(new Observer()
			{
				@Override
				public void update(Observable o, Object arg) {
					send();
				}
			});
		timer.start();
	}

	@Override
	public synchronized void stop() {
		if( timer == null )
			return;
		timer.cancel();
		timer = null;
	}

	@Override
	public synchronized void step() {
		if( timer == null )
			send();
	}

	@Override
	public synchronized void rewind() {
		stop();
		if( fileSource != null )
			fileSource.reset();
	}

	@Override
	public void setSimulationFile(File simulationFile, int skipLines, int timeField ) throws IOException
	{
		stop();
			fileSource = new FileSource(simulationFile, skipLines, timeField );
			fileSource.setLooping(looping);
	}
	
	@Override
	public void addMessageListener(MessageDestination listener) {
		listeners.add(listener);
	}

	@Override
	public void removeMessageListener( MessageDestination out )	{
		listeners.remove(out);
	}
	
	@Override
	public void addEndOfSimulationListener(Observer listener) {
		endOfSimulationListeners.add( listener );
	}

	@Override
	public long getSentEventCount() {
		return sentEventCount;
	}

	@Override
	public long getTotalEventCount() {
		if( fileSource == null )
			return 0;
		return fileSource.getEventCount();
	}

	@Override
	public String getFilename() {
		if( fileSource == null )
			return null;
		return fileSource.getFilename();
	}
	
	private void send()
	{
		try
		{
			MessageDestination destination = getNextDestination();
			if( destination != null )
			{
				String event = fileSource.getNextEvent();
				destination.send(event);
				sentEventCount++;
			}
		}catch( IOException ex )
		{
			// Do nothing if there's a network error while sending the event.
		} catch (EndOfFileException e) {
			stop();
			for( Observer listener : endOfSimulationListeners )
				listener.update( null, this );
		}
	}

	private MessageDestination getNextDestination()
	{
		if( iterator == null )
			iterator = listeners.iterator();

		if( !iterator.hasNext() )
		{
			if(listeners.size() == 0)
				return null;
			iterator = listeners.iterator();
			if( !iterator.hasNext() )
				return null;
		}

		return iterator.next();
	}

	@Override
	public void setFixedRate(int batchSize, int period) {
		if( playbackMode == PlayMode.FIXED && this.batchSize == batchSize && this.period == period )
			return;
		playbackMode = PlayMode.FIXED;
		this.batchSize = batchSize;
		this.period = period;
		if( isRunning() )
		{
			stop();
			start();
		}
	}

	@Override
	public void setRealRate(float realPlaybackRate) {
		if( playbackMode == PlayMode.REAL && this.realPlaybackRate == realPlaybackRate )
			return;
		playbackMode = PlayMode.REAL;
		this.realPlaybackRate = realPlaybackRate;
		if( isRunning() )
		{
			stop();
			start();
		}
	}

	@Override
	public synchronized boolean isRunning() {
		return timer != null;
	}

	@Override
	public boolean isReady() {
		return fileSource != null;
	}

	@Override
	public void setLooping(boolean looping) {
		this.looping = looping;
		FileSource fileSource = this.fileSource;
		if( fileSource != null )
			fileSource.setLooping(looping);
	}

	@Override
	public void resetCounter() {
		sentEventCount = 0;
	}

}
