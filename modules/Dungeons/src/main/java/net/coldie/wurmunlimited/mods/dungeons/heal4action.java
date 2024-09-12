package net.coldie.wurmunlimited.mods.dungeons;


import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.players.Player;


import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class heal4action implements WurmServerMod, ItemTypes, MiscConstants, ModAction, BehaviourProvider, ActionPerformer {

	public static short actionId;
	static ActionEntry actionEntry;

	public heal4action() {
		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(actionId, dungeonmain.currencyname4+" Heal", dungeonmain.currencyname4+" Healing", new int[]{ 
				}); 
		ModActions.registerAction(actionEntry);
	}

	@Override
	public BehaviourProvider getBehaviourProvider() {
		return this;
	}

	@Override
	public ActionPerformer getActionPerformer() {
		return this;
	}

	@Override
	public short getActionId() {
		return actionId;
	}
	
	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Creature target) {
		int used = source.getTemplateId();
		if (dungeonmain.currency4enabled && used == dungeonmain.statuettetemplate4 && target instanceof Player) {
				return (List<ActionEntry>) Arrays.asList(actionEntry);
			} else {
				return null;
			}
	}
	
	
	@Override
	public boolean action(Action act, Creature performer, Item source, Creature target, short action, float counter) {
		int used = source.getTemplateId();
		Float X = target.getPosX()/4;
		Float Y = target.getPosY()/4;
        if (dungeonmain.currency4enabled){
            if (dungeonmain.zone4minx <= X && X <= dungeonmain.zone4maxx && dungeonmain.zone4miny <= Y && Y <= dungeonmain.zone4maxy){
				if (used == dungeonmain.statuettetemplate4 && target instanceof Player) {
					if (target.getBody().getWounds() != null && target.getBody().getWounds().getWounds().length > 0) {
			          Connection dbcon = null;
			  	      PreparedStatement ps = null;
			  	      ResultSet rs = null;
			  	      long lastheal = 0;
			  	      try
			  	      {	      
			  	      dbcon = ModSupportDb.getModSupportDb();
			  	      ps = dbcon.prepareStatement("SELECT * FROM DungeonCurrency WHERE name = \""+target.getName()+"\"");
			  	      rs = ps.executeQuery();
			  	      lastheal = rs.getLong("LastHealed");
			  	      rs.close();
			  	      ps.close();
			  	      dbcon.close();
			  	    }
			  	      catch (SQLException e) {
			  	          throw new RuntimeException(e);
			  	        }
			  	    if (System.currentTimeMillis()-lastheal > dungeonmain.healcooldown4*1000){
						int power = dungeonmain.healpower4;
						int w4 = 0;
						int numhealed = 0;
						int numfullhealed = 0;
				        Wound[] wounds4 = target.getBody().getWounds().getWounds();
				        if (target.getBody().getWounds() != null && ( wounds4.length > 0)) {
					        	while (w4 < wounds4.length){
					            if (wounds4[w4].getSeverity() / 1000.0f < (float)power) {
					                wounds4[w4].heal();
					                numfullhealed = numfullhealed+1;
					                }else{
					            wounds4[w4].modifySeverity((- power) * 1000);
					            numhealed = numhealed + 1;
					            }
					            ++w4;}
				        	
				        		//add time to char healed in db
			                	Connection dbcon4 = null;
							      PreparedStatement ps4 = null;	    	
							      try
							      {
								      dbcon4 = ModSupportDb.getModSupportDb();
								      ps4 = dbcon4.prepareStatement("UPDATE DungeonCurrency "
								      + "SET LastHealed = "+System.currentTimeMillis()+" WHERE name = \""+target.getName()+"\"");
								      ps4.executeUpdate();
								      ps4.close(); 
								      dbcon4.close();
							      }
						      catch (SQLException e) {
						          throw new RuntimeException(e);
						        }
							      performer.getCommunicator().sendSafeServerMessage("Target "+target.getName()+": "+numfullhealed+" fully healed, "+numhealed+" partially healed wounds.");  
				        	}else{
				        		performer.getCommunicator().sendSafeServerMessage(target.getName()+" has no wounds to heal.");
				        	}
				        	
			  	    }else{
			  	    long timeleft = (System.currentTimeMillis()-lastheal)/1000;
			  	    timeleft = dungeonmain.healcooldown4 - timeleft;
			  	    performer.getCommunicator().sendSafeServerMessage(target.getName()+" cant be healed like this for another "+timeleft+" seconds.");	
			  	    }
					}
		
				}
            }else{
            performer.getCommunicator().sendSafeServerMessage("Nice try, you need to be in "+dungeonmain.currencyname4+" in order to use this item.");
            }
        }
		return true;
	}
	
}