<center><img src="https://i.imgur.com/iBWSME0.png" alt="Logo" width="1897" height="672" /></center>

**Epic Fight** adds complex game mechanics and fighting styles to your gameplay.  
With a pinch of *soulslike combat* and unique animation handling that bends **Minecraft**,  
Epic Fight will spice up your gameplay to a whole new level and bring great new challenges along the way.

You'll face a new take on **Minecraft's combat**,  
where all entities have new and challenging fighting mechanics.

## Developer outline: how the mod fits together

This section is a map for reading and changing the code. Start with the two Fabric entry points, then follow the
runtime flow or the subsystem table below. Paths are relative to `src/main` unless stated otherwise.

### The short version

```text
fabric.mod.json
  |-- common entry point -> EpicFightFabricInitializer
  |     |-- registries, commands, server callbacks, data reloaders
  |     `-- packet registration and server packet listener
  |
  `-- client entry point -> EpicFightFabricClientInitializer
        |-- controls, HUD, renderers, particles, shaders
        `-- client callbacks, resource reloaders, client packet listener

Minecraft Entity / ItemStack
  `-- capability provider attaches an Epic Fight wrapper (called a "patch")
        |-- EntityPatch / LivingEntityPatch / PlayerPatch / MobPatch
        |-- CapabilityItem / WeaponCapability
        |-- Animator + animation state
        `-- SkillContainer + equipped Skill instances
```

A **patch** does not replace a Minecraft object. It wraps that object with Epic Fight state and behavior. For example,
a `ServerPlayer` receives a `ServerPlayerPatch`, which owns combat mode, stamina, skills, animator state, and the
player-specific event listener. Most combat code works with the patch and calls `getOriginal()` only when it needs the
underlying Minecraft entity.

### Startup and registration

| Start here | Points to | What it does |
| --- | --- | --- |
| `resources/fabric.mod.json` | `main/EpicFightFabricInitializer.java` | Declares the common/server entry point, mod dependencies, mixins, and access widener. |
| `main/EpicFightFabricInitializer.java` | registries, reload listeners, callbacks, commands, packets | Builds all gameplay-side systems. Its method order is the common startup order. |
| `main/EpicFightFabricClientInitializer.java` | input, HUD, renderers, particles, shaders, client reloaders | Builds client-only systems and binds them to the local player. Dedicated servers must never load these classes. |
| `main/EpicFightFabricEventBridge.java` | `events/*` through `forgecompat` events | Converts Fabric callbacks into the event shapes used by the shared Epic Fight code. |
| `main/EpicFightFabricRegistryBridge.java` | `gameasset/*`, `world/item/*`, `world/entity/*`, and other registries | Moves deferred registrations into Minecraft/Fabric registries. |
| `main/EpicFightMod.java` | shared constants and legacy setup helpers | The original shared/Forge-facing mod class. On Fabric, use the two Fabric initializers above as the actual entry points. |

### Runtime flow: a player attack

1. `client/input/*` and `client/events/engine/ControlEngine.java` interpret a key, mouse, or controller action.
2. The appropriate `SkillContainer` checks its equipped `Skill`, stamina/cooldown, player mode, and current animation
   state.
3. A `network/client/CP*.java` packet sends the request to the server. `CP` means **client to server**.
4. The server handler finds the player's `ServerPlayerPatch`, validates the action, and starts the skill/animation.
5. `api/animation/types/AttackAnimation.java` uses animation phases and `api/collider/*` to find hit entities.
6. Damage and stun are applied through `LivingEntityPatch` plus the events in `world/entity/eventlistener/*`.
7. `network/server/SP*.java` packets mirror the accepted state to clients. `SP` means **server to client**.
8. `client/renderer/*` reads the patched entity's animator and pose to render the model, weapon, trails, particles,
   HUD, and camera effects.

The server is authoritative: client code requests actions and predicts/presents them, but gameplay-changing decisions
belong in the server handler or shared code running on the logical server.

### Java subsystem map

| Package | Responsibility | Useful starting points |
| --- | --- | --- |
| `main` | Fabric entry points and platform bridges | `EpicFightFabricInitializer`, `EpicFightFabricClientInitializer`, `EpicFightFabricEventBridge` |
| `api/animation` | Skeleton poses, animation playback, state windows, root motion, and animation loading | `AnimationManager`, `Animator`, `DynamicAnimation`, `StaticAnimation`, `AttackAnimation` |
| `api/collider` | Line, plane, and oriented-box hit detection used by attack phases | `Collider`, `OBBCollider`, `MultiCollider` |
| `api/model` and `model/armature` | Mesh/armature data and named skeleton joints | `Armature`, `Joint`, `HumanoidArmature` |
| `world/capabilities` | Attaches Epic Fight data/behavior to vanilla entities, items, projectiles, and players | `EpicFightCapabilities`, `EntityPatchProvider`, `ItemCapabilityProvider` |
| `world/capabilities/entitypatch` | Combat-aware wrappers around Minecraft entities | `EntityPatch`, `LivingEntityPatch`, `PlayerPatch`, `MobPatch` |
| `world/capabilities/item` | Weapon categories, styles, combos, colliders, and item attributes | `CapabilityItem`, `WeaponCapability`, `WeaponTypeReloadListener` |
| `skill` | Skill definitions, slots, activation, resource use, cooldowns, and synchronized skill data | `Skill`, `SkillContainer`, `SkillDataManager`; implementations live in the subpackages |
| `network` | Packet registration and distribution | `EpicFightNetworkManager`; `client/CP*` travels to the server, `server/SP*` travels to clients |
| `client/events/engine` | High-level client control and rendering coordination | `ControlEngine`, `RenderEngine` |
| `client/renderer` | Patched entity/item rendering, animation layers, trails, and shaders | `client/events/engine/RenderEngine`, `PatchedEntityRenderer`, `RenderingTool` |
| `events` | Global gameplay event subscribers | `CapabilityEvents`, `EntityEvents`, `PlayerEvents`, `WorldEvents` |
| `world/entity/eventlistener` | Fine-grained events owned by each patched player | `PlayerEventListener`, `EventTrigger`, and event classes such as `DealDamageEvent` |
| `api/data/reloader` | Reads datapack definitions and rebuilds runtime data | `SkillManager`, `ItemCapabilityReloadListener`, `MobPatchReloadListener` |
| `gameasset` | Java registrations for built-in animations, armatures, skills, sounds, and colliders | `Animations`, `Armatures`, `EpicFightSkills`, `EpicFightSounds` |
| `mixin` | Hooks at points where Fabric events cannot expose the required Minecraft behavior | `mixins.epicfight.json` is the complete mixin list |
| `forgecompat` | Small Forge-shaped compatibility layer used to keep shared/upstream code portable on Fabric | Treat this as platform plumbing, not gameplay code |
| `compat` | Optional integrations, isolated by mod | Each `*Compat` class should guard access to classes from its optional dependency |
| `config` | Common, server, and client configuration values | `CommonConfig`, `ServerConfig`, `ClientConfig` |
| `world/item`, `world/entity`, `world/effect`, `world/level` | Concrete registered game content | The `EpicFight*` registry class in each package points to its implementations |
| `server/commands` | `/epicfight`-related commands and arguments | `AnimatorCommand`, `PlayerModeCommand`, `PlayerSkillCommand`, `PlayerStaminaCommand` |

### Resource and datapack map

| Resource path | Consumed by | What changing it affects |
| --- | --- | --- |
| `resources/assets/epicfight/animmodels` | `AnimationManager`, mesh/armature loaders | Animation clips, armatures, and animated model geometry |
| `resources/assets/epicfight/item_skins` | `ItemSkinsReloadListener` | How held/equipped items are rendered on animated models |
| `resources/assets/epicfight/models`, `textures`, `shaders`, `particles`, `sounds` | client render/audio registrations | Visual and audio presentation |
| `resources/assets/epicfight/lang` and `tips` | Minecraft localization and Epic Fight tip UI | Player-facing text; add matching translation keys when adding content |
| `resources/data/epicfight/capabilities` | item and mob capability reloaders | Epic Fight behavior assigned to items and entities |
| `resources/data/minecraft/capabilities` | the same capability reloaders | Epic Fight behavior assigned to vanilla Minecraft items/entities |
| `resources/data/epicfight/skill_parameters` | `SkillManager` | Tunable values for registered skills without changing Java code |
| `resources/data/epicfight/recipes`, `loot_modifiers`, `damage_type`, `tags` | Minecraft data loaders and Epic Fight reloaders | Recipes, skill-book loot, damage semantics, and grouping tags |
| `resources/packs/epicfight_legacy` | built-in resource-pack registration | Optional legacy visuals |

### Where should a change go?

| Goal | Usually change |
| --- | --- |
| Add or tune a skill | A class under `skill/*`, its registration in `gameasset/EpicFightSkills.java`, and usually a `skill_parameters` JSON |
| Add a weapon type or combo | `world/capabilities/item/*` plus capability JSON; animation registrations may also be needed |
| Change when an attack can hit | `AttackAnimation`, its phase/state properties, or a collider class/preset |
| Change player combat behavior | `PlayerPatch`/`ServerPlayerPatch`; client input presentation belongs in `LocalPlayerPatch` or `ControlEngine` |
| Change a mob's combat AI | Its class under `world/capabilities/entitypatch/mob` and, when data-driven, the mob capability JSON |
| Add a packet | Add the `CP*` or `SP*` type and register it in the exact same order on both sides in `EpicFightNetworkManager` |
| Add a visual effect | A client renderer/particle/shader plus its registry call in `EpicFightFabricClientInitializer` |
| Hook an uncovered vanilla action | Prefer a Fabric callback; otherwise add the smallest possible mixin and list it in `mixins.epicfight.json` |
| Add optional mod support | A guarded module under `compat`; never reference optional-mod classes from an unconditional common entry point |

### Naming clues

- `*Patch`: Epic Fight state/behavior attached to a vanilla object.
- `CP*` / `SP*`: client-to-server request / server-to-client synchronization packet.
- `*ReloadListener`: rebuilds runtime objects when datapacks or resource packs reload.
- `EpicFight*`: usually the central registry for one kind of content.
- `Mixin*`: a narrow injection into Minecraft or an optional mod.
- `forgecompat`: a local adapter that imitates the part of a Forge API used by shared code.

When debugging, first decide which **logical side** owns the bad state, then locate the patch that owns it. From there,
follow either its `SkillContainer`, `Animator`, or packet handler. That route is usually much shorter than starting from
the renderer or a mixin.

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

## **Controls**

<details><summary>Tap to show/hide</summary>

- *Vanilla Basic Attack | Epic Fight Basic Attack*  
  <img src="https://i.imgur.com/e5pJkAV.png" alt="Vanilla Attack" width="157" height="162" /> <img src="https://i.imgur.com/V8HWA9i.png" alt="Epic Fight Attack" width="157" height="162" />

- *Dash Attack* (Attack + Sprinting)  
  <img src="https://i.imgur.com/XGuPrSE.png" alt="Dash Attack" width="157" height="162" /> <img src="https://i.imgur.com/Em7T2Kl.png" alt="Dash Attack Epic" width="157" height="162" />

- *Dodging* (Left Alt, requires a Dodge Skill)  
  <img src="https://i.imgur.com/RSV2eVc.png" alt="Dodging" width="272" height="187" />

- *Special Attack* (Press Attack key)  
  <img src="https://i.imgur.com/fAEgF3X.gif" alt="Special Attack" />

- *Toggle Epic Fight Mode* (optional, initial key: R)  
  <img src="https://i.imgur.com/2DW5saD.png" alt="Toggle Epic Fight Mode" width="423" height="242" />

  > Toggling Epic Fight mode allows the player to disable the Epic Fight player model, animations, and combat mechanics
  while in-game.

</details>

## **Abilities**

<details><summary>Tap to show/hide</summary>

### *Weapon & Armor*

![Weapon & Armor](https://i.imgur.com/AbdYsTu.png)

- **Armor Negation:** This percentage won't decrease the total damage by defense points.
- **Impact:** Increases the total stun time of the hit target.
- **Hit N enemies per swing:** Maximum number of hittable enemies per swing.
- ![Weight](https://i.imgur.com/4BRqNoF.png)
- **Weight:** Shortens the stun time when hit, increases stamina consumption of skills, and decreases attack speed.
  Items with high attack speed are affected more.
- **Stun Armor:** Increases the time between stuns.

---

### *The Special Attack*

![Special Attack](https://i.imgur.com/jdsZCPP.png)

Most weapons have a special attack. To use it, fill the special attack gauge by dealing damage to any entity with HP.
Special attacks are more powerful than basic and dash attacks. You can see the tooltip of a special attack by pressing
the special attack tooltip key (initial setting: **P**).

---

### *Enchantments*

#### **Sharpness**

Increases damage to every type of attack.

#### **Sweeping Edge**

Increases special attack damage (50% | 67% | 75%).

#### **Knockback**

Increases stun time.

---

### *Restrictions*

![Restrictions](https://i.imgur.com/QipRzp0.png)

- Two-handed weapons cannot be held in the offhand and disable offhand functionality.

---

### *Weapon Combination*

![Weapon Combination](https://i.imgur.com/PHVYrrq.png)

Some weapons have different attack styles depending on what is held in the offhand. Basic attack animations and special
attacks change accordingly. Try the various attack styles!

---

### *Stun*

![Stun](https://i.imgur.com/wWN4u82.png)

All animated attacks stun the target for a short time, including the player. You can't control your character during
this time. Stun Armor and Weight points help protect yourself.

- **Stun Armor & Weight:** Acquired by equipping armors. Higher-value armor grants more Stun Armor points.

---

### *Stamina & Skills*

![Stamina & Skills 1](https://i.imgur.com/kFMMpaR.png)  
![Stamina & Skills 2](https://i.imgur.com/FSnJN9z.png)

You can learn skills through the skill book (found in dungeon chests or dropped from hostile mobs). Skills are
classified into three types:

- **Dodge Skills:** Consume stamina; stamina is displayed in the bottom-right corner.
- **Guard Skills:** Consume stamina; block attacks like shields but have a short stun time.
- **Passive Skills:** Automatically applied when their conditions are met.

</details>

## **FAQ**

<details><summary>Tap to show/hide</summary>

- **Fabric port?**  
  Currently, there are no immediate plans for a Fabric port due to the extensive development involved.  
  It may be considered in the future.

- **Making different mods compatible**  
  Achieving compatibility can be complex and may require Java knowledge.  
  If you are a mod developer,
  refer to the [API Guide](https://epicfight-docs.readthedocs.io/API/Starting/) for detailed instructions.  
  Alternatively, you can explore the [Epic Fight Wiki](https://epicfight-docs.readthedocs.io/) for additional guidance.

- **Creating custom weapons or assigning weapon types to modded weapons**  
  Refer to the [Item Capability Guide](https://epicfight-docs.readthedocs.io/Guides/Weapons/page1/)  
  and the [Weapon Type Editor Guide](https://epicfight-docs.readthedocs.io/Guides/Weapons/page2/) for detailed
  instructions.

- **Patching custom modded entities to use Epic Fight animations and mechanics**  
  Refer to the [Custom Entity Datapack Guide](https://epicfight-docs.readthedocs.io/Guides/Entities/page1/) for
  instructions.  
  For advanced cases, Java code may be necessary to fully patch an entity.

- **Modded armor appears invisible or looks unusual**  
  Epic Fight changes the player model to support more complex animations.  
  To make armor display correctly, a compatibility resource pack is required.  
  If you're familiar with Blender or 3D modeling,
  check out this [Epic Fight guide](https://epicfight-docs.readthedocs.io/Guides/Armor/3Darmor_page1/) for detailed
  instructions.

- **Backport the latest Epic Fight update to older Minecraft versions?**  
  There are currently no plans to backport newer Epic Fight features to older Minecraft versions, due to the major
  code differences between versions and the maintenance burden it would create for developers.

- **Any plans to make a Bedrock version?**  
  Minecraft Bedrock is completely different from Java Edition.

- **New Translations?**  
  In the future, new translations will be added.
  Currently, only verified members can submit translations, so adding new languages takes time.

</details>

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

## **Support & Affiliates**

<center><a href="https://billing.sparkedhost.com/aff.php?aff=2724"><img src="https://i.imgur.com/kTgYcrh.pnghttps://i.imgur.com/kTgYcrh.png" alt="Modloader"></a></center>

<br>

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

<center><a title="Patreon" href="https://www.patreon.com/bePatron?u=53051224" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Yesssssman/epicfightmod/assets/77132244/7c517b51-581a-48dc-9130-aaad326dbcb4" alt="Patreon" width="150" height="150" /></a>&nbsp; &nbsp;<a title="Discord" href="https://discord.com/invite/NbAJwj8RHg" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Yesssssman/epicfightmod/assets/77132244/f3358cb9-f3cd-46e7-9ed0-a90bc2b1b188" alt="Discord" width="150" height="150" /></a>&nbsp; &nbsp;<a title="YouTube" href="https://www.youtube.com/@yesman4100" target="_blank" rel="noopener noreferrer"><img src="https://github.com/Yesssssman/epicfightmod/assets/77132244/3f2de855-e926-4eb9-a20c-4c6f44828250" alt="Youtube" width="150" height="150" /></a>&nbsp; &nbsp;<a href="https://epicfight-docs.readthedocs.io/en/latest/"><img src="https://github.com/Yesssssman/epicfightmod/assets/77132244/23220c47-c1e5-4e2b-82aa-876a86d7ed1a" alt="GitHub" width="150" height="150" /></a></center>

## 🔧 **Compatibility**

### **✅ Fully Supported and Compatible Mods**

<details><summary>Tap to show/hide</summary>

- [Epic Fight: Skill Tree](https://modrinth.com/mod/epic-fight-skill-tree)
- [Controlify](https://modrinth.com/mod/controlify) OR [Controlify: Forgified 1.20.1 (Unofficial)](https://modrinth.com/mod/controlify-forgified)
- [ParCool](https://modrinth.com/mod/parcool) (via [Epic x ParCool](https://modrinth.com/mod/official-epic-x-parcool))
- [Just Enough Items (JEI)](https://modrinth.com/mod/jei)
- [Sodium](https://modrinth.com/mod/sodium)
- [Iris Shaders](https://modrinth.com/mod/iris)
- [3D Skin Layers](https://modrinth.com/mod/3dskinlayers)
- [Vampirism](https://modrinth.com/mod/vampirism)
- [Werewolves](https://modrinth.com/mod/werewolves)
- [Shoulder Surfing Reloaded](https://modrinth.com/mod/shoulder-surfing-reloaded)
- [playerAnimator](https://modrinth.com/mod/playeranimator)
- [Geckolib](https://modrinth.com/mod/geckolib)
- [AzureLib](https://modrinth.com/mod/azurelib)
- [First-person Model](https://modrinth.com/mod/first-person-model)
- [Curios API](https://modrinth.com/mod/curios)
- [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll)
- [KubeJS](https://modrinth.com/mod/kubejs)

</details>

### **⚠️ Incompatible Mods**

<details><summary>Tap to show/hide</summary>

- **Optifine** (closed-source mod)
- [Controllable](https://www.curseforge.com/minecraft/mc-mods/controllable) —
  Consider [Controlify](https://modrinth.com/mod/controlify)

</details>

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

## **🐞 Bug Reports**

If you encounter any crashes or bugs in **Epic Fight**:

1. **Reproduce the issue** in a fresh Minecraft instance with as few mods as possible.
2. **Generate a crash/log report** using [mclo.gs](https://mclo.gs/).
3. **Submit the report** by sending the link along with detailed relevant information (e.g., Epic Fight version) to [**this GitHub repository**](https://github.com/Epic-Fight/epicfight/issues/new/choose).

> ⚠️ **Important:** Always reproduce the crash or bug with a **minimal set of mods**.  
> Sharing reports from instances with 100+ mods makes it nearly impossible to debug each mod individually due to the high volume of reports and limited resources.

**Note:** We only address issues and bugs that **originate from Epic Fight itself**.

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

## **Official Integrations**

### **Skill Tree**

The [Epic Fight: Skill Tree](https://modrinth.com/mod/epic-fight-skill-tree) is an official first-party addon
developed by the Epic Fight team to replace the **Skill Book Items** system with a **Skill Tree**,
providing a more RPG-focused experience.

<iframe width="560" height="315" src="https://www.youtube.com/embed/KtY_zayLzYw?si=62WHXDS6TV2bagbO&amp;start=116" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

### **ParCool**

The [Epic x ParCool](https://modrinth.com/mod/official-epic-x-parcool) is an official addon
developed in collaboration with the [ParCool](https://modrinth.com/mod/parcool) project authors.

<iframe width="560" height="315" src="https://www.youtube.com/embed/T-uMmLCYbn4?si=kY0vKLD2PNeAaN48" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

### **Controlify (Controller Support)**

In recent versions of Epic Fight, the Epic Fight team added an
official [Controlify](https://modrinth.com/mod/controlify) integration,
allowing you to use Epic Fight seamlessly with a controller,
with enhanced support for all input actions and GUI operations.

> **Tip:** If you're on Forge 1.20.1,
> use [Controlify: Forgified (Unofficial backport)](https://modrinth.com/mod/controlify-forgified) instead.
> Epic Fight's on-screen controller button guides are **not supported** on Minecraft 1.20.1.

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

## **Community addons**

These are community-made mods that add optional support for this mod.
They are **not officially supported** and may not be compatible with all versions.

<details><summary>Tap to show/hide</summary>

- [Epic Fight - Sword Soaring](https://www.curseforge.com/minecraft/mc-mods/sword-soaring)
- [Weapons of miracles](https://modrinth.com/mod/weapons-of-miracles)
- [Epic Fight: More Skill Slots](https://modrinth.com/mod/epic-fight-more-skill-slots)
- [P1nero's Epic Fight X Cataclysm](https://www.curseforge.com/minecraft/mc-mods/p1neros-epic-x-cataclysm)
- [P1nero's Epic Bow](https://www.curseforge.com/minecraft/mc-mods/p1neros-epic-bow)
- [Epic Fight - The Wraithon](https://www.curseforge.com/minecraft/mc-mods/epic-fight-wraithon)
- [Epic Fight : Dodge Parry Reward](https://www.curseforge.com/minecraft/mc-mods/epic-fight-dodge-parry-reward)
- [Epic Fight Nightfall](https://www.curseforge.com/minecraft/mc-mods/epicfight-nightfall)

</details>

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

## **📚 WIKI**

For detailed information on **Epic Fight**, including skills, recipes, API and Blender guides,
custom trails, weapon or
entity patches, armor fixes, resource pack integrations and more, visit
the [Epic Fight WIKI](https://epicfight-docs.readthedocs.io/).

<img src="https://i.imgur.com/CWsUfxt.jpg" alt="Bold Breakline" width="1344" height="48" />

## **Contributing**

For contribution guidelines,
please refer to the [Contributing Guide](https://github.com/Epic-Fight/epicfight/blob/1.21.1/CONTRIBUTING.md).

## **License**

- **Source Code (Java, Kotlin, C++, C, Groovy, etc.):** [GNU GPLv3](./LICENSE)
    - Applies only to the source code in this repository.

- **Assets (images, models, textures, sounds, translations, animations, native libraries, etc.):**
  [All Rights Reserved](./LICENSE-ASSETS)
    - Applies only to the mod assets (under `src/main/resources/assets`) in this repository.

### **You CAN**

- Use this mod in a modpack.
- Write your own code that uses this code as a dependency (such as addons, resource packs or datapacks).
- Submit Pull Requests to this repository.
- Fork and modify the code as long as modifications are not distributed publicly.
- Third-party redistribution is allowed only if all the following conditions are met:
    - All assets under `src/main/resources/assets` are removed and replaced with custom, original-made assets.
    - The mod and project use a different name to avoid confusion with Epic Fight.
    - Credit and a link to the original Epic Fight project are provided.
    - You grant the Epic Fight project a non-exclusive, royalty-free, worldwide license to use, copy, modify, and
      distribute your modifications, provided that appropriate credit or attribution is given to you.

### **You CANNOT**

- Redistribute ANY assets from this mod (located under `src/main/resources/assets`).
    - **Note:** You may replace all original assets with your own to redistribute your fork publicly.
- Sell addons, mods, or other derivative works that use any Epic Fight assets, including but not limited to animations,
  textures, sounds, or models.

### **Disclaimer**

<sub> 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
</sub>

To comply with the [Minecraft Essential guidelines](https://www.minecraft.net/en-us/usage-guidelines),
the following disclaimer is included:

> **NOT AN OFFICIAL MINECRAFT Epic Fight. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFTMOJANG
OR MICROSOFT.**
