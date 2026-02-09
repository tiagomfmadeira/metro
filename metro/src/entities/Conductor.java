package entities;

import regions.Train;
import pt.ua.gboard.GBoard;
import regions.Station;

/**
 * General description: definition of the Conductor.
 *
 * @author Tiago Madeira 76321
 */
public class Conductor extends Thread
{

    /**
     * Internal data
     */
    private Train train;

    /**
     *
     *
     * @param name  Conductor thread name
     * @param train Train in which the Conductor will work
     */
    public Conductor(String name, Train train)
    {
        super(name);
        assert train != null;
        this.train = train;
    }

    /**
     * Internal data
     */
    @Override
    public void run()
    {
        while (true)
        {
            train.move();
            GBoard.sleep(100);

            Station currStation = train.getCurrentStation();

            if (currStation != null)
            {
                // let passengers leave the train
                train.letPassengersOffTheTrain(currStation);

                // let passengers into the train
                currStation.letPassengersIntoTrain();
                GBoard.sleep(200);
            }
        }
    }

    /**
     * Returns the train in which the Conductor works.
     *
     * @return the Train in which the Conductor works
     */
    public Train getTrain()
    {
        return this.train;
    }
}
