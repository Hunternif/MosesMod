Moses Mod
=========

This mod introduces the item "Staff of Moses", which can be obtained with the help of a burning bush.

With the staff equipped, right-click in the direction of water will create a passage to a dramatic music cut from "The Prince of Egypt"; right-click in any other direction or logging out will close all passages. Hitting stone with the staff will create a source of water.
One passage is maximum 5 blocks wide and 64 blocks long (it is now configurable); it may be narrower if the coastline is irregular (I might fix this someday).

As people have suggested, there is now a staff that parts both water and lava. In order to obtain one, you must throw the regular Staff of Moses into lava.

This mod should work in single player and multiplayer.

Building
--------

1. Check out the project and import it into a workspace with the project Minecraft.
2. Create a copy of the file `local.properties.example`, rename it to `local.properties` (Git ignores it) and change the properties to valid paths on your computer. The paths are either absolute or relative to the project folder.
3. Run the Ant script `build.xml`. By default it will create a release build, deobfuscated build and a zip file with the JavaDocs. Creation of JavaDocs will fail unless you run Ant in a JDK (instead of a simple JRE), but it will not affect the release builds.

TODO Features
-------------

* Left-click on sand to send a swarm of flies that hurt entities except you.

* During a thundering storm stand on top of a mountain (y > some threshold) and type in chat something along the lines of "Praise the Lord", a lightning will strike and spawn 2 stone tablets. Opening those tablets will show the Ten Commandments.
