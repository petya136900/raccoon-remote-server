package com.petya136900.raccoonvpn.longpolling;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LongPollStorage {
	// <Key,<TS,Event>>
	class EventsStorage {
		long lastTs=0;
		private ConcurrentHashMap<Long, LongPollEvent> events = new ConcurrentHashMap<Long, LongPollEvent>();
		public ConcurrentHashMap<Long, LongPollEvent> getEvents() {
			return events;
		}
		public long getLastTs() {
			return lastTs;
		}
		public void setLastTs(long ts) {
			lastTs = ts;
		}
		public long incAngGet() {
			lastTs = lastTs+1;
			return lastTs;
		}
	}
	private ConcurrentHashMap<Object, EventsStorage> longPollStorage = new ConcurrentHashMap<Object, EventsStorage>();
	private Long WIPE_TIME_SEC = 3600L; // Seconds
	private Long lastWipe = 0L;	
	private void checkWipe() {
		if(System.currentTimeMillis()>(lastWipe+WIPE_TIME_SEC*1000))
			wipe();
	}
	private void wipe() {
		synchronized (longPollStorage) {
			longPollStorage.forEach((x,y)->{
				// x - Key
				// y - []<TS,Event>
				y.getEvents().forEach((ts,event)->{
					if(System.currentTimeMillis()>(event.getTime()+WIPE_TIME_SEC*1000)) {
						if(event.getTs()==0) {
							y.getEvents().remove(ts);
							if(ts.equals(y.getLastTs()))
								y.getEvents().clear();
						} else {
							event.setTs(0);
						}
						if(y.getEvents().size()<1) {
							longPollStorage.remove(x);
						}
					}
				});
			});	
		}
	}
	public LongPollEvent getEvent(Object key, Long ts, long timeout) {
		if(ts==Long.MAX_VALUE)
			ts=0l;
		long endTime = System.currentTimeMillis()+timeout;
		LongPollEvent event = null;
		Boolean isTimeout=false;
		do {
			if(System.currentTimeMillis()>=endTime) {
				isTimeout=true;
				break;
			}
			if(ts!=-1) {
				event = syncGetEvent(key,ts+1);
			} else {
				event = syncGetEvent(key,-1l);
			}
			if((event==null||event.getTs()==ts)&&(ts!=-1)) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) { }
			} else {
				if(event==null)
					event = new LongPollEvent();
				event.setTimeout(false);
				break;
			}
		} while(true);
		if(event==null)
			event = syncGetEvent(key,-1l);
		if(event==null)
			event = new LongPollEvent();
		if(isTimeout)
			event.setTimeout(true);
		return event;
	}
	class LPEventStorage {
		private LongPollEvent event;
		public LongPollEvent getEvent() {
			return event;
		}
		public void setEvent(LongPollEvent event) {
			this.event=event;
		}
	}
	private LongPollEvent syncGetEvent(Object key, Long ts) {
		checkWipe();
		synchronized (longPollStorage) {
			EventsStorage eventsStorage = longPollStorage.get(key);
			if(eventsStorage==null)
				return null;
			ConcurrentHashMap<Long, LongPollEvent> eventsForKey = eventsStorage.getEvents();
			LPEventStorage evStorage = new LPEventStorage();
			if(ts.equals(-1l)) {
				return eventsForKey.get(eventsStorage.getLastTs());
			} else{
				evStorage.setEvent(eventsForKey.get(ts));
			}
			return evStorage.getEvent();			
		}
	}
	public LongPollEvent addEvent(Object key, long code) {
		return addEvent(key, new LongPollEvent().setCode(code));
	}
	public LongPollEvent addEvent(Object key, String desc) {
		return addEvent(key, new LongPollEvent().setDesc(desc).setCode(0l));
	}
	public LongPollEvent addEvent(Object key, long code, String desc) {
		return addEvent(key, new LongPollEvent().setDesc(desc).setCode(code));
	}
	/*
	 * 0 - Don't wipe
	 */
	public Long getWIPE_TIME_SEC() {
		return WIPE_TIME_SEC;
	}
	public void setWIPE_TIME_SEC(Long wIPE_TIME_SEC) {
		WIPE_TIME_SEC = wIPE_TIME_SEC;
	}
	public LongPollEvent addEvent(Object key, LongPollEvent newEvent) {
		synchronized(longPollStorage) {
			EventsStorage eventsStorage = longPollStorage.get(key);
			if(eventsStorage==null) {
				eventsStorage = new EventsStorage();
				longPollStorage.put(key, eventsStorage);
			}
			long ts = eventsStorage.incAngGet();
			if(ts==Long.MAX_VALUE) {
				ts=1;
				eventsStorage.setLastTs(ts);
			}
			newEvent.setTs(ts);
			newEvent.setTime(System.currentTimeMillis());
			eventsStorage.getEvents().put(ts, newEvent);
			return newEvent;			
		}
	}
}
