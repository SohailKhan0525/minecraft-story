import { world, system } from "@minecraft/server";
import { ActionFormData } from "@minecraft/server-ui";

const STAGE = "minecraftstory_stage";
const PROGRESS = "minecraftstory_progress";
const CHECKPOINT = "minecraftstory_checkpoint";
const MEMORY_READY = "minecraftstory_memory_ready";
const VOICE_LAST = "minecraftstory_last_voice";
const WORLD_MARK = "minecraftstory_bedrock_world_built";
const WORLD_VERSION = "minecraftstory_bedrock_world_version";
const MEMORY_TAG = "minecraftstory_memory";
const VOICE_PREFIX = "minecraftstory:voice.";
const STORY_TAG_PREFIX = "minecraftstory_npc:";
const WAVE_TAG = "minecraftstory_wave";

const NPCS = [
  ["mara", "Mara Vale", 20, 64, 0],
  ["elias", "Elias Venn", 24, 64, 4],
  ["cael", "Brother Cael", 10, 64, 8],
  ["sera", "Sera Voss", 38, 64, -8],
  ["bram", "Bram the Baker", 28, 64, 8],
  ["nessa", "Nessa the Blacksmith", 0, 64, -8],
  ["pip", "Pip", 16, 64, 4],
  ["toma", "Toma", 14, 64, -3],
  ["lio", "Lio the Fisher", 18, 64, 24],
  ["renn", "Old Renn", -8, 64, 4]
];

const CHECKPOINTS = {
  0: {x:-12.5,y:65,z:35.5},
  1: {x:8,y:65,z:8},
  2: {x:25,y:65,z:11},
  3: {x:33,y:65,z:1},
  4: {x:36,y:65,z:-6},
  5: {x:35,y:65,z:-14},
  6: {x:39,y:65,z:-22},
  7: {x:38,y:65,z:-8},
  8: {x:35,y:56,z:-6},
  9: {x:41,y:56,z:-4},
  10:{x:45,y:56,z:0},
  11:{x:42,y:57,z:-4},
  12:{x:42,y:55,z:6},
  13:{x:45,y:56,z:14},
  14:{x:45,y:56,z:18},
  15:{x:45,y:56,z:20},
  16:{x:42,y:56,z:20},
  17:{x:18,y:65,z:-4},
  18:{x:18,y:65,z:-4}
};

function stage(p){ return Number(p.getDynamicProperty(STAGE) ?? 0); }
function setStage(p,n){ p.setDynamicProperty(STAGE,n); p.setDynamicProperty(PROGRESS,0); setCheckpoint(p,n); }
function progress(p){ return Number(p.getDynamicProperty(PROGRESS) ?? 0); }
function setProgress(p,n){ p.setDynamicProperty(PROGRESS,n); }
function near(p,x,y,z,r=6){ const a=p.location; return (a.x-x)**2+(a.y-y)**2+(a.z-z)**2<=r*r; }
function tell(p,t){ p.sendMessage("§8[§6Minecraft Story§8] §f"+t); }
function title(p,a,b=""){ p.onScreenDisplay.setTitle(a,{stayDuration:100,fadeInDuration:5,fadeOutDuration:10,subtitle:b}); }
function setCheckpoint(p,n){
  const c=CHECKPOINTS[n] ?? CHECKPOINTS[0];
  p.setDynamicProperty(CHECKPOINT, n);
  p.setDynamicProperty("minecraftstory_checkpoint_x", c.x);
  p.setDynamicProperty("minecraftstory_checkpoint_y", c.y);
  p.setDynamicProperty("minecraftstory_checkpoint_z", c.z);
}
function voice(p,scene){
  if(p.getDynamicProperty(VOICE_LAST)===scene)return;
  p.setDynamicProperty(VOICE_LAST,scene);
  try{ p.playSound(VOICE_PREFIX+scene,{volume:1.0,pitch:1.0}); } catch(_e){ tell(p,"Voice audio is unavailable; subtitles remain enabled."); }
}
async function choice(p,titleText,body,buttons){
  const f=new ActionFormData().title(titleText).body(body);
  buttons.forEach(b=>f.button(b));
  const r=await f.show(p);
  return r.canceled?null:r.selection;
}
function storyEntities(d,tag){
  return d.getEntities({type:"minecraft:villager",tags:[STORY_TAG_PREFIX+tag]});
}
function ensureNpc(d,id,name,x,y,z){
  const tag=STORY_TAG_PREFIX+id;
  const found=storyEntities(d,id);
  if(found.length){
    found.slice(1).forEach(e=>e.remove());
    const e=found[0];
    e.nameTag=name;
    if(distanceSq(e.location,{x,y,z})>64) e.teleport({x,y,z});
    return e;
  }
  const e=d.spawnEntity("minecraft:villager",{x,y,z});
  e.nameTag=name;
  e.addTag(tag);
  return e;
}
function distanceSq(a,b){ return (a.x-b.x)**2+(a.y-b.y)**2+(a.z-b.z)**2; }
function ensureNpcs(d){
  const completed=world.getPlayers().every(p=>stage(p)>=17);
  for(const [id,name,x,y,z] of NPCS){
    ensureNpc(d,id,name,x,y,z);
  }
  if(!completed){
    const epiloguePlayers=world.getPlayers().filter(p=>stage(p)===17);
    if(epiloguePlayers.length) ensureNpc(d,"mira","Mira",18,64,-4);
  }
}
function blockText(d,text,x,y,z,block="gold_block"){
  const glyphs={
    "C":["1111","1000","1000","1000","1000","1000","1111"],
    "H":["1001","1001","1001","1111","1001","1001","1001"],
    "A":["0110","1001","1001","1111","1001","1001","1001"],
    "P":["1110","1001","1001","1110","1000","1000","1000"],
    "T":["1111","0110","0110","0110","0110","0110","0110"],
    "E":["1111","1000","1000","1110","1000","1000","1111"],
    "R":["1110","1001","1001","1110","1010","1001","1001"],
    "1":["0110","1110","0110","0110","0110","0110","1111"],
    " ":[ "0000","0000","0000","0000","0000","0000","0000" ]
  };
  let cursor=x;
  for(const ch of text.toUpperCase()){
    const g=glyphs[ch]||glyphs[" "];
    for(let row=0;row<7;row++) for(let col=0;col<4;col++) if(g[row][col]==="1"){
      d.runCommand("setblock "+(cursor+col)+" "+(y+(6-row))+" "+z+" "+block);
    }
    cursor+=5;
  }
}
function patchWorldV3(d){
  d.runCommand("fill 34 54 -11 49 54 20 deepslate");
  d.runCommand("fill 34 55 -10 34 57 20 obsidian");
  d.runCommand("fill 49 55 -10 49 57 20 obsidian");
  d.runCommand("fill 34 58 -11 49 58 20 obsidian");
  d.runCommand("fill 35 55 -9 48 57 19 air");
  for(let i=0;i<20;i++){
    const y=63-Math.floor(i/2), z=-18+i;
    d.runCommand("setblock 38 "+y+" "+z+" polished_deepslate_stairs");
    d.runCommand("setblock 37 "+y+" "+z+" cobbled_deepslate");
    d.runCommand("setblock 39 "+y+" "+z+" cobbled_deepslate");
  }
  for(const [x,y,z,b] of [
    [25,64,11,"lectern"],[33,64,1,"tripwire_hook"],[36,64,-6,"chiseled_deepslate"],
    [35,64,-14,"polished_blackstone"],[39,64,-22,"cartography_table"],
    [32,64,-25,"chiseled_stone_bricks"],[37,64,-16,"chiseled_stone_bricks"],[41,64,-9,"chiseled_stone_bricks"],
    [38,54,-10,"cartography_table"],[35,55,-6,"gold_block"],[41,55,-4,"iron_block"],
    [45,55,0,"crying_obsidian"],[42,56,-4,"amethyst_block"],[42,55,6,"iron_bars"],
    [45,56,20,"crying_obsidian"],[45,57,20,"amethyst_block"]
  ]) d.runCommand("setblock "+x+" "+y+" "+z+" "+b);
  d.runCommand("fill -14 64 18 14 64 20 stone_bricks");
  blockText(d,"CHAPTER 1",-13,65,18,"gold_block");
  d.runCommand("setblock -14 64 17 glowstone");
  world.setDynamicProperty(WORLD_VERSION,3);
}
function build(d){
  const version=Number(world.getDynamicProperty(WORLD_VERSION)??0);
  if(world.getDynamicProperty(WORLD_MARK) && version>=3)return;
  if(world.getDynamicProperty(WORLD_MARK) && version<3){
    patchWorldV3(d);
    return;
  }
  d.runCommand("fill -60 63 -50 60 63 38 grass_block");
  d.runCommand("fill -60 64 28 60 64 33 water");
  d.runCommand("fill -60 64 27 60 64 27 sand");
  d.runCommand("fill -60 64 34 60 64 34 sand");
  d.runCommand("fill 4 64 4 14 68 14 stone_bricks");
  d.runCommand("fill 6 65 6 12 67 12 air");
  for(const p of ["7 65 7","11 65 7","7 65 11"]) d.runCommand("setblock "+p+" soul_fire");
  d.runCommand("fill 38 53 14 52 53 26 obsidian");
  d.runCommand("fill 39 54 15 51 56 25 air");
  d.runCommand("fill 34 54 -11 49 54 20 deepslate");
  d.runCommand("fill 34 55 -10 34 57 20 obsidian");
  d.runCommand("fill 49 55 -10 49 57 20 obsidian");
  d.runCommand("fill 34 58 -11 49 58 20 obsidian");
  d.runCommand("fill 35 55 -9 48 57 19 air");
  for(let i=0;i<12;i++){
    const y=63-Math.floor(i/2);
    const z=-18+i;
    d.runCommand("setblock 38 "+y+" "+z+" polished_deepslate_stairs");
    d.runCommand("setblock 37 "+y+" "+z+" cobbled_deepslate");
    d.runCommand("setblock 39 "+y+" "+z+" cobbled_deepslate");
  }
  for(const [x,y,z,b] of [
    [25,64,11,"lectern"],
    [33,64,1,"tripwire_hook"],
    [36,64,-6,"chiseled_deepslate"],
    [35,64,-14,"polished_blackstone"],
    [39,64,-22,"cartography_table"],
    [32,64,-25,"chiseled_stone_bricks"],
    [37,64,-16,"chiseled_stone_bricks"],
    [41,64,-9,"chiseled_stone_bricks"],
    [38,54,-10,"cartography_table"],
    [35,55,-6,"gold_block"],
    [41,55,-4,"iron_block"],
    [45,55,0,"crying_obsidian"],
    [42,56,-4,"amethyst_block"],
    [42,55,6,"iron_bars"],
    [45,56,20,"crying_obsidian"],
    [45,57,20,"amethyst_block"]
  ]) d.runCommand("setblock "+x+" "+y+" "+z+" "+b);
  d.runCommand("fill 39 54 15 51 56 25 air");
  d.runCommand("setblock 45 56 20 crying_obsidian");
  d.runCommand("setblock 45 57 20 amethyst_block");

  for(let i=0;i<8;i++) d.runCommand("setblock "+(39+i%2)+" "+(64-Math.floor(i/2))+" "+(-18-i)+" polished_deepslate_stairs");

  d.runCommand("fill -14 64 18 14 64 20 stone_bricks");
  blockText(d,"CHAPTER 1", -13, 65, 18, "gold_block");
  d.runCommand("setblock -14 64 17 glowstone");
  world.setDynamicProperty(WORLD_MARK,true);
  world.setDynamicProperty(WORLD_VERSION,3);
}
async function intro(p){
  voice(p,"havenfall");
  const n=await choice(p,"Mara Vale","“Did you see the light?”",["Yes. I saw it.","I don't remember.","I'm not sure."]);
  if(n===null)return;
  if(n===0)p.setDynamicProperty("minecraftstory_intro_choice","saw");
  if(n===1)p.setDynamicProperty("minecraftstory_intro_choice","missing");
  if(n===2)p.setDynamicProperty("minecraftstory_intro_choice","unsure");
  setStage(p,1);
  tell(p,"Mara: Then we start with the chapel. Follow the blue fire.");
}
async function seraChoice(p){
  voice(p,"first_choice");
  const n=await choice(p,"The First Choice","Sera is trapped beyond the lower passage.",["Rescue Sera","Follow the archive"]);
  if(n===null)return;
  p.setDynamicProperty("minecraftstory_path",n===0?"mercy":"knowledge");
  setStage(p,8);
  title(p,"THE OBSERVATORY","The three relics are waiting.");
}
async function finalChoice(p){
  voice(p,"ending");
  const n=await choice(p,"The Heart of the Observatory","The black crystal speaks with your own voice.\n\n“Remember.”",["Seal the Heart","Touch the Heart","Try to destroy it"]);
  if(n===null)return;
  p.setDynamicProperty("minecraftstory_ending",n);
  setStage(p,16);
  tell(p,["You sealed the Heart.","You touched the Heart.","You tried to destroy the Heart."][n]);
  title(p,"THE NIGHT IS NOT OVER","Havenfall is waiting.");
  system.runTimeout(()=>ending(p),160);
}
function spawnWave(p,count,name,tag=WAVE_TAG){
  for(let i=0;i<count;i++){
    const e=p.dimension.spawnEntity("minecraft:zombie",{x:p.location.x+3+(i%3)*1.7,y:p.location.y,z:p.location.z+5+Math.floor(i/3)*1.7});
    e.nameTag=name;
    e.addTag(tag);
  }
}
function waveAlive(p){
  return p.dimension.getEntities({tags:[WAVE_TAG]}).some(e=>distanceSq(e.location,p.location)<=900);
}
function ending(p){
  title(p,"CREDITS","Minecraft Story • Chapter 1");
  tell(p,"CHAPTER 1 COMPLETE");
  tell(p,"The Night the Sky Broke");
  tell(p,"Four lights have appeared beyond the mountains.");
  system.runTimeout(()=>postCredits(p),220);
}
function postCredits(p){
  setStage(p,17);
  ensureNpc(p.dimension,"mira","Mira",18,64,-4);
  p.teleport({x:17,y:65,z:-2},{dimension:p.dimension});
  voice(p,"post_credits");
  title(p,"AFTER THE CREDITS","Mira");
  tell(p,"Mira: For someone who fell out of the sky, you're surprisingly bad at introductions.");
  tell(p,"Wanderer: I was distracted.");
  tell(p,"Mira: By the mountain? The impossible stars? The terrifying crystal?");
  tell(p,"Wanderer: Mostly by you laughing at me.");
  tell(p,"Mira: Good. I was hoping you'd notice.");
  tell(p,"Wanderer: Do you always talk to strangers like this?");
  tell(p,"Mira: Only the interesting ones.");
  system.runTimeout(()=>{
    title(p,"TO BE CONTINUED","CHAPTER 2 — COMING SOON");
    tell(p,"Mira: Stay a little longer. I still have questions.");
    setStage(p,18);
  },220);
}
function respawnToCheckpoint(p){
  const n=Number(p.getDynamicProperty(CHECKPOINT) ?? 0);
  const c=CHECKPOINTS[n] ?? CHECKPOINTS[0];
  system.runTimeout(()=>p.teleport(c,{dimension:p.dimension}),2);
  system.runTimeout(()=>tell(p,"Chapter 1 checkpoint restored."),4);
}
function interactNpc(p,t){
  const tags=t.getTags();
  if(tags.includes(STORY_TAG_PREFIX+"mara") && stage(p)===0)void intro(p);
  else if(tags.includes(STORY_TAG_PREFIX+"cael") && stage(p)===1) {
    title(p,"BROTHER CAEL","The flame remembers.");
    tell(p,"Cael: Inspect all three cold blue flames.");
  }
  else if(tags.includes(STORY_TAG_PREFIX+"elias") && stage(p)===6) {
    setStage(p,7);
    tell(p,"Elias: I didn't find this. It found me.");
    title(p,"THE FIRST CHOICE","Sera is waiting below.");
    voice(p,"first_choice");
  }
  else if(tags.includes(STORY_TAG_PREFIX+"sera") && stage(p)===7)void seraChoice(p);
  else if(tags.includes(STORY_TAG_PREFIX+"mira") && stage(p)===17){
    title(p,"MIRA","Chapter 2 is still coming.");
    tell(p,"Mira: Stay a little longer. I still have questions.");
    voice(p,"post_credits");
  }
}
function inspectBlock(p,b){
  const s=stage(p);
  const x=b.location.x,y=b.location.y,z=b.location.z;

  if(s===1 && ((x===7&&y===65&&z===7)||(x===11&&y===65&&z===7)||(x===7&&y===65&&z===11))){
    const n=Math.min(3,progress(p)+1);
    setProgress(p,n); tell(p,"Cold blue flame inspected: "+n+"/3.");
    if(n>=3){ setStage(p,2); voice(p,"chapel"); title(p,"BLUE FIRE","The silence points toward Elias."); }
    return;
  }
  const clue=[25,33,36,35,39];
  const clueZ=[11,1,-6,-14,-22];
  if(s>=2&&s<=6){
    const idx=s-2;
    if(x===clue[idx]&&y===64&&z===clueZ[idx]){
      setStage(p,s+1);
      tell(p,"Clue "+(idx+1)+"/5 recovered.");
      if(s+1===6){ voice(p,"missing_sound"); title(p,"FIND ELIAS","The forest found the mapmaker."); }
      return;
    }
  }
  if(s===8&&x===35&&y===55&&z===-6){ setStage(p,9); tell(p,"Ash Lens recovered."); return; }
  if(s===9&&x===41&&y===55&&z===-4){ setStage(p,10); tell(p,"Star-Iron Shard recovered."); return; }
  if(s===10&&x===45&&y===55&&z===0){ setStage(p,11); tell(p,"Warden Seal recovered."); return; }
  if(s===11&&x===42&&y===56&&z===-4){ setStage(p,12); tell(p,"The ring recognizes all three relics."); return; }
  if(s===12&&x===42&&y===55&&z===6){
    setStage(p,13); voice(p,"door_below"); title(p,"THE HOLLOW KNIGHT","You are late."); tell(p,"Hollow Knight: You are late.");
    spawnWave(p,2,"Hollow Knight Guardian");
    return;
  }
  if(s===15&&x===45&&y===56&&z===20){
    p.dimension.getEntities({tags:[MEMORY_TAG]}).forEach(e=>e.remove());
    void finalChoice(p); return;
  }
}
function advanceStory(p){
  const s=stage(p);
  if(s===0&&near(p,20,64,0))void intro(p);
  else if(s===2&&near(p,25,64,11)) tell(p,"Inspect the first map clue.");
  else if(s===6&&near(p,39,64,-22)) tell(p,"Speak to Elias at the final clue.");
  else if(s===7&&near(p,38,64,-8))void seraChoice(p);
  else if(s===13&&!waveAlive(p.dimension)){
    setStage(p,14);
    voice(p,"heart");
    title(p,"THE MEMORY","Stop attacking. Something is waiting.");
    tell(p,"The Hollow Knight's memory stands before the crystal. Do not attack it.");
    const memory=p.dimension.spawnEntity("minecraft:armor_stand",{x:p.location.x,y:p.location.y,z:p.location.z+5});
    memory.nameTag="Memory of the Hollow Knight";
    memory.addTag(MEMORY_TAG);
    system.runTimeout(()=>{
      if(stage(p)!==14)return;
      setStage(p,15);
      p.setDynamicProperty(MEMORY_READY,true);
      title(p,"THE HEART","Touch the crying crystal.");
      tell(p,"The memory fades without a fight. Touch the crying crystal at the center.");
    },100);
  } else if(s===14&&!waveAlive(p)){
    setStage(p,15);
    p.setDynamicProperty(MEMORY_READY,true);
    title(p,"THE HEART","Touch the crying crystal.");
    tell(p,"Touch the crying crystal at the center.");
  } else if(s===15&&near(p,45,56,20,8)){
    tell(p,"Touch the crying crystal to make your final choice.");
  } else if(s===16){
    // Ending is driven by finalChoice.
  }
}
world.afterEvents.playerSpawn.subscribe(e=>{
  const p=e.player;
  build(p.dimension);
  ensureNpcs(p.dimension);
  if(e.initialSpawn){
    if(p.getDynamicProperty(STAGE)===undefined){
      setStage(p,0);
      p.setDynamicProperty(VOICE_LAST,"");
      setCheckpoint(p,0);
      p.teleport({x:13.5,y:65,z:5.5});
      voice(p,"cold_open");
      title(p,"CHAPTER 1","THE NIGHT THE SKY BROKE");
      tell(p,"Start at Havenfall. Mara Vale is waiting ahead.");
      tell(p,"The forest has gone silent.");
    } else {
      title(p,"CHAPTER 1","Continue your story.");
    }
  } else {
    respawnToCheckpoint(p);
  }
});
world.afterEvents.playerInteractWithEntity.subscribe(e=>{
  if(e.target.typeId!=="minecraft:villager")return;
  interactNpc(e.player,e.target);
});
world.afterEvents.playerInteractWithBlock.subscribe(e=>inspectBlock(e.player,e.block));
system.runInterval(()=>{
  ensureNpcs(world.getDimension("overworld"));
  for(const p of world.getPlayers()){
    if(stage(p)>=18)continue;
    build(p.dimension);
    advanceStory(p);
    if(stage(p)===13 && !waveAlive(p))advanceStory(p);
  }
},20);
