import { world, system } from "@minecraft/server";
import { ActionFormData } from "@minecraft/server-ui";

const STAGE = "minecraftstory_stage";
const WORLD_MARK = "minecraftstory_bedrock_world_built";
const NAMES = {
  mara: "Mara Vale",
  elias: "Elias Venn",
  sera: "Sera Voss",
  mira: "Mira"
};

function stage(player) {
  return Number(player.getDynamicProperty(STAGE) ?? 0);
}

function setStage(player, value) {
  player.setDynamicProperty(STAGE, value);
}

function near(player, x, y, z, radius = 5) {
  const dx = player.location.x - x;
  const dy = player.location.y - y;
  const dz = player.location.z - z;
  return dx * dx + dy * dy + dz * dz <= radius * radius;
}

function tell(player, text) {
  player.sendMessage("§8[§6Minecraft Story§8] §f" + text);
}

function title(player, titleText, subtitle = "") {
  player.onScreenDisplay.setTitle(titleText, {
    stayDuration: 100,
    fadeInDuration: 5,
    fadeOutDuration: 10,
    subtitle: subtitle
  });
}

function findNamedVillager(dimension, name) {
  return dimension.getEntities({ type: "minecraft:villager" }).find(entity => entity.nameTag === name);
}

function spawnNpc(dimension, name, x, y, z) {
  const existing = findNamedVillager(dimension, name);
  if (existing) return existing;
  const npc = dimension.spawnEntity("minecraft:villager", { x, y, z });
  npc.nameTag = name;
  return npc;
}

function buildStarterWorld(dimension) {
  if (world.getDynamicProperty(WORLD_MARK)) return;
  dimension.runCommand("fill -12 63 -6 52 63 34 grass_block");
  dimension.runCommand("fill -12 64 28 52 64 33 water");
  dimension.runCommand("fill -12 64 27 52 64 27 sand");
  dimension.runCommand("fill -12 64 34 52 64 34 sand");
  dimension.runCommand("fill 4 64 4 14 68 14 stone_bricks");
  dimension.runCommand("fill 6 65 6 12 67 12 air");
  dimension.runCommand("fill 7 65 7 7 65 7 soul_fire");
  dimension.runCommand("fill 11 65 7 11 65 7 soul_fire");
  dimension.runCommand("fill 7 65 11 7 65 11 soul_fire");
  dimension.runCommand("fill 42 54 17 48 54 23 obsidian");
  dimension.runCommand("setblock 45 55 20 crying_obsidian");
  dimension.runCommand("setblock 45 56 20 amethyst_block");
  world.setDynamicProperty(WORLD_MARK, true);
}

async function maraDialogue(player) {
  const form = new ActionFormData()
    .title("Mara Vale")
    .body("Mara studies the scar on your hand.\n\n“Did you see the light?”")
    .button("Yes. I saw it.")
    .button("I don't remember.")
    .button("I'm not sure.");
  const result = await form.show(player);
  if (result.canceled) return;
  setStage(player, 1);
  tell(player, "Mara: Then we start with the chapel. Follow the blue fire.");
}

async function seraChoice(player) {
  const form = new ActionFormData()
    .title("The First Choice")
    .body("Sera is trapped beyond the lower passage. The archive route could reveal the truth.")
    .button("Rescue Sera")
    .button("Follow the archive");
  const result = await form.show(player);
  if (result.canceled) return;
  setStage(player, 4);
  tell(player, result.selection === 0
    ? "Sera: You came back. I won't forget that."
    : "Sera: So the mystery wins. Try not to die for it.");
  title(player, "THE OBSERVATORY", "The Heart is waiting below.");
}

async function finalChoice(player) {
  const form = new ActionFormData()
    .title("The Heart of the Observatory")
    .body("The black crystal speaks with your own voice.\n\n“Remember.”")
    .button("Seal the Heart")
    .button("Touch the Heart")
    .button("Try to destroy it");
  const result = await form.show(player);
  if (result.canceled) return;
  setStage(player, 5);
  const ending = ["You sealed the Heart.", "You touched the Heart.", "You tried to destroy the Heart."][result.selection ?? 0];
  tell(player, ending);
  title(player, "THE NIGHT IS NOT OVER", "The mountains split. Four lights appear.");
  system.runTimeout(() => credits(player), 120);
}

function credits(player) {
  title(player, "CREDITS", "Story • World • Quests • Characters • Cinematics • Code");
  tell(player, "Chapter 1 complete. Chapter 2 is coming soon.");
  system.runTimeout(() => {
    setStage(player, 6);
    const mira = spawnNpc(player.dimension, NAMES.mira, 18, 64, -4);
    player.teleport({ x: 17, y: 65, z: -2 }, { dimension: player.dimension });
    title(player, "AFTER THE CREDITS", "Mira");
    tell(player, "Mira: For someone who fell out of the sky, you're surprisingly bad at introductions.");
    tell(player, "Wanderer: I was distracted.");
    tell(player, "Mira: By the mountain? The impossible stars? The terrifying crystal?");
    tell(player, "Wanderer: Mostly by you laughing at me.");
    tell(player, "Mira: Good. I was hoping you'd notice.");
    system.runTimeout(() => {
      title(player, "TO BE CONTINUED", "CHAPTER 2 — COMING SOON");
      tell(player, "Mira: Stay a little longer. I still have questions.");
    }, 100);
  }, 240);
}

world.afterEvents.playerSpawn.subscribe(event => {
  if (!event.initialSpawn) return;
  const player = event.player;
  if (player.getDynamicProperty(STAGE) !== undefined) return;
  setStage(player, 0);
  buildStarterWorld(player.dimension);
  spawnNpc(player.dimension, NAMES.mara, 20, 64, 0);
  spawnNpc(player.dimension, NAMES.elias, 39, 64, -22);
  spawnNpc(player.dimension, NAMES.sera, 38, 64, -8);
  title(player, "THE NIGHT THE SKY BROKE", "Follow the river toward Havenfall.");
  tell(player, "Wanderer: Where am I?");
});

world.afterEvents.playerInteractWithEntity.subscribe(event => {
  const player = event.player;
  const target = event.target;
  if (target.typeId !== "minecraft:villager") return;
  if (target.nameTag === NAMES.mara && stage(player) === 0) {
    void maraDialogue(player);
  }
});

system.runInterval(() => {
  for (const player of world.getPlayers()) {
    const current = stage(player);
    if (current === 0 && near(player, 20, 64, 0, 6)) {
      void maraDialogue(player);
    } else if (current === 1 && near(player, 9, 65, 9, 8)) {
      setStage(player, 2);
      title(player, "BLUE FIRE", "The flame is cold.");
      tell(player, "The chapel's three blue flames answer one another.");
    } else if (current === 2 && near(player, 39, 64, -22, 6)) {
      setStage(player, 3);
      title(player, "FIND ELIAS", "The map should not exist.");
      tell(player, "Elias: I didn't find this. It found me.");
    } else if (current === 3 && near(player, 38, 64, -8, 6)) {
      void seraChoice(player);
    } else if (current === 4 && near(player, 45, 56, 20, 7)) {
      void finalChoice(player);
    }
  }
}, 20);
