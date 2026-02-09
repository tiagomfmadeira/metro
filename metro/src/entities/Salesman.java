package entities;

import regions.Station;

/**
 * General description: definition of the Salesman.
 *
 * @author Tiago Madeira 76321
 */
public class Salesman extends Thread
{

    /**
     * Internal data
     */
    private Station station;

    /**
     * Constructor
     *
     * @param name    Salesman thread name
     * @param station Station in which the Salesman will work
     */
    public Salesman(String name, Station station)
    {
        super(name);
        assert station != null;
        this.station = station;
    }

    /**
     * Life cycle
     */
    @Override
    public void run()
    {
        station.sellTickets();
    }

}
