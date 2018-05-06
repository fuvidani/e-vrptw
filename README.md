# Electric-Vehicle Routing Problem with Time Windows (EVRPTW)  <br/> [![Build Status](https://travis-ci.com/fuvidani/e-vrptw.svg?token=nWakM5wh7rnyXAfUiELD&branch=master)](https://travis-ci.com/fuvidani/e-vrptw) [![Gradle Status](https://gradleupdate.appspot.com/int128/latest-gradle-wrapper/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/latest-gradle-wrapper/status) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
Solver for the Electric Vehicle Routing Problem with Time Windows.

<p align="center">
  <img src="http://ls11-www.cs.tu-dortmund.de/people/chimani/VehicleRouting/vr.png">
</p>

[Image Source](http://ls11-www.cs.tu-dortmund.de/people/chimani/VehicleRouting/)

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
where _h_ denotes the constant charge consumption rate. Furthermore, a set of homogeneous 
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

## Usage

// to be written

## Contributions
Special thanks to my dear friend and colleague [David Molnar](https://github.com/dmolnar99)
for participating in this project.

## License
This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). Feel free to
use, extend or fit it to your needs.

