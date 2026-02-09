package main;

import static java.lang.System.*;
import regions.Train;
import entities.Conductor;
import entities.Inspector;
import entities.Pawn;
import java.awt.*;
import pt.ua.gboard.*;
import pt.ua.gboard.basic.*;
import pt.ua.gboard.games.*;
import regions.MetroMap;
import regions.Track;

/**
 * General description: Main class.
 *
 * @author Tiago Madeira 76321
 */
public class Metro
{

    protected static int N;
    protected static GBoard board;

    /**
     * Creation and starting of the various entity's threads. Instantiation of
     * shared memory regions.
     *
     * @param args name of the map file
     */
    static public void main(String[] args)
    {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Load map file
        String map = "map0.txt";
        if (args.length != 1)
        {
            out.println("Usage: TestLabyrinth <map-file>");
            out.println();
            out.println("Using \"" + map + "\" as default!\n");
        } else
        {
            map = args[0];
        }

        if (!Labyrinth.validMapFile(map))
        {
            err.println("ERROR: invalid map file \"" + map + "\"");
            exit(1);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Process symbols and create Gelems
        LabyrinthGelem.setShowRoadBoundaries();
        char[] roadSymbols =
        {
            'G', 'g', 'R', 'r', 'P', 'p', 'B', 'b', 'X'
        };

        N = 4;
        Labyrinth.setNumberOfLayers(4);
        Labyrinth.setWindowName("Metro");
        Labyrinth metro = new Labyrinth(map, roadSymbols, N);
        board = metro.board;

        // Number of tracks in map
        int nrTracks = 4;
        // Number of Pawns to spawn
        int maxPawns = 1000;
        // Rate of generation of Pawns (ms)
        int pawnRate = 500;

        // Define colours for tracks
        Color[] trackColors = new Color[nrTracks];
        trackColors[0] = Color.getHSBColor(5.30f, 0.60f, 0.70f);
        trackColors[1] = Color.getHSBColor(0.02f, 0.90f, 0.90f);
        trackColors[2] = Color.getHSBColor(4.90f, 0.80f, 0.80f);
        trackColors[3] = Color.getHSBColor(3.55f, 0.90f, 0.80f);
        Color stationColor = Color.getHSBColor(0f, 0f, 0.90f);

        // Attach gelems to each track symbol
        for (int i = 0; i < nrTracks; i++)
        {
            // track gelem
            metro.attachGelemToRoadSymbol(roadSymbols[i * 2], new FilledGelem(trackColors[i], 100, N, N));
            // station gelem
            Gelem[] stationGelem = new Gelem[2];
            stationGelem[0] = new FilledGelem(trackColors[i], 100, N, N);
            stationGelem[1] = new FilledGelem(stationColor, 30, N, N);
            metro.attachGelemToRoadSymbol(roadSymbols[(i * 2) + 1], new ComposedGelem(stationGelem));
        }

        // X marks the destination
        metro.attachGelemToRoadSymbol('X', new StringGelem("x", Color.BLACK, N, N));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Calculate tracks
        Position[] startPositions = new Position[nrTracks];
        startPositions[0] = new Position(5, 25);
        startPositions[1] = new Position(2, 16);
        startPositions[2] = new Position(5, 10);
        startPositions[3] = new Position(17, 4);

        Position[] endPositions = new Position[nrTracks];
        endPositions[0] = new Position(33, 28);
        endPositions[1] = new Position(29, 24);
        endPositions[2] = new Position(33, 19);
        endPositions[3] = new Position(35, 13);

        Track[] tracks = new Track[nrTracks];

        for (int i = 0; i < nrTracks; i++)
        {
            // Create track
            Track track = new Track(metro, startPositions[i], endPositions[i], roadSymbols[i * 2], roadSymbols[i * 2 + 1], N);
            tracks[i] = track;

            // Create train
            Train train1 = new Train(metro, board, track, startPositions[i], 0, N, 2);
            Train train2 = new Train(metro, board, track, endPositions[i], track.getMovesSize() / 2 + -2, N, 3);

            // Create inspector
            Inspector ticketInspector1 = new Inspector("Inspector_" + (i * 2), train1);
            Inspector ticketInspector2 = new Inspector("Inspector_" + (i * 2 + 1), train2);

            // Start the inspectors
            ticketInspector1.start();
            ticketInspector2.start();

            // Create conductors
            Conductor conductor1 = new Conductor("Conductor_" + (i * 2), train1);
            Conductor conductor2 = new Conductor("Conductor_" + (i * 2 + 1), train2);

            // Start the conductor threads
            conductor1.start();
            conductor2.start();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Create Map
        MetroMap metroMap = new MetroMap(metro, tracks);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Create pawns
        int i = 0;
        while (true)
        {
            Pawn pawn = new Pawn("Pawn_" + i, board, metroMap, metro, N, 1);
            pawn.start();
            GBoard.sleep(pawnRate);
            i++;
            if (i == maxPawns)
            {
                break;
            }
        }
    }
}
