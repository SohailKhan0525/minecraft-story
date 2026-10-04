import { world, system } from "@minecraft/server";
import { ActionFormData } from "@minecraft/server-ui";

const STAGE = "minecraftstory_stage";
const WORLD_MARK = "minecraftstory_bedrock_world_built";

function stage(p){return Number(p.getDynamicProperty(STAGE)??0)}
function setStage(p,n){p.setDynamicProperty(STAGE,n)}
function near(p,x,y,z,r=6){const a=p.location;return (a.x-x)**2+(a.y-y)**2+(a.z-z)**2<=r*r}
function tell(p,t){p.sendMessage("§8[§6Minecraft Story§8] §f"+t)}
function title(p,a,b=""){p.onScreenDisplay.setTitle(a,{stayDuration:100,fadeInDuration:5,fadeOutDuration:10,subtitle:b})}
function npc(d,name,x,y,z){const e=d.getEntities({type:"minecraft:villager"}).find(v=>v.nameTag===name);if(e)return e;const v=d.spawnEntity("minecraft:villager",{x,y,z});v.nameTag=name;return v}
function build(d){
 if(world.getDynamicProperty(WORLD_MARK))return;
 d.runCommand("fill -60 63 -50 60 63 38 grass_block");
 d.runCommand("fill -60 64 28 60 64 33 water"); d.runCommand("fill -60 64 27 60 64 27 sand");
 d.runCommand("fill -60 64 34 60 64 34 sand"); d.runCommand("fill 4 64 4 14 68 14 stone_bricks");
 d.runCommand("fill 6 65 6 12 67 12 air");
 for(const p of ["7 65 7","11 65 7","7 65 11"])d.runCommand("setblock "+p+" soul_fire");
 d.runCommand("fill 38 53 14 52 53 26 obsidian"); d.runCommand("fill 39 54 15 51 56 25 air");
 d.runCommand("setblock 45 56 20 crying_obsidian"); d.runCommand("setblock 45 57 20 amethyst_block");
 for(let i=0;i<8;i++)d.runCommand("setblock "+(39+i%2)+" "+(64-Math.floor(i/2))+" "+(-18-i)+" polished_deepslate_stairs");
 world.setDynamicProperty(WORLD_MARK,true);
}
async function choice(p,titleText,body,buttons){const f=new ActionFormData().title(titleText).body(body);buttons.forEach(b=>f.button(b));const r=await f.show(p);return r.canceled?null:r.selection}
async function mara(p){const n=await choice(p,"Mara Vale","“Did you see the light?”",["Yes. I saw it.","I don't remember.","I'm not sure."]);if(n===null)return;setStage(p,1);tell(p,"Mara: Then we start with the chapel. Follow the blue fire.")}
async function sera(p){const n=await choice(p,"The First Choice","Sera is trapped beyond the lower passage.",["Rescue Sera","Follow the archive"]);if(n===null)return;setStage(p,5);tell(p,n===0?"Sera: You came back. I won't forget that.":"Sera: So the mystery wins. Try not to die for it.");title(p,"THE OBSERVATORY","The Heart is waiting below.")}
async function finalChoice(p){const n=await choice(p,"The Heart of the Observatory","The black crystal speaks with your own voice.\n\n“Remember.”",["Seal the Heart","Touch the Heart","Try to destroy it"]);if(n===null)return;setStage(p,10);tell(p,["You sealed the Heart.","You touched the Heart.","You tried to destroy the Heart."][n]);title(p,"THE NIGHT IS NOT OVER","The mountains split. Four lights appear.");system.runTimeout(()=>credits(p),160)}
function spawnWave(p,count,name="Hollow Knight"){
 for(let i=0;i<count;i++){const e=p.dimension.spawnEntity("minecraft:zombie",{x:p.location.x+3+i,y:p.location.y,z:p.location.z+6});e.nameTag=name;e.addTag("minecraftstory_wave")}
}
function wavesAlive(p){return p.dimension.getEntities({tags:["minecraftstory_wave"]}).length}
function credits(p){
 title(p,"CREDITS","Story • World • Quests • Characters • Cinematics • Code");
 tell(p,"Chapter 1 complete. Chapter 2 is coming soon.");
 system.runTimeout(()=>{setStage(p,11);npc(p.dimension,"Mira",18,64,-4);p.teleport({x:17,y:65,z:-2},{dimension:p.dimension});title(p,"AFTER THE CREDITS","Mira");
 tell(p,"Mira: For someone who fell out of the sky, you're surprisingly bad at introductions.");
 tell(p,"Wanderer: I was distracted.");tell(p,"Mira: By the mountain? The impossible stars? The terrifying crystal?");
 tell(p,"Wanderer: Mostly by you laughing at me.");tell(p,"Mira: Good. I was hoping you'd notice.");
 tell(p,"Wanderer: Do you always talk to strangers like this?");tell(p,"Mira: Only the interesting ones.");
 system.runTimeout(()=>{title(p,"TO BE CONTINUED","CHAPTER 2 — COMING SOON");tell(p,"Mira: Stay a little longer. I still have questions.");setStage(p,12)},180)},300)
}
world.afterEvents.playerSpawn.subscribe(e=>{if(!e.initialSpawn)return;const p=e.player;if(p.getDynamicProperty(STAGE)!==undefined)return;setStage(p,0);build(p.dimension);npc(p.dimension,"Mara Vale",20,64,0);npc(p.dimension,"Elias Venn",39,64,-22);npc(p.dimension,"Sera Voss",38,64,-8);title(p,"THE NIGHT THE SKY BROKE","Follow the river toward Havenfall.");tell(p,"Wanderer: Where am I?")});
world.afterEvents.playerInteractWithEntity.subscribe(e=>{const p=e.player,t=e.target;if(t.typeId!=="minecraft:villager")return;if(t.nameTag==="Mara Vale"&&stage(p)===0)void mara(p);if(t.nameTag==="Sera Voss"&&stage(p)===4)void sera(p)});
system.runInterval(()=>{for(const p of world.getPlayers()){const s=stage(p);
 if(s===0&&near(p,20,64,0))void mara(p);
 else if(s===1&&near(p,9,65,9,8)){setStage(p,2);title(p,"BLUE FIRE","The flame is cold.");tell(p,"Three blue flames answer one another.")}
 else if(s===2&&near(p,39,64,-22)){setStage(p,3);title(p,"FIND ELIAS","The map should not exist.");tell(p,"Elias: I didn't find this. It found me.")}
 else if(s===3&&near(p,32,64,-25)){setStage(p,4);title(p,"BENEATH THE ROOTS","The staircase has appeared.");tell(p,"Sera: The forest disagrees with us being here.")}
 else if(s===4&&near(p,38,64,-8))void sera(p);
 else if(s===5&&near(p,35,55,-6)){setStage(p,6);title(p,"THE DOOR BENEATH THE WORLD","Recover the three relics.");tell(p,"Ash Lens recovered.")}
 else if(s===6&&near(p,41,55,-4)){setStage(p,7);tell(p,"Star-Iron Shard recovered.")}
 else if(s===7&&near(p,45,55,0)){setStage(p,8);tell(p,"Warden Seal recovered.")}
 else if(s===8&&near(p,42,55,6)){setStage(p,9);title(p,"THE HOLLOW KNIGHT","You are late.");tell(p,"Hollow Knight: You are late.");spawnWave(p,2);system.runTimeout(()=>{if(stage(p)===9&&!wavesAlive(p)){title(p,"THE HEART","Do not attack the memory.");spawnWave(p,2,"Memory Echo");setStage(p,9)}},80)}
 else if(s===9&&near(p,45,56,20)){const echoes=p.dimension.getEntities({tags:["minecraftstory_wave"]});if(echoes.length){tell(p,"The memory cannot be defeated. Stop attacking and touch the Heart.");echoes.forEach(e=>e.remove())}void finalChoice(p)}
 }}},20);
