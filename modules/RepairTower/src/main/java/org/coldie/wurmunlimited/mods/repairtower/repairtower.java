package org.coldie.wurmunlimited.mods.repairtower;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class repairtower implements WurmServerMod, Initable {
	public static final Logger logger = Logger.getLogger(repairtower.class.getName());
	public static final String version = "ty1.0";

	@Override
	public void init() {
		try {
			CtClass ctc = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.ItemBehaviour");
			ctc.getDeclaredMethod("action").instrument(new ExprEditor(){
				public void edit(MethodCall m) throws CannotCompileException {
					if (m.getMethodName().equals("getRealKnowledge"))
						m.replace("$_ = 25;");
				}
			});
		}
		catch (CannotCompileException | NotFoundException e) {
			logger.log(Level.SEVERE, "Failed to apply repair tower hook", e);
		}
	}

	@Override
	public String getVersion(){
		return version;
	}
}