# Electric-Vehicle Routing Problem with Time Windows (EVRPTW)  <br/> [![Build Status](https://travis-ci.com/fuvidani/e-vrptw.svg?token=nWakM5wh7rnyXAfUiELD&branch=master)](https://travis-ci.com/fuvidani/e-vrptw) [![Gradle Status](https://gradleupdate.appspot.com/int128/latest-gradle-wrapper/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/latest-gradle-wrapper/status) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
Solver for the Electric Vehicle Routing Problem with Time Windows.

<p align="center">
  <img src="http://ls11-www.cs.tu-dortmund.de/people/chimani/VehicleRouting/vr.png"><br/>
  <a href="http://ls11-www.cs.tu-dortmund.de/people/chimani/VehicleRouting/" target="_blank">Image Source<a/>
</p>

## Problem description
In the E-VRPTW, a set of customers must be served by a fleet of battery electric 
vehicles (BEV). The E-VRPTW extends the well-know [VRPTW](https://en.wikipedia.org/wiki/Vehicle_routing_problem), 
where customers must be served within a given time window, by the restriction of 
reduced operating range of the vehicles and the possibility to recharge at certain 
station to increase this range. The assumption of flat terrain and constant travel 
speed holds in the E-VRPTW. The objective function is to minimize the total travel 
distance.

More formal, the EVRPTW is defined on a complete directed graph G = (V,A) 
consisting of a set of depot, customer, and recharging station nodes and a set of edges. 
Each edge _(i, j)_ has a distance _d<sub>ij</sub>_ and a travel time  _t<sub>ij</sub>_ associated and each traveled 
edge consumes the amount of _r_ x _d<sub>ij</sub>_ of the remaining battery charge of the vehicle, 
where _r_ denotes the constant charge consumption rate. Furthermore, a set of homogeneous 
vehicles is given, where each vehicle has a maximal capacity of _C_ and is located, full 
loaded at the depot. Each node has a positive demand  _q<sub>i</sub>_, a service time  _s<sub>i</sub>_ and a time 
window [ _e<sub>i</sub>_, _l<sub>i</sub>_ ] assigned. The service must start within the given time window (thus, 
finishing the service could be later than  _l<sub>i</sub>_). At recharging stations, the difference 
between the present charge level and the battery capacity _Q_ is recharged (always loaded 
until battery is full) with a constant recharging rate of _g_. **Note, that it is allowed 
to visit recharging stations more than once.**

The aim is to construct routes, such that all customers are visited exactly once and 
served within their given time window. All routes must begin and end in the depot and 
the vehicle capacity and battery capacity must be respected. The objective function is 
to minimize the total traveled distance.

## Implementation Details

Our E-VRPTW Solver consists of two parts:
1. Construction heuristic: [Time-Oriented, Nearest-Neighbor Heuristic](https://pubsonline.informs.org/doi/abs/10.1287/opre.35.2.254?journalCode=opre) (Solomon 1987)
2. Metaheuristic: For this part, we tried to implement a Hybrid VNS/TS metaheuristic proposed in the paper [The Electric Vehicle-Routing Problem with Time Windows and Recharging Stations](https://pubsonline.informs.org/doi/10.1287/trsc.2013.0490),
however our solution is not the most efficient one and requires further optimizations (especially in route representation)  

Model objects like `EVRPTWInstance`, `Customer`, `Node` have been copied from the [E-VRPTW Solution Verifier (Java)](https://github.com/ghiermann/evrptw-verifier) implemented by @ghiermann.

See our [presentation slides](https://docs.google.com/presentation/d/1WnySkapfZkM57kC_8XTIKsyNZOhBhiNAdNTUWY201tQ/edit?usp=sharing) for more details.

## Usage

See Main.kt for instance/plotting/performance-recording customizations.


## Contributions
Special thanks to my dear friend and colleague [David Molnar](https://github.com/dmolnar99)
for participating in this project:

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore -->
| [<img src="https://avatars3.githubusercontent.com/u/16260193?s=400&v=4" width="100px;"/><br /><sub><b>David Molnar</b></sub>](https://github.com/dmolnar99)<br />[🤔](#ideas "Ideas and Planning") [💻](https://github.com/fuvidani/clickbait-defeater/commits?author=dmolnar99 "Code") [⚠️](https://github.com/fuvidani/clickbait-defeater/commits?author=dmolnar99 "Tests") |
| :---: |
<!-- ALL-CONTRIBUTORS-LIST:END -->

## License
This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). Feel free to
use, extend or fit it to your needs.

