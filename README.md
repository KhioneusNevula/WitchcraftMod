# WitchcraftMod
A mod for witchcraft. Witchcraft is magic that revolves around SpellSpaces, which are areas where magical elements interact with each other. <br>
A spell space is defined by either a) drawing dust, or b) obelisks (unimplemented)
<br>Drawing dust is small redstone-like symbol blocks which each have a tile entity. They send out pulses, and once drawing dust blocks are all joined in a single, closed shape formed of adjacent blocks on the same Y-level, they form a "spellspace," an area which is ten blocks tall that allows for magic to happen within it, the entire shape formed by the interior of the spellspace shape and ten blocks above. The "head" of the spellspace is one of its border blocks. The current algorithm for finding the shape is just a recursive flood-fill with a tolerance of maybe 3600 blocks--I'd prefer it to have a higher tolerance in case you want to, like, cover an entire village
<br>If a spellspace is affected, its spell effects are affected.
<br> The wand is an item which can be shift right clicked on spell components to cycle their functions
<br>Remnants are just small glass bottles which can hold an entity "type"
<br>Certain tileentities are "SpellComponents," which are just tile entities which can do something magical when a spellspace is activated and transfer their ouputs to adjacent spellcomponents. Spellcomponents can act as sensors and placers and transformers etc and can be linked to locations not their own with Fruits, Leaves, and Roots of Yggdrasil.<br>
<ul>
<li>Froots of Yggdrasil designate a block location upon right-click
<li>Leaves of Yggdrasil select an entity on right click or shift right click for self
<li>Roots of Yggdrasil select an entity and its spawnpoint for this dimension on right click
</ul>
Some components:
<ul> 
<li>the Wind Chime returns as its output the uuids of all the entities which are within a certain radius of it, and will chime when entities enter its radius even outside of spellcasting. It can also accept certain types of entities from a blood bag to include or exclude.
<li>The Philosophic Crystal will transform the block at the position(s) (or entity positions) which it is affecting into the block/liquid which it takes as input, and output the drops and blocktype. It can transform even bedrock.
<li>The sacrificial altar is a SpellGenerator, which gains power when a living being is killed above it, and sends the power into its spellspace if it is within one. Each spellcomponent may have a required power amount; if the amount is not met, the spell will not work
<li>The Abacus outputs a number which another spellcomponent takes as input
<li>The Earth Totem places the earth-based (sand, stone, prismarine, netherrack, etc) block inputted to it at the location it is bound to, which defaults to right above it; it cannot replace existing blocks however. The Nature Totem and the Infinite Chalice do the same things except for plants and liquids. The Paradoxic Totem, gained from demons, does this for all blocks which do not have an ItemBlock. The Divine Totem, given by a god, does this for any block that exists. 
<li>The Crystal ball senses properties of the blockpositions or entities (it will have a mode-switch function) given to it, and it can be used to test for a certain property by opening its gui
<li>The Wishbone returns the positions of all liquids within its radius
<li>The Resonant Crystal returns the positions of all instances of the block-type inputted to it in its radius (it does not test for tile entity data, only state metadata)
<li>The Pedestal outputs the block right above it to all adjacent spellcomponents that accept a blocktype. If no block is above it it will simply use air. It will destroy the block above it when activated.
<li>The Well outputs the liquid in it to adjacent components
<li>The Cauldron applies the effect of the inputted potion item in the mode it is configured in (splash, linger, drink, etc). If in drink mode, it will disregard non-entity positions; if in any other mode, it will treat entity positions as block positions
<li>The Wood Hand outputs the single item in its inventory to all adjacent spellcomponents and consumes the item.
<li>The Chaos Flower will output a random number between 0 and the value it is set to (inclusive). An abacus next to it will change the minimum value
<li>The Ender Flower teleports all inputted entities (through sides) to the position or entity inputted through its top. If it gets multiple positions through its top, it teleports randomly
<li>The Bottomless Bag will create all items inputted to it within it
<li>The Stone Avatar will right click the block it is pointing at and show its gui (if it has one) to whichever entities are inputted to it. Alternatively, it can right click the item inputted to it and apply its effect either right in front of it or at the positions of entities it is inputted
<li>The Seismograph will sense blockupdates in its radius and return the positions and type (e.g. FALLING, REDSTONE, DOOR_OPEN, SWITCH_ACTIVATE, MAGIC_START, OTHER)
<li>The Pendulum will return the block position that a bound item inputted to it has; e.g. a spellspace item such as a portion of Yggdrasil, or an item linked to a spellspace. 
<li>The Cloak will prevent a linked item of a spellspace from divulging its location of linkage if up to four are placed in the spellspace. 
<li>The Divine Mirror will accept an integer input and a block input and then create a dimension inside itself shaped in a square with sides of that integer and with block sides of the block input. A divine mirror cannot be activated twice
<li>The Divine Crucible will allow one to mix different mob drops (or just put one Remnant) to create a magical spawn egg for its mob when it is activated; alternatively, one might get a new kind of mob. It is not a programmable spell component. 
<li>The Divine Reincarnation Cauldron will transform the entity inputted to it into the mob type
<li>The Atropotome will set a single Cloak on fire at an item's bound location if placed next to a pendulum. Multiple will break more than one cloak.
<li>The blood bag can hold Remnants and outputs types of entities
<li>The Salt Effigy when put in a spellspace makes its borders solid on activation. It can accept two modes: exclude and include, and it will accept either EntityTypes or Entities to allow through the solid border or exclude from it
<li>The Doll, when put in a spellspace outputs the entity bound to it with a Leaf
<li>Touch-me-not senses when the position or entity inputted to it is touched and will output the entity or entities that touches
<li>The representational Forge, which outputs a RIGHT_CLICKED, LEFT_CLICKED, TICK, DROPPED, or DROPPED_TICK value along with the positions of item used whenever a linked item is used
<li>The representational armory, an armor-stand like BLOCK (not entity) which outputs ATTACKED, IS_ATTACKING, FALLEN as well as Integer for the damage of the armor if linked to an armor with magic applied to it. 
<li>The Magical Piston, which simply modifies the affecting position of whatever it is adjacent to to be with the value set in its gui (three vals: x y z). E.g. if this is placed near a PhilosophicCrystal with a Y value of 2, the philosophic crystal will transform whatever is two blocks above its linked pos
<li>The Map, when a root or fruit of Yggdrasil is put in it it will output that position to nearby components
<li>The Knight (chess piece), when a Leaf of yggdrasil is in it it will output the entity to nearby components
</ul>
There are also maybe a few devices, such as the WitchOven, which allows you to bake certain items into other items, e.g. bake apple pies from one egg, one milk, one bread, and one apple. It also might be used to bake village children :P. It also allows you to combine amber chunks with other items
<br>The Forge, which MIGHT be a device used to bind a spellspace to an Amber chunk, sword, armor, etc so that the spellspace's activation is controlled by the item (the spellspace will automatically chunkload if a spellspace item is used to load it). It won't be a spellspace component, but if you place it in a spellspace it will automatically (or maybe manually) create a SpellSoul, a spectral orb item linked to the spellspace used to bind items to it. If the spellspace is affected, any linked items will be affected, and if the spellspace is destroyed the item will stop working and lose its bindings data
<br>The Amber Block, which might act as a sort of machine which can hold a spellspace in its own pocket dimension
<br>And lastly, there will be a few new dimensions: the spirit world, residence of fairies and yokai, two types of tradable/fightable mobs that can give magic components based on illusions, nature, combat, etc; Eden, the residence of gods, powerful mobs which can be worshiped or fought to gain components that can insta-kill, create worlds, allow total immortality (with a price, of course), etc; and maybe the SpellMachine dimension, which is a CompactMachines style dimension where spellspaces can be stored for compactness' sake
<br>In essence, this is a magic mod where the magic is creatively customizable. Create a plague that is transferred through contact. Create a storm that rains fire. Create armor that summons wolves when you are attacked. That's the idea.