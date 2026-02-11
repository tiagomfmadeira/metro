<h1 align="center">Metro</h1>

<p align="center">
  <img width="600" height="550" alt="sim" src="https://github.com/user-attachments/assets/7ed83db3-bdf3-4a78-909e-271f793e306e" />
</p>

A concurrent object-oriented metro system simulation with animated trains, stations, and passengers moving across a map. 
The simulation models trains circulating on multiple metro lines while passengers buy tickets, wait at stations, board trains, ride to their destination, and exit the system, all visualized in real time on a grid-based map.
Everything runs concurrently: trains, passengers, ticket sales, inspections, and movement are all handled by independent threads.

---

- Colored tracks represent different metro lines
- Trains move continuously along each line  
- Passengers appear as colored dots, enter stations, ride trains, and exit at their destination  
- Destinations are marked temporarily and cleared once reached

---

## Train sprites and animation
<p align="center">
<img width="420" height="550" alt="trains" src="https://github.com/user-attachments/assets/db543a3d-16be-4a32-9d12-5c79f2c570a8" />
</p>

Trains are rendered using image sprites that change depending on direction and movement.  
Each train follows a closed loop defined by the track layout and animates smoothly as it moves through the map.

---

## Map layout
<p align="center">
<img width="250" height="340" alt="Map" src="https://github.com/user-attachments/assets/2c055f32-5683-4ff0-83a0-7f96d6d129d7" />
</p>

The simulation world is defined by a plain-text map.  
Each character represents a different element of the metro system:

- Uppercase letters define tracks  
- Lowercase letters define stations associated with each track  
- Free spaces are walkable areas for passengers  

Because train paths are computed directly from the map, changing the layout automatically updates train movement, station placement, and passenger behavior.

---

## How it works

- The map is loaded from a text file where characters define tracks, stations, and free space  
- Trains automatically compute their movement paths from the map  
- Passengers spawn at random locations and choose destinations  
- Stations handle ticket sales and waiting logic  
- Trains stop at stations to let passengers on and off  
- Inspectors check tickets while passengers are inside the train  

The system is designed so that tracks, stations, train counts, and passenger rates can be changed without rewriting movement logic.

---

## Customization

You can easily tweak:
- Track layouts by editing the map file  
- Number of tracks and trains  
- Passenger spawn rate and maximum count  
- Simulation speed (train movement and walking speed)

---

## Credits

This project uses the [**GBoard**](https://sweet.ua.pt/mos/pt.ua.gboard/index.html) library for grid-based visualization and interaction.  

---

## License

Distributed under the **GPL-3.0 License**. See [`LICENSE`](LICENSE) for more information.
