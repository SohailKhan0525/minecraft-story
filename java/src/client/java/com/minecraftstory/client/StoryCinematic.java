package com.minecraftstory.client;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public final class StoryCinematic {
 private static String title="", subtitle=""; private static int ticks;
 private StoryCinematic(){}
 public static void play(String t,String s,int d){title=t==null?"":t;subtitle=s==null?"":s;ticks=Math.max(1,d);}
 public static void register(){net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.attachElementBefore(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CHAT,com.minecraftstory.MinecraftStory.id("story_cinematic"),StoryCinematic::extract);}
 private static void extract(GuiGraphicsExtractor g,DeltaTracker dt){if(ticks<=0)return;Minecraft m=Minecraft.getInstance();if(m.player==null)return;int w=m.getWindow().getGuiScaledWidth(),h=m.getWindow().getGuiScaledHeight();g.fill(0,0,w,28,0xDD000000);g.fill(0,h-28,w,h,0xDD000000);if(!title.isBlank()){int tw=m.font.width(title);g.text(m.font,Component.literal(title),(w-tw)/2,h/2-14,0xFFFFFFFF,true);}if(!subtitle.isBlank()){String[] lines=subtitle.split("\\n"); int y=h/2-2; for(String line:lines){int sw=m.font.width(line);g.text(m.font,Component.literal(line),(w-sw)/2,y,0xFFE0E0E0,false); y+=12;}}ticks--;}
}
