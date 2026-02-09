package regions;

import entities.Conductor;
import entities.Pawn;
import pt.ua.gboard.basic.Position;

/**
 * General description: Definition of the Station region.
 *
 * @author Tiago Madeira 76321
 */
public class Station
{

    /**
     * Internal data
     */
    private Position location;
    private boolean canBuyTicket = false;
    private boolean nextPassengerCanBuyTicket = false;
    private boolean lastPassengerToEnter = false;
    private boolean trainIsInStation = false;
    private int numPassengers = 0;
    private Train train = null;
    private int ticketPrice = 10;
    private int ticketsSold = 0;
    private char trackSym;

    /**
     * Constructor
     *
     * @param pos      Position of the Station on the map
     * @param trackSym the symbol associated with the track
     */
    public Station(Position pos, char trackSym)
    {
        assert pos != null;
        this.location = pos;
        this.trackSym = trackSym;
    }

    ///////////////////////////////////////////////////////////////////////
    // Pawn
    /**
     * Sleeps waiting for a signal from the Salesman to buy a ticket. When woken
     * up processes the transaction. Taking money from the wallet and saving the
     * ticket. In the end wakes up the Salesman so he can process the next Pawn.
     */
    public synchronized void buyTicket()
    {
        //System.out.println("Waiting to buy ticket...");
        while (!canBuyTicket)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {

            }
        }
        canBuyTicket = false;

        // Process the transaction
        Pawn tmpPawn = ((Pawn) Thread.currentThread());
        tmpPawn.updateWalletValue(-ticketPrice);
        tmpPawn.saveTicket(this.trackSym);
        this.ticketsSold++;
        System.out.println(tmpPawn.getName() + ": Bought ticket for track "
                + tmpPawn.seeTicket() + "! My new balance is " + tmpPawn.getWalletValue() + "€\n");

        // Wake up salesman so he can process the next Pawn
        nextPassengerCanBuyTicket = true;
        notifyAll();
    }

    /**
     * Sleep waiting for a train to reach the station. Upon waking up decrease
     * the variable which counts the number of passengers at the station. Get
     * the Train reference from the shared variable. If it's the last Pawn to
     * get into the train signal the Conductor to wake up.
     *
     * @return the Train in which the passenger will travel
     */
    public synchronized Train waitForTrain()
    {
        //System.out.println("Gonna sleep in station...");
        numPassengers++;

        // Wait for the train to reach the station
        while (!trainIsInStation)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {

            }
        }
        //System.out.println("Woke up in station!");
        numPassengers--;
        Train t = this.train;

        // Last passeneger to enter the train wakes up the driver
        if (numPassengers == 0)
        {
            //System.out.println("I was last to get into train!");
            lastPassengerToEnter = true;
            notifyAll();
        }

        return t;
    }

    ///////////////////////////////////////////////////////////////////////
    // Conductor
    /**
     * If there are passengers waiting at the station, save the train reference
     * in the shared variable and signal them to wake up and sleep waiting for
     * all of them to get into the train. Upon being awaken by the last
     * passenger to get on, remove the train and reset the variables.
     */
    public synchronized void letPassengersIntoTrain()
    {
        if (numPassengers == 0)
        {
            return;
        }

        // get the train into the station
        this.train = ((Conductor) Thread.currentThread()).getTrain();
        trainIsInStation = true;
        notifyAll();

        // sleep waiting for passengers to get in
        while (!lastPassengerToEnter)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {

            }
        }

        //System.out.println("Train is leaving!");
        this.train = null;
        trainIsInStation = false;
        lastPassengerToEnter = false;
    }

    ///////////////////////////////////////////////////////////////////////
    // Salesman
    /**
     * Infinitely sell tickets at the station. Signal that one transaction may
     * be completed and sleep. Wait to be awaken by a Pawn completing the
     * transaction.
     */
    public synchronized void sellTickets()
    {

        canBuyTicket = true;
        notifyAll();

        // Forever selling tickets
        while (true)
        {
            try
            {
                while (!nextPassengerCanBuyTicket)
                {
                    try
                    {
                        wait();
                    } catch (InterruptedException e)
                    {
                        throw e;
                    }
                }

                nextPassengerCanBuyTicket = false;

                canBuyTicket = true;
                notifyAll();
            } catch (InterruptedException e)
            {

            }

            // Done with this transaction
            //System.out.println(((Salesman) Thread.currentThread()).getName()
            //+ ": Have a nice ride!\n");
        }

    }

    /**
     * Returns the location of the station.
     *
     * @return a Position representing the location of the station
     */
    public Position getLocation()
    {
        return this.location;
    }
}
