# Arms 'n Artifice: Heavy Artillery

An addon for [Iron's Arms 'n Artifice](https://www.curseforge.com/minecraft/mc-mods/irons-artifice) that adds big late-game guns made of blackened steel and copper.

## What's in it

Three new guns. The Gunsteel Gatling is a hand-cranked six-barrel gun, the Gunsteel Crucible is a flamethrower that melts your bullets into burning slag (fit a Breaching Shell and it sets fire to whatever burnable blocks it hits), and the Gunsteel Congreve fires rockets that blow up everything around whatever they hit.

Two attachments that work on any gun, including the ones from Arms 'n Artifice: a tripod you set down by crouching, and a reflex sight for aiming down a red dot.

Four new modifiers, also for any gun: Drum Magazine, Rifled Barrel, Powder Ram and Enchanted Magazine.

Everything is crafted from Heavy Components, and all the recipes are in the recipe book.

## Config

Modpacks can change the numbers in `config/artifice_heavy_artillery-server.toml`:

- **Each gun:** damage, spread, fire rate, magazine size, reload time, modifier slots and how much it slows you down, plus its own numbers (the Gatling's spin-up, the Crucible's globs and burn time, the Congreve's bullets per rocket and blast radius).
- **Each modifier:** the Drum Magazine's capacity and reload penalty, the Rifled Barrel's damage per block, range and reload penalty, the Powder Ram's bonus and charge time, and how much the Reflex Sight and Portable Tripod cut spread and recoil (and how fast you walk while aiming).

The file is made on first launch. Players get the server's values when they join, and changes apply as soon as the file is saved, to guns already made too. If a gun's modifier slots are lowered, the modifiers in the removed slots go back to the player the next time they open that gun's modifier menu.

## Requirements

- Minecraft 26.1.2 with NeoForge
- [Iron's Arms 'n Artifice](https://www.curseforge.com/minecraft/mc-mods/irons-artifice)
- [Iron's Lib](https://www.curseforge.com/minecraft/mc-mods/irons-lib)
- [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib)

Install it on both the client and the server.

Playing on 1.21.1? Use the [1.21.1 branch](https://github.com/guij2/artifice-heavy-artillery/tree/1.21.1).

## Building

```
./gradlew build
```

The jar ends up in `build/libs`. Gradle downloads the right Java version by itself.

## Bugs

Report them in [Issues](https://github.com/guij2/artifice-heavy-artillery/issues). This is an unofficial addon, so please don't send its bugs to the Arms 'n Artifice team.

## License

MIT, see [LICENSE](LICENSE).
