# Damage Counter [![Plugin Installs](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/damage-counter)](https://runelite.net/plugin-hub/0anth) [![Plugin Rank](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/rank/plugin/damage-counter)](https://runelite.net/plugin-hub)
This plugin will display the damage you did to a boss and how long it took to kill.

When in combat with a boss, an overlay is displayed, detailing how much damage you have dealt and what percentage of the boss' total health that is.

Useful when bossing with other players.

[If you encounter any bugs, please raise an issue by clicking here.](https://github.com/0anth/damage-counter/issues/new)

## Features

- Overlay with damage information (togglable)
- Chat log message on boss death, detailing your damage, total damage, percentage and kill time (togglable)
- Option to display either total damage done or DPS
- Option to automatically hide overlay when boss dies
- Option to add your own NPCs to be tracked by Name
- Shows other user's damage, if they are in a party with you
- Option to sort the list of players in party by damage done in descending order

## Changelog

###### v1.8 - 28-Jul-2022
- Added feature to sort the list of players in party by damage done in descending order

###### v1.7 - 20-Jul-2022
- Refactored the plugin to make it work with the new Party plugin

###### v1.6 - 17-Jan-2021
- Added side panel (to be expanded upon soon)

###### v1.5 - 09-Dec-2020
- Counter now automatically resets when engaging combat with a different boss
- Counter will no longer automatically reset when teleporting/running away from boss
- Counter will still automatically reset when boss dies
- Extra config option added to allow custom NPCs to be tracked by name

###### v1.4.4 - 08-Dec-2020
- Bugfix for Giant Mole

###### v1.4.3 - 03-Dec-2020
- Bugfix for conflicting class names
- Counter will now reset if you run away or teleport from combat


###### v1.4.2 - 25-Nov-2020
- Bugfix for Barrows again
- Shortened chat log message
- Chat log message will now only mention percentage, if it is less than 100%


###### v1.4.1 - 18-Nov-2020
- Bugfix for counter not resetting on boss death


###### v1.4 - 11-Nov-2020
- Included config option to always hide overlay


###### v1.3 - 11-Nov-2020
- Included kill time in overlay, rather than mouseover
- Fixed bug with Barrows brothers


###### v1.2 - 10-Nov-2020
- Added kill time to chat log message


###### v1.1 - 04-Nov-2020
- Added config option to automatically hide overlay when boss dies


###### v1.0 - 27-Oct-2020
- Initial commit
