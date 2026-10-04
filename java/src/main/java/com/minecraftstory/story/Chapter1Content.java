package com.minecraftstory.story;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public final class Chapter1Content {
    public record Line(String speaker, String text, String mood, boolean optional, String condition) {}
    public record Scene(String id, String title, int targetSeconds, List<Line> lines) {}
    public record Npc(String id, String name, String role, String voiceStyle, List<String> ambient) {}
    public record Quest(String id, String title, String objective, int targetMinutes) {}

    private static final Map<String, Scene> SCENES = new LinkedHashMap<>();

    static {
        scene("cold_open", "The Sky", 180,
            l("Narrator","The forest was loud enough to feel alive.","quiet"),
            l("Narrator","Wolves hunted. Birds argued. Water worried at the stones.","quiet"),
            l("Wanderer","...Where am I?","confused"),
            l("Narrator","Then every sound stopped.","horror"),
            l("Wanderer","What was that?","fear"),
            l("Narrator","A white wound opened across the sky.","awe"),
            l("Narrator","Something fell through it.","awe"),
            l("Narrator","The impact came without thunder.","horror"),
            l("Unknown Voice","Not yet.","distant"),
            l("Wanderer","Who said that?","fear")
        );

        scene("havenfall", "A Village That Heard Something", 360,
            l("Mara","Stop there. Hands where I can see them.","guarded"),
            l("Wanderer","I don't remember how I got here.","disoriented"),
            l("Mara","Convenient.","dry"),
            l("Wanderer","I remember a river. A light. Then nothing.","uncertain"),
            l("Mara","Did you see the light?","serious"),
            l("Elias","Mara! You can't interrogate every person who walks in.","urgent"),
            l("Mara","I can if they arrive from the wrong direction.","dry"),
            l("Elias","He arrived from a river. Rivers are famously terrible at directions.","bright"),
            l("Bram","Does the stranger want bread?","cheerful",true),
            l("Mara","Bram, nobody asked.","tired",true),
            l("Bram","That's how bread gets lonely.","dramatic",true),
            l("Nessa","If he has a sword, I want to know where he got it.","blunt",true),
            l("Nessa","I repair swords. I do not explain mysteries.","flat",true),
            l("Pip","I saw the star first.","excited",true),
            l("Toma","It was definitely a glowing chicken.","confident",true),
            l("Pip","Chickens don't glow.","flat",true),
            l("Toma","They do now.","proud",true),
            l("Lio","The fish are gone.","quiet",true),
            l("Old Renn","Quiet rivers are never good rivers.","wise",true),
            l("Lio","You said that when your roof collapsed.","dry",true),
            l("Old Renn","And was I wrong?","proud",true),
            l("Mara","Come inside. Before something else falls out of the sky.","serious")
        );

        scene("chapel", "The Blue Fire", 300,
            l("Cael","Do not touch the flame.","urgent"),
            l("Wanderer","It's cold.","surprised"),
            l("Cael","I know.","worried"),
            l("Wanderer","That's not possible.","confused"),
            l("Cael","A great many things became possible last night.","sad"),
            l("Cael","May I see your hand?","careful"),
            l("Wanderer","Why?","guarded"),
            l("Cael","Because I know that mark.","shaken"),
            l("Wanderer","Where have you seen it?","urgent"),
            l("Cael","In a book that should have been burned.","quiet"),
            l("Cael","And on a door beneath this village.","ominous"),
            l("Unknown Voice","Not yet.","distant"),
            l("Wanderer","You heard that.","alarmed"),
            l("Cael","I heard nothing.","fearful"),
            l("Wanderer","Then why are you afraid?","quiet"),
            l("Cael","Because I know when silence is listening.","ominous")
        );

        scene("missing_sound", "The Missing Sound", 240,
            l("Mara","Everyone stop.","commanding"),
            l("Elias","What?","confused"),
            l("Mara","Listen.","tense"),
            l("Elias","I am listening.","confused"),
            l("Mara","No. You're talking.","dry"),
            l("Elias","Oh.","embarrassed"),
            l("Narrator","The village had lost its sound.","horror"),
            l("Wanderer","I can't hear my own footsteps.","afraid"),
            l("Mara","Where is Elias?","horror"),
            l("Narrator","The place beside you was empty.","horror"),
            l("Cael","The forest took him.","quiet"),
            l("Mara","Then we take him back.","determined")
        );

        scene("silent_forest", "The Silent Forest", 300,
            l("Mara","Stay close.","commanding"),
            l("Wanderer","There's no birds.","uneasy"),
            l("Sera","You two always walk this loudly?","dry"),
            l("Mara","Who are you?","alert"),
            l("Sera","Someone who would like to leave.","blunt"),
            l("Sera","Unfortunately, the forest disagrees.","dry"),
            l("Wanderer","You're injured.","concerned"),
            l("Sera","Observant. That's either useful or irritating.","dry"),
            l("Mara","Where is Elias?","urgent"),
            l("Sera","The boy with the map? He went down.","grim"),
            l("Wanderer","Down where?","urgent"),
            l("Sera","The staircase that wasn't there yesterday.","afraid"),
            l("Sera","I hate magical architecture.","annoyed",true),
            l("Elias","I didn't find this!","distant"),
            l("Elias","It found me!","distant"),
            l("Elias","If anyone has a plan, now would be fantastic.","nervous")
        );

        scene("observatory", "Beneath the Roots", 420,
            l("Elias","Look at this wall.","awed"),
            l("Wanderer","Those are stars.","quiet"),
            l("Elias","No. They're positions.","excited"),
            l("Mara","Positions of what?","skeptical"),
            l("Elias","That's the problem.","worried"),
            l("Cael","The Observatory of the Twelve.","ominous"),
            l("Wanderer","You knew this place existed.","accusing"),
            l("Cael","I knew the warning existed.","guarded"),
            l("Mara","You could have mentioned that earlier.","angry"),
            l("Cael","You were busy threatening a stranger.","dry",true),
            l("Mara","Fair.","flat",true),
            l("Elias","There are twelve seats.","curious"),
            l("Wanderer","How many are occupied?","uneasy"),
            l("Elias","Eleven.","quiet"),
            l("Sera","That's not comforting.","dry",true),
            l("Wanderer","What's missing?","serious"),
            l("Elias","A person.","afraid"),
            l("Narrator","A city appeared beneath a red sky.","vision"),
            l("Narrator","An army knelt before a black star.","vision"),
            l("Wanderer","That person...","shaken"),
            l("Cael","You know them.","quiet"),
            l("Wanderer","I don't.","defensive"),
            l("Cael","Your hand disagrees.","grim"),
            l("Sera","If the ruin starts whispering, we leave.","dry",true)
        );

        scene("first_choice", "The First Choice", 300,
            l("Sera","The lower passage is collapsing.","urgent"),
            l("Mara","Sera is still inside.","determined"),
            l("Wanderer","Then we go back.","firm"),
            l("Elias","Or we take the archive route.","conflicted"),
            l("Elias","It could explain the star.","excited"),
            l("Mara","And Sera could die while we read.","angry"),
            l("Sera","For the record, I vote against dying.","dry"),
            l("Sera","You came back.","surprised"),
            l("Wanderer","Both can be true.","dry",true),
            l("Sera","If I survive, I'm charging you for the rescue.","deadpan",true),
            l("Mara","You're charging us?","confused",true),
            l("Sera","Emotional damages.","deadpan",true),
            l("Mara","You were already emotional.","dry",true),
            l("Sera","Exactly. Expensive.","deadpan",true)
        );

        scene("door_below", "The Door Beneath the World", 360,
            l("Elias","Ash Lens goes here.","focused"),
            l("Mara","Star-Iron there.","focused"),
            l("Cael","And the Warden Seal in the center.","quiet"),
            l("Wanderer","You seem very sure.","suspicious"),
            l("Cael","I am very afraid.","honest"),
            l("Sera","Finally, someone speaking sense.","dry",true),
            l("Elias","If the mechanism opens, don't touch anything.","warning"),
            l("Sera","That instruction is aimed directly at you.","dry",true),
            l("Elias","I have touched one mysterious object.","defensive",true),
            l("Mara","You touched the map.","flat",true),
            l("Elias","It was a very interesting map.","sheepish",true),
            l("Narrator","The door opened downward.","ominous"),
            l("Wanderer","There's no bottom.","quiet"),
            l("Hollow Knight","You are late.","unnatural"),
            l("Mara","Who are you?","alert"),
            l("Hollow Knight","A promise.","unnatural"),
            l("Wanderer","Late for what?","confused"),
            l("Hollow Knight","Remembering.","unnatural")
        );

        scene("heart", "The Heart of the Observatory", 420,
            l("Elias","This is beautiful.","awed"),
            l("Sera","That's a dangerous thing to say underground.","dry",true),
            l("Mara","Eleven seats.","quiet"),
            l("Cael","And the twelfth is empty.","grim"),
            l("Wanderer","No.","shaken"),
            l("Wanderer","It isn't empty.","afraid"),
            l("Narrator","A black crystal pulsed in the center.","horror"),
            l("Black Crystal","Remember.","distant"),
            l("Wanderer","Remember what?","afraid"),
            l("Black Crystal","Yourself.","distant"),
            l("Elias","Your scar is glowing.","terrified"),
            l("Sera","I am officially done with glowing people.","dry",true),
            l("Mara","Everyone back.","commanding"),
            l("Warden of Deep","RETURN.","layered"),
            l("Warden of Deep","THE COMMAND REMAINS.","layered"),
            l("Wanderer","I never gave you a command.","defiant"),
            l("Warden of Deep","YOU DID.","layered"),
            l("Mara","Then give it another one.","determined"),
            l("Sera","Mara, that's not how ancient monsters work.","dry",true),
            l("Mara","It was worth trying.","flat",true),
            l("Elias","I have a different idea.","nervous"),
            l("Wanderer","Please tell me it is good.","dry",true),
            l("Elias","No.","honest"),
            l("Wanderer","Wonderful.","flat",true)
        );

        scene("ending", "The Night Is Not Over", 180,
            l("Narrator","The crystal screamed without making a sound.","horror"),
            l("Wanderer","Stay back!","urgent"),
            l("Mara","What did you do?","shaken"),
            l("Wanderer","I chose.","quiet"),
            l("Elias","The sky...","awed"),
            l("Sera","There are four lights.","afraid"),
            l("Cael","That isn't a star.","grim"),
            l("Mara","What is it?","urgent"),
            l("Cael","A signal.","quiet"),
            l("Narrator","The mountains split.","horror"),
            l("Unknown Voice","The first one has awakened.","distant"),
            l("Unknown Voice","No. The first one has returned.","distant"),
            l("Elias","I really miss boring.","exhausted",true),
            l("Sera","You were never boring.","dry",true),
            l("Mara","Nobody is sleeping tonight.","tired",true),
            l("Bram","I have bread.","cheerful",true),
            l("Mara","That may be the most reassuring thing anyone has said.","relieved",true)
        );
    }

    private static Line l(String speaker, String text, String mood) { return new Line(speaker,text,mood,false,"always"); }
    private static Line l(String speaker, String text, String mood, boolean optional) { return new Line(speaker,text,mood,optional,"always"); }

    private static void scene(String id, String title, int seconds, Line... lines) {
        SCENES.put(id, new Scene(id,title,seconds,List.of(lines)));
    }

    public static Scene scene(String id) {
        Scene s = SCENES.get(id);
        if (s == null) throw new IllegalArgumentException("Unknown scene: " + id);
        return s;
    }

    public static List<Scene> scenes() { return List.copyOf(SCENES.values()); }
    public static int authoredLines() { return SCENES.values().stream().mapToInt(s -> s.lines().size()).sum(); }
    public static int dialogueSeconds() { return SCENES.values().stream().mapToInt(Scene::targetSeconds).sum(); }

    public static List<Npc> npcs() {
        return List.of(
            new Npc("mara","Mara Vale","warden","low, controlled, tired authority",
                List.of("Nobody leaves the north road alone.","Elias, stop climbing things.","If the bell rings twice, wake me.")),
            new Npc("elias","Elias Venn","mapmaker apprentice","fast, curious, breathless",
                List.of("Maps are just arguments with geography.","I have a plan. It is approximately sixty percent terrible.")),
            new Npc("cael","Brother Cael","chapel keeper","soft, measured, secretive",
                List.of("Some bells call people. Some bells call things.","Do not confuse silence with peace.")),
            new Npc("sera","Sera Voss","expedition survivor","dry, blunt, skeptical",
                List.of("I have survived worse. I have also made better decisions.","If the ruin starts whispering, we leave.")),
            new Npc("bram","Bram the Baker","baker and professional worrier","warm, dramatic",
                List.of("The end of the world starts when everyone forgets breakfast.","Emergency rolls. Two for every hero.")),
            new Npc("nessa","Nessa the Blacksmith","blacksmith","blunt, practical, dry",
                List.of("If it sparks, it is either useful or dangerous.","I repair swords. I do not explain mysteries.")),
            new Npc("pip","Pip","child","excited, conspiratorial",
                List.of("I saw the star first.","The forest is pretending to be quiet.")),
            new Npc("toma","Toma","child","confident, ridiculous",
                List.of("It was definitely a glowing chicken.","If monsters come, I will negotiate.")),
            new Npc("renn","Old Renn","retired guard","slow, gravelly",
                List.of("I've seen strange things. Most were raccoons.","If the mountain opens, don't stand underneath it.")),
            new Npc("lio","Lio the Fisher","fisher","quiet, observant",
                List.of("The river knows when something is wrong.","No fish today."))
        );
    }

    public static List<Quest> quests() {
        return List.of(
            new Quest("bell","A Bell Before Breakfast","Reach Havenfall and speak with Mara.",5),
            new Quest("blue_fire","Blue Fire","Investigate the cold blue flame in the chapel.",6),
            new Quest("elias","Find Elias","Search the village and follow Elias's trail.",7),
            new Quest("roots","Beneath the Roots","Enter the revealed staircase and recover the Observatory map.",10),
            new Quest("door","The Door Beneath the World","Solve the Observatory mechanism and open the lower door.",8),
            new Quest("knight","The Hollow Knight","Survive the guardian encounter.",3),
            new Quest("heart","The Heart of the Observatory","Confront the Warden and decide the crystal's fate.",8),
            new Quest("night","The Night Is Not Over","Escape and witness the four lights.",4)
        );
    }
}
