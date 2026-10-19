# Screw you 4

Screw you 4 is the child of [Screw You 3](https://github.com/Geming400/ScrewYou3), a Geometry Dash mod.
There was no reason for Minecraft to escape this fate, it has now been screwed over (4).

![The mod's logo](/src/main/resources/assets/screw-you-4/icon.png)

## What does it do

> [!WARNING]
> This can and **WILL** make your Minecraft crash eventually

> [!CAUTION]
> **THIS CAN ACTUALLY CORRUPT STUFF** IF YOU ARE UNLUCKY ENOUGH.
If something breaks, this is **YOUR FAULT**

Every time the player dies, a random minecraft function "dies". What does this mean ?
Well the return value of that function gets completely messed up and can return gibberish.

Some return values may produce crashes or corruption, while some may just produce funny effects.

## Why is this the best mod ever

It's better than create, tfc, or whatever you can think of. Why ? Well you see I'm mixining 35739 functions.

## How does it perform

You can look at how amazing benchmark comparing sodium to screw you 4:

![Benchmark comparing vanilla Minecraft, sodium and Screw You 4](./readmeResources/benchmark.png)

Crazy, right ? Screw you 4 is the future of minecraft optimization !

### How does it work

#### New way: (>= `v1.0.3`)

Using a sophisticated technology known as *"java agents"*, we at Screw You 4™
have brought you even more screws !

For reference, here's an accurate depiction of what this looks like:

<img alt="Accurate depiction of you being screwed over (4)" height="256" width="256" src="readmeResources/screwing-you-4.png"/>

Or if you're feeling a little *screwy*:

<img alt="Accurate depiction of you being screwed over (4) (Mk. 2)" height="128" width="512" src="readmeResources/screws.png"/>

> These are some beautiful screws right there<br>
> \- Geming400, 2026

---

Seriously, like mixins, classes are "transformed" at runtime.
Except, these transformations only happen on the player's death.

#### Old way

Old way: (prior to `v1.0.3`)
> Using reflection, all `net.minecraft.**` classes are loaded and then inspected
> to create these mixins.
>
> All the work is done in the `Generator.java` file.
