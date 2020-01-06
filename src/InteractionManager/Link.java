/*
 * Link.java
 *
 * Created on October 14, 2005, 3:19 PM
 */

package InteractionManager;

/**
 *
 * @author Samarth Swarup
 */
public class Link {
    
    public int from;
    public int to;
    
    /** Creates a new instance of Link */
    public Link(int f, int t) {
        this.from = f;
        this.to = t;
    }
    
    public boolean equals(Object otherLink)
    {
        if (((Link)otherLink).from == this.from && ((Link)otherLink).to == this.to)
            return true;
        else
            return false;
    }
    
}
