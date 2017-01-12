DMDirc plugins
================================================================================

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/920da51fe86c4341a2ffa320d531cad3)](https://www.codacy.com/app/DMDirc/Plugins?utm_source=github.com&utm_medium=referral&utm_content=DMDirc/Plugins&utm_campaign=badger)

This repository contains plugins for [DMDirc](https://www.dmdirc.com/), a Java
IRC client.

Development information
--------------------------------------------------------------------------------

### Gradle configurations

We have two custom configurations for use when defining plugin dependencies:

The **bundle** configuration allows dependencies to be bundled together into
the plugin's jar file. This should be used for dependencies required at runtime
that are not included in the main DMDirc client, or accessed via other plugins.
Most of a plugin's dependencies should end up in the bundle configuration.

The **provided** configuration works like the Maven 'provided' scope. It
defines dependencies required to compile and run the plugin that will be
provided to it somehow externally. Anything in the provided configuration
(including transitive dependencies) will *not* be bundled into the plugin jar.
The provided configuration is used for the main DMDirc client, and should be
used for any intra-plugin dependencies.
