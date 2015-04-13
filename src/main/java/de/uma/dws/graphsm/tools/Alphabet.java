package de.uma.dws.graphsm.tools;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A mapping between integers and objects where the mapping in each direction is
 * efficient. The trick is given by (1) using an ArrayList for going from
 * integers to objects and a map to go from objects to integers; (2) use the GNU
 * trove libraries. Integers are assigned consecutively, starting at zero, as
 * objects are added to the Alphabet. Objects can not be deleted from the
 * Alphabet and thus the integers are never reused.
 * <p>
 * The most common use of an alphabet is as a dictionary of feature names. This
 * class is simply a simplified version of the same Alphabet class given in the
 * MALLET toolkit (just removed methods and added Java 5 generics).
 * 
 * @author ponzetto, flati
 * 
 * @param <T>
 */
public class Alphabet<T> implements Serializable
{
    private static final long serialVersionUID = 0xfeedfeeb1ef42L;

    private TObjectIntHashMap<T> map;
    
    private List<T> entries;
    
    private boolean growing;
    
    /** The class of the entries */

    public Alphabet()
    {
    	this.growing = true;
        this.map = new TObjectIntHashMap<T>();
        this.entries = new ArrayList<T>();
    }

    public void stopGrowth() { this.growing = false;}
    public void startGrowth() { this.growing = true;}

    /** Return the index of a given object. Add the Object to this if not
     *  present */
    public int lookupIndex (T entry)
    {
        if (entry == null)
        	throw new IllegalArgumentException ("Can't lookup \"null\" in an Alphabet.");

        int value = map.get(entry);
        if (value != 0) return value;
        else if (growing)
        {
            entries.add(entry);
            value = entries.size();
            map.put(entry, value);
            return value;
        } else {
            return -1;
        }
    }

    public T lookupObject(int index) { return entries.get(index); }

    public Object[] toArray() { return entries.toArray(); }

    public boolean contains (Object entry) { return map.contains (entry); }

    public int size () { return entries.size(); }

    public List<T> getEntries() { return new ArrayList<T>(entries); }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (T entry : entries)
        {
        	sb.append(lookupIndex(entry)).append(" <=> ").
        	   append(entry.toString()).
        	   append('\n');
        }
        return sb.toString();
    }
}

