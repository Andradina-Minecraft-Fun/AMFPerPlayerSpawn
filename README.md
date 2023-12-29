# AMFPerPlayerSpawn

This plugin gives you the ability to set spawn point for each player. Nice to servers that doesn't have a spawn area.

With this, every player will have your `/spawn` location

# Permissions

`amf.perplayerspawn.spawn`: can perform `/spawn` command, and back to spawn location
`amf.perplayerspawn.spawn.set`: can perform `/spawn set` command, and set the current location as new spawn point
`amf.perplayerspawn.spawn.new`: can perform `/spawn new` command, and search for new location as spawn point (nice replace for `/rtp` or `/wild`)

# Compile

just clone this repository, and run command

```bash
mvn compile && mvn package
```

# ToDo

- [ ] Translate messages
- [ ] Prefix for messages
- [ ] On find new spawn point location, verify if this location are protected by `RedProtect` or `GriefPrevention`
- [ ] Cooldown for `/spawn set` and `/spawn new`
- [ ] Give permission to set new spawn point in certain radius, configurable
- [ ] Give permission to set new spawn point verticaly, configurable