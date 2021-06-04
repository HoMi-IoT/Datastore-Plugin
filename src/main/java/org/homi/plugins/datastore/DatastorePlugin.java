package org.homi.plugins.datastore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.homi.plugin.api.basicplugin.AbstractBasicPlugin;
import org.homi.plugin.api.commander.CommanderBuilder;
import org.homi.plugin.api.commander.Commander;
import org.homi.plugin.api.observer.*;
import org.homi.plugin.api.PluginID;
import org.homi.plugins.datastoreSpec.*;
import java.util.concurrent.*;
@PluginID (id = "DataStorePlugin")
public class DatastorePlugin extends AbstractBasicPlugin {
	
	private static Map<String, Datum> datastore = new ConcurrentHashMap<>();
	private final Executor observerQueue = Executors.newCachedThreadPool(); 
		
	@Override
	public void setup() {
		// TODO Auto-generated method stub
		CommanderBuilder<DatastoreSpec> cb = new CommanderBuilder<>(DatastoreSpec.class) ;
		
		
		Commander<DatastoreSpec> c = cb.onCommandEquals(DatastoreSpec.CREATE, this::create).
										onCommandEquals(DatastoreSpec.READ, this::read).
										onCommandEquals(DatastoreSpec.UPDATE, this::update).
										onCommandEquals(DatastoreSpec.DELETE, this::delete).
										onCommandEquals(DatastoreSpec.OBSERVE, this::observe).
										build();
		
		addCommander(DatastoreSpec.class, c);
	}
	
	private class Datum implements ISubject{
	
		private List<IObserver> observers = new ArrayList<>();
		private Object value;
		
		public Datum(Object object) {
			// TODO Auto-generated constructor stub
			value = object;
		}

		public void setvalue(Object o) {
			this.value = o;
			this.notifyListeners(o);
		} 
		
		public Object getvalue() {
			return this.value;
		}
		
		@Override
		public void attach(IObserver arg0) {
			// TODO Auto-generated method stub
			this.observers.add(arg0);
		}

		@Override
		public void detach(IObserver arg0) {
			// TODO Auto-generated method stub
			this.observers.remove(arg0);
		}

		@Override
		public void notifyListeners(Object... arg0) {
			// TODO Auto-generated method stub
			for(IObserver o: this.observers) {
				observerQueue.execute(
					new Runnable() {
						public void run() {
							o.update(arg0);
						}
					}
				);
				
			}
		}
	}
	
	private Void create(Object ...objects) {
		String key = (String) objects[0];
		Datum value = new Datum(objects[1]);
		DatastorePlugin.datastore.put(key, value);
		return null;
	}

	private Object read(Object ...objects) {
		return DatastorePlugin.datastore.get((String)objects[0]).getvalue();
	}

	private Void update(Object ...objects) {
		
		DatastorePlugin.datastore.get((String)objects[0]).setvalue(objects[1]);
		return null;
	}

	private Void delete(Object ...objects) {
		return null;
	}

	private Void observe(Object ...objects) {
		DatastorePlugin.datastore.get((String)objects[0]).attach((IObserver)objects[1]);
		return null;
	}
	@Override
	public void teardown() {
		observerQueue.shutdown();
	}
}
