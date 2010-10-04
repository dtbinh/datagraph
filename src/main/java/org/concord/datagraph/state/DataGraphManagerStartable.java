/**
 * 
 */
package org.concord.datagraph.state;

import java.util.ArrayList;

import org.concord.datagraph.engine.DataGraphable;
import org.concord.framework.data.stream.DataProducer;
import org.concord.framework.data.stream.DataStore;
import org.concord.framework.data.stream.DefaultMultipleDataProducer;
import org.concord.framework.startable.AbstractStartable;
import org.concord.framework.startable.StartableEvent;
import org.concord.framework.startable.StartableInfo;
import org.concord.graph.examples.GraphWindowToolBar;

class DataGraphManagerStartable extends AbstractStartable {
	/**
	 * 
	 */
	private final DataGraphManager dataGraphManager;
	private StartableInfo info = new StartableInfo();

	private ArrayList<Thread> eventThreads = new ArrayList<Thread>();
	
	/**
	 * @param dataGraphManager
	 */
	DataGraphManagerStartable(DataGraphManager dataGraphManager) {
		this.dataGraphManager = dataGraphManager;
		updateInfo();
	}

	public boolean isRunning() {
		// see if there are any data producers running in the graph
		return dataGraphManager.dataGraph.getRunningDataProducers().size() > 0;
	}

	@Override
	public boolean isInInitialState() {				
		if (dataGraphManager.sourceGraphable != null) {
			DataStore dataStore = dataGraphManager.sourceGraphable.getDataStore();
			if (dataStore != null && dataStore.getTotalNumSamples() > 0) {
				return false;
			}
		}				
		return true;
	}

	public void reset() {
		reset(true);
	}
	
	protected void reset(boolean notifySourceProducer) {
		eventThreads.add(Thread.currentThread());
		// We bypass the normal dataGraph reset method so only the
		// selected graphable is cleared.
		if (dataGraphManager.sourceGraphable != null && dataGraphManager.sourceGraphable.getDataStore() != null) {
			dataGraphManager.sourceGraphable.reset();
		}

		DataProducer dp = dataGraphManager.getSourceDataProducer();
		
		// sfentress: If our data producer is a MultipleDataProducer, 
		// clear should clear all data lines that are being actively 
		// generated by that data producer, including any graphables 
		// with the same producer
		
//		if (dp instanceof DefaultMultipleDataProducer) {
//			if (((DefaultMultipleDataProducer) dp).getClearAll()){
//				ArrayList<DataGraphable> graphablesToReset = dataGraphManager.getDataGraph().getAllGraphables(dp);
//				for (int i = 0; i < graphablesToReset.size(); i++) {
//	               graphablesToReset.get(i).reset();
//	            }
//			}
//		}
		
		// UPDATE: We should clear all lines that have the same dataproducer,
		// regardless of whether it's a MultipleDataProducer
		
		ArrayList<DataGraphable> graphablesToReset = dataGraphManager.getDataGraph().getAllGraphables(dp);
		for (int i = 0; i < graphablesToReset.size(); i++) {
           graphablesToReset.get(i).reset();
        }
		

		if(dp != null && notifySourceProducer) {
			dp.reset();
		}
				
		if (dataGraphManager.dataGraph.isAdjustOriginOffsetOnReset()){
		    dataGraphManager.dataGraph.resetGraphArea();
		}
		
		if (dataGraphManager.dataGraph.restoreScaleOnReset()){
		    GraphWindowToolBar toolbar = dataGraphManager.dataGraph.getToolBar();
		    if (toolbar != null){
		    	toolbar.restoreOriginalScale();
		    }
		}
		
		notifyReset();
		eventThreads.remove(Thread.currentThread());
	}

	public void start() {
		start(true);
	}
	
	protected void start(boolean notifySourceProducer) {
		eventThreads.add(Thread.currentThread());
		dataGraphManager.dataGraph.start();
		DataProducer sourceDataProducer = dataGraphManager.getSourceDataProducer();
		if(sourceDataProducer != null && notifySourceProducer){
			sourceDataProducer.start();
		}
		notifyStarted(isInInitialState());
		eventThreads.remove(Thread.currentThread());
	}

	public void stop() {
		stop(true);
	}	
	
	protected void stop(boolean notifySourceProducer) {
		eventThreads.add(Thread.currentThread());
		dataGraphManager.dataGraph.stop();
		DataProducer sourceDataProducer = dataGraphManager.getSourceDataProducer();
		if(sourceDataProducer != null && notifySourceProducer){
			sourceDataProducer.stop();
		}
		
		// This might not be necessary but it can't hurt
		DataProducer oldSourceDataProducer = dataGraphManager.sourceGraphable.findDataProducer();
		if(oldSourceDataProducer != null && 
				oldSourceDataProducer != sourceDataProducer){
			oldSourceDataProducer.stop();
		}
		
		// and finally we stop all of the running data producers. 
		ArrayList<DataProducer> runningDataProducers = dataGraphManager.dataGraph.getRunningDataProducers();
		for (DataProducer dataProducer : runningDataProducers) {
			dataProducer.stop();					
		}
		notifyStopped();
		eventThreads.remove(Thread.currentThread());
	}

	@Override
	public StartableInfo getStartableInfo() {
		return info;
	}
	
	public void update() {
		updateInfo();
		
		notifyUpdated();
	}
	
	protected void updateInfo() {
		info.resetVerb = "Clear";
		if (dataGraphManager.sourceGraphable == null){
			info.enabled = false;
		} else if ((dataGraphManager.sourceGraphable.isLocked() ||
				!dataGraphManager.sourceGraphable.isVisible())) {
			info.enabled = false;
		} else {
			info.enabled = true;
		}
		info.sendsEvents = true;
		info.startStopLabel = "collecting data";
		info.resetLabel = "collected data";
		info.canRestartWithoutReset = false;
	}

	/**
	 * FIXME Currently this will cause a event loop
	 * @param event
	 */
	public void relayEvent(StartableEvent event) {		
		// only relay events if we aren't in the middle of sending one 
		if(eventThreads.contains(Thread.currentThread())){
			return;
		}
		switch(event.getType()){
			case RESET:
				reset(false);
				break;
			case STARTED:
				start(false);
				break;
			case STOPPED:
				stop(false);
				break;
			case UPDATED:
				update();
				break;		
		}
	}
}