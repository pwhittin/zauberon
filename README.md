# zauberon ('sŏw-bă-ŏn) 
![Zauberon](zauberon.png)A Clojure program to simulate the quantum physical characteristics of photons as described in the book 'New Age Quantum Physics' by Al Schneider, ISBN-10: 1467938009.  In the book, fundamental particles are called photons.  For this simulation I've chosen the name 'zauberon' because I don't want there to be any confusion with the established concept of a photon, and to pay homage to Al's prowess in the world of magic.

## Basic Model Concepts
 
1. All zauberons have a counter that can never be negative.
2. All zauberons travel in a helix at the same constant axial speed.  
3. The change in position over time of zauberons is described by a radius, axial vector, radial angle, and direction 
of rotation. 
4. As zauberons move their integer counter decrements by one for each period of travel.
5. zauberons 'collide' when they are at the same position within a 3D space at the same time.
6. When collisions occur a collision function executes on each set of colliding zauberons, and returns the new state of 
the zauberon set, and a new collision function.
7. All zauberons have and initial state (i.e., Position, radius, axial vector, and radial angle).

## License

Copyright © 2020 by Paul H. Whittington.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
