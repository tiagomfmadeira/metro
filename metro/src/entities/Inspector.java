package entities;

import regions.Train;

/**
 * General description: definition of the Inspector.
 *
 * @author Tiago Madeira 76321
 */
public class Inspector extends Thread
{

    /**
     * Internal data
     */
    private Train train;

    /**
     * Constructor
     *
     * @param name Inspector thread name
     * @param t    train in which the Inspector will work
     */
    public Inspector(String name, Train t)
    {
        super(name);
        assert t != null;
        this.train = t;
    }

    /**
     * Life cycle
     */
    @Override
    public void run()
    {
        train.checkTickets();
    }

}
