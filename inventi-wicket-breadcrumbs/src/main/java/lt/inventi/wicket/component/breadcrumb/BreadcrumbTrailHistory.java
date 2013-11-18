package lt.inventi.wicket.component.breadcrumb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;


class BreadcrumbTrailHistory implements Serializable {
    
	private static final long serialVersionUID = 8071831537136122583L;
	@SuppressWarnings("serial")
	private static final MetaDataKey<BreadcrumbTrailHistory> CONTAINER = new MetaDataKey<BreadcrumbTrailHistory>(){ /* empty */ };

    private static BreadcrumbTrailHistory get() {
        Session session = Session.get();      
        BreadcrumbTrailHistory container = session.getMetaData(CONTAINER);
        if (container == null) {
            container = new BreadcrumbTrailHistory();
            session.setMetaData(CONTAINER, container);
        }

        if (session.isTemporary()) {
            session.bind();
        }
        return container;
    }

    static void extendTrail(String maybeTrailId, Breadcrumb newCrumb) {
        BreadcrumbTrailHistory history = get();
        Breadcrumb crumb = history.useExistingCrumbIfPossible(newCrumb);

        PersistentList previousTrail = history.breadcrumbMap.get(maybeTrailId);
        if (BreadcrumbsSettings.isCompactionEnabled()) {
            previousTrail = compact(previousTrail, crumb);
        }
        history.breadcrumbMap.put(crumb.getId(), new PersistentList(previousTrail, crumb));
    }

    private static PersistentList compact(PersistentList trail, Breadcrumb crumb) {
        if (trail == null) {
            return null;
        }

        PersistentList next = trail;
        while (true) {
            if (next.tail.getType().equals(crumb.getType())) {
                return next.first;
            }
            if (next.first == null) {
                break;
            }
            next = next.first;
        }
        return trail;
    }

    /**
     * Tries to get the nth breadcrumb from the end for the provided trail id.
     * <p>
     * In case provided trail id is null or no breadcrumb history exists for the
     * id, nothing is returned.
     *
     * @param maybeTrailId
     * @return (trail size - n)th breadcrumb from the history associated with
     *         the provided trail or null if no trail exists
     */
    static Breadcrumb getLastBreadcrumbFor(String maybeTrailId) {
        BreadcrumbTrailHistory history = get();
        if (maybeTrailId != null && history.breadcrumbMap.containsKey(maybeTrailId)) {
            PersistentList trail = history.breadcrumbMap.get(maybeTrailId);
            return trail.tail;
        }
        return null;
    }

    static Breadcrumb getPenultimateBreadcrumbFor(String maybeTrailId) {
        BreadcrumbTrailHistory history = get();
        if (maybeTrailId != null && history.breadcrumbMap.containsKey(maybeTrailId)) {
            PersistentList trail = history.breadcrumbMap.get(maybeTrailId);
            if (trail.first != null) {
                return trail.first.tail;
            }
        }
        return null;
    }

    static List<Breadcrumb> getTrail(String trailId) {
        return get().breadcrumbMap.get(trailId).toList();
    }

    private final Map<String, Breadcrumb> crumbsByPageIdClass = new CrumbsHolder();
    private final Map<String, PersistentList> breadcrumbMap = new LimitedLinkedHashMap<String, PersistentList>();

    /**
     * This allows us to update breadcrumb titles in the existing trails. We
     * also share breadcrumb objects across trails instead of always storing new
     * ones on each render.
     */
    private Breadcrumb useExistingCrumbIfPossible(Breadcrumb newCrumb) {
        Breadcrumb crumb = crumbsByPageIdClass.get(newCrumb.getStableId());
        if (crumb != null) {
            crumb.updateWith(newCrumb);
        } else {
            crumb = newCrumb;
            crumbsByPageIdClass.put(crumb.getStableId(), crumb);
        }
        return crumb;
    }

    private static class PersistentList implements Serializable {        
		private static final long serialVersionUID = 587069254839862015L;
		final PersistentList first;
        final Breadcrumb tail;

        PersistentList(PersistentList first, Breadcrumb tail) {
            this.first = first;
            this.tail = tail;
        }

        public List<Breadcrumb> toList() {
            List<Breadcrumb> result = new ArrayList<Breadcrumb>();
            result.add(tail);
            if (first == null) {
                return result;
            }

            PersistentList prev = first;
            while (prev != null) {
                result.add(prev.tail);
                prev = prev.first;
            }
            Collections.reverse(result);
            return result;
        }
    }
    
    private class LimitedLinkedHashMap<K,V> extends LinkedHashMap<K, V>{
    	    	        	
		private static final long serialVersionUID = -8430506851255708235L;

		@Override
    	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
    		return size()>BreadcrumbsSettings.historyLimit();    		
    	}
    }
    
    private class CrumbsHolder implements Map<String, Breadcrumb>, Serializable{    	
		private static final long serialVersionUID = -6694551155283613646L;

		private transient WeakHashMap<String, Breadcrumb> map = new WeakHashMap<String, Breadcrumb>();

		@Override
		public int size() {			
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {			
			return map.containsKey(value);
		}

		@Override
		public Breadcrumb get(Object key) {
			return map.get(key);
		}

		@Override
		public Breadcrumb put(String key, Breadcrumb value) {
			return map.put(key, value);
		}

		@Override
		public Breadcrumb remove(Object key) {			
			return map.remove(key);
		}

		@Override
		public void putAll(Map<? extends String, ? extends Breadcrumb> m) {
			map.putAll(m);
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public Set<String> keySet() {			
			return map.keySet();
		}

		@Override
		public Collection<Breadcrumb> values() {			
			return map.values();
		}

		@Override
		public Set<java.util.Map.Entry<String, Breadcrumb>> entrySet() {			
			return map.entrySet();
		}
		
		private void writeObject(java.io.ObjectOutputStream s) throws IOException
		 {
			s.defaultWriteObject();
			int size = map.size();
			s.writeInt(size);
			Set<java.util.Map.Entry<String, Breadcrumb>> entrySet = map.entrySet();
			if(size>0)
			for (Entry<String, Breadcrumb> e : entrySet) {
				s.writeObject(e.getKey());
				s.writeObject(e.getValue());
			}
		 }
				
		private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException
		{
		        s.defaultReadObject();
		        int size = s.readInt();
		        map = new WeakHashMap<String, Breadcrumb>(size);		        
		        for (int i=0; i<size; i++) {
		        	String key = (String) s.readObject();
		        	Breadcrumb value = (Breadcrumb) s.readObject();
		            map.put(key, value);
		        }
		}
    }
}
